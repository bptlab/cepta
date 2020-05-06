package main

import (
	"context"
	"errors"
	"fmt"
	"net"
	"os"
	"os/signal"
	"strings"
	"syscall"

	"github.com/bptlab/cepta/ci/versioning"
	pb "github.com/bptlab/cepta/models/grpc/usermgmt"
	"github.com/bptlab/cepta/models/internal/types/result"
	"github.com/bptlab/cepta/models/internal/types/users"
	"github.com/bptlab/cepta/models/internal/types/ids"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/bptlab/cepta/osiris/lib/utils"
	lib "github.com/bptlab/cepta/osiris/usermgmt/lib"

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

var server UserMgmtServer

// UserMgmtServer  ...
type UserMgmtServer struct {
	pb.UnimplementedUserManagementServer
	grpcServer 		*grpc.Server
	MongoConfig    libdb.MongoDBConfig
	DB             *libdb.MongoDB
	UserCollection string
	Reset          bool
	DefaultUser    users.InternalUser
}

// NewUserMgmtServer ...
func NewUserMgmtServer(mongoConfig libdb.MongoDBConfig) UserMgmtServer {
	return UserMgmtServer{
		MongoConfig: mongoConfig,
	}
}

// Shutdown ...
func (s *UserMgmtServer) Shutdown() {
	log.Info("Graceful shutdown")
	log.Info("Stopping GRPC server")
	s.grpcServer.Stop()
}

// GetUser ...
func (s *UserMgmtServer) GetUser(ctx context.Context, in *pb.GetUserRequest) (*users.User, error) {
	var user *users.User
	var err error
	token := utils.GetUserToken(ctx)
	if token == "" {
		// return nil, errors.New("Access denied")
	}
	// User ID takes precedence
	if in.UserId != nil && in.UserId.Id != "" {
		user, err = lib.GetUserByID(s.DB.DB.Collection(s.UserCollection), in.UserId)
		if err == nil && user != nil {
			return user, nil
		}
	}
	// Try email
	if in.Email != "" {
		user, err = lib.GetUserByEmail(s.DB.DB.Collection(s.UserCollection), in.Email)
		if err == nil && user != nil {
			return user, nil
		}
	}
	if err != nil {
		return nil, status.Error(codes.Internal, "Failed to query the database")
	}
	return nil, status.Error(codes.NotFound, "No such user found")
}

// GetSubscribersForTransport ...
func (s *UserMgmtServer) GetSubscribersForTransport(request *pb.GetSubscribersRequest, stream pb.UserManagement_GetSubscribersForTransportServer) error {
	token := utils.GetUserToken(stream.Context())
	if token == "" {
		// return &result.Empty{}, nil
	}
	if request.GetTransportId() == nil || request.GetTransportId().GetId() == "" {
		log.Errorf("Failed to get subscribers for transport: bad transport ID %v", request.GetTransportId())
		return status.Error(codes.InvalidArgument, "Bad transport ID")
	}
	if err := lib.StreamUsersByTransportID(s.DB.DB.Collection(s.UserCollection), request.GetTransportId(), stream); err != nil {
		log.Errorf("Failed to query the database for users by transport ID: %v", err)
		return status.Error(codes.Internal, "Failed to query the database for users by transport ID")
	}
	return nil
}

// GetUsers ...
func (s *UserMgmtServer) GetUsers(empty *result.Empty, stream pb.UserManagement_GetUsersServer) error {
	token := utils.GetUserToken(stream.Context())
	if token == "" {
		// return &result.Empty{}, nil
	}
	if err := lib.StreamUsers(s.DB.DB.Collection(s.UserCollection), stream); err != nil {
		log.Errorf("Failed to query all users: %v", err)
		return status.Error(codes.Internal, "Failed to query the database")
	}
	return nil
}

// UpdateUser ...
func (s *UserMgmtServer) UpdateUser(ctx context.Context, in *pb.UpdateUserRequest) (*result.Empty, error) {
	token := utils.GetUserToken(ctx)
	if token == "" {
		// return &result.Empty{}, nil
	}
	if err := lib.UpdateUser(s.DB.DB.Collection(s.UserCollection), in.User.User.Id, in.User); err != nil {
		log.Error("Failed to update the user: ", err)
		return &result.Empty{}, status.Error(codes.Internal, "Failed to update the user")
	}
	return &result.Empty{}, nil
}

// AddUser ...
func (s *UserMgmtServer) AddUser(ctx context.Context, in *pb.AddUserRequest) (*users.User, error) {
	token := utils.GetUserToken(ctx)
	if token == "" {
		// return &result.Empty{}, nil
	}

	in.GetUser().Password = strings.TrimSpace(in.GetUser().Password)
	in.GetUser().GetUser().Email = strings.TrimSpace(in.GetUser().GetUser().Email)

	// Check email and password are valid
	if in.GetUser().GetPassword() == "" {
		return nil, status.Error(codes.InvalidArgument, "Password must not be empty")
	}
	if in.GetUser().GetUser().GetEmail() == "" || !utils.IsValidEmail(in.GetUser().GetUser().GetEmail()) {
		return nil, status.Error(codes.InvalidArgument, "Email must be a valid email")
	}

	// Check if user with same mail already exists
	email := in.User.User.Email
	found, err := lib.GetUserByEmail(s.DB.DB.Collection(s.UserCollection), email)
	if err != nil {
		log.Error("Failed to lookup user: ", err)
		return nil, status.Error(codes.Internal, "Failed to lookup user")
	}
	if found != nil {
		return nil, status.Errorf(codes.AlreadyExists, "User with email %s already exists", email)
	}

	user, err := lib.AddUser(s.DB.DB.Collection(s.UserCollection), in.User)
	if err != nil {
		log.Error("Failed to add the user: ", err)
		return nil, status.Error(codes.Internal, "Failed to add the user")
	}
	return user, nil
}

// RemoveUser ...
func (s *UserMgmtServer) RemoveUser(ctx context.Context, in *pb.RemoveUserRequest) (*result.Empty, error) {
	token := utils.GetUserToken(ctx)
	if token == "" {
		// return &result.Empty{}, nil
	}
	// Check for at least one other admin user
	ok, err := lib.HasAdminUser(s.DB.DB.Collection(s.UserCollection), []*users.UserID{in.UserId})
	if err != nil {
		log.Error("Failed to check for admin users: ", err)
		return &result.Empty{}, status.Error(codes.Internal, "Failed to check admin users")
	}
	if !ok {
		return &result.Empty{}, status.Error(codes.PermissionDenied, "Require at least one admin user")
	}
	if err := lib.RemoveUser(s.DB.DB.Collection(s.UserCollection), in.UserId); err != nil {
		log.Error("Failed to remove the user: ", err)
		return &result.Empty{}, status.Error(codes.Internal, "Failed to remove the user")
	}
	return &result.Empty{}, nil
}

// GetUserCount ...
func (s *UserMgmtServer) GetUserCount(ctx context.Context, in *result.Empty) (*pb.UserCount, error) {
	count, err := lib.CountUsers(s.DB.DB.Collection(s.UserCollection))
	if err != nil {
		log.Error("Error counting users: ", err)
		return nil, status.Error(codes.Internal, "Error counting users")
	}
	return &pb.UserCount{Value: count}, nil
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
			Value:   "users",
			Aliases: []string{"mongodb-collection"},
			EnvVars: []string{"MONGO_COLLECTION", "COLLECTION"},
			Usage:   "the mongo collection containing the user data",
		},
		&cli.StringFlag{
			Name:    "default-email",
			Value:   "cepta@cepta.org",
			Aliases: []string{"initial-email", "admin-email"},
			EnvVars: []string{"DEFAULT_EMAIL", "INITIAL_EMAIL", "ADMIN_EMAIL"},
			Usage:   "Inital admin email (created if user database is empty)",
		},
		&cli.StringFlag{
			Name:    "default-password",
			Value:   "cepta",
			Aliases: []string{"initial-password", "admin-password"},
			EnvVars: []string{"DEFAULT_PASSWORD", "INITIAL_PASSWORD", "ADMIN_PASSWORD"},
			Usage:   "Inital admin password (created if user database is empty)",
		},
		&cli.BoolFlag{
			Name:    "reset",
			Value:   false,
			Aliases: []string{"clear", "delete"},
			EnvVars: []string{"RESET", "CLEAR", "DELETE"},
			Usage:   "Delete all user data before startup",
		},
	}...)

	app := &cli.App{
		Name:    "CEPTA user management microservice",
		Version: versioning.BinaryVersion(Version, BuildTime),
		Usage:   "Manages the user database",
		Flags:   cliFlags,
		Action: func(ctx *cli.Context) error {
			level, err := log.ParseLevel(ctx.String("log"))
			if err != nil {
				log.Warnf("Log level '%s' does not exist.")
				level = log.InfoLevel
			}
			log.SetLevel(level)

			server = UserMgmtServer{
				MongoConfig:    libdb.MongoDBConfig{}.ParseCli(ctx),
				UserCollection: ctx.String("collection"),
				Reset:          ctx.Bool("reset"),
				DefaultUser: users.InternalUser{
					User: &users.User{
						Email: ctx.String("default-email"),
						Transports: []*ids.CeptaTransportID {
							{Id: "123"},
						},
					},
					Password: ctx.String("default-password"),
				},
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
func (s *UserMgmtServer) Setup() error {
	mongo, err := libdb.MongoDatabase(&s.MongoConfig)
	if err != nil {
		return err
	}
	s.DB = mongo

	if s.UserCollection == "" {
		return errors.New("need to specify a valid collection name")
	}

	// Eventually clear the user database
	if s.Reset {
		if err := s.DB.DB.Collection(s.UserCollection).Drop(context.Background()); err != nil {
			log.Errorf("Failed to clear the user database: %v", err)
		}
		log.Info("Cleared user database")
	}

	hasAdmin, err := lib.HasAdminUser(s.DB.DB.Collection(s.UserCollection), []*users.UserID{})
	if err != nil {
		return fmt.Errorf("failed to check for admin users: %v", err)
	}
	if !hasAdmin {
		if s.DefaultUser.User != nil && s.DefaultUser.User.Email != "" && s.DefaultUser.Password != "" {
			defaultUser, err := lib.AddUser(s.DB.DB.Collection(s.UserCollection), &s.DefaultUser)
			if err != nil {
				return fmt.Errorf("failed to add default admin user: %v", err)
			}
			log.Infof("Added default user: %s", defaultUser)
		} else {
			return errors.New("empty user database and no default admin user specified")
		}
	}
	return nil
}

// Serve starts the service
func (s *UserMgmtServer) Serve(listener net.Listener) error {
	log.Infof("User Management service ready at %s", listener.Addr())
	s.grpcServer = grpc.NewServer()
	pb.RegisterUserManagementServer(s.grpcServer, s)

	if err := s.grpcServer.Serve(listener); err != nil {
		return err
	}
	log.Info("Closing socket")
	listener.Close()
	return nil
}
