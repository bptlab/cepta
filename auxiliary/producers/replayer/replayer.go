package main

import (
	// "database/sql"
	"fmt"
	"time"

	"github.com/Shopify/sarama"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	// libdb "github.com/bptlab/cepta/osiris/lib/db"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/bptlab/cepta/auxiliary/producers/replayer/extractors"
	"github.com/bptlab/cepta/osiris/lib/utils"
	"github.com/golang/protobuf/proto"
	// "github.com/golang/protobuf/ptypes"
	// "github.com/jinzhu/gorm"
	"github.com/sirupsen/logrus"
)

// Replayer ...
type Replayer struct {
	Ctrl       chan pb.InternalControlMessageType
	SourceName  string
	Query 	*extractors.ReplayQuery
	Speed      *int32
	Active     *bool
	Repeat     bool
	Mode       *pb.ReplayType
	// Db         *libdb.PostgresDB
	// DbModel    DbExtractor
	Extractor  extractors.Extractor
	Topic      string
	Brokers    []string
	log        *logrus.Entry
	running    bool
	producer   *kafkaproducer.KafkaProducer
}

func (r Replayer) produce() error {
	if r.Extractor == nil {
		return fmt.Errorf("Missing extractor for the %s replayer", r.SourceName)
	}

	err := r.Extractor.StartQuery(r.SourceName, r.Query)
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
				err := r.Extractor.StartQuery(r.SourceName, r.Query)
				if err != nil {
					r.log.Error("Cannot reset")
				}
			}
		default:
			if !*r.Active {
				time.Sleep(1 * time.Second)
			} else if r.Extractor.Next() {
				newTime, event, err := r.Extractor.Get() //  r.Db.DB)
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
				r.log.Infof("%v have passed since the last event", passedTime)
				r.producer.Send(r.Topic, r.Topic, sarama.ByteEncoder(eventBytes))
				r.log.Debugf("Speed is %d, mode is %s", *r.Speed, *r.Mode)

				waitTime := int64(0) // time.Duration(0)
				switch *r.Mode {
				case pb.ReplayType_CONSTANT:
					waitTime = int64(*r.Speed) * time.Second.Nanoseconds()
				case pb.ReplayType_PROPORTIONAL:
					waitTime = passedTime.Nanoseconds() / utils.MaxInt64(1, int64(*r.Speed))
				default:
					waitTime = 10 // * time.Second
				}

				r.log.Infof("Produced a message to %s and will sleep for %v seconds", r.Topic, time.Duration(waitTime))
				time.Sleep(time.Duration(waitTime))
				recentTime = newTime

			} else if r.Repeat {
				err := r.Extractor.StartQuery(r.SourceName, r.Query)
				if err != nil {
					r.log.Error("Cannot repeat replay")
				}
			}
		}
	}
	r.Extractor.Done()
	return nil
}

/*
func (r Replayer) produceOld() error {
	query := r.makeQuery()
	rows, err := query.Rows()
	if err == nil {
		defer rows.Close()
		recentTime := time.Time{}
		passedTime := time.Duration(0)
		for {
			select {
			case ctrlMessage := <-r.Ctrl:
				switch ctrlMessage {
				case pb.InternalControlMessageType_SHUTDOWN:
					// Acknowledge shutdown
					// rows.Close()
					r.Ctrl <- pb.InternalControlMessageType_SHUTDOWN
					return err
				case pb.InternalControlMessageType_RESET:
					// Recreate the query and row cursor
					query = r.makeQuery()
					rows, err = query.Rows()
					// defer rows.Close()
					if err != nil {
						r.log.Error("Cannot reset")
					}
				}
			default:
				if !*r.Active {
					time.Sleep(1 * time.Second)
				} else if rows.Next() {
					newTime, event, err := r.DbModel.GetAll(rows, r.Db.DB)
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
					r.log.Infof("%v have passed since the last event", passedTime)
					r.producer.Send(r.Topic, r.Topic, sarama.ByteEncoder(eventBytes))
					r.log.Debugf("Speed is %d, mode is %s", *r.Speed, *r.Mode)

					waitTime := int64(0) // time.Duration(0)
					switch *r.Mode {
					case pb.ReplayType_CONSTANT:
						waitTime = int64(*r.Speed) * time.Second.Nanoseconds()
					case pb.ReplayType_PROPORTIONAL:
						waitTime = passedTime.Nanoseconds() / utils.MaxInt64(1, int64(*r.Speed))
					default:
						waitTime = 10 // * time.Second
					}

					r.log.Infof("Produced a message to %s and will sleep for %v seconds", r.Topic, time.Duration(waitTime))
					time.Sleep(time.Duration(waitTime))
					recentTime = newTime

				} else if r.Repeat {
					rows, err = query.Rows()
					if err != nil {
						r.log.Error("Cannot repeat replay")
					}
					// defer rows.Close()
				}
			}
		}
	}
	return err
}
*/

// Start ...
func (r Replayer) Start(log *logrus.Logger) error {
	r.log = log.WithField("source", r.SourceName)
	r.log.Info("Starting to produce")
	var err error
	r.producer, err = kafkaproducer.KafkaProducer{}.ForBroker(r.Brokers)
	if err != nil {
		log.Warnf("Failed to start kafka producer: %s", err.Error())
		log.Fatal("Cannot produce events")
	}
	defer func() {
		if err := r.producer.Close(); err != nil {
			r.log.Println("Failed to close server", err)
		}
	}()
	r.Extractor.SetDebug(r.log.Logger.IsLevelEnabled(logrus.DebugLevel))
	err = r.produce()
	if err != nil {
		log.Error(err)
	}
	return nil
}
