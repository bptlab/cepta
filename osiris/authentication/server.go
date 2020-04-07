package main

import (
	"context"
	"fmt"
	"net"
	"os"
	"os/signal"
	"strconv"
	"syscall"

	pb "github.com/bptlab/cepta/models/grpc/authentication"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"

	"github.com/jinzhu/gorm"
	"github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
	"google.golang.org/grpc"
)

var grpcServer *grpc.Server
var done = make(chan bool, 1)
var log *logrus.Logger
var db *libdb.PostgresDB

type Server struct {
	pb.UnimplementedAuthenticationServer
	active bool
	db     *libdb.PostgresDB
}

// User is a struct to rep user account
type User struct {
	gorm.Model        // adds the fields ID, CreatedAt, UpdatedAt, DeletedAt automatically
	Email      string `json:"email"`
	Password   string `json:"password"`
	Token      string `json:"token";sql:"-"`
}

// AddUser adds a new user
func (server *Server) AddUser(ctx context.Context, in *pb.User) (*pb.Success, error) {
	user := User{
		//ID:       int(in.GetId().GetValue()),
		Email:    in.GetEmail(),
		Password: in.GetPassword(),
	}
	server.db.DB.NewRecord(user)
	err := server.db.DB.Create(&user).Error
	if err != nil {
		return &pb.Success{Success: false}, err
	}
	return &pb.Success{Success: true}, nil
}

// Login logs in a user
func (server *Server) Login(ctx context.Context, in *pb.UserId) (*pb.Validation, error) {
	var user User
	err := server.db.DB.First(&user, int(in.GetValue())).Error
	if err != nil {
		return &pb.Validation{
			Value: false}, nil
	}
	return &pb.Validation{
		Value: true}, nil
}

// RemoveUser removes a user
func (server *Server) RemoveUser(ctx context.Context, in *pb.UserId) (*pb.Success, error) {
	var user User
	err := server.db.DB.First(&user, int(in.GetValue())).Error
	if err != nil {
		return &pb.Success{Success: false}, err
	}
	err = server.db.DB.Delete(&user).Error
	if err != nil {
		return &pb.Success{Success: false}, err
	}
	return &pb.Success{Success: true}, nil
}

// SetEmail sets a users email
func (server *Server) SetEmail(ctx context.Context, in *pb.UserIdEmailInput) (*pb.Success, error) {
	var id int = int(in.GetUserId().GetValue())
	var email string = in.GetEmail()
	var user User
	err := server.db.DB.First(&user, id).Error
	if err != nil {
		return &pb.Success{Success: false}, err
	}
	err = server.db.DB.Model(&user).Update("Email", email).Error
	if err != nil {
		return &pb.Success{Success: false}, err
	}
	return &pb.Success{Success: true}, nil
}

func main() {

	shutdown := make(chan os.Signal)
	signal.Notify(shutdown, syscall.SIGINT, syscall.SIGTERM)
	go func() {
		<-shutdown
		log.Info("Graceful shutdown")
		log.Info("Stopping GRPC server")
		grpcServer.Stop()
	}()

	cliFlags := []cli.Flag{}
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServicePort, libcli.ServiceLogLevel)...)
	cliFlags = append(cliFlags, libdb.PostgresDatabaseCliOptions...)

	log = logrus.New()
	go func() {
		app := &cli.App{
			Name:  "CEPTA User management server",
			Usage: "manages the user database",
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

func serve(ctx *cli.Context, log *logrus.Logger) error {
	postgresConfig := libdb.PostgresDBConfig{}.ParseCli(ctx)
	var err error
	db, err = libdb.PostgresDatabase(&postgresConfig)
	db.DB.AutoMigrate(&User{})

	if err != nil {
		log.Fatalf("failed to initialize database: %v", err)
	}

	authenticationServer := Server{
		active: true,
		db:     db,
	}

	port := fmt.Sprintf(":%d", ctx.Int("port"))
	log.Printf("Serving at %s", port)
	listener, err := net.Listen("tcp", port)
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	grpcServer = grpc.NewServer()
	pb.RegisterAuthenticationServer(grpcServer, &authenticationServer)

	if err := grpcServer.Serve(listener); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
	log.Info("Closing socket")
	listener.Close()
	done <- true
	return nil
}

// returns true if the given validations have the same values
func equalValidation(v1 *pb.Validation, v2 *pb.Validation) bool {
	if v1.Value == v2.Value {
		return true
	}
	return false
}

// returns true if the given users have the same attribute values
func equalUser(u1 *pb.User, u2 *pb.User) bool {
	if !equalUserID(u1.Id, u2.Id) || u1.Email != u2.Email || u1.Password != u2.Password {
		return false
	}
	return true
}

// equalUserID returns true if the given ids have the same attribute values
func equalUserID(id1 *pb.UserId, id2 *pb.UserId) bool {
	if id1 == nil && id2 == nil {
		return true
	} else if id1 == nil || id2 == nil {
		return false
	} else if id1.Value != id2.Value {
		return false
	}
	return true
}

func int64FromString(text string) int64 {
	integer, _ := strconv.Atoi(text)
	return int64(integer)
}
func int64ToString(num int64) string {
	text := strconv.Itoa(int(num))
	return text
}

func removeElementFromStringArray(slice []string, element string) []string {
	for index, elem := range slice {
		if elem == element {
			return removeIndexFromStringArray(slice, index)
		}
	}
	return slice
}
func removeIndexFromStringArray(slice []string, index int) []string {
	return append(slice[:index], slice[index+1:]...)
}
