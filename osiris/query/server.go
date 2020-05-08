package main

import (
	"context"
	"errors"
	"fmt"
	"net"
	"os"
	"os/signal"
	"syscall"

	"github.com/bptlab/cepta/ci/versioning"
	pb "github.com/bptlab/cepta/models/grpc/query"
	"github.com/bptlab/cepta/models/internal/transport"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/bptlab/cepta/osiris/lib/utils"
	lib "github.com/bptlab/cepta/osiris/query/lib"

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

var server QueryServer

// QueryServer  ...
type QueryServer struct {
	pb.UnimplementedQueryServer
	grpcServer 		*grpc.Server
	MongoConfig    libdb.MongoDBConfig
	DB             *libdb.MongoDB
	TransportsCollection string
}

// NewQueryServer ...
func NewQueryServer(mongoConfig libdb.MongoDBConfig) QueryServer {
	return QueryServer{
		MongoConfig: mongoConfig,
	}
}

// Shutdown ...
func (s *QueryServer) Shutdown() {
	log.Info("Graceful shutdown")
	log.Info("Stopping GRPC server")
	s.grpcServer.Stop()
}

// rpc GetTransport(SingleTransportRequest) returns (models.internal.Transport) {}
//    rpc QueryTransports(QueryTransportsRequest) returns (stream models.internal.Transport) {}


// GetTransport ...
func (s *QueryServer) GetTransport(ctx context.Context, in *pb.SingleTransportRequest) (*transport.Transport, error) {
	token := utils.GetUserToken(ctx)
	if token == "" {
		// return nil, errors.New("Access denied")
	}
	t, ok, err := lib.GetTransport(s.DB.DB.Collection(s.TransportsCollection), in.GetTransportId())
	if err != nil {
		return nil, status.Error(codes.Internal, "Failed to query archive for transport")
	}
	if ok {
		return t, nil
	}
	return nil, status.Error(codes.NotFound, "Transport not found")
}

// QueryTransports ...
func (s *QueryServer) QueryTransports(request *pb.QueryTransportsRequest, stream pb.Query_QueryTransportsServer) error {
	token := utils.GetUserToken(stream.Context())
	if token == "" {
		// return &result.Empty{}, nil
	}
	if err := lib.QueryTransports(s.DB.DB.Collection(s.TransportsCollection), request, stream); err != nil {
		log.Errorf("Failed to query the archive for transports: %v", err)
		return status.Error(codes.Internal, "Failed to query archive for transports")
	}
	return nil
}

func main() {
	shutdown := make(chan os.Signal)
	signal.Notify(shutdown, syscall.SIGINT, syscall.SIGTERM)
	go func() {
		<-shutdown
		server.Shutdown()
	}()

	cliFlags := []cli.Flag{}
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServicePort, libcli.ServiceLogLevel)...)
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceConnectionTolerance)...)
	cliFlags = append(cliFlags, libdb.MongoDatabaseCliOptions...)
	cliFlags = append(cliFlags, []cli.Flag{
		&cli.StringFlag{
			Name:    "collection",
			Value:   "transports",
			Aliases: []string{"mongodb-collection"},
			EnvVars: []string{"MONGO_COLLECTION", "COLLECTION"},
			Usage:   "the mongo collection containing the transport archive",
		},
	}...)

	app := &cli.App{
		Name:    "CEPTA user management microservice",
		Version: versioning.BinaryVersion(Version, BuildTime),
		Usage:   "Queries the transport archive",
		Flags:   cliFlags,
		Action: func(ctx *cli.Context) error {
			level, err := log.ParseLevel(ctx.String("log"))
			if err != nil {
				log.Warnf("Log level '%s' does not exist.")
				level = log.InfoLevel
			}
			log.SetLevel(level)

			server = QueryServer{
				MongoConfig:    libdb.MongoDBConfig{}.ParseCli(ctx),
				TransportsCollection: ctx.String("collection"),
			}

			port := fmt.Sprintf(":%d", ctx.Int("port"))
			listener, err := net.Listen("tcp", port)
			if err != nil {
				return fmt.Errorf("failed to listen: %v", err)
			}
			if err := server.Setup(); err != nil {
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
func (s *QueryServer) Setup() error {
	mongo, err := libdb.MongoDatabase(&s.MongoConfig)
	if err != nil {
		return err
	}
	s.DB = mongo

	if s.TransportsCollection == "" {
		return errors.New("need to specify a valid collection name")
	}
	return nil
}

// Serve starts the service
func (s *QueryServer) Serve(listener net.Listener) error {
	log.Infof("Query service ready at %s", listener.Addr())
	s.grpcServer = grpc.NewServer()
	pb.RegisterQueryServer(s.grpcServer, s)

	if err := s.grpcServer.Serve(listener); err != nil {
		return err
	}
	log.Info("Closing socket")
	listener.Close()
	return nil
}
