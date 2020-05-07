package main

import (
	"context"
	"fmt"
	"time"

	"github.com/Shopify/sarama"
	"github.com/bptlab/cepta/auxiliary/producers/replayer/extractors"
	"github.com/bptlab/cepta/models/constants/topic"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/bptlab/cepta/osiris/lib/utils"
	"github.com/golang/protobuf/proto"
	"github.com/golang/protobuf/ptypes/wrappers"
	"github.com/sirupsen/logrus"
)

// Replayer ...
type Replayer struct {
	Ctrl        chan pb.InternalControlMessageType
	SourceName  string
	IDFieldName string
	Options     *pb.SourceReplay
	Extractor   extractors.Extractor
	Topic       topic.Topic
	Brokers     []string
	log         *logrus.Entry
	running     bool
	producer    *kafkaproducer.Producer
}

var (
	defaultSpeed = map[pb.ReplayMode]int32{
		pb.ReplayMode_CONSTANT:     2000, // 2 Secs
		pb.ReplayMode_PROPORTIONAL: 500,  // 500x speedup
	}
	defaultMode          = pb.ReplayMode_PROPORTIONAL
	defaultReplayOptions = pb.ReplayOptions{
		Mode:   defaultMode,
		Speed:  &pb.Speed{Speed: defaultSpeed[defaultMode]},
		Repeat: &wrappers.BoolValue{Value: true},
	}
)

func (r *Replayer) queryAndSend(ctx context.Context, query *pb.SourceQuery, handlerFunc func(re *pb.ReplayedEvent)) error {
	if r.Extractor == nil {
		return fmt.Errorf("missing extractor for the %s replayer", r.SourceName)
	}
	if err := r.Extractor.StartQuery(context.Background(), r.SourceName, r.Options); err != nil {
		return err
	}

	for {
		exit := false
		select {
		case <-ctx.Done():
			// Query was canceled
			exit = true
		case ctrlMessage := <-r.Ctrl:
			switch ctrlMessage {
			case pb.InternalControlMessageType_SHUTDOWN:
				// Acknowledge shutdown
				r.Ctrl <- pb.InternalControlMessageType_SHUTDOWN
				return nil
			}
		default:
			if r.Extractor.Next() {
				replayTime, replayedEvent, err := r.Extractor.Get()
				if err != nil {
					r.log.Errorf("Failed to get from database: %v", err)
					continue
				}
				if ptime, err := utils.ToProtoTime(replayTime); err == nil {
					replayedEvent.ReplayTimestamp = ptime
				}
				r.log.Debugf("%v", replayedEvent)
				handlerFunc(replayedEvent)
			} else {
				// Do not repeat when querying
				exit = true
				break
			}
		}
		if exit {
			break
		}
	}
	r.log.Debug("Exiting")
	r.Extractor.Done()
	return nil
}

func (r *Replayer) bootstrap() error {
	// Wait for control message
	defer func() {
		// Acknowledge shutdown
		r.Ctrl <- pb.InternalControlMessageType_SHUTDOWN
	}()
	for {
		ctrlMessage := <-r.Ctrl
		switch ctrlMessage {
		case pb.InternalControlMessageType_SHUTDOWN:
			return nil
		case pb.InternalControlMessageType_START:
			r.running = true
			return r.produce()
		default:
			// Noop
		}
	}
}

func (r *Replayer) produce() error {
	var ready, exit bool
	defer r.Extractor.Done()
	defer r.log.Warn("Exiting")

	startQuery := func() {
		r.Extractor.Done()
		done := make(chan bool)
		ctx, cancel := context.WithCancel(context.Background())
		defer cancel()
		go func() {
			// This can potentially take a long time
			ready = false
			if err := r.Extractor.StartQuery(ctx, r.SourceName, r.Options); err != nil {
				if ctx.Err() == nil {
					r.log.Error("Cannot reset: ", err)
				} else {
					exit = true
				}
				ready = false
			} else {
				ready = true
			}
			done <- true
		}()
		select {
		case <-done:
			return
		case ctrlMessage := <-r.Ctrl:
			// Abort heavy aggregation on all control messages except START
			if ctrlMessage == pb.InternalControlMessageType_SHUTDOWN {
				exit = true
				return
			} else if ctrlMessage != pb.InternalControlMessageType_START {
				return
			}
		}
	}

	startQuery()
	if ready {
		r.log.Info("Started")
	}

	recentTime := time.Time{}
	passedTime := time.Duration(0)
	for {
		if exit {
			break
		}
		select {
		case ctrlMessage := <-r.Ctrl:
			switch ctrlMessage {
			case pb.InternalControlMessageType_SHUTDOWN:
				exit = true
				break
			case pb.InternalControlMessageType_RESET:
				// Recreate the query
				startQuery()
			case pb.InternalControlMessageType_START:
				r.running = true
			case pb.InternalControlMessageType_STOP:
				r.running = false
			}
		default:
			if !r.running || !ready {
				time.Sleep(1 * time.Second)
			} else if r.Extractor.Next() {
				newTime, replayedEvent, err := r.Extractor.Get()
				if err != nil {
					r.log.Errorf("Fail: %s", err.Error())
					continue
				}
				r.log.Debugf("%v", replayedEvent.Event)
				eventBytes, err := proto.Marshal(replayedEvent.Event)
				if err != nil {
					r.log.Errorf("Failed to marshal proto:", err)
					continue
				}

				if !recentTime.IsZero() {
					passedTime = newTime.Sub(recentTime)
				}
				r.log.Debugf("%v have passed since the last event", passedTime)
				r.producer.Send(r.Topic.String(), r.Topic.String(), sarama.ByteEncoder(eventBytes))

				mode := r.Options.GetOptions().GetMode()
				speed := r.Options.GetOptions().GetSpeed().GetSpeed()
				r.log.Debugf("Speed is %d, mode is %s", speed, mode)

				var waitTime int64
				switch mode {
				case pb.ReplayMode_CONSTANT:
					waitTime = int64(speed) * time.Millisecond.Nanoseconds()
				case pb.ReplayMode_PROPORTIONAL:
					waitTime = passedTime.Nanoseconds() / utils.MaxInt64(1, int64(speed))
				default:
					waitTime = 10
				}

				r.log.Debugf("Produced a message to %s and will sleep for %v seconds", r.Topic, time.Duration(waitTime))
				time.Sleep(time.Duration(waitTime))
				recentTime = newTime

			} else if r.Options.GetOptions().GetRepeat().GetValue() == true {
				startQuery()
			}
		}
	}
	return nil
}

func (r *Replayer) awaitShutdown() {
	select {
	case ctrlMessage := <-r.Ctrl:
		if ctrlMessage == pb.InternalControlMessageType_SHUTDOWN {
			return
		}
	default:
		// Noop
	}
}

// Start ...
func (r *Replayer) Start(log *logrus.Logger) {
	r.log = log.WithField("source", r.SourceName)
	r.log.Info("Waiting")
	if r.Extractor == nil {
		r.log.Errorf("Missing extractor. Waiting for shutdown.", r.SourceName)
		r.awaitShutdown()
		return
	}
	err := r.bootstrap()
	if err != nil {
		r.log.Error(err)
	}
}
