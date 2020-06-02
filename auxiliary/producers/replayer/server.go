package main

import (
	"context"
	"errors"
	"fmt"
	"net"
	"os"
	"os/signal"
	"sort"
	"strings"
	"syscall"
	"time"

	"github.com/bptlab/cepta/ci/versioning"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	"github.com/bptlab/cepta/models/internal/types/result"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/bptlab/cepta/osiris/lib/utils"
	clivalues "github.com/romnnn/flags4urfavecli/values"

	topics "github.com/bptlab/cepta/models/constants/topic"

	"github.com/golang/protobuf/proto"
	tspb "github.com/golang/protobuf/ptypes/timestamp"
	"github.com/golang/protobuf/ptypes/wrappers"
	"github.com/romnnn/flags4urfavecli/values"
	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
	"google.golang.org/grpc"
	"google.golang.org/grpc/keepalive"
	"google.golang.org/grpc/metadata"
)

// Version will be injected at build time
var Version string = "Unknown"

// BuildTime will be injected at build time
var BuildTime string = ""

const (
	sortEventsLimit = 100000
)

// ReplayerServer ...
type ReplayerServer struct {
	pb.UnimplementedReplayerServer

	Immediate    bool
	StartOptions pb.ReplayStartOptions
	Active       bool
	KafkaConfig  kafkaproducer.Config
	MongoConfig  libdb.MongoDBConfig

	Replayers []*Replayer

	CheckpointsRplr           *Replayer
	CrewActivityRplr          *Replayer
	CrewEndRplr               *Replayer
	CrewShiftRplr             *Replayer
	CrewTransitionRplr        *Replayer
	DelayExplanationRplr      *Replayer
	InfrastructureManagerRplr *Replayer
	LiveTrainRplr             *Replayer
	LocationRplr              *Replayer
	PlannedTrainRplr          *Replayer
	PredictedTrainRplr        *Replayer
	RailwayUndertakingRplr    *Replayer
	StationRplr               *Replayer
	TrainInformationRplr      *Replayer
	VehicleRplr               *Replayer
	WeatherRplr               *Replayer
	GpsRplr                   *Replayer

	mongo             *libdb.MongoDB
	grpcServer        *grpc.Server
	done              chan bool
	cancelSetup       context.Context
	producer          *kafkaproducer.Producer
	logLevel          log.Level
	replayLogLevel    log.Level
	extractorLogLevel log.Level
}

// NewReplayerServer ...
func NewReplayerServer(mongoConfig libdb.MongoDBConfig, kafkaConfig kafkaproducer.Config) ReplayerServer {
	srv := ReplayerServer{
		KafkaConfig: kafkaConfig,
		MongoConfig: mongoConfig,
	}
	srv.StartOptions.Options = &pb.ReplayOptions{}
	srv.StartOptions.Sources = []*pb.SourceReplay{}
	return srv
}

// SeekTo ...
func (s *ReplayerServer) SeekTo(ctx context.Context, in *tspb.Timestamp) (*result.Empty, error) {
	log.Infof("Seeking to: %v", in)

	// Overrides all timerange starting points
	s.StartOptions.Options.Timerange.Start = in
	for _, source := range s.StartOptions.Sources {
		source.Options.Timerange.Start = in
	}
	for _, replayer := range s.Replayers {
		// Send RESET control message
		replayer.Ctrl <- pb.InternalControlMessageType_RESET
	}
	return &result.Empty{}, nil
}

// Reset ...
func (s *ReplayerServer) Reset(ctx context.Context, in *result.Empty) (*result.Empty, error) {
	log.Infof("Resetting")
	for _, replayer := range s.Replayers {
		// Send RESET control message
		replayer.Ctrl <- pb.InternalControlMessageType_RESET
	}
	return &result.Empty{}, nil
}

func mergeReplayerOptions(dest proto.Message, merge proto.Message) error {
	if utils.IsNilInterface(merge) {
		return nil
	}
	if dest == nil {
		return errors.New("merge option destination can not be nil")
	}
	proto.SetDefaults(merge)
	msg := proto.Clone(merge)
	return utils.MergeWithOverride(dest, msg)
}

func (s *ReplayerServer) setStartOptions(in *pb.ReplayStartOptions) error {
	newStartOptions := pb.ReplayStartOptions{
		Options: &defaultReplayOptions,
	}
	proto.SetDefaults(in)

	// Set default options if no options are provided
	if err := mergeReplayerOptions(&newStartOptions, proto.Clone(in)); err != nil {
		return errors.New("failed to merge replay options")
	}

	// Include all replayers if no sources are specified
	if len(newStartOptions.GetSources()) < 1 {
		for _, replayer := range s.Replayers {
			include(replayer, &newStartOptions.Sources)
		}
	}
	// Merge source and global options
	for _, source := range newStartOptions.Sources {
		source.Options = proto.Clone(newStartOptions.Options).(*pb.ReplayOptions)
	}

	for _, inSource := range in.Sources {
		for _, source := range newStartOptions.Sources {
			if inSource.Source == source.Source {
				if err := mergeReplayerOptions(source.Options, inSource.Options); err != nil {
					return errors.New("failed to merge replay options")
				}
			}
		}
	}

	for _, source := range newStartOptions.GetSources() {
		if replayer, included := getReplayer(s.Replayers, source.Source); included {
			replayer.Options = source
		}
	}
	s.StartOptions = newStartOptions
	return nil
}

// Start ...
func (s *ReplayerServer) Start(ctx context.Context, in *pb.ReplayStartOptions) (res *result.Empty, err error) {
	if s.Active {
		err = errors.New("replayer is already started and must be stopped first")
		return
	}
	s.Active = true
	res = &result.Empty{}
	_ = s.setStartOptions(in)
	s.start()
	log.Debugf("Sources: %v", s.StartOptions.GetSources())
	return
}

func (s *ReplayerServer) start() {
	log.Debugf("Start options: %v", s.StartOptions)
	for _, source := range s.StartOptions.GetSources() {
		if replayer, included := getReplayer(s.Replayers, source.Source); included {
			// Send START control message
			replayer.Ctrl <- pb.InternalControlMessageType_START
		}
	}
}

// Stop ...
func (s *ReplayerServer) Stop(ctx context.Context, in *result.Empty) (*result.Empty, error) {
	log.Infof("Stopping")
	if md, ok := metadata.FromIncomingContext(ctx); ok {
		log.Info(md)
	}
	go func() {
		s.Active = false
		for _, replayer := range s.Replayers {
			// Send STOP control message
			replayer.Ctrl <- pb.InternalControlMessageType_STOP
		}
	}()
	return &result.Empty{}, nil
}

// SetSpeed ...
func (s *ReplayerServer) SetSpeed(ctx context.Context, in *pb.Speed) (*result.Empty, error) {
	log.Infof("Setting speed to: %v", int(in.GetSpeed()))
	// Overrides all speed values
	s.StartOptions.Options.Speed = in
	for _, source := range s.StartOptions.Sources {
		source.Options.Speed = in
	}
	return &result.Empty{}, nil
}

// SetType ...
func (s *ReplayerServer) SetType(ctx context.Context, in *pb.ReplayModeOption) (*result.Empty, error) {
	log.Infof("Setting replay type to: %v", in.GetMode())
	// Overrides all modes
	s.StartOptions.Options.Mode = in.GetMode()
	for _, source := range s.StartOptions.Sources {
		source.Options.Mode = in.GetMode()
	}
	return &result.Empty{}, nil
}

func applyActiveOptions(options *pb.ActiveReplayOptions, dest *pb.ReplayOptions) error {
	return mergeReplayerOptions(dest, &pb.ReplayOptions{
		Speed:     options.Speed,
		Mode:      options.Mode,
		Timerange: options.Timerange,
		Repeat:    options.Repeat,
	})
}

// SetOptions ...
func (s *ReplayerServer) SetOptions(ctx context.Context, in *pb.ReplaySetOptionsRequest) (*result.Empty, error) {
	log.Infof("Setting replay options")

	// Set global options first
	if in.Options != nil {
		_ = applyActiveOptions(in.Options, s.StartOptions.Options)
		for _, source := range s.StartOptions.Sources {
			_ = applyActiveOptions(in.Options, source.Options)
		}
	}

	// Set source level options
	for _, source := range s.StartOptions.Sources {
		// Find replaying source with matching topic
		for _, srcReq := range in.Sources {
			if srcReq.Source == source.Source {
				_ = applyActiveOptions(srcReq, source.Options)
			}
		}
	}
	return &result.Empty{}, nil
}

// GetStatus ...
func (s *ReplayerServer) GetStatus(ctx context.Context, in *result.Empty) (*pb.ReplayStatus, error) {
	log.Info("Handling query for current replay status")
	return &pb.ReplayStatus{Active: s.Active}, nil
}

// GetOptions ...
func (s *ReplayerServer) GetOptions(ctx context.Context, in *result.Empty) (*pb.ReplayStartOptions, error) {
	log.Info("Handling query for current replay options")
	return &s.StartOptions, nil
}

// Query ...
func (s *ReplayerServer) Query(in *pb.QueryOptions, stream pb.Replayer_QueryServer) error {
	log.Infof("Handling query for %d sources", len(in.Sources))

	// Update to  new startOptions
	var updateStartOptions pb.ReplayStartOptions
	for _, source := range in.Sources {
		updateStartOptions.Sources = append(updateStartOptions.Sources, &pb.SourceReplay{
			Source: source.Source,
			Ids:    source.Ids,
			Options: &pb.ReplayOptions{
				Timerange: source.GetOptions().GetTimerange(),
				Limit:     source.GetOptions().GetLimit(),
				Offset:    source.GetOptions().GetOffset(),
			},
		})
	}
	_ = s.setStartOptions(&updateStartOptions)

	replaySort := in.GetSort()
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	var events []*pb.ReplayedEvent
	var eventCount int
	handler := func(r *Replayer) func(event *pb.ReplayedEvent) {
		return func(event *pb.ReplayedEvent) {
			if !replaySort {
				// Send right away
				if err := stream.Send(event); err != nil {
					r.log.Errorf("Failed to send: %v", err)
				}
			} else {
				events = append(events, event)
				eventCount++
				if eventCount > sortEventsLimit {
					// Too many events to sort. Abort.
					cancel()
				}
			}
		}
	}

	for _, source := range in.Sources {
		if replayer, included := getReplayer(s.Replayers, source.Source); included {
			// Collect all replay datasets from all replayers
			if err := replayer.queryAndSend(ctx, source, handler(replayer)); err != nil {
				if ctx.Err() == nil {
					return err
				}
			}
		}
	}

	// All done, sort now. This will HURT memory but okay for usage in tests with small data sets
	if replaySort {
		if ctx.Err() != nil {
			return errors.New("too many events. Either disable sort or set a limit")
		}
		replayTime := func(e1, e2 *pb.ReplayedEvent) bool {
			if t1, err := utils.FromProtoTime(e1.GetReplayTimestamp()); err == nil {
				if t2, err := utils.FromProtoTime(e2.GetReplayTimestamp()); err == nil {
					return t1.Sub(t2) < 0
				}
			}
			return false
		}
		by(replayTime).sort(events)

		for _, event := range events {
			if err := stream.Send(event); err != nil {
				log.Errorf("Failed to send: %v", err)
			}
		}
	}
	return nil
}

type by func(e1, e2 *pb.ReplayedEvent) bool

func (b by) sort(events []*pb.ReplayedEvent) {
	es := &eventSorter{
		events: events,
		by:     b,
	}
	sort.Sort(es)
}

type eventSorter struct {
	events []*pb.ReplayedEvent
	by     func(e1, e2 *pb.ReplayedEvent) bool
}

func (s *eventSorter) Len() int {
	return len(s.events)
}

func (s *eventSorter) Swap(i, j int) {
	s.events[i], s.events[j] = s.events[j], s.events[i]
}

func (s *eventSorter) Less(i, j int) bool {
	return s.by(s.events[i], s.events[j])
}

func getReplayer(replayers []*Replayer, included topics.Topic) (*Replayer, bool) {
	for _, replayer := range replayers {
		if replayer.Topic == included {
			return replayer, true
		}
	}
	return nil, false
}

func filterReplayers(replayers []*Replayer, filterFunc func(r *Replayer) bool) []*Replayer {
	var included []*Replayer
	for _, replayer := range replayers {
		if filterFunc(replayer) {
			included = append(included, replayer)
		}
	}
	return included
}

func include(r *Replayer, sources *[]*pb.SourceReplay) {
	// Check if the replayer already exists
	for _, source := range *sources {
		if source.Source == r.Topic {
			return
		}
	}
	*sources = append(*sources, &pb.SourceReplay{
		Source: r.Topic,
	})
}

// Serve ...
func (s *ReplayerServer) Serve(listener net.Listener) error {

	// For reference: When using postgres as a replaying database:
	/*
		postgresConfig := libdb.PostgresDBConfig{}.ParseCli(ctx)
		postgres, err := libdb.PostgresDatabase(&postgresConfig)
		if err != nil {
			log.Fatalf("failed to initialize postgres database: %v", err)
		}
	*/
	defer func() {
		if err := s.producer.Close(); err != nil {
			log.Errorf("Failed to close kafka producer: %v", err)
		}
		if err := s.mongo.Close(); err != nil {
			log.Errorf("Failed to close mongo connection: %v", err)
		}
	}()

	logger := log.New()

	for _, replayer := range s.Replayers {
		// Set common replayer parameters
		replayer.producer = s.producer
		replayer.Ctrl = make(chan pb.InternalControlMessageType, 10)
		replayer.Brokers = s.KafkaConfig.Brokers
		replayer.Extractor.SetLogLevel(s.extractorLogLevel)
		logger.SetLevel(s.replayLogLevel)
		go replayer.Start(logger)
	}

	log.Infof("Serving at %s", listener.Addr())
	s.grpcServer = grpc.NewServer(
		grpc.ConnectionTimeout(300 * time.Second),
		grpc.KeepaliveParams(
			keepalive.ServerParameters{
				MaxConnectionIdle:     10 * time.Minute,
				MaxConnectionAgeGrace: 10 * time.Minute,
			},
		),
	)
	pb.RegisterReplayerServer(s.grpcServer, s)

	log.Info("Replayer ready")
	if s.Immediate {
		go func() {
			log.Info("Immediate replay will start in 1 second")
			time.Sleep(1 * time.Second)
			_ = s.setStartOptions(&s.StartOptions)
			s.start()
		}()
	} else {
		log.Info("Waiting for GRPC START request")
	}

	if err := s.grpcServer.Serve(listener); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
	log.Info("Closing socket")
	_ = listener.Close()
	s.done <- true
	return nil
}

// Shutdown ...
func (s *ReplayerServer) Shutdown() {
	log.Info("Graceful shutdown")
	log.Info("Sending SHUTDOWN signal to all replaying topics")
	for _, replayer := range s.Replayers {
		log.Debugf("Sending SHUTDOWN signal to %s", replayer.SourceName)
		replayer.Ctrl <- pb.InternalControlMessageType_SHUTDOWN
		// Wait for ack
		log.Debugf("Waiting for ack from %s", replayer.SourceName)
		<-replayer.Ctrl
		log.Debugf("Shutdown complete for %s", replayer.SourceName)
	}
	log.Info("Stopping GRPC server")
	if s.grpcServer != nil {
		s.grpcServer.GracefulStop()
	}
}

func main() {
	var sources []string
	for t := range topics.Topic_value {
		sources = append(sources, t)
	}

	var cliFlags []cli.Flag
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServicePort, libcli.ServiceLogLevel)...)
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceConnectionTolerance)...)
	cliFlags = append(cliFlags, libdb.PostgresDatabaseCliOptions...)
	cliFlags = append(cliFlags, libdb.MongoDatabaseCliOptions...)
	cliFlags = append(cliFlags, kafkaproducer.CliOptions...)
	cliFlags = append(cliFlags, []cli.Flag{
		&cli.GenericFlag{
			Name: "extractor-log",
			Value: &values.EnumValue{
				Enum:    []string{"info", "debug", "warn", "fatal", "trace", "error", "panic"},
				Default: "info",
			},
			Aliases: []string{"extractor-log-level"},
			EnvVars: []string{"EXTRACTOR_LOG", "EXTRACTOR_LOG_LEVEL"},
			Usage:   "Log level",
		},
		&cli.GenericFlag{
			Name: "replay-log",
			Value: &values.EnumValue{
				Enum:    []string{"info", "debug", "warn", "fatal", "trace", "error", "panic"},
				Default: "info",
			},
			Aliases: []string{"replay-log-level"},
			EnvVars: []string{"REPLAY_LOG", "REPLAY_LOG_LEVEL"},
			Usage:   "Log level",
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
		&cli.BoolFlag{
			Name: "immediate",
			Value: false,
			Aliases: []string{"no-wait", "instant"},
			EnvVars: []string{"IMMEDIATE", "NO_WAIT", "INSTANT"},
			Usage:   "do not wait for a start signal and start producing immediately",
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
			Value:   500, // 500x speedup
			Aliases: []string{"freq", "speed"},
			EnvVars: []string{"FREQUENCY", "SPEED", "FREQ"},
			Usage:   "speedup factor for proportional replay (as integer)",
		},
		&cli.IntFlag{
			Name:    "pause",
			Value:   2000, // 2sec intervals
			Aliases: []string{"wait"},
			EnvVars: []string{"PAUSE"},
			Usage:   "pause between sending events when using constant replay (in milliseconds)",
		},
		&cli.BoolFlag{
			Name:    "no-repeat",
			Value:   false,
			EnvVars: []string{"DISABLE_REPEAT"},
			Usage:   "disable resume after replay ended",
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

	app := &cli.App{
		Name:    "CEPTA Train data replayer producer",
		Version: versioning.BinaryVersion(Version, BuildTime),
		Usage:   "Produces train data events by replaying a database dump",
		Flags:   cliFlags,
		Action: func(ctx *cli.Context) error {
			done := make(chan bool, 1)
			go func() {
				var err error
				port := fmt.Sprintf(":%d", ctx.Int("port"))
				listener, err := net.Listen("tcp", port)
				if err != nil {
					log.Fatalf("failed to listen: %v", err)
				}

				server := NewReplayerServer(
					libdb.MongoDBConfig{}.ParseCli(ctx),
					kafkaproducer.Config{}.ParseCli(ctx),
				)

				// Register shutdown routine
				setupCtx, cancelSetup := context.WithCancel(context.Background())
				shutdown := make(chan os.Signal)
				signal.Notify(shutdown, syscall.SIGINT, syscall.SIGTERM)
				go func() {
					<-shutdown
					cancelSetup()
					server.Shutdown()
				}()

				server.done = done
				server.Immediate = ctx.Bool("immediate")
				server.extractorLogLevel, err = log.ParseLevel(ctx.String("extractor-log"))
				server.replayLogLevel, err = log.ParseLevel(ctx.String("replay-log"))
				server.logLevel, err = log.ParseLevel(ctx.String("log"))
				log.SetLevel(server.logLevel)

				if err := server.Setup(setupCtx); err != nil {
					log.Fatalf("Failed to setup replayer server: %v", err)
				}
				server.StartOptions.Options = &pb.ReplayOptions{Repeat: &wrappers.BoolValue{Value: !ctx.Bool("no-repeat")}}

				// Parse included and excluded values
				included := clivalues.EnumListValue{}.Parse(ctx.String("include-sources"))
				log.Infof("Include: %s", included)
				excluded := clivalues.EnumListValue{}.Parse(ctx.String("exclude-sources"))
				log.Infof("Exclude: %s", excluded)

				replayers := filterReplayers(server.Replayers, func(r *Replayer) bool {
					if len(included) > 0 && !utils.Contains(included, strings.ToLower(r.Topic.String())) {
						return false
					}
					if len(excluded) > 0 && utils.Contains(excluded, strings.ToLower(r.Topic.String())) {
						return false
					}
					return true
				})
				for _, r := range replayers {
					include(r, &server.StartOptions.Sources)
				}

				// Parse replay mode
				if mode, found := pb.ReplayMode_value[strings.ToUpper(ctx.String("mode"))]; found {
					server.StartOptions.GetOptions().Mode = pb.ReplayMode(mode)
				} else {
					server.StartOptions.GetOptions().Mode = pb.ReplayMode_PROPORTIONAL
				}

				// Parse timerange
				if startTime, err := time.Parse(clivalues.DefaultTimestampFormat, ctx.String("start-timestamp")); err != nil {
					if protoStartTime, err := utils.ToProtoTime(startTime); err != nil {
						server.StartOptions.Options.Timerange.Start = protoStartTime
					}
				}
				if endTime, err := time.Parse(clivalues.DefaultTimestampFormat, ctx.String("end-timestamp")); err != nil {
					if protoEndTime, err := utils.ToProtoTime(endTime); err != nil {
						server.StartOptions.Options.Timerange.End = protoEndTime
					}
				}

				// Parse pause / frequency
				switch server.StartOptions.GetOptions().GetMode() {
				case pb.ReplayMode_CONSTANT:
					server.StartOptions.Options.Speed = &pb.Speed{Speed: int32(ctx.Int("pause"))}
					log.Infof("Using constant replay with pause=%d", server.StartOptions.GetOptions().GetSpeed().GetSpeed())
				case pb.ReplayMode_PROPORTIONAL:
					server.StartOptions.Options.Speed = &pb.Speed{Speed: int32(ctx.Int("frequency"))}
					log.Infof("Using proportional replay with frequency=%d", server.StartOptions.GetOptions().GetSpeed().GetSpeed())
				default:
					server.StartOptions.Options.Speed = &pb.Speed{Speed: 5000}
				}

				if err := server.Serve(listener); err != nil {
					log.Errorf("Failed to serve: %v", err)
				}
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
