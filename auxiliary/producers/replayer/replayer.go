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
	Active      *bool
	Repeat      bool
	Mode        *pb.ReplayType
	Extractor   extractors.Extractor
	Topic       topic.Topic
	Brokers     []string
	log         *logrus.Entry
	running     bool
	producer    *kafkaproducer.KafkaProducer
}

func (r Replayer) query() (*pb.ReplayDataset, error) {
	var dataset *pb.ReplayDataset

	if r.Extractor == nil {
		return dataset, fmt.Errorf("Missing extractor for the %s replayer", r.SourceName)
	}

	err := r.Extractor.StartQuery(r.SourceName, r.IDFieldName, r.Query)
	if err != nil {
		return dataset, err
	}

	for {
		select {
		case ctrlMessage := <-r.Ctrl:
			switch ctrlMessage {
			case pb.InternalControlMessageType_SHUTDOWN:
				// Acknowledge shutdown
				r.Ctrl <- pb.InternalControlMessageType_SHUTDOWN
				return dataset, err
			}
		default:
			if r.Extractor.Next() {
				event, err := r.Extractor.GetReplayedEvent()
				if err != nil {
					r.log.Errorf("Fail: %s", err.Error())
					continue
				}
				r.log.Debugf("%v", event)
				dataset.Events = append(dataset.Events, event)
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
	return dataset, nil
}

func (r Replayer) produce() error {
	if r.Extractor == nil {
		return fmt.Errorf("Missing extractor for the %s replayer", r.SourceName)
	}

	err := r.Extractor.StartQuery(r.SourceName, r.IDFieldName, r.Query)
	if err != nil {
		return err
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
			}
		default:
			if !*r.Active {
				time.Sleep(1 * time.Second)
			} else if r.Extractor.Next() {
				newTime, event, err := r.Extractor.Get()
				if err != nil {
					r.log.Errorf("Fail: %s", err.Error())
					continue
				}
				r.log.Debugf("%v", event)
				eventBytes, err := proto.Marshal(event)
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
	r.log.Info("Starting to produce")
	defer func() {
		if err := r.producer.Close(); err != nil {
			r.log.Errorf("Failed to close server", err)
		}
	}()
	r.Extractor.SetDebug(r.log.Logger.IsLevelEnabled(logrus.DebugLevel))
	err := r.produce()
	if err != nil {
		log.Error(err)
	}
	return nil
}
