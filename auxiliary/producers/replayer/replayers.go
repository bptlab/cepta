package main

import (
	libdb "github.com/bptlab/cepta/osiris/lib/db"

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

// Setup ...
func (s *ReplayerServer) Setup() {

	// Initialization will happen later on
	s.mongo = new(libdb.MongoDB)

	s.CheckpointsRplr = &Replayer{
		SourceName: "checkpoints",
		Query:      &extractors.ReplayQuery{SortColumn: "departureTime"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_Checkpoint{Checkpoint: event.(*checkpointpb.CheckpointData)}}
		}, &checkpointpb.CheckpointData{}),
		Topic: topics.Topic_CHECKPOINT_DATA,
	}

	s.CrewActivityRplr = &Replayer{
		SourceName: "crew_activity",
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_CrewActivity{CrewActivity: event.(*crewactivitypb.CrewActivityData)}}
		}, &crewactivitypb.CrewActivityData{}),
		Topic: topics.Topic_CREW_ACTIVITY_DATA,
	}

	s.CrewPrepEndRplr = &Replayer{
		SourceName: "crew_end",
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_CrewPrepEnd{CrewPrepEnd: event.(*crewprependpb.CrewPrepEndData)}}
		}, &crewprependpb.CrewPrepEndData{}),
		Topic: topics.Topic_CREW_PREP_DATA,
	}

	s.CrewShiftRplr = &Replayer{
		SourceName: "crew_shift",
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_CrewShift{CrewShift: event.(*crewshiftpb.CrewShiftData)}}
		}, &crewshiftpb.CrewShiftData{}),
		Topic: topics.Topic_CREW_SHIFT_DATA,
	}

	s.CrewTransitionRplr = &Replayer{
		SourceName: "crew_transition",
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_CrewTransition{CrewTransition: event.(*crewtransitionpb.CrewTransitionData)}}
		}, &crewtransitionpb.CrewTransitionData{}),
		Topic: topics.Topic_CREW_TRANSITION_DATA,
	}

	s.DelayExplanationRplr = &Replayer{
		SourceName: "vsp",
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_DelayExplanation{DelayExplanation: event.(*delayexplanationpb.DelayExplanationData)}}
		}, &delayexplanationpb.DelayExplanationData{}),
		Topic: topics.Topic_DELAY_EXPLANATION_DATA,
	}

	s.InfrastructureManagerRplr = &Replayer{
		SourceName: "infrastructure_managerdata",
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_InfrastructureManager{InfrastructureManager: event.(*infrastructuremanagerpb.InfrastructureManagerData)}}
		}, &infrastructuremanagerpb.InfrastructureManagerData{}),
		Topic: topics.Topic_INFRASTRUCTURE_MANAGER_DATA,
	}

	s.LiveTrainRplr = &Replayer{
		SourceName: "livetraindata",
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_LiveTrain{LiveTrain: event.(*livetrainpb.LiveTrainData)}}
		}, &livetrainpb.LiveTrainData{}),
		Topic: topics.Topic_LIVE_TRAIN_DATA,
	}

	s.LocationRplr = &Replayer{
		SourceName: "locationdata",
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_Location{Location: event.(*locationpb.LocationData)}}
		}, &locationpb.LocationData{}),
		Topic: topics.Topic_LOCATION_DATA,
	}

	s.PlannedTrainRplr = &Replayer{
		SourceName: "plannedtraindata",
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_PlannedTrain{PlannedTrain: event.(*plannedtrainpb.PlannedTrainData)}}
		}, &plannedtrainpb.PlannedTrainData{}),
		Topic: topics.Topic_PLANNED_TRAIN_DATA,
	}

	s.PredictedTrainRplr = &Replayer{
		SourceName: "predictedtraindata",
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_PredictedTrain{PredictedTrain: event.(*predictedtrainpb.PredictedTrainData)}}
		}, &predictedtrainpb.PredictedTrainData{}),
		Topic: topics.Topic_PREDICTED_TRAIN_DATA,
	}

	s.RailwayUndertakingRplr = &Replayer{
		SourceName: "railwayundertakingdata",
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_RailwayUndertaking{RailwayUndertaking: event.(*railwayundertakingpb.RailwayUndertakingData)}}
		}, &railwayundertakingpb.RailwayUndertakingData{}),
		Topic: topics.Topic_RAILWAY_UNDERTAKING_DATA,
	}

	s.StationRplr = &Replayer{
		SourceName: "station",
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_Station{Station: event.(*stationpb.StationData)}}
		}, &stationpb.StationData{}),
		Topic: topics.Topic_STATION_DATA,
	}

	s.TrainInformationRplr = &Replayer{
		SourceName: "traininformationdata",
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_TrainInformation{TrainInformation: event.(*traininformationpb.TrainInformationData)}}
		}, &traininformationpb.TrainInformationData{}),
		Topic: topics.Topic_TRAIN_INFORMATION_DATA,
	}

	s.VehicleRplr = &Replayer{
		SourceName: "vehicle",
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_Vehicle{Vehicle: event.(*vehiclepb.VehicleData)}}
		}, &vehiclepb.VehicleData{}),
		Topic: topics.Topic_VEHICLE_DATA,
	}

	s.WeatherRplr = &Replayer{
		SourceName: "weather",
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_Weather{Weather: event.(*weatherpb.WeatherData)}}
		}, &weatherpb.WeatherData{}),
		Topic: topics.Topic_WEATHER_DATA,
	}

	s.GpsRplr = &Replayer{
		SourceName: "gpsupdates",
		Query:      &extractors.ReplayQuery{SortColumn: "actualTime"},
		Extractor: extractors.NewMongoExtractor(s.mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_GpsTripUpdate{GpsTripUpdate: event.(*gpstripupdatespb.GPSTripUpdate)}}
		}, &gpstripupdatespb.GPSTripUpdate{}),
		Topic: topics.Topic_GPS_TRIP_UPDATE_DATA,
	}

	s.Replayers = []*Replayer{
		s.CheckpointsRplr,
		s.CrewActivityRplr,
		s.CrewPrepEndRplr,
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
}
