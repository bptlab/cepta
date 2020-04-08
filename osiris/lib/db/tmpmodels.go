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

// LiveTrainData ...
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

// TableName ...
func (LiveTrainData) TableName() string {
	return "public.live"
}

// GetAll ...
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

// GetInstance ...
func (ltd LiveTrainData) GetInstance() interface{} {
	return &LiveTrainData{}
}

// AsProtoMessage ..
func (ltd LiveTrainData) AsProtoMessage() proto.Message {
	return &livetraindataevent.LiveTrainData{
		Id:                           ltd.Id,
		TrainSectionId:               ltd.Train_id,
		StationId:                    ltd.Location_id,
		EventTime:                    toTimestamp(ltd.Actual_time),
		Status:                       ltd.Status,
		FirstTrainId:                 ltd.First_train_number,
		TrainId:                      ltd.Train_number_reference,
		PlannedArrivalTimeEndStation: toTimestamp(ltd.Arrival_time_reference),
		Delay:                        ltd.Planned_arrival_deviation,
		EndStationId:                 ltd.Transfer_location_id,
		ImId:                         ltd.Reporting_im_id,
		FollowingImId:                ltd.Next_im_id,
		MessageStatus:                ltd.Message_status,
		IngestionTime:                toTimestamp(ltd.Message_creation),
	}
}

// WeatherData ...
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

// TableName ...
func (wd WeatherData) TableName() string {
	return "public.weather"
}

// GetAll ...
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

// GetInstance ...
func (wd WeatherData) GetInstance() interface{} {
	return &WeatherData{}
}

// AsProtoMessage ...
func (wd WeatherData) AsProtoMessage() proto.Message {
	return &weatherdataevent.WeatherData{
		EventClass:           wd.Class,
		Latitude:             wd.Latitude,
		Longitude:            wd.Longitude,
		StartTime:            toTimestamp(wd.Starttimestamp),
		EndTime:              toTimestamp(wd.Endtimestamp),
		DetectionTime:        toTimestamp(wd.Detectiontimestamp),
		Title:                wd.Title,
		Description:          wd.Description,
		Temperature:          wd.Temperature,
		Rain:                 wd.Rain,
		WindSpeed:            wd.Windspeed,
		CloudPercentage:      wd.Cloudpercentage,
		CityName:             wd.Cityname,
		Identifier:           wd.Identifier,
		Pressure:             wd.Pressure,
		Ozone:                wd.Ozone,
		Humidity:             wd.Humidity,
		WindBearing:          wd.Windbearing,
		PrecipPropability:    wd.Precipprobability,
		PrecipType:           wd.Preciptype,
		DewPoint:             wd.Dewpoint,
		NearestStormBearing:  wd.Neareststormbearing,
		NearestStormDistance: wd.Neareststormdistance,
		Visibility:           wd.Visibility,
	}
}

// PlannedTrainData ...
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

// TableName ...
func (PlannedTrainData) TableName() string {
	return "public.planned"
}
