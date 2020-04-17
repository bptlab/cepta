package main

import (
	"fmt"
	"time"

	"github.com/Shopify/sarama"
	"github.com/bptlab/cepta/auxiliary/producers/replayer/extractors"
	"github.com/bptlab/cepta/models/constants/topic"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/bptlab/cepta/osiris/lib/utils"
	"github.com/golang/protobuf/proto"
	"github.com/sirupsen/logrus"
)

// Replayer ...
type Replayer struct {
	Ctrl        chan pb.InternalControlMessageType
	SourceName  string
	IDFieldName string
	Query       *extractors.ReplayQuery
	Speed       *int32
	Repeat      bool
	Mode        *pb.ReplayType
	Extractor   extractors.Extractor
	Topic       topic.Topic
	Brokers     []string
	log         *logrus.Entry
	running     bool
	producer    *kafkaproducer.KafkaProducer
}

func (r Replayer) queryAndSend(stream pb.Replayer_QueryServer) error {
	if r.Extractor == nil {
		return fmt.Errorf("Missing extractor for the %s replayer", r.SourceName)
	}

	err := r.Extractor.StartQuery(r.SourceName, r.IDFieldName, r.Query)
	if err != nil {
		return err
	}

	for {
		select {
		case ctrlMessage := <-r.Ctrl:
			switch ctrlMessage {
			case pb.InternalControlMessageType_SHUTDOWN:
				// Acknowledge shutdown
				r.Ctrl <- pb.InternalControlMessageType_SHUTDOWN
				return err
			}
		default:
			if r.Extractor.Next() {
				replayTime, replayedEvent, err := r.Extractor.Get()
				if err != nil {
					r.log.Errorf("Fail: %s", err.Error())
					continue
				}
				if ptime, err := utils.ToProtoTime(replayTime); err == nil {
					replayedEvent.ReplayTimestamp = ptime
				}
				r.log.Debugf("%v", replayedEvent)
				if err := stream.Send(replayedEvent); err != nil {
					return err
				}
				// dataset.Events = append(dataset.Events, event)
				/* &pb.ReplayedEvent{
				  ReplayTimestamp: newTime,
				  // reflect.TypeOf(ex.Proto).Elem()
				  // Event: event.(pb.isReplayedEvent_Event),
				})
				*/

				/*
					eventBytes, err := proto.Marshal(event)
					if err != nil {
						r.log.Errorf("Failed to marshal proto:", err)
						continue
					}

					r.log.Debugf("%v have passed since the last event", passedTime)
					r.producer.Send(r.Topic, r.Topic, sarama.ByteEncoder(eventBytes))
					r.log.Debugf("Speed is %d, mode is %s", *r.Speed, *r.Mode)
				*/
			}
		}
	}
	r.Extractor.Done()
	log.Info("Test")
	return nil
}

func (r *Replayer) loop() error {
	// Wait for control message
	for {
		ctrlMessage := <-r.Ctrl
		switch ctrlMessage {
		case pb.InternalControlMessageType_SHUTDOWN:
			// Acknowledge shutdown
			r.Ctrl <- pb.InternalControlMessageType_SHUTDOWN
			return nil
		case pb.InternalControlMessageType_START:
			// Start to produce
			return r.produce()
		default:
			// Noop
		}
	}
}

func (r *Replayer) produce() error {
	err := r.Extractor.StartQuery(r.SourceName, r.IDFieldName, r.Query)
	if err != nil {
		return err
	}

	for {
		ctrlMessage := <-r.Ctrl
		switch ctrlMessage {
		case pb.InternalControlMessageType_SHUTDOWN:
			// Acknowledge shutdown
			r.Ctrl <- pb.InternalControlMessageType_SHUTDOWN
			return err
		case pb.InternalControlMessageType_RESET:
			// Recreate the query
			err := r.Extractor.StartQuery(r.SourceName, r.IDFieldName, r.Query)
			if err != nil {
				r.log.Error("Cannot reset")
			}
		case pb.InternalControlMessageType_START:
			// Start to produce
			err := r.Extractor.StartQuery(r.SourceName, r.IDFieldName, r.Query)
			if err != nil {
				r.log.Error("Cannot reset")
			}
		case pb.InternalControlMessageType_STOP:
			// // Noop
		}
	}

	recentTime := time.Time{}
	passedTime := time.Duration(0)
	for {
		select {
		case ctrlMessage := <-r.Ctrl:
			switch ctrlMessage {
			case pb.InternalControlMessageType_SHUTDOWN:
				// Acknowledge shutdown
				r.Ctrl <- pb.InternalControlMessageType_SHUTDOWN
				return err
			case pb.InternalControlMessageType_RESET:
				// Recreate the query
				err := r.Extractor.StartQuery(r.SourceName, r.IDFieldName, r.Query)
				if err != nil {
					r.log.Error("Cannot reset")
				}
			case pb.InternalControlMessageType_START:
				r.running = true
			case pb.InternalControlMessageType_STOP:
				r.running = false
			}
		default:
			if !r.running {
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
				r.log.Debugf("Speed is %d, mode is %s", *r.Speed, *r.Mode)

				waitTime := int64(0)
				switch *r.Mode {
				case pb.ReplayType_CONSTANT:
					waitTime = int64(*r.Speed) * time.Millisecond.Nanoseconds()
				case pb.ReplayType_PROPORTIONAL:
					waitTime = passedTime.Nanoseconds() / utils.MaxInt64(1, int64(*r.Speed))
				default:
					waitTime = 10
				}

				r.log.Debugf("Produced a message to %s and will sleep for %v seconds", r.Topic, time.Duration(waitTime))
				time.Sleep(time.Duration(waitTime))
				recentTime = newTime

			} else if r.Repeat {
				err := r.Extractor.StartQuery(r.SourceName, r.IDFieldName, r.Query)
				if err != nil {
					r.log.Error("Cannot repeat replay")
				}
			}
		}
	}
	r.Extractor.Done()
	return nil
}

// Start ...
func (r Replayer) Start(log *logrus.Logger) error {
	r.log = log.WithField("source", r.SourceName)
	r.log.Info("Starting")
	if r.Extractor == nil {
		return fmt.Errorf("Missing extractor for the %s replayer", r.SourceName)
	}
	r.Extractor.SetDebug(r.log.Logger.IsLevelEnabled(logrus.DebugLevel))
	err := r.loop()
	if err != nil {
		log.Error(err)
	}
	return nil
}
