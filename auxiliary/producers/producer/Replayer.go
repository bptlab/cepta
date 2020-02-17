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
	Log        *logrus.Logger
	running    bool
	producer   *kafkaproducer.KafkaProducer
}

// Producer can be used to extract data from a database
type Producer interface {
	GetParent() *Replayer
	Start() error
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
	logEntry := this.Log.WithFields(logrus.Fields{
		"source": this.TableName,
		"action": "enriching query",
	})
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
		logEntry.Info(constraint)
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
func (this Replayer) DebugDatabase() *gorm.DB {
	if this.Log.IsLevelEnabled(logrus.DebugLevel) {
		return this.Db.DB.Debug()
	}
	return this.Db.DB
}

// Start starts the replaying
func (this Replayer) Start() error {
	logEntry := this.Log.WithFields(logrus.Fields{
		"source": this.TableName,
		"action": "starting",
	})

	logEntry.Info("Starting to produce")
	// brokerList := []string{"localhost:29092"}

	query := this.getQueryAfterModel()

	var err error
	this.producer, err = kafkaproducer.KafkaProducer{}.ForBroker(this.Brokers)
	if err != nil {
		logEntry.Warnf("Failed to start kafka producer: %s", err.Error())
		logEntry.Fatal("Cannot produce events")
	}
	defer func() {
		if err := this.producer.Close(); err != nil {
			logEntry.Println("Failed to close server", err)
		}
	}()
	err = this.produce(query)
	if err != nil {
		panic(err)
	}
	return nil
}

func (this *Replayer) getQueryAfterModel() *gorm.DB {
	switch this.TableName {
	case "public.live":
		{
			return this.DebugDatabase().Model(&libdb.LiveTrainData{})
		}
	case "public.weather":
		{
			return this.DebugDatabase().Model(&libdb.WeatherData{})
		}
		//add other cases for e.g weather
	}
	return nil
}

func (this Replayer) getDataStruct() EventData {
	switch this.TableName {
	case "public.live":
		{
			return new(libdb.LiveTrainData)
		}
	case "public.weather":
		{
			return new(libdb.WeatherData)
		}
		//add other cases for e.g weather
	}
	return nil
}

func (this Replayer) produce(query *gorm.DB) error {
	logEntry := this.Log.WithFields(logrus.Fields{
		"source": this.TableName,
		"action": "producing",
	})

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
					logEntry.Info("Shutting down ...")
					// Acknowledge shutdown
					// rows.Close()
					this.Ctrl <- pb.InternalControlMessageType_SHUTDOWN
					return err
				case pb.InternalControlMessageType_RESET:
					// Recreate the query and row cursor
					logEntry.Info("resetting ...")
					query = this.enrichQuery(query)
					rows, err = query.Rows()
					// defer rows.Close()
					if err != nil {
						logEntry.Error("Cannot reset")
					}
				}
			default:
				if !*this.Active {
					time.Sleep(1 * time.Second)
				} else if rows.Next() {
					eventData := this.getDataStruct()
					err := this.Db.DB.ScanRows(rows, &eventData)
					if err != nil {
						logEntry.Debugf("%v", err)
						continue
					}
					logEntry.Debugf("%v", eventData)
					newTime := eventData.GetActualTime()
					if !recentTime.IsZero() {
						passedTime = newTime.Sub(recentTime)
					}
					logEntry.Infof("%v have passed since the last event", passedTime)

					// Serialize event
					event := eventData.ToEvent()
					eventBytes, err := proto.Marshal(event)
					if err != nil {
						logEntry.Fatalf("Failed to marshal proto:", err)
					}
					topic := eventData.GetTopic()
					this.producer.Send(topic, topic, sarama.ByteEncoder(eventBytes))
					logEntry.Debugf("Speed is %d, mode is %s", *this.Speed, *this.Mode)

					waitTime := int64(0) // time.Duration(0)
					switch *this.Mode {
					case pb.ReplayType_CONSTANT:
						waitTime = 10 // * time.Second
					case pb.ReplayType_PROPORTIONAL:
						waitTime = passedTime.Nanoseconds() / max(1, int64(*this.Speed))
					default:
						waitTime = 10 // * time.Second
					}

					logEntry.Infof("Produced a message to %s and will sleep for %v seconds", topic, time.Duration(waitTime))
					// time.Sleep(time.Duration(waitTime))
					time.Sleep(5 * time.Second)
					recentTime = newTime

				} else if this.Repeat {
					rows, err = query.Rows()
					if err != nil {
						logEntry.Error("Cannot repeat replay")
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
