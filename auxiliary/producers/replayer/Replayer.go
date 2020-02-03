package replayer

import (
	"fmt"

	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/golang/protobuf/ptypes"
	"github.com/jinzhu/gorm"
	"github.com/sirupsen/logrus"
)

type Replayer struct {
	TableName  string
	MustMatch  *[]string
	Timerange  *pb.Timerange
	SortColumn string
	Limit      *int
	Speed      *int
	Mode       *pb.ReplayType
	Offset     int
	Db         *libdb.DB
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

func DebugDatabase(r *Replayer) *gorm.DB {
	if r.log.Logger.IsLevelEnabled(logrus.DebugLevel) {
		return r.Db.DB.Debug()
	}
	return r.Db.DB
}

func (r Replayer) Start(log *logrus.Logger) error {
	r.log = log.WithField("source", r.TableName)
	r.log.Info("Starting to produce")
	brokerList := []string{"localhost:29092"}
	var err error
	r.producer, err = kafkaproducer.KafkaProducer{}.ForBroker(brokerList)
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
