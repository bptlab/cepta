package db

import "time"

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
