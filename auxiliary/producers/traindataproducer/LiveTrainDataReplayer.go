package livetraindatareplayer

import (
	"fmt"
	"time"

	"github.com/Shopify/sarama"
	"github.com/bptlab/cepta/constants"
	livetraindataevent "github.com/bptlab/cepta/models/events/livetraindataevent"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/golang/protobuf/proto"
	"github.com/golang/protobuf/ptypes"
	tspb "github.com/golang/protobuf/ptypes/timestamp"
	"github.com/jinzhu/gorm"
	"github.com/sirupsen/logrus"
)

type Replayer struct {
	Ctrl	   chan pb.InternalControlMessageType
	TableName  string
	MustMatch  *[]string
	Timerange  *pb.Timerange
	SortColumn string
	Limit      *int
	Speed      *int32
	Active	   *bool
	Repeat	   bool
	Mode       *pb.ReplayType
	Offset     int
	Db         *libdb.DB
	Brokers    []string
	log        *logrus.Entry
	running    bool
	producer   *kafkaproducer.KafkaProducer
}

func toTimestamp(t time.Time) *tspb.Timestamp {
	ts, _ := ptypes.TimestampProto(t)
	return ts
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

func DebugDatabase(r *Replayer) *gorm.DB {
	if r.log.Logger.IsLevelEnabled(logrus.DebugLevel) {
		return r.Db.DB.Debug()
	}
	return r.Db.DB
}

func (r Replayer) makeQuery() *gorm.DB {
	query := DebugDatabase(&r).Model(&libdb.LiveTrainData{})
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
					var livetraindata libdb.LiveTrainData
					err := r.Db.DB.ScanRows(rows, &livetraindata)
					if err != nil {
						r.log.Debugf("%v", err)
						continue
					}
					r.log.Debugf("%v", livetraindata)

					newTime := livetraindata.Actual_time
					if !recentTime.IsZero() {
						passedTime = newTime.Sub(recentTime)
					}
					r.log.Infof("%v have passed since the last event", passedTime)

					// Serialize event
					event := &livetraindataevent.LiveTrainData{
						Id: livetraindata.Id,
						TrainId: livetraindata.Train_id,            
						LocationId: livetraindata.Location_id,
						ActualTime: toTimestamp(livetraindata.Actual_time),
						Status: livetraindata.Status,
						FirstTrainNumber: livetraindata.First_train_number,
						TrainNumberReference: livetraindata.Train_number_reference,
						ArrivalTimeReference: toTimestamp(livetraindata.Arrival_time_reference),
						PlannedArrivalDeviation: livetraindata.Planned_arrival_deviation,
						TransferLocationId: livetraindata.Transfer_location_id,
						ReportingImId: livetraindata.Reporting_im_id,
						NextImId: livetraindata.Next_im_id,
						MessageStatus: livetraindata.Message_status,
						MessageCreation: toTimestamp(livetraindata.Message_creation),
					}
					eventBytes, err := proto.Marshal(event)
					if err != nil {
						r.log.Fatalf("Failed to marshal proto:", err)
					}
					topic := constants.Topics_LIVE_TRAIN_DATA.String()
					r.producer.Send(topic, topic, sarama.ByteEncoder(eventBytes))
					r.log.Debugf("Speed is %d, mode is %s", *r.Speed, *r.Mode)

					waitTime := int64(0) // time.Duration(0)
					switch *r.Mode {
					case pb.ReplayType_CONSTANT:
						waitTime = 10 // * time.Second
					case pb.ReplayType_PROPORTIONAL:
						waitTime = passedTime.Nanoseconds() / max(1, int64(*r.Speed))
					default:
						waitTime = 10 // * time.Second
					}

					r.log.Infof("Produced a message to %s and will sleep for %v seconds", topic, time.Duration(waitTime))
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
