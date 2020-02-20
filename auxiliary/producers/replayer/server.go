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

	"github.com/bptlab/cepta/constants"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"

	"github.com/golang/protobuf/ptypes"
	tspb "github.com/golang/protobuf/ptypes/timestamp"
	"github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
	"google.golang.org/grpc"
)

var grpcServer *grpc.Server
var done = make(chan bool, 1)
var db *libdb.PostgresDB
var log *logrus.Logger
var replayers = []*Replayer{}

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

func serve(ctx *cli.Context, log *logrus.Logger) error {
	postgresConfig := libdb.PostgresDBConfig{}.ParseCli(ctx)
	// mongoConfig := libdb.MongoDBConfig{}.ParseCli(ctx)
	kafkaConfig := kafkaproducer.KafkaProducerOptions{}.ParseCli(ctx)

	var err error
	db, err = libdb.PostgresDatabase(&postgresConfig)
	if err != nil {
		log.Fatalf("failed to initialize database: %v", err)
	}

	// Parse CLI replay type
	var startMode pb.ReplayType
	if mode, found := pb.ReplayType_value[strings.ToUpper(ctx.String("mode"))]; found {
		startMode = pb.ReplayType(mode)
	} else {
		startMode = pb.ReplayType_PROPORTIONAL
	}

	// Parse CLI timerange
	timeRange := pb.Timerange{}
	if startTime, err := time.Parse(libcli.DefaultTimestampFormat, ctx.String("start-timestamp")); err != nil {
		if protoStartTime, err := ptypes.TimestampProto(startTime); err != nil {
			timeRange.Start = protoStartTime
		}
	}
	if endTime, err := time.Parse(libcli.DefaultTimestampFormat, ctx.String("end-timestamp")); err != nil {
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
	case pb.ReplayType_PROPORTIONAL:
		replayerServer.speed = int32(ctx.Int("frequency"))
	default:
		replayerServer.speed = 5000
	}

	live := &Replayer{
		TableName:  "public.live",
		SortColumn: "ACTUAL_TIME",
		DbModel:    libdb.LiveTrainData{},
		Topic:      constants.Topics_LIVE_TRAIN_DATA.String(),
	}

	weather := &Replayer{
		TableName:  "public.weather",
		SortColumn: "STARTTIMESTAMP",
		DbModel:    libdb.WeatherData{},
		Topic:      constants.Topics_WEATHER_DATA.String(),
	}

	replayers = []*Replayer{
		live,
		weather,
	}

	// Set common replayer parameters
	for _, replayer := range replayers {
		replayer.Ctrl = make(chan pb.InternalControlMessageType)
		replayer.MustMatch = &replayerServer.ids
		replayer.Timerange = &replayerServer.timerange
		replayer.Limit = &replayerServer.limit
		replayer.Offset = 0
		replayer.Db = db
		replayer.Active = &replayerServer.active
		replayer.Speed = &replayerServer.speed
		replayer.Mode = &replayerServer.mode
		replayer.Repeat = ctx.Bool("repeat")
		replayer.Brokers = kafkaConfig.Brokers
		go replayer.Start(log)
	}

	port := fmt.Sprintf(":%d", ctx.Int("port"))
	log.Printf("Serving at %s", port)
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
		for _, replayer := range replayers {
			log.Debugf("Sending SHUTDOWN signal to %s", replayer.TableName)
			replayer.Ctrl <- pb.InternalControlMessageType_SHUTDOWN
			// Wait for ack
			log.Debugf("Waiting for ack from %s", replayer.TableName)
			<-replayer.Ctrl
			log.Debugf("Shutdown complete for %s", replayer.TableName)
		}

		log.Info("Stopping GRPC server")
		grpcServer.Stop()
	}()

	cliFlags := []cli.Flag{}
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServicePort, libcli.ServiceLogLevel)...)
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
			Name: "mode",
			Value: &libcli.EnumValue{
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
			EnvVars: []string{"FREQENCY", "SPEED", "FREQ"},
			Usage:   "speedup factor for proportional replay (as integer)",
		},
		&cli.IntFlag{
			Name:    "pause",
			Value:   2,
			Aliases: []string{"wait"},
			EnvVars: []string{"PAUSE"},
			Usage:   "pause between sending events when using constant replay (in seconds)",
		},
		&cli.BoolFlag{
			Name:    "repeat",
			Value:   true,
			EnvVars: []string{"REPEAT"},
			Usage:   "whether or not to automatically resume and repeat the replay",
		},
		&cli.GenericFlag{
			Name:    "start-timestamp",
			Value:   &libcli.TimestampValue{},
			Aliases: []string{"start"},
			EnvVars: []string{"START_TIMESTAMP", "START"},
			Usage:   "start timestamp",
		},
		&cli.GenericFlag{
			Name:    "end-timestamp",
			Value:   &libcli.TimestampValue{},
			Aliases: []string{"end"},
			EnvVars: []string{"END_TIMESTAMP", "END"},
			Usage:   "end timestamp",
		},
	}...)

	log = logrus.New()
	go func() {
		app := &cli.App{
			Name:  "CEPTA Train data replayer producer",
			Usage: "Produces train data events by replaying a database dump",
			Flags: cliFlags,
			Action: func(ctx *cli.Context) error {
				level, err := logrus.ParseLevel(ctx.String("log"))
				if err != nil {
					log.Warnf("Log level '%s' does not exist.")
					level = logrus.InfoLevel
				}
				log.SetLevel(level)
				ret := serve(ctx, log)
				return ret
			},
		}
		err := app.Run(os.Args)
		if err != nil {
			log.Fatal(err)
		}
	}()

	<-done
	log.Info("Exiting")
}
