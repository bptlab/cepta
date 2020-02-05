/*package main


import (
	"context"
	"fmt"
	"net"
	"os"
	"strings"
	"time"

	livetraindatareplayer "github.com/bptlab/cepta/auxiliary/producers/producer/livetraindatareplayer"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"

	"github.com/golang/protobuf/ptypes"
	tspb "github.com/golang/protobuf/ptypes/timestamp"
	"github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
	"google.golang.org/grpc"
)

var db *libdb.DB
var log *logrus.Logger
var replayers []*livetraindatareplayer.Replayer

type server struct {
	pb.UnimplementedReplayerServer
	speed     int
	limit     int
	mode      pb.ReplayType
	timerange pb.Timerange
	ids       []string
}

func (s *server) SeekTo(ctx context.Context, in *tspb.Timestamp) (*pb.Success, error) {
	log.Infof("Seeking to: %v", in)
	return &pb.Success{Success: true}, nil
}

func (s *server) Reset(ctx context.Context, in *pb.Empty) (*pb.Success, error) {
	log.Infof("Resetting")
	return &pb.Success{Success: true}, nil
}

func (s *server) Start(ctx context.Context, in *pb.ReplayStartOptions) (*pb.Success, error) {
	log.Infof("Starting")
	return &pb.Success{Success: true}, nil
}

func (s *server) Stop(ctx context.Context, in *pb.Empty) (*pb.Success, error) {
	log.Infof("Stopping")
	return &pb.Success{Success: true}, nil
}

func (s *server) SetSpeed(ctx context.Context, in *pb.Speed) (*pb.Success, error) {
	log.Infof("Setting speed to: %v", int(in.GetSpeed()))
	s.speed = int(in.GetSpeed())
	return &pb.Success{Success: true}, nil
}

func (s *server) SetType(ctx context.Context, in *pb.ReplayTypeOption) (*pb.Success, error) {
	log.Infof("Setting replay type to: %v", in.GetType())
	s.mode = in.GetType()
	return &pb.Success{Success: true}, nil
}

func (s *server) GetStatus(ctx context.Context, in *pb.Empty) (*pb.ReplayStatus, error) {
	log.Info("Handling query for current replay status")
	return &pb.ReplayStatus{Active: true}, nil
}

func (s *server) GetOptions(ctx context.Context, in *pb.Empty) (*pb.ReplayStartOptions, error) {
	log.Info("Handling query for current replay options")
	return &pb.ReplayStartOptions{
		Ids: []string{},
	}, nil
}

func serve(ctx *cli.Context, log *logrus.Logger) error {
	config := libdb.DBConfig{}.ParseCli(ctx)
	var err error
	db, err = libdb.PostgresDatabase(&config)
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
		speed:     5000,
		limit:     100,
		mode:      startMode,
		timerange: timeRange,
		ids:       strings.Split(ctx.String("include"), ","),
	}
	kafkaBrokers := strings.Split(ctx.String("kafka-brokers"), ",")
	live := &livetraindatareplayer.Replayer{
		TableName:  "public.live",
		MustMatch:  &replayerServer.ids,
		Timerange:  &replayerServer.timerange,
		SortColumn: "ACTUAL_TIME",
		Limit:      &replayerServer.limit,
		Offset:     0,
		Db:         db,
		Speed:      &replayerServer.speed,
		Mode:       &replayerServer.mode,
		Brokers:    kafkaBrokers,
	}
	replayers = append(replayers, live)
	go live.Start(log)

	port := fmt.Sprintf(":%d", ctx.Int("port"))
	log.Printf("Serving at %s", port)
	listener, err := net.Listen("tcp", port)
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	s := grpc.NewServer()
	pb.RegisterReplayerServer(s, &replayerServer)
	if err := s.Serve(listener); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
	return nil
}

func main() {
	log = logrus.New()
	app := &cli.App{
		Name:  "CEPTA Train data replayer producer",
		Usage: "Produces train data events by replaying a database dump",
		Flags: append(libdb.DatabaseCliOptions, []cli.Flag{
			&cli.GenericFlag{
				Name: "log",
				Value: &libcli.EnumValue{
					Enum:    []string{"info", "debug", "warn", "fatal", "trace", "error", "panic"},
					Default: "info",
				},
				Aliases: []string{"log-level"},
				EnvVars: []string{"LOG", "LOG_LEVEL"},
				Usage:   "Log level",
			},
			&cli.IntFlag{
				Name:    "port",
				Value:   80,
				Aliases: []string{"p"},
				EnvVars: []string{"PORT"},
				Usage:   "grpc service port",
			},
			&cli.StringFlag{
				Name:    "include",
				Value:   "",
				Aliases: []string{"must-match", "match", "errids"},
				EnvVars: []string{"INCLUDE", "ERRIDS", "MATCH"},
				Usage:   "ids to be included in the replay",
			},
			&cli.StringFlag{
				Name:    "kafka-brokers",
				Value:   "localhost:29092",
				Aliases: []string{"brokers", "kafka"},
				EnvVars: []string{"BROKERS", "KAFKA_BROKERS"},
				Usage:   "comma separated list of kafka brokers",
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
				Value:   200,
				Aliases: []string{"freq", "speed"},
				EnvVars: []string{"FREQENCY", "SPEED", "FREQ"},
				Usage:   "speedup factor for proportional replay (as integer)",
			},
			&cli.IntFlag{
				Name:    "pause",
				Value:   2 * 1000,
				Aliases: []string{"wait"},
				EnvVars: []string{"PAUSE"},
				Usage:   "pause between sending events when using constant replay (in milliseconds)",
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
		}...),
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
}
*/
