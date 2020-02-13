package replayer

import (
	"fmt"
	"time"

	"github.com/Shopify/sarama"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/golang/protobuf/proto"
	"github.com/golang/protobuf/ptypes"
	tspb "github.com/golang/protobuf/ptypes/timestamp"
	"github.com/jinzhu/gorm"
	"github.com/sirupsen/logrus"
)

// EventData types
type EventData interface {
	GetActualTime() time.Time
	ToEvent() proto.Message
	GetTopic() string
}

// Any types can be everything
type Any interface {
}

// Replayer implements the Producer interface
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
	Brokers    []string
	log        *logrus.Entry
	running    bool
	producer   *kafkaproducer.KafkaProducer
}

// Producer can be used to extract data from a database
type Producer interface {
	GetParent() *Replayer
	Start(*logrus.Logger, *EventData) error
}

func toTimestamp(t time.Time) *tspb.Timestamp {
	ts, _ := ptypes.TimestampProto(t)
	return ts
}

// TODO: rename for better understanding
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

func (this Replayer) enrichQuery(query *gorm.DB) *gorm.DB {
	// Match ERRIDs
	if this.MustMatch != nil {
		for _, condition := range *(this.MustMatch) {
			if len(condition) > 0 {
				query = query.Where(condition)
				// *query.Where(condition)
			}
		}
	}
	// Match time range
	for _, constraint := range buildQuery(this.SortColumn, this.Timerange) {
		this.log.Info(constraint)
		query = query.Where(constraint)
		// *query.Where(constraint)
	}
	// Order by column (ascending order)
	query = query.Order(fmt.Sprintf("%s asc", this.SortColumn))
	// *query.Order(fmt.Sprintf("%s asc", r.SortColumn))
	// Set limit
	if this.Limit != nil {
		query = query.Limit(*(this.Limit))
		// *query.Limit(*(r.Limit))
	}
	// Set offset
	query = query.Offset(this.Offset)
	// *query.Offset(r.Offset)

	return query
}

// DebugDatabase creates the base for the database query
func (this Replayer) DebugDatabase(log *logrus.Logger) *gorm.DB {
	if log.IsLevelEnabled(logrus.DebugLevel) {
		return this.Db.DB.Debug()
	}
	return this.Db.DB
}

// Start starts the replaying
func (this Replayer) Start(log *logrus.Logger, query *gorm.DB, resultStore *EventData) error {
	this.log = log.WithField("source", this.TableName)
	this.log.Info("Starting to produce")
	// brokerList := []string{"localhost:29092"}
	var err error
	this.producer, err = kafkaproducer.KafkaProducer{}.ForBroker(this.Brokers)
	if err != nil {
		log.Warnf("Failed to start kafka producer: %s", err.Error())
		log.Fatal("Cannot produce events")
	}
	defer func() {
		if err := this.producer.Close(); err != nil {
			this.log.Println("Failed to close server", err)
		}
	}()
	err = this.produce(query, resultStore)
	if err != nil {
		panic(err)
	}
	return nil
}

func (this Replayer) produce(query *gorm.DB, resultStore *EventData) error {

	rows, err := query.Rows()
	if err == nil {
		defer rows.Close()
		recentTime := time.Time{}
		passedTime := time.Duration(0)
		for {
			select {
			case ctrlMessage := <-this.Ctrl:
				switch ctrlMessage {
				case pb.InternalControlMessageType_SHUTDOWN:
					// Acknowledge shutdown
					// rows.Close()
					this.Ctrl <- pb.InternalControlMessageType_SHUTDOWN
					return err
				case pb.InternalControlMessageType_RESET:
					// Recreate the query and row cursor
					query = this.enrichQuery(query)
					rows, err = query.Rows()
					// defer rows.Close()
					if err != nil {
						this.log.Error("Cannot reset")
					}
				}
			default:
				if !*this.Active {
					time.Sleep(1 * time.Second)
				} else if rows.Next() {
					err := this.Db.DB.ScanRows(rows, resultStore)
					if err != nil {
						this.log.Debugf("%v", err)
						continue
					}
					eventData := *resultStore
					this.log.Debugf("%v", eventData)
					newTime := eventData.GetActualTime()
					if !recentTime.IsZero() {
						passedTime = newTime.Sub(recentTime)
					}
					this.log.Infof("%v have passed since the last event", passedTime)

					// Serialize event
					event := eventData.ToEvent()
					eventBytes, err := proto.Marshal(event)
					if err != nil {
						this.log.Fatalf("Failed to marshal proto:", err)
					}
					topic := eventData.GetTopic()
					this.producer.Send(topic, topic, sarama.ByteEncoder(eventBytes))
					this.log.Debugf("Speed is %d, mode is %s", *this.Speed, *this.Mode)

					waitTime := int64(0) // time.Duration(0)
					switch *this.Mode {
					case pb.ReplayType_CONSTANT:
						waitTime = 10 // * time.Second
					case pb.ReplayType_PROPORTIONAL:
						waitTime = passedTime.Nanoseconds() / max(1, int64(*this.Speed))
					default:
						waitTime = 10 // * time.Second
					}

					this.log.Infof("Produced a message to %s and will sleep for %v seconds", topic, time.Duration(waitTime))
					time.Sleep(time.Duration(waitTime))
					recentTime = newTime

				} else if this.Repeat {
					rows, err = query.Rows()
					if err != nil {
						this.log.Error("Cannot repeat replay")
					}
					// defer rows.Close()
				}
			}
		}
	}
	return err
}

func max(a int64, b int64) int64 {
	if a > b {
		return a
	}
	return b
}
