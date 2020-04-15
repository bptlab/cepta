package main

import (
	"context"
	"fmt"
	"net"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"github.com/bptlab/cepta/auxiliary/producers/replayer/extractors"
	"github.com/bptlab/cepta/ci/versioning"
	topics "github.com/bptlab/cepta/models/constants/topic"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/bptlab/cepta/osiris/lib/utils"
	clivalues "github.com/romnnn/flags4urfavecli/values"

	checkpointpb "github.com/bptlab/cepta/models/events/checkpointdataevent"
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

	"github.com/golang/protobuf/ptypes"
	tspb "github.com/golang/protobuf/ptypes/timestamp"
	"github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
	"google.golang.org/grpc"
)

// Version will be injected at build time
var Version string = "Unknown"

// BuildTime will be injected at build time
var BuildTime string = ""

var grpcServer *grpc.Server
var done = make(chan bool, 1)
var log *logrus.Logger
var replayers = []*Replayer{}
var activeReplayers = []*Replayer{}

type server struct {
	pb.UnimplementedReplayerServer
	speed     int32
	limit     int
	mode      pb.ReplayType
	timerange pb.Timerange
	ids       []string
	active    bool
}

func (s *server) SeekTo(ctx context.Context, in *tspb.Timestamp) (*pb.Success, error) {
	log.Infof("Seeking to: %v", in)
	s.timerange.Start = in
	for _, replayer := range replayers {
		// Send RESET control message
		replayer.Ctrl <- pb.InternalControlMessageType_RESET
	}
	return &pb.Success{Success: true}, nil
}

func (s *server) Reset(ctx context.Context, in *pb.Empty) (*pb.Success, error) {
	log.Infof("Resetting")
	for _, replayer := range replayers {
		// Send RESET control message
		replayer.Ctrl <- pb.InternalControlMessageType_RESET
	}
	return &pb.Success{Success: true}, nil
}

func (s *server) Start(ctx context.Context, in *pb.ReplayStartOptions) (*pb.Success, error) {
	log.Infof("Starting")
	s.active = true
	for _, replayer := range replayers {
		go replayer.Start(log)
	}
	return &pb.Success{Success: true}, nil
}

func (s *server) Stop(ctx context.Context, in *pb.Empty) (*pb.Success, error) {
	log.Infof("Stopping")
	s.active = false
	return &pb.Success{Success: true}, nil
}

func (s *server) SetSpeed(ctx context.Context, in *pb.Speed) (*pb.Success, error) {
	log.Infof("Setting speed to: %v", int(in.GetSpeed()))
	s.speed = int32(in.GetSpeed())
	return &pb.Success{Success: true}, nil
}

func (s *server) SetType(ctx context.Context, in *pb.ReplayTypeOption) (*pb.Success, error) {
	log.Infof("Setting replay type to: %v", in.GetType())
	s.mode = in.GetType()
	return &pb.Success{Success: true}, nil
}

func (s *server) SetOptions(ctx context.Context, in *pb.ReplayOptions) (*pb.Success, error) {
	log.Infof("Setting replay options")
	success, err := s.SetSpeed(ctx, in.GetSpeed())
	if err != nil {
		return success, err
	}
	success, err = s.SeekTo(ctx, in.GetRange().GetStart())
	if err != nil {
		return success, err
	}
	success, err = s.SetType(ctx, &pb.ReplayTypeOption{Type: in.GetType()})
	if err != nil {
		return success, err
	}
	return &pb.Success{Success: true}, nil
}

func (s *server) GetStatus(ctx context.Context, in *pb.Empty) (*pb.ReplayStatus, error) {
	log.Info("Handling query for current replay status")
	return &pb.ReplayStatus{Active: s.active}, nil
}

func (s *server) GetOptions(ctx context.Context, in *pb.Empty) (*pb.ReplayStartOptions, error) {
	log.Info("Handling query for current replay options")
	return &pb.ReplayStartOptions{
		Speed: &pb.Speed{Speed: s.speed},
		Type:  s.mode,
		Range: &s.timerange,
		Ids:   s.ids,
	}, nil
}

func (s *server) Query(ctx context.Context, in *pb.QueryOptions) (*pb.ReplayDataset, error) {
	log.Info("Handling query for replay data")

	// allowed_sources

	// Collect all replay datasets from all replayers
	for _, replayer := range replayers {
		log.Info(replayer)
		// if utils.Contains()
	}
	/*
			replayer.Query.IncludeIds = &replayerServer.ids
		  replayer.Query.Timerange = &replayerServer.timerange
		  replayer.Query.Limit = &replayerServer.limit
		  replayer.Query.Offset = 0
	*/

	return &pb.ReplayDataset{
		Events: []*pb.ReplayedEvent{},
	}, nil
}

func serve(ctx *cli.Context, log *logrus.Logger, mongoPtr *libdb.MongoDB) error {
	kafkaConfig := kafkaproducer.KafkaProducerOptions{}.ParseCli(ctx)

	// For reference: When using postgres as a replaying database:
	/*
		postgresConfig := libdb.PostgresDBConfig{}.ParseCli(ctx)
		postgres, err := libdb.PostgresDatabase(&postgresConfig)
		if err != nil {
			log.Fatalf("failed to initialize postgres database: %v", err)
		}
	*/

	mongoConfig := libdb.MongoDBConfig{}.ParseCli(ctx)
	mongo, err := libdb.MongoDatabase(&mongoConfig)
	if err != nil {
		log.Fatalf("failed to initialize mongo database: %v", err)
	}
	mongoPtr.DB = mongo.DB

	// Parse CLI replay type
	var startMode pb.ReplayType
	if mode, found := pb.ReplayType_value[strings.ToUpper(ctx.String("mode"))]; found {
		startMode = pb.ReplayType(mode)
	} else {
		startMode = pb.ReplayType_PROPORTIONAL
	}

	// Parse CLI timerange
	timeRange := pb.Timerange{}
	if startTime, err := time.Parse(clivalues.DefaultTimestampFormat, ctx.String("start-timestamp")); err != nil {
		if protoStartTime, err := ptypes.TimestampProto(startTime); err != nil {
			timeRange.Start = protoStartTime
		}
	}
	if endTime, err := time.Parse(clivalues.DefaultTimestampFormat, ctx.String("end-timestamp")); err != nil {
		if protoEndTime, err := ptypes.TimestampProto(endTime); err != nil {
			timeRange.End = protoEndTime
		}
	}

	replayerServer := server{
		active:    true,
		limit:     100,
		mode:      startMode,
		timerange: timeRange,
		ids:       strings.Split(ctx.String("include"), ","),
	}

	switch startMode {
	case pb.ReplayType_CONSTANT:
		replayerServer.speed = int32(ctx.Int("pause"))
		log.Infof("Using constant replay with pause=%d", replayerServer.speed)
	case pb.ReplayType_PROPORTIONAL:
		replayerServer.speed = int32(ctx.Int("frequency"))
		log.Infof("Using proportional replay with frequency=%d", replayerServer.speed)
	default:
		replayerServer.speed = 5000
	}

	// Connect to kafka
	producer, err := kafkaproducer.KafkaProducer{}.Create(kafkaConfig)
	if err != nil {
		log.Fatalf("Cannot produce events: %s", err.Error())
	}

	includedSrcs := clivalues.EnumListValue{}.Parse(ctx.String("include-sources"))
	log.Infof("Include: %s", includedSrcs)
	excludedSrcs := clivalues.EnumListValue{}.Parse(ctx.String("exclude-sources"))
	log.Infof("Exclude: %s", excludedSrcs)

	for _, replayer := range replayers {
		// Filter replayers
		if len(includedSrcs) > 0 && !utils.Contains(includedSrcs, replayer.SourceName) {
			log.Debugf("Skipping %s", replayer.SourceName)
			continue
		}
		if len(excludedSrcs) > 0 && utils.Contains(excludedSrcs, replayer.SourceName) {
			log.Debugf("Skipping %s", replayer.SourceName)
			continue
		}
		// Set common replayer parameters
		replayer.producer = producer
		replayer.Ctrl = make(chan pb.InternalControlMessageType)
		replayer.Query.IncludeIds = &replayerServer.ids
		replayer.Query.Timerange = &replayerServer.timerange
		replayer.Query.Limit = &replayerServer.limit
		replayer.Query.Offset = 0
		replayer.Active = &replayerServer.active
		replayer.Speed = &replayerServer.speed
		replayer.Mode = &replayerServer.mode
		replayer.Repeat = ctx.Bool("repeat")
		replayer.Brokers = kafkaConfig.Brokers
		activeReplayers = append(activeReplayers, replayer)
		go replayer.Start(log)
	}

	port := fmt.Sprintf(":%d", ctx.Int("port"))
	log.Infof("Serving at %s", port)
	listener, err := net.Listen("tcp", port)
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	grpcServer = grpc.NewServer()
	pb.RegisterReplayerServer(grpcServer, &replayerServer)

	if err := grpcServer.Serve(listener); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
	log.Info("Closing socket")
	listener.Close()
	done <- true
	return nil
}

func main() {
	// Register shutdown routine
	shutdown := make(chan os.Signal)
	signal.Notify(shutdown, syscall.SIGINT, syscall.SIGTERM)
	go func() {
		<-shutdown
		log.Info("Graceful shutdown")
		log.Info("Sending SHUTDOWN signal to all replaying topics")
		for _, replayer := range activeReplayers {
			log.Debugf("Sending SHUTDOWN signal to %s", replayer.SourceName)
			replayer.Ctrl <- pb.InternalControlMessageType_SHUTDOWN
			// Wait for ack
			log.Debugf("Waiting for ack from %s", replayer.SourceName)
			<-replayer.Ctrl
			log.Debugf("Shutdown complete for %s", replayer.SourceName)
		}

		log.Info("Stopping GRPC server")
		grpcServer.Stop()
	}()

	mongo := new(libdb.MongoDB)

	checkpoints := &Replayer{
		SourceName: topics.Topic_CHECKPOINT_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "departureTime"},
		Extractor:  extractors.NewMongoExtractor(mongo, &checkpointpb.CheckpointData{}),
		Topic:      topics.Topic_CHECKPOINT_DATA,
	}

	crewActivity := &Replayer{
		SourceName: topics.Topic_CREW_ACTIVITY_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &crewactivitypb.CrewActivityData{}),
		Topic:      topics.Topic_CREW_ACTIVITY_DATA,
	}
	crewEnd := &Replayer{
		SourceName: topics.Topic_CREW_END_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &crewprependpb.CrewPrepEndData{}),
		Topic:      topics.Topic_CREW_END_DATA,
	}
	crewPrep := &Replayer{
		SourceName: topics.Topic_CREW_PREP_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &crewprependpb.CrewPrepEndData{}),
		Topic:      topics.Topic_CREW_PREP_DATA,
	}
	crewShift := &Replayer{
		SourceName: topics.Topic_CREW_SHIFT_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &crewshiftpb.CrewShiftData{}),
		Topic:      topics.Topic_CREW_SHIFT_DATA,
	}
	crewTransition := &Replayer{
		SourceName: topics.Topic_CREW_TRANSITION_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &crewtransitionpb.CrewTransitionData{}),
		Topic:      topics.Topic_CREW_TRANSITION_DATA,
	}

	delayExplanation := &Replayer{
		SourceName: topics.Topic_DELAY_EXPLANATION_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &delayexplanationpb.DelayExplanationData{}),
		Topic:      topics.Topic_DELAY_EXPLANATION_DATA,
	}

	infrastructureManager := &Replayer{
		SourceName: topics.Topic_INFRASTRUCTURE_MANAGER_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &infrastructuremanagerpb.InfrastructureManagerData{}),
		Topic:      topics.Topic_INFRASTRUCTURE_MANAGER_DATA,
	}

	liveTrain := &Replayer{
		SourceName: topics.Topic_LIVE_TRAIN_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &livetrainpb.LiveTrainData{}),
		Topic:      topics.Topic_LIVE_TRAIN_DATA,
	}

	location := &Replayer{
		SourceName: topics.Topic_LOCATION_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &locationpb.LocationData{}),
		Topic:      topics.Topic_LOCATION_DATA,
	}

	plannedTrain := &Replayer{
		SourceName: topics.Topic_PLANNED_TRAIN_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &plannedtrainpb.PlannedTrainData{}),
		Topic:      topics.Topic_PLANNED_TRAIN_DATA,
	}

	predictedTrain := &Replayer{
		SourceName: topics.Topic_PREDICTED_TRAIN_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &predictedtrainpb.PredictedTrainData{}),
		Topic:      topics.Topic_PREDICTED_TRAIN_DATA,
	}

	railwayUndertaking := &Replayer{
		SourceName: topics.Topic_RAILWAY_UNDERTAKING_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &railwayundertakingpb.RailwayUndertakingData{}),
		Topic:      topics.Topic_RAILWAY_UNDERTAKING_DATA,
	}

	station := &Replayer{
		SourceName: topics.Topic_STATION_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &stationpb.StationData{}),
		Topic:      topics.Topic_STATION_DATA,
	}

	trainInformation := &Replayer{
		SourceName: topics.Topic_TRAIN_INFORMATION_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &traininformationpb.TrainInformationData{}),
		Topic:      topics.Topic_TRAIN_INFORMATION_DATA,
	}

	vehicle := &Replayer{
		SourceName: topics.Topic_VEHICLE_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &vehiclepb.VehicleData{}),
		Topic:      topics.Topic_VEHICLE_DATA,
	}

	weather := &Replayer{
		SourceName: topics.Topic_WEATHER_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "id"},
		Extractor:  extractors.NewMongoExtractor(mongo, &weatherpb.WeatherData{}),
		Topic:      topics.Topic_WEATHER_DATA,
	}

	gps := &Replayer{
		SourceName: topics.Topic_GPS_TRIP_UPDATE_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "actualTime"},
		Extractor:  extractors.NewMongoExtractor(mongo, &gpstripupdatespb.GPSTripUpdate{}),
		Topic:      topics.Topic_GPS_TRIP_UPDATE_DATA,
	}

	replayers = []*Replayer{
		checkpoints,
		crewActivity,
		crewEnd,
		crewPrep,
		crewShift,
		crewTransition,
		delayExplanation,
		infrastructureManager,
		liveTrain,
		location,
		plannedTrain,
		predictedTrain,
		railwayUndertaking,
		station,
		trainInformation,
		vehicle,
		weather,
		gps,
	}

	sources := make([]string, len(replayers))
	for i := range replayers {
		sources[i] = replayers[i].SourceName
	}

	cliFlags := []cli.Flag{}
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServicePort, libcli.ServiceLogLevel)...)
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceConnectionTolerance)...)
	cliFlags = append(cliFlags, libdb.PostgresDatabaseCliOptions...)
	cliFlags = append(cliFlags, libdb.MongoDatabaseCliOptions...)
	cliFlags = append(cliFlags, kafkaproducer.KafkaProducerCliOptions...)
	cliFlags = append(cliFlags, []cli.Flag{
		&cli.StringFlag{
			Name:    "include",
			Value:   "",
			Aliases: []string{"must-match", "match", "errids"},
			EnvVars: []string{"INCLUDE", "ERRIDS", "MATCH"},
			Usage:   "ids to be included in the replay",
		},
		&cli.GenericFlag{
			Name: "include-sources",
			Value: &clivalues.EnumListValue{
				Enum:       sources,
				Default:    []string{},
				AllowEmpty: true,
			},
			EnvVars: []string{"INCLUDE_SOURCES"},
			Usage:   "sources to be included in the replay (default: all)",
		},
		&cli.GenericFlag{
			Name: "exclude-sources",
			Value: &clivalues.EnumListValue{
				Enum:       sources,
				Default:    []string{},
				AllowEmpty: true,
			},
			EnvVars: []string{"EXCLUDE_SOURCES"},
			Usage:   "sources to be excluded from the replay (default: none)",
		},
		&cli.GenericFlag{
			Name: "mode",
			Value: &clivalues.EnumValue{
				Enum:    []string{"constant", "proportional"},
				Default: "proportional",
			},
			Aliases: []string{"replay-type", "type", "replay"},
			EnvVars: []string{"REPLAY_MODE", "MODE", "REPLAY"},
			Usage:   "replay mode (constant or proportional)",
		},
		&cli.IntFlag{
			Name:    "frequency",
			Value:   5000,
			Aliases: []string{"freq", "speed"},
			EnvVars: []string{"FREQUENCY", "SPEED", "FREQ"},
			Usage:   "speedup factor for proportional replay (as integer)",
		},
		&cli.IntFlag{
			Name:    "pause",
			Value:   2000,
			Aliases: []string{"wait"},
			EnvVars: []string{"PAUSE"},
			Usage:   "pause between sending events when using constant replay (in milliseconds)",
		},
		&cli.BoolFlag{
			Name:    "repeat",
			Value:   true,
			EnvVars: []string{"REPEAT"},
			Usage:   "whether or not to automatically resume and repeat the replay",
		},
		&cli.GenericFlag{
			Name:    "start-timestamp",
			Value:   &clivalues.TimestampValue{},
			Aliases: []string{"start"},
			EnvVars: []string{"START_TIMESTAMP", "START"},
			Usage:   "start timestamp",
		},
		&cli.GenericFlag{
			Name:    "end-timestamp",
			Value:   &clivalues.TimestampValue{},
			Aliases: []string{"end"},
			EnvVars: []string{"END_TIMESTAMP", "END"},
			Usage:   "end timestamp",
		},
	}...)

	log = logrus.New()

	app := &cli.App{
		Name:    "CEPTA Train data replayer producer",
		Version: versioning.BinaryVersion(Version, BuildTime),
		Usage:   "Produces train data events by replaying a database dump",
		Flags:   cliFlags,
		Action: func(ctx *cli.Context) error {
			go func() {
				level, err := logrus.ParseLevel(ctx.String("log"))
				if err != nil {
					log.Warnf("Log level '%s' does not exist.")
					level = logrus.InfoLevel
				}
				log.SetLevel(level)
				serve(ctx, log, mongo)
			}()
			<-done
			log.Info("Exiting")
			return nil
		},
	}
	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}
