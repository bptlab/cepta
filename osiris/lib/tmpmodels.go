package db

import (
	"database/sql"
	"time"

	livetraindataevent "github.com/bptlab/cepta/models/events/livetraindataevent"
	weatherdataevent "github.com/bptlab/cepta/models/events/weatherdataevent"
	"github.com/golang/protobuf/proto"
	"github.com/golang/protobuf/ptypes"
	tspb "github.com/golang/protobuf/ptypes/timestamp"
	"github.com/jinzhu/gorm"
)

func toTimestamp(t time.Time) *tspb.Timestamp {
	ts, _ := ptypes.TimestampProto(t)
	return ts
}

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

func (ltd LiveTrainData) GetAll(rows *sql.Rows, db *gorm.DB) (time.Time, proto.Message, error) {
	var instance LiveTrainData
	err := db.ScanRows(rows, &instance)
	if err != nil {
		return time.Time{}, nil, err
	}
	newTime := instance.Actual_time
	event := instance.AsProtoMessage()
	return newTime, event, err
}

func (ltd LiveTrainData) GetInstance() interface{} {
	return &LiveTrainData{}
}

func (ltd LiveTrainData) AsProtoMessage() proto.Message {
	return &livetraindataevent.LiveTrainData{
		Id:                      ltd.Id,
		TrainId:                 ltd.Train_id,
		LocationId:              ltd.Location_id,
		ActualTime:              toTimestamp(ltd.Actual_time),
		Status:                  ltd.Status,
		FirstTrainNumber:        ltd.First_train_number,
		TrainNumberReference:    ltd.Train_number_reference,
		ArrivalTimeReference:    toTimestamp(ltd.Arrival_time_reference),
		PlannedArrivalDeviation: ltd.Planned_arrival_deviation,
		TransferLocationId:      ltd.Transfer_location_id,
		ReportingImId:           ltd.Reporting_im_id,
		NextImId:                ltd.Next_im_id,
		MessageStatus:           ltd.Message_status,
		MessageCreation:         toTimestamp(ltd.Message_creation),
	}
}

type WeatherData struct {
	Class                string
	Latitude             float64
	Longitude            float64
	Starttimestamp       time.Time
	Endtimestamp         time.Time
	Detectiontimestamp   time.Time
	Title                string
	Description          string
	Temperature          float64
	Rain                 float64
	Windspeed            float64
	Cloudpercentage      float64
	Cityname             string
	Identifier           string
	Pressure             float64
	Ozone                float64
	Humidity             float64
	Windbearing          int64
	Precipprobability    float64
	Preciptype           string
	Dewpoint             float64
	Neareststormbearing  int64
	Neareststormdistance int64
	Visibility           float64
}

func (wd WeatherData) TableName() string {
	return "public.weather"
}

func (wd WeatherData) GetAll(rows *sql.Rows, db *gorm.DB) (time.Time, proto.Message, error) {
	var instance WeatherData
	err := db.ScanRows(rows, &instance)
	if err != nil {
		return time.Time{}, nil, err
	}
	newTime := instance.Starttimestamp
	event := instance.AsProtoMessage()
	return newTime, event, err
}

func (wd WeatherData) GetInstance() interface{} {
	return &WeatherData{}
}

func (wd WeatherData) AsProtoMessage() proto.Message {
	return &weatherdataevent.WeatherData{
		Eventclass:           wd.Class,
		Latitude:             wd.Latitude,
		Longitude:            wd.Longitude,
		Starttimestamp:       toTimestamp(wd.Starttimestamp),
		Endtimestamp:         toTimestamp(wd.Endtimestamp),
		Detectiontimestamp:   toTimestamp(wd.Detectiontimestamp),
		Title:                wd.Title,
		Description:          wd.Description,
		Temperature:          wd.Temperature,
		Rain:                 wd.Rain,
		Windspeed:            wd.Windspeed,
		Cloudpercentage:      wd.Cloudpercentage,
		Cityname:             wd.Cityname,
		Identifier:           wd.Identifier,
		Pressure:             wd.Pressure,
		Ozone:                wd.Ozone,
		Humidity:             wd.Humidity,
		Windbearing:          wd.Windbearing,
		Precippropability:    wd.Precipprobability,
		Preciptype:           wd.Preciptype,
		Dewpoint:             wd.Dewpoint,
		Neareststormbearing:  wd.Neareststormbearing,
		Neareststormdistance: wd.Neareststormdistance,
		Visibility:           wd.Visibility,
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
