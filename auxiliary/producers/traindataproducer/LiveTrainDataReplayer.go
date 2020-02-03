package livetraindatareplayer

import (
	"fmt"
	"time"

	"github.com/Shopify/sarama"
	livetraindataevent "github.com/bptlab/cepta/models/events/livetraindataevent"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	replayer "github.com/bptlab/cepta/auxiliary/producers/replayer/replayer"
	"github.com/golang/protobuf/proto"
	"github.com/golang/protobuf/ptypes"
	"github.com/jinzhu/gorm"
	"github.com/sirupsen/logrus"
)

type LiveTrainReplayer struct {
	replayer.Replayer
}

func (r LiveTrainReplayer) produce() error {
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
	rows, err := query.Rows()
	defer rows.Close()
	if err == nil {
		recentTime := time.Time{}
		passedTime := time.Duration(0)

		for rows.Next() {
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

			// Serialize proto
			event := &livetraindataevent.LiveTrainData{Id: 23}
			eventBytes, err := proto.Marshal(event)
			if err != nil {
				r.log.Fatalf("Failed to marshal proto:", err)
			}
			r.producer.Send("live", "live", sarama.ByteEncoder(eventBytes))
			r.log.Debugf("Speed is %d, mode is %s", *r.Speed, *r.Mode)

			waitTime := int64(0) // time.Duration(0)
			switch *r.Mode {
			case pb.ReplayType_CONSTANT:
				waitTime = 10 // * time.Second
			case pb.ReplayType_PROPORTIONAL:
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