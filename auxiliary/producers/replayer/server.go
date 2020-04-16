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

	"github.com/bptlab/cepta/ci/versioning"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/bptlab/cepta/osiris/lib/utils"
	clivalues "github.com/romnnn/flags4urfavecli/values"

	"github.com/bptlab/cepta/auxiliary/producers/replayer/extractors"
	topics "github.com/bptlab/cepta/models/constants/topic"
	checkpointpb "github.com/bptlab/cepta/models/events/checkpointdataevent"
	eventpb "github.com/bptlab/cepta/models/events/event"

	// crewactivitypb "github.com/bptlab/cepta/models/events/crewactivitydataevent"
	// crewprependpb "github.com/bptlab/cepta/models/events/crewprependdataevent"
	// crewshiftpb "github.com/bptlab/cepta/models/events/crewshiftdataevent"
	// crewtransitionpb "github.com/bptlab/cepta/models/events/crewtransitiondataevent"
	// delayexplanationpb "github.com/bptlab/cepta/models/events/delayexplanationdataevent"
	// gpstripupdatespb "github.com/bptlab/cepta/models/events/gpstripupdate"
	// infrastructuremanagerpb "github.com/bptlab/cepta/models/events/infrastructuremanagerdataevent"
	// livetrainpb "github.com/bptlab/cepta/models/events/livetraindataevent"
	// locationpb "github.com/bptlab/cepta/models/events/locationdataevent"
	// plannedtrainpb "github.com/bptlab/cepta/models/events/plannedtraindataevent"
	// predictedtrainpb "github.com/bptlab/cepta/models/events/predictedtraindataevent"
	// railwayundertakingpb "github.com/bptlab/cepta/models/events/railwayundertakingdataevent"
	// stationpb "github.com/bptlab/cepta/models/events/stationdataevent"
	// traininformationpb "github.com/bptlab/cepta/models/events/traininformationdataevent"
	// vehiclepb "github.com/bptlab/cepta/models/events/vehicledataevent"
	// weatherpb "github.com/bptlab/cepta/models/events/weatherdataevent"

	"github.com/golang/protobuf/proto"
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
	speed       int32
	limit       int
	mode        pb.ReplayType
	timerange   pb.Timerange
	ids         []string
	included    []string
	excluded    []string
	active      bool
	repeat      bool
	kafkaConfig kafkaproducer.KafkaProducerOptions
	mongoConfig libdb.MongoDBConfig
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

func (s *server) Query(in *pb.QueryOptions, stream pb.Replayer_QueryServer) error {
	included := make([]string, len(in.Sources))
	for si := range in.Sources {
		included[si] = in.Sources[si].String()
	}
	log.Infof("Handling query for sources: %v", included)
	// Collect all replay datasets from all replayers
	for _, replayer := range replayers {
		if !shouldInclude(replayer, included, []string{}, true) {
			continue
		}
		// Configure replayer
		limit := int(in.Limit)
		replayer.Query.IncludeIds = &in.Ids
		replayer.Query.Timerange = in.Timerange
		replayer.Query.Limit = &limit
		replayer.Query.Offset = int(in.Offset)
		log.Debug(replayer.SourceName)
		replayer.queryAndSend(stream)
	}
	return nil
}

func shouldInclude(replayer *Replayer, included []string, excluded []string, explicit bool) bool {
	explicitInclude := explicit || len(included) > 0
	if explicitInclude && !utils.Contains(included, replayer.Topic.String()) {
		log.Debugf("Skipping %s", replayer.Topic.String())
		return false
	}
	if len(excluded) > 0 && utils.Contains(excluded, replayer.Topic.String()) {
		log.Debugf("Skipping %s", replayer.Topic.String())
		return false
	}
	return true
}

func (s *server) serve(listener net.Listener, log *logrus.Logger) error {

	// For reference: When using postgres as a replaying database:
	/*
		postgresConfig := libdb.PostgresDBConfig{}.ParseCli(ctx)
		postgres, err := libdb.PostgresDatabase(&postgresConfig)
		if err != nil {
			log.Fatalf("failed to initialize postgres database: %v", err)
		}
	*/

	mongo, err := libdb.MongoDatabase(&s.mongoConfig)
	if err != nil {
		log.Fatalf("failed to initialize mongo database: %v", err)
	}

	checkpoints := &Replayer{
		SourceName: topics.Topic_CHECKPOINT_DATA.String(),
		Query:      &extractors.ReplayQuery{SortColumn: "departureTime"},
		Extractor: extractors.NewMongoExtractor(mongo, func(event proto.Message) *eventpb.Event {
			return &eventpb.Event{Event: &eventpb.Event_Checkpoint{Checkpoint: event.(*checkpointpb.CheckpointData)}}
		}, &checkpointpb.CheckpointData{}),
		Topic: topics.Topic_CHECKPOINT_DATA,
	}

	/*
		crewActivity := &Replayer{
			SourceName: topics.Topic_CREW_ACTIVITY_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_CrewActivity{}, &crewactivitypb.CrewActivityData{}),
			Topic:      topics.Topic_CREW_ACTIVITY_DATA,
		}
		crewEnd := &Replayer{
			SourceName: topics.Topic_CREW_END_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_CrewPrepEnd{}, &crewprependpb.CrewPrepEndData{}),
			Topic:      topics.Topic_CREW_END_DATA,
		}
		crewPrep := &Replayer{
			SourceName: topics.Topic_CREW_PREP_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_CrewPrepEnd{}, &crewprependpb.CrewPrepEndData{}),
			Topic:      topics.Topic_CREW_PREP_DATA,
		}
		crewShift := &Replayer{
			SourceName: topics.Topic_CREW_SHIFT_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_CrewShift{}, &crewshiftpb.CrewShiftData{}),
			Topic:      topics.Topic_CREW_SHIFT_DATA,
		}
		crewTransition := &Replayer{
			SourceName: topics.Topic_CREW_TRANSITION_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_CrewTransition{}, &crewtransitionpb.CrewTransitionData{}),
			Topic:      topics.Topic_CREW_TRANSITION_DATA,
		}

		delayExplanation := &Replayer{
			SourceName: topics.Topic_DELAY_EXPLANATION_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_DelayExplanation{}, &delayexplanationpb.DelayExplanationData{}),
			Topic:      topics.Topic_DELAY_EXPLANATION_DATA,
		}

		infrastructureManager := &Replayer{
			SourceName: topics.Topic_INFRASTRUCTURE_MANAGER_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_InfrastructureManager{}, &infrastructuremanagerpb.InfrastructureManagerData{}),
			Topic:      topics.Topic_INFRASTRUCTURE_MANAGER_DATA,
		}

		liveTrain := &Replayer{
			SourceName: topics.Topic_LIVE_TRAIN_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_LiveTrain{}, &livetrainpb.LiveTrainData{}),
			Topic:      topics.Topic_LIVE_TRAIN_DATA,
		}

		location := &Replayer{
			SourceName: topics.Topic_LOCATION_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_Location{}, &locationpb.LocationData{}),
			Topic:      topics.Topic_LOCATION_DATA,
		}

		plannedTrain := &Replayer{
			SourceName: topics.Topic_PLANNED_TRAIN_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_PlannedTrain{}, &plannedtrainpb.PlannedTrainData{}),
			Topic:      topics.Topic_PLANNED_TRAIN_DATA,
		}

		predictedTrain := &Replayer{
			SourceName: topics.Topic_PREDICTED_TRAIN_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_PredictedTrain{}, &predictedtrainpb.PredictedTrainData{}),
			Topic:      topics.Topic_PREDICTED_TRAIN_DATA,
		}

		railwayUndertaking := &Replayer{
			SourceName: topics.Topic_RAILWAY_UNDERTAKING_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_RailwayUndertaking{}, &railwayundertakingpb.RailwayUndertakingData{}),
			Topic:      topics.Topic_RAILWAY_UNDERTAKING_DATA,
		}

		station := &Replayer{
			SourceName: topics.Topic_STATION_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_Station{}, &stationpb.StationData{}),
			Topic:      topics.Topic_STATION_DATA,
		}

		trainInformation := &Replayer{
			SourceName: topics.Topic_TRAIN_INFORMATION_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_TrainInformation{}, &traininformationpb.TrainInformationData{}),
			Topic:      topics.Topic_TRAIN_INFORMATION_DATA,
		}

		vehicle := &Replayer{
			SourceName: topics.Topic_VEHICLE_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_Vehicle{}, &vehiclepb.VehicleData{}),
			Topic:      topics.Topic_VEHICLE_DATA,
		}

		weather := &Replayer{
			SourceName: topics.Topic_WEATHER_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "id"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_Weather{}, &weatherpb.WeatherData{}),
			Topic:      topics.Topic_WEATHER_DATA,
		}

		gps := &Replayer{
			SourceName: topics.Topic_GPS_TRIP_UPDATE_DATA.String(),
			Query:      &extractors.ReplayQuery{SortColumn: "actualTime"},
			Extractor:  extractors.NewMongoExtractor(mongo, &eventpb.Event_GPSTripUpdate{}, &gpstripupdatespb.GPSTripUpdate{}),
			Topic:      topics.Topic_GPS_TRIP_UPDATE_DATA,
		}
	*/

	replayers = []*Replayer{
		checkpoints,
		/*
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
		*/
	}

	// Connect to kafka
	producer, err := kafkaproducer.KafkaProducer{}.Create(s.kafkaConfig)
	if err != nil {
		log.Fatalf("Cannot produce events: %s", err.Error())
	}

	for _, replayer := range replayers {
		if !shouldInclude(replayer, s.included, s.excluded, false) {
			continue
		}
		// Set common replayer parameters
		replayer.producer = producer
		replayer.Ctrl = make(chan pb.InternalControlMessageType)
		replayer.Query.IncludeIds = &s.ids
		replayer.Query.Timerange = &s.timerange
		replayer.Query.Limit = &s.limit
		replayer.Query.Offset = 0
		replayer.Active = &s.active
		replayer.Speed = &s.speed
		replayer.Mode = &s.mode
		replayer.Repeat = s.repeat
		replayer.Brokers = s.kafkaConfig.Brokers
		activeReplayers = append(activeReplayers, replayer)
	}

	log.Infof("Serving at %s", listener.Addr())
	log.Info("Replayer ready")
	grpcServer = grpc.NewServer()
	pb.RegisterReplayerServer(grpcServer, s)

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

	var sources []string
	for t := range topics.Topic_value {
		sources = append(sources, t)
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
				port := fmt.Sprintf(":%d", ctx.Int("port"))
				listener, err := net.Listen("tcp", port)
				if err != nil {
					log.Fatalf("failed to listen: %v", err)
				}

				rs := server{
					active: true,
					limit:  100,
					repeat: ctx.Bool("repeat"),
					ids:    strings.Split(ctx.String("include"), ","),
				}

				// Parse mongo and kafka configs
				rs.kafkaConfig = kafkaproducer.KafkaProducerOptions{}.ParseCli(ctx)
				rs.mongoConfig = libdb.MongoDBConfig{}.ParseCli(ctx)

				// Parse CLI replay type
				if mode, found := pb.ReplayType_value[strings.ToUpper(ctx.String("mode"))]; found {
					rs.mode = pb.ReplayType(mode)
				} else {
					rs.mode = pb.ReplayType_PROPORTIONAL
				}

				// Parse CLI timerange
				if startTime, err := time.Parse(clivalues.DefaultTimestampFormat, ctx.String("start-timestamp")); err != nil {
					if protoStartTime, err := utils.ToProtoTime(startTime); err != nil {
						rs.timerange.Start = protoStartTime
					}
				}
				if endTime, err := time.Parse(clivalues.DefaultTimestampFormat, ctx.String("end-timestamp")); err != nil {
					if protoEndTime, err := utils.ToProtoTime(endTime); err != nil {
						rs.timerange.End = protoEndTime
					}
				}

				// Parse pause / frequency
				switch rs.mode {
				case pb.ReplayType_CONSTANT:
					rs.speed = int32(ctx.Int("pause"))
					log.Infof("Using constant replay with pause=%d", rs.speed)
				case pb.ReplayType_PROPORTIONAL:
					rs.speed = int32(ctx.Int("frequency"))
					log.Infof("Using proportional replay with frequency=%d", rs.speed)
				default:
					rs.speed = 5000
				}

				// Parse included and excluded values
				includedSrcs := clivalues.EnumListValue{}.Parse(ctx.String("include-sources"))
				log.Infof("Include: %s", includedSrcs)
				excludedSrcs := clivalues.EnumListValue{}.Parse(ctx.String("exclude-sources"))
				log.Infof("Exclude: %s", excludedSrcs)

				rs.serve(listener, log)
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
