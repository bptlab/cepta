package main

import (
	"context"
	"log"
	"net"
	"os"

  	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
	"google.golang.org/grpc"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
)

const (
	port = ":50051"
)

type server struct {
	pb.UnimplementedReplayerServer
}

func (s *server) SeekTo(ctx context.Context, in *pb.Timestamp) (*pb.Success, error) {
	log.Printf("Received: %v", in.GetTimestamp())
	return &pb.Success{success: true}, nil
}

func (s *server) Reset(ctx context.Context, in *pb.Empty) (*pb.Success, error) {
	log.Printf("Reset")
	return &pb.Success{success: true}, nil
}

func (s *server) Start(ctx context.Context, in *pb.Empty) (*pb.Success, error) {
	log.Printf("Start")
	return &pb.Success{success: true}, nil
}

func (s *server) Stop(ctx context.Context, in *pb.Empty) (*pb.Success, error) {
	log.Printf("Stop")
	return &pb.Success{success: true}, nil
}

func (s *server) SetSpeed(ctx context.Context, in *pb.Frequency) (*pb.Success, error) {
	log.Printf("SetSpeed")
	return &pb.Success{success: true}, nil
}

func serve() {
	lis, err := net.Listen("tcp", port)
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	s := grpc.NewServer()
	pb.RegisterGreeterServer(s, &server{})
	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}

func main() {
	app := &cli.App{
		Name: "CEPTA Train data replayer producer",
		Usage: "Produces train data events by replaying a database dump",
		Flags: []cli.Flag {
			&cli.BoolFlag{
				Name: "debug",
				Value: false,
				Aliases: []string{"d"},
				EnvVars: []string{"DEBUG", "ENABLE_DEBUG"},
				Usage: "Enable debug output",
			},
			&cli.IntFlag{
				Name: "port",
				Value: 80,
				Aliases: []string{"p"},
				EnvVars: []string{"PORT"},
				Usage: "grpc service port",
			},
			&cli.StringFlag{
				Name: "db-host",
				Value: "localhost",
				Aliases: []string{"db-hostname"},
				EnvVars: []string{"DB_HOST", "DB_HOSTNAME"},
				Usage: "Postgres database host",
			},
			&cli.IntFlag{
				Name: "db-port",
				Value: 5432,
				EnvVars: []string{"DB_PORT"},
				Usage: "Postgres database port",
			},
			&cli.StringFlag{
				Name: "db-user",
				Value: "postgres",
				Aliases: []string{"db-username"},
				EnvVars: []string{"DB_USER", "DB_USERNAME"},
				Usage: "Postgres database user",
			},
			&cli.StringFlag{
				Name: "db-name",
				Value: "postgres",
				Aliases: []string{"db", "database"},
				EnvVars: []string{"DB_NAME", "DB_DATABASE_NAME"},
				Usage: "Postgres database name",
			},
			&cli.StringFlag{
				Name: "db-password",
				Value: "postgres",
				Aliases: []string{"db-pass"},
				EnvVars: []string{"DB_PASSWORD", "DB_PASS"},
				Usage: "Postgres database password",
			},
		  },
		Action: func(ctx *cli.Context) error {
			ret := serve(ctx)
			return ret
		},
	}

	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}
