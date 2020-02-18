package main

import (
	"database/sql"
	"fmt"
	"time"

	"github.com/Shopify/sarama"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/golang/protobuf/proto"
	"github.com/golang/protobuf/ptypes"
	"github.com/jinzhu/gorm"
	"github.com/sirupsen/logrus"
)

type DbExtractor interface {
	GetAll(*sql.Rows, *gorm.DB) (time.Time, proto.Message, error)
	GetInstance() interface{}
}

type Replayer struct {
	Ctrl       chan pb.InternalControlMessageType
	TableName  string
	MustMatch  *[]string
	Timerange  *pb.Timerange
	SortColumn string
	Limit      *int
	Speed      *int32
	Active     *bool
	Repeat     bool
	Mode       *pb.ReplayType
	Offset     int
	Db         *libdb.DB
	DbModel    DbExtractor
	Topic      string
	Brokers    []string
	log        *logrus.Entry
	running    bool
	producer   *kafkaproducer.KafkaProducer
}

func buildQuery(column string, timerange *pb.Timerange) []string {
	frmt := "2006-01-02 15:04:05" // Go want's this date!
	query := []string{}
	if start, err := ptypes.Timestamp(timerange.GetStart()); err != nil && start.Unix() > 0 {
		query = append(query, fmt.Sprintf("%s >= '%s'", column, start.Format(frmt)))
	}
	if end, err := ptypes.Timestamp(timerange.GetEnd()); err != nil && end.Unix() > 0 {
		query = append(query, fmt.Sprintf("%s < '%s'", column, end.Format(frmt)))
	}
	return query
}

func debugDatabase(r *Replayer) *gorm.DB {
	if r.log.Logger.IsLevelEnabled(logrus.DebugLevel) {
		return r.Db.DB.Debug()
	}
	return r.Db.DB
}

func (r Replayer) makeQuery() *gorm.DB {
	query := debugDatabase(&r).Model(r.DbModel.GetInstance())
	// Match ERRIDs
	if r.MustMatch != nil {
		for _, condition := range *(r.MustMatch) {
			if len(condition) > 0 {
				query = query.Where(condition)
			}
		}
	}
	// Match time range
	for _, constraint := range buildQuery(r.SortColumn, r.Timerange) {
		r.log.Info(constraint)
		query = query.Where(constraint)
	}
	// Order by column (ascending order)
	query = query.Order(fmt.Sprintf("%s asc", r.SortColumn))
	// Set limit
	if r.Limit != nil {
		query = query.Limit(*(r.Limit))
	}
	// Set offset
	query = query.Offset(r.Offset)
	return query
}

func max(a int64, b int64) int64 {
	if a > b {
		return a
	}
	return b
}

func (r Replayer) produce() error {
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
						waitTime = passedTime.Nanoseconds() / max(1, int64(*r.Speed))
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

func (r Replayer) Start(log *logrus.Logger) error {
	r.log = log.WithField("source", r.TableName)
	r.log.Info("Starting to produce")
	// brokerList := []string{"localhost:29092"}
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
	err = r.produce()
	if err != nil {
		panic(err)
	}
	return nil
}
