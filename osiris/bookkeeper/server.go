package main

import (
	"context"
	"errors"
	"fmt"
	"github.com/bptlab/cepta/osiris/lib/utils"
	"github.com/golang/protobuf/proto"
	"net"
	"os"
	"os/signal"
	"strings"
	"syscall"

	"github.com/bptlab/cepta/ci/versioning"
	pb "github.com/bptlab/cepta/models/grpc/bookkeeper"
	topics "github.com/bptlab/cepta/models/constants/topic"
	"github.com/bptlab/cepta/models/internal/transport"
	"github.com/bptlab/cepta/models/internal/updates/plan"
	"github.com/bptlab/cepta/models/internal/station"
	"github.com/bptlab/cepta/models/internal/types/result"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	kafkaconsumer "github.com/bptlab/cepta/osiris/lib/kafka/consumer"
	libdb "github.com/bptlab/cepta/osiris/lib/db"

	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

// Version will be injected at build time
var Version string = "Unknown"

// BuildTime will be injected at build time
var BuildTime string = ""

var (
	defaultTransportPlanUpdateTopic = topics.Topic_TRANSPORT_PLAN_UPDATES
)

var server BookkeeperServer

// BookkeeperServer  ...
type BookkeeperServer struct {
	pb.UnimplementedBookkeeperServer
	grpcServer 		*grpc.Server

	MongoConfig    libdb.MongoDBConfig
	DB             *libdb.MongoDB

	kafkacConfig kafkaconsumer.Config
	kc           *kafkaconsumer.Consumer

	TransportsCollection string
	transportPlanUpdateTopic topics.Topic
}

// NewBookkeeperServer ...
func NewBookkeeperServer(mongoConfig libdb.MongoDBConfig, kafkaConfig kafkaconsumer.Config,) BookkeeperServer {
	return BookkeeperServer{
		MongoConfig: mongoConfig,
		kafkacConfig: kafkaConfig,
		transportPlanUpdateTopic: topics.Topic_TRANSPORT_PLAN_UPDATES,
	}
}

// Shutdown ...
func (s *BookkeeperServer) Shutdown() {
	log.Info("Graceful shutdown")
	log.Info("Stopping GRPC server")
	s.grpcServer.Stop()
	s.DB.Close()
	_ = s.kc.Close()
}

// AddTransport ...
func (s *BookkeeperServer) AddTransport(ctx context.Context, in *transport.Transport) (*result.Empty, error) {
	// s.DB.DB.Collection(s.TransportsCollection)
	// return nil, status.Error(codes.Internal, "Failed to bookkeeper archive for transport")

	return nil, status.Error(codes.NotFound, "Transport not found")
}

// AddStation ...
func (s *BookkeeperServer) AddStation(ctx context.Context, in *station.Station) (*result.Empty, error) {
	// s.DB.DB.Collection(s.TransportsCollection)
	// return nil, status.Error(codes.Internal, "Failed to bookkeeper archive for transport")

	return nil, status.Error(codes.NotFound, "Transport not found")
}

func main() {
	setupCtx, cancel := context.WithCancel(context.Background())
	defer cancel()
	shutdown := make(chan os.Signal)
	signal.Notify(shutdown, syscall.SIGINT, syscall.SIGTERM)
	go func() {
		<-shutdown
		cancel()
		server.Shutdown()
	}()

	var cliFlags []cli.Flag
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServicePort, libcli.ServiceLogLevel)...)
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceConnectionTolerance)...)
	cliFlags = append(cliFlags, libdb.MongoDatabaseCliOptions...)
	cliFlags = append(cliFlags, kafkaconsumer.CliOptions...)
	cliFlags = append(cliFlags, []cli.Flag{
		&cli.StringFlag{
			Name:    "collection",
			Value:   "transports",
			Aliases: []string{"mongodb-collection"},
			EnvVars: []string{"MONGO_COLLECTION", "COLLECTION"},
			Usage:   "the mongo collection containing the transport archive",
		},
		&cli.StringFlag{
			Name:    "topic",
			Value:   defaultTransportPlanUpdateTopic.String(),
			Aliases: []string{"plan-update-topic", "plan-updates"},
			EnvVars: []string{"TOPIC", "PLAN_UPDATE_TOPIC", "PLAN_UPDATES"},
			Usage:   "the kafka topic to subscribe for updates to the planned data",
		},
	}...)

	app := &cli.App{
		Name:    "CEPTA bookkeeper microservice",
		Version: versioning.BinaryVersion(Version, BuildTime),
		Usage:   "Archives transport data for bookkeeping and querying",
		Flags:   cliFlags,
		Action: func(ctx *cli.Context) error {
			level, err := log.ParseLevel(ctx.String("log"))
			if err != nil {
				log.Warnf("Log level '%s' does not exist.")
				level = log.InfoLevel
			}
			log.SetLevel(level)

			server = BookkeeperServer{
				MongoConfig:    libdb.MongoDBConfig{}.ParseCli(ctx),
				TransportsCollection: ctx.String("collection"),
			}

			port := fmt.Sprintf(":%d", ctx.Int("port"))
			listener, err := net.Listen("tcp", port)
			if err != nil {
				return fmt.Errorf("failed to listen: %v", err)
			}
			if err := server.Setup(setupCtx); err != nil {
				return err
			}
			return server.Serve(listener)
		},
	}
	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}

// Setup prepares the service
func (s *BookkeeperServer) Setup(ctx context.Context) error {
	mongo, err := libdb.MongoDatabase(&s.MongoConfig)
	if err != nil {
		return err
	}
	s.DB = mongo


	s.kafkacConfig.Topics = []string{s.transportPlanUpdateTopic.String()}
	if s.kafkacConfig.Group == "" {
		s.kafkacConfig.Group = "PlanUpdateConsumerGroup"
	}
	log.Infof("Will consume topic %s from %s (group %s)", s.kafkacConfig.Topics, strings.Join(s.kafkacConfig.Brokers, ", "), s.kafkacConfig.Group)
	s.kc, _, err = kafkaconsumer.ConsumeGroup(ctx, s.kafkacConfig)
	if err != nil {
		log.Warnf("Failed to connect to kafka broker (%s) (group %s) on topic %s",
			strings.Join(s.kafkacConfig.Brokers, ", "), s.kafkacConfig.Group, s.kafkacConfig.Topics)
		return err
	}

	if s.TransportsCollection == "" {
		return errors.New("need to specify a valid collection name")
	}
	return nil
}

// Serve starts the service
func (s *BookkeeperServer) Serve(listener net.Listener) error {
	log.Infof("Bookkeeper service ready at %s", listener.Addr())
	s.grpcServer = grpc.NewServer()
	pb.RegisterBookkeeperServer(s.grpcServer, s)

	go func() {
		for {
			msg := <-s.kc.Messages
			var planUpdate plan.PlanUpdate
			err := proto.Unmarshal(msg.Value, &planUpdate)
			if err != nil {
				log.Errorf("unmarshal error: %v", err)
			}
			log.Debugf("Received plan update: %v (%v)", planUpdate, msg.Timestamp)
		}
	}()

	if err := s.grpcServer.Serve(listener); err != nil {
		return err
	}
	log.Info("Closing socket")
	listener.Close()
	return nil
}
