package main

import (
	"context"
	"fmt"

	libdb "github.com/bptlab/cepta/osiris/lib/db"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	log "github.com/sirupsen/logrus"

	"github.com/bptlab/cepta/auxiliary/producers/replayer/extractors"
	topics "github.com/bptlab/cepta/models/constants/topic"
	checkpointpb "github.com/bptlab/cepta/models/events/checkpointdataevent"
	eventpb "github.com/bptlab/cepta/models/events/event"

	crewactivitypb "github.com/bptlab/cepta/models/events/crewactivitydataevent"
	crewprependpb "github.com/bptlab/cepta/models/events/crewprependdataevent"
	crewshiftpb "github.com/bptlab/cepta/models/events/crewshiftdataevent"
	crewtransitionpb "github.com/bptlab/cepta/models/events/crewtransitiondataevent"
	delayexplanationpb "github.com/bptlab/cepta/models/events/delayexplanationdataevent"
	gpstripupdatespb "github.com/bptlab/cepta/models/events/gpstripupdate"
	infrastructuremanagerpb "github.com/bptlab/cepta/models/events/infrastructuremanagerdataevent"
	livetrainpb "github.com/bptlab/cepta/models/events/livetraindataevent"
	locationpb "github.com/bptlab/cepta/models/events/locationdataevent"
	plannedtrainpb "github.com/bptlab/cepta/models/events/plannedtraindataevent"
	predictedtrainpb "github.com/bptlab/cepta/models/events/predictedtraindataevent"
	railwayundertakingpb "github.com/bptlab/cepta/models/events/railwayundertakingdataevent"
	stationpb "github.com/bptlab/cepta/models/events/stationdataevent"
	traininformationpb "github.com/bptlab/cepta/models/events/traininformationdataevent"
	vehiclepb "github.com/bptlab/cepta/models/events/vehicledataevent"
	weatherpb "github.com/bptlab/cepta/models/events/weatherdataevent"

	"github.com/golang/protobuf/proto"
)

func setSortAndID(SortFieldName string, IDFieldName string) extractors.MongoExtractorConfig {
	return extractors.MongoExtractorConfig{SortFieldName: SortFieldName, IDFieldName: IDFieldName}
}

// Setup ...
func (s *ReplayerServer) Setup(ctx context.Context) error {

	// Initialization will happen later on
	s.mongo = new(libdb.MongoDB)

	s.CheckpointsRplr = &Replayer{
		SourceName: "checkpoints",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_Checkpoint{Checkpoint: event.(*checkpointpb.CheckpointData)}}
		}, &checkpointpb.CheckpointData{}, setSortAndID("startDate", "trainId")),
		Topic: topics.Topic_CHECKPOINT_DATA,
	}

	s.CrewActivityRplr = &Replayer{
		SourceName: "crew_activity",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_CrewActivity{CrewActivity: event.(*crewactivitypb.CrewActivityData)}}
		}, &crewactivitypb.CrewActivityData{}, setSortAndID("activityId", "activityId")),
		Topic: topics.Topic_CREW_ACTIVITY_DATA,
	}

	s.CrewEndRplr = &Replayer{
		SourceName: "crew_end",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_CrewPrepEnd{CrewPrepEnd: event.(*crewprependpb.CrewPrepEndData)}}
		}, &crewprependpb.CrewPrepEndData{}, setSortAndID("activityId", "activityId")),
		Topic: topics.Topic_CREW_PREP_DATA,
	}

	s.CrewShiftRplr = &Replayer{
		SourceName: "crew_shift",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_CrewShift{CrewShift: event.(*crewshiftpb.CrewShiftData)}}
		}, &crewshiftpb.CrewShiftData{}, setSortAndID("shiftId", "shiftId")),
		Topic: topics.Topic_CREW_SHIFT_DATA,
	}

	s.CrewTransitionRplr = &Replayer{
		SourceName: "crew_transition",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_CrewTransition{CrewTransition: event.(*crewtransitionpb.CrewTransitionData)}}
		}, &crewtransitionpb.CrewTransitionData{}, setSortAndID("activityId", "activityId")),
		Topic: topics.Topic_CREW_TRANSITION_DATA,
	}

	s.DelayExplanationRplr = &Replayer{
		SourceName: "delay_explanation",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_DelayExplanation{DelayExplanation: event.(*delayexplanationpb.DelayExplanationData)}}
		}, &delayexplanationpb.DelayExplanationData{}, setSortAndID("trainId", "trainId")),
		Topic: topics.Topic_DELAY_EXPLANATION_DATA,
	}

	s.InfrastructureManagerRplr = &Replayer{
		SourceName: "infrastructuremanager",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_InfrastructureManager{InfrastructureManager: event.(*infrastructuremanagerpb.InfrastructureManagerData)}}
		}, &infrastructuremanagerpb.InfrastructureManagerData{}, setSortAndID("imId", "imId")),
		Topic: topics.Topic_INFRASTRUCTURE_MANAGER_DATA,
	}

	s.LiveTrainRplr = &Replayer{
		SourceName: "livetraindata",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_LiveTrain{LiveTrain: event.(*livetrainpb.LiveTrainData)}}
		}, &livetrainpb.LiveTrainData{}, setSortAndID("eventTime", "trainId")), // id is mostly nil so we choose trainId
		Topic: topics.Topic_LIVE_TRAIN_DATA,
	}

	s.LocationRplr = &Replayer{
		//SourceName: "locationdata",
		SourceName: "eletastations",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_Location{Location: event.(*locationpb.LocationData)}}
		}, &locationpb.LocationData{}, setSortAndID("id", "id")),
		Topic: topics.Topic_LOCATION_DATA,
	}

	s.PlannedTrainRplr = &Replayer{
		SourceName: "plannedtraindata",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_PlannedTrain{PlannedTrain: event.(*plannedtrainpb.PlannedTrainData)}}
		}, &plannedtrainpb.PlannedTrainData{}, setSortAndID("ingestionTime", "trainId")), // id is mostly nil so we choose trainId
		Topic: topics.Topic_PLANNED_TRAIN_DATA,
	}

	s.PredictedTrainRplr = &Replayer{
		SourceName: "predictedtraindata",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_PredictedTrain{PredictedTrain: event.(*predictedtrainpb.PredictedTrainData)}}
		}, &predictedtrainpb.PredictedTrainData{}, setSortAndID("trainId", "trainId")), // id is mostly nil so we choose trainId
		Topic: topics.Topic_PREDICTED_TRAIN_DATA,
	}

	s.RailwayUndertakingRplr = &Replayer{
		SourceName: "railwayundertaking",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_RailwayUndertaking{RailwayUndertaking: event.(*railwayundertakingpb.RailwayUndertakingData)}}
		}, &railwayundertakingpb.RailwayUndertakingData{}, setSortAndID("ruName", "ruName")), // ruId is mostly nil so we choose ruName
		Topic: topics.Topic_RAILWAY_UNDERTAKING_DATA,
	}

	s.StationRplr = &Replayer{
		SourceName: "station",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_Station{Station: event.(*stationpb.StationData)}}
		}, &stationpb.StationData{}, setSortAndID("stationName", "stationName")),
		Topic: topics.Topic_STATION_DATA,
	}

	s.TrainInformationRplr = &Replayer{
		SourceName: "traininformationdata",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_TrainInformation{TrainInformation: event.(*traininformationpb.TrainInformationData)}}
		}, &traininformationpb.TrainInformationData{}, setSortAndID("id", "id")),
		Topic: topics.Topic_TRAIN_INFORMATION_DATA,
	}

	s.VehicleRplr = &Replayer{
		SourceName: "locomotive",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_Vehicle{Vehicle: event.(*vehiclepb.VehicleData)}}
		}, &vehiclepb.VehicleData{}, setSortAndID("vehicleId", "vehicleId")),
		Topic: topics.Topic_VEHICLE_DATA,
	}

	s.WeatherRplr = &Replayer{
		SourceName: "weather",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_Weather{Weather: event.(*weatherpb.WeatherData)}}
		}, &weatherpb.WeatherData{}, setSortAndID("detectionTime", "eventClass")),
		Topic: topics.Topic_WEATHER_DATA,
	}

	s.GpsRplr = &Replayer{
		SourceName: "gpsupdates",
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_GpsTripUpdate{GpsTripUpdate: event.(*gpstripupdatespb.GPSTripUpdate)}}
		}, &gpstripupdatespb.GPSTripUpdate{}, setSortAndID("eventTime", "tripId")),
		Topic: topics.Topic_GPS_TRIP_UPDATE_DATA,
	}

	s.Replayers = []*Replayer{
		s.CheckpointsRplr,
		s.CrewActivityRplr,
		s.CrewEndRplr,
		s.CrewShiftRplr,
		s.CrewTransitionRplr,
		s.DelayExplanationRplr,
		s.InfrastructureManagerRplr,
		s.LiveTrainRplr,
		s.LocationRplr,
		s.PlannedTrainRplr,
		s.PredictedTrainRplr,
		s.RailwayUndertakingRplr,
		s.StationRplr,
		s.TrainInformationRplr,
		s.VehicleRplr,
		s.WeatherRplr,
		s.GpsRplr,
	}

	// Connect to mongoDB
	log.Info("Connecting to MongoDB...")
	mongo, err := libdb.MongoDatabase(&s.MongoConfig)
	if err != nil {
		return fmt.Errorf("failed to initialize mongo database: %v", err)
	}
	*s.mongo = *mongo

	// Connect to kafka
	log.Info("Connecting to Kafka...")
	s.producer, err = kafkaproducer.Create(ctx, s.KafkaConfig)
	if err != nil {
		return fmt.Errorf("cannot produce events: %v", err)
	}

	return nil
}
