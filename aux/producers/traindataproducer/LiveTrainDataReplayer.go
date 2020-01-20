package livetraindatareplayer

import (
	"fmt"
	"time"
	"github.com/jinzhu/gorm"
	"github.com/sirupsen/logrus"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/Shopify/sarama"
	"github.com/bptlab/cepta/models/events/livetraindataevent"
	"github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/golang/protobuf/proto"
)

type Timerange struct {
	from *time.Time
	to *time.Time
}

type Replayer struct {
	TableName 	string
	MustMatch 	*string
	Timerange 	Timerange
	SortColumn 	string
	Limit 		*int
	Speed		*int
	Mode		*string
	Offset 		int
	Db 			*libdb.DB
	log			*logrus.Entry
	running		bool
	producer	*kafkaproducer.KafkaProducer
}

type LiveTrainData struct {
	Id			int64
    Train_id	int64
    Location_id	int64
    Actual_time	time.Time
    Status		int64
    First_train_number		int64
    Train_number_reference	int64
    Arrival_time_reference	time.Time
    Planned_arrival_deviation	int64
    Transfer_location_id	int64
    Reporting_im_id		int64
    Next_im_id			int64
    Message_status		int64
    Message_creation	time.Time
}

func (LiveTrainData) TableName() string {
    return "public.live"
}

func (timerange Timerange) buildQuery(column string) []string {
	frmt := "2006-01-02 15:04:05" // Go want's this date!
	query := []string{}
	if timerange.from != nil {
		query = append(query, fmt.Sprintf("%s >= '%s'", column, timerange.from.Format(frmt)))
	}
	if timerange.from != nil {
		query = append(query, fmt.Sprintf("%s < '%s'", column, timerange.to.Format(frmt)))
	}
	return query
}

func DebugDatabase(r *Replayer) *gorm.DB {
	if r.log.Logger.IsLevelEnabled(logrus.DebugLevel) {
		return r.Db.DB.Debug()
	}
	return r.Db.DB
}

func (r Replayer) produce() error {
	query := DebugDatabase(&r).Model(&LiveTrainData{})
	// Match ERRIDs
	if r.MustMatch != nil {
		query = query.Where(*(r.MustMatch))
	}
	// Match time range
	for _, constraint := range r.Timerange.buildQuery(r.SortColumn) {
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
	rows, err := query.Rows()
	defer rows.Close()
	if err == nil {
		recentTime := time.Time{}
		passedTime := time.Duration(0)
		
		for rows.Next() {
			var livetraindata LiveTrainData
			r.Db.DB.ScanRows(rows, &livetraindata) //.Error
			r.log.Debugf("%v", livetraindata)
			
			newTime := livetraindata.Actual_time
			if !recentTime.IsZero() {
				passedTime = newTime.Sub(recentTime)
			}
			r.log.Infof("%v have passed since the last event", passedTime)

			// Serialize proto
			event := &train.LiveTrainData{Id: 23}
			eventBytes, err := proto.Marshal(event)
			if err != nil {
				r.log.Fatalf("Failed to marshal proto:", err)
			}
			r.producer.Send("live", "live", sarama.ByteEncoder(eventBytes))
			r.log.Debugf("Speed is %d, mode is %s", *r.Speed, *r.Mode)

			waitTime := int64(0) // time.Duration(0)
			switch *r.Mode {
			case "constant":
				waitTime = 10 // * time.Second
			case "proportional":
				waitTime = passedTime.Nanoseconds() / int64(*r.Speed)
			default:
				waitTime = 10 // * time.Second
			}
			
			r.log.Infof("Produced a message and will sleep for %v seconds", time.Duration(waitTime))
			time.Sleep(time.Duration(waitTime))
			recentTime = newTime
		}
	}
	return err
}

func (r Replayer) Start(log *logrus.Logger) error {
	r.log = log.WithField("source", r.TableName)
	r.log.Info("Starting to produce")
	brokerList := []string{"localhost:29092"}
	r.producer = kafkaproducer.KafkaProducer{}.ForBroker(brokerList)
	defer func() {
		if err := r.producer.Close(); err != nil {
			r.log.Println("Failed to close server", err)
		}
	}()
	err := r.produce()
	if err != nil {
		panic(err)
	}
	return nil
}