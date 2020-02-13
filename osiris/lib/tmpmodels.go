package db

import (
	"time"

	"github.com/bptlab/cepta/constants"
	livetraindataevent "github.com/bptlab/cepta/models/events/livetraindataevent"
	"github.com/golang/protobuf/ptypes"
	tspb "github.com/golang/protobuf/ptypes/timestamp"
)

type LiveTrainData struct {
	Id                        int64
	Train_id                  int64
	Location_id               int64
	Actual_time               time.Time
	Status                    int64
	First_train_number        int64
	Train_number_reference    int64
	Arrival_time_reference    time.Time
	Planned_arrival_deviation int64
	Transfer_location_id      int64
	Reporting_im_id           int64
	Next_im_id                int64
	Message_status            int64
	Message_creation          time.Time
}

func (LiveTrainData) TableName() string {
	return "public.live"
}

func (livetraindataevent LiveTrainData) GetTopic() string {
	return constants.Topics_LIVE_TRAIN_DATA.String()
}
func (livetraindataevent LiveTrainData) GetActualTime() time.Time {
	return livetraindataevent.Actual_time
}
func (livetraindata LiveTrainData) ToEvent() *livetraindataevent.LiveTrainData {
	return &livetraindataevent.LiveTrainData{
		Id:                      livetraindata.Id,
		TrainId:                 livetraindata.Train_id,
		LocationId:              livetraindata.Location_id,
		ActualTime:              toTimestamp(livetraindata.Actual_time),
		Status:                  livetraindata.Status,
		FirstTrainNumber:        livetraindata.First_train_number,
		TrainNumberReference:    livetraindata.Train_number_reference,
		ArrivalTimeReference:    toTimestamp(livetraindata.Arrival_time_reference),
		PlannedArrivalDeviation: livetraindata.Planned_arrival_deviation,
		TransferLocationId:      livetraindata.Transfer_location_id,
		ReportingImId:           livetraindata.Reporting_im_id,
		NextImId:                livetraindata.Next_im_id,
		MessageStatus:           livetraindata.Message_status,
		MessageCreation:         toTimestamp(livetraindata.Message_creation),
	}
}

type PlannedTrainData struct {
	Id                        int64
	Train_id                  int64
	Location_id               int64
	Actual_time               time.Time
	Status                    int64
	First_train_number        int64
	Train_number_reference    int64
	Arrival_time_reference    time.Time
	Planned_arrival_deviation int64
	Transfer_location_id      int64
	Reporting_im_id           int64
	Next_im_id                int64
	Message_status            int64
	Message_creation          time.Time
}

func (PlannedTrainData) TableName() string {
	return "public.planned"
}

func toTimestamp(t time.Time) *tspb.Timestamp {
	ts, _ := ptypes.TimestampProto(t)
	return ts
}
