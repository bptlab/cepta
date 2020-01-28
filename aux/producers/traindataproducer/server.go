package main

import (
	"context"
	"fmt"
	"net"
	"os"

	livetraindatareplayer "github.com/bptlab/cepta/aux/producers/traindataproducer/livetraindatareplayer"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	libutils "github.com/bptlab/cepta/osiris/lib/utils"
	"github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
	"google.golang.org/grpc"
)

var speed = 5000
var limit = 100
var mode = "proportional"

var db *libdb.DB
var log *logrus.Logger

type server struct {
	pb.UnimplementedReplayerServer
}

func (s *server) SeekTo(ctx context.Context, in *pb.Timestamp) (*pb.Success, error) {
	log.Infof("Seeking to: %v", in.GetTimestamp())
	return &pb.Success{Success: true}, nil
}

func (s *server) Reset(ctx context.Context, in *pb.Empty) (*pb.Success, error) {
	log.Infof("Resetting")
	return &pb.Success{Success: true}, nil
}

func (s *server) Start(ctx context.Context, in *pb.Empty) (*pb.Success, error) {
	log.Infof("Starting")
	return &pb.Success{Success: true}, nil
}

func (s *server) Stop(ctx context.Context, in *pb.Empty) (*pb.Success, error) {
	log.Infof("Stopping")
	return &pb.Success{Success: true}, nil
}

func (s *server) SetSpeed(ctx context.Context, in *pb.Frequency) (*pb.Success, error) {
	log.Infof("Setting speed to: %v", int(in.GetFrequency()))
	speed = int(in.GetFrequency())
	return &pb.Success{Success: true}, nil
}

func serve(ctx *cli.Context, log *logrus.Logger) error {
	config := libdb.DBConfig{}.ParseCli(ctx)
	var err error
	db, err = libdb.PostgresDatabase(&config)
	if err != nil {
		log.Fatalf("failed to initialize database: %v", err)
	}

	mode = ctx.String("mode")
	if !libutils.Contains([]string{"proportional", "constant"}, mode) {
		log.Warnf("%s is not a supported mode.")
		mode = "proportional"
	}
	live := &livetraindatareplayer.Replayer{
		TableName: "public.live",
		// mustMatch 	*string
		// timerange 	Timerange
		SortColumn: "ACTUAL_TIME",
		Limit:      &limit,
		Offset:     0,
		Db:         db,
		Speed:      &speed,
		Mode:       &mode,
	}
	go live.Start(log)

	port := fmt.Sprintf(":%d", ctx.Int("port"))
	log.Printf("Serving at %s", port)
	listener, err := net.Listen("tcp", port)
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	s := grpc.NewServer()
	pb.RegisterReplayerServer(s, &server{})
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
			&cli.StringFlag{
				Name:    "start-timestamp",
				Aliases: []string{"start"},
				EnvVars: []string{"START_TIMESTAMP", "START"},
				Usage:   "start timestamp",
			},
			&cli.StringFlag{
				Name:    "end-timestamp",
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
