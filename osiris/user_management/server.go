package main

import (
	"context"
	"fmt"
	"net"
	"os"
	"os/signal"
	"strconv"
	"syscall"

	pb "github.com/bptlab/cepta/models/grpc/user_management"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"

	"github.com/jinzhu/gorm"
	"github.com/lib/pq"
	"github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
	"google.golang.org/grpc"
)

var grpcServer *grpc.Server
var done = make(chan bool, 1)
var log *logrus.Logger
var db *libdb.DB

type server struct {
	pb.UnimplementedUserManagementServer
	active bool
	db     *libdb.DB
}

//Account is a struct to rep user account
type Account struct {
	gorm.Model
	ID       int
	Email    string         `json:"email"`
	TrainIds pq.StringArray `gorm:"type:int[]"`
	Password string         `json:"password"`
	Token    string         `json:"token";sql:"-"`
}

// SetEmail sets a users email
func (server *server) SetEmail(ctx context.Context, in *pb.UserIdEmailInput) (*pb.Success, error) {
	var id int = int(in.GetUserId().GetValue())
	var email string = in.GetEmail()
	var account Account
	errors := server.db.DB.First(&account, "id = ?", id).GetErrors()
	if errors != nil {
		return handleErrors(errors, "Could not fetch account")
	}
	errors = server.db.DB.Model(&account).Update("Email", email).GetErrors()
	if errors != nil {
		return handleErrors(errors, "Could not update account")
	}
	return &pb.Success{Success: true}, nil
}

// AddTrain adds a train to a user
func (server *server) AddTrain(ctx context.Context, in *pb.UserIdTrainIdInput) (*pb.Success, error) {
	var userID int = int(in.GetUserId().GetValue())
	var trainID int = int(in.GetTrainId().GetValue())
	var account Account
	errors := server.db.DB.First(&account, "id = ?", userID).GetErrors()
	if errors != nil {
		return handleErrors(errors, "Could not fetch account")
	}
	trains := append(account.TrainIds, string(trainID))
	errors = server.db.DB.Model(&account).Update("TrainIds", trains).GetErrors()
	if errors != nil {
		return handleErrors(errors, "Could not update account")
	}
	return &pb.Success{Success: true}, nil
}

// RemoveTrain removes a train from a user
func (server *server) RemoveTrain(ctx context.Context, in *pb.UserIdTrainIdInput) (*pb.Success, error) {
	// TODO: do this... I am clueless how to nicely handle the array
	return &pb.Success{Success: true}, nil
}

// AddUser adds a new user
func (server *server) AddUser(ctx context.Context, in *pb.User) (*pb.Success, error) {
	user := Account{
		ID:       int(in.GetId().GetValue()),
		Email:    in.GetEmail(),
		Password: in.GetPassword(),
	}
	if server.db != nil {
		server.db.DB.NewRecord(user)
		server.db.DB.Create(&user)
		return &pb.Success{Success: true}, nil
	}
	return &pb.Success{Success: false}, nil
}

// RemoveUser removes a user
func (server *server) RemoveUser(ctx context.Context, in *pb.UserId) (*pb.Success, error) {
	var user Account
	errors := server.db.DB.First(&user, "id = ?", int(in.GetValue())).GetErrors()
	if errors != nil {
		return handleErrors(errors, "Could not find that account")
	}
	errors = server.db.DB.Delete(&user).GetErrors()
	if errors != nil {
		return handleErrors(errors, "Could not delete that user")
	}
	return &pb.Success{Success: true}, nil
}

// GetUser fetches all information to a user
func (server *server) GetUser(ctx context.Context, in *pb.UserId) (*pb.User, error) {
	var user Account
	errors := server.db.DB.First(&user, "id = ?", int(in.GetValue())).GetErrors()
	if errors != nil {
		printErrors(errors)
		return &pb.User{}, errors[0]
	}
	var ids []*pb.TrainId
	for _, id := range user.TrainIds {
		ids = append(ids, &pb.TrainId{Value: int64FromString(id)})
	}
	return &pb.User{
		Id:       &pb.UserId{Value: int64(user.ID)},
		Email:    user.Email,
		Password: user.Password,
		Trains:   ids}, nil
}

// GetTrains fetches all information to a user
func (server *server) GetTrains(ctx context.Context, in *pb.UserId) (*pb.TrainIds, error) {
	var user Account
	errors := server.db.DB.First(&user, "id = ?", int(in.GetValue())).GetErrors()
	if errors != nil {
		printErrors(errors)
		return &pb.TrainIds{}, errors[0]
	}
	var ids []*pb.TrainId
	for _, id := range user.TrainIds {
		ids = append(ids, &pb.TrainId{Value: int64FromString(id)})
	}
	return &pb.TrainIds{
		Ids: ids}, nil
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
	cliFlags = append(cliFlags, libdb.DatabaseCliOptions...)

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
	postgresConfig := libdb.DBConfig{}.ParseCli(ctx)
	var err error
	db, err = libdb.PostgresDatabase(&postgresConfig)
	db.DB.Debug().AutoMigrate(&Account{})

	if err != nil {
		log.Fatalf("failed to initialize database: %v", err)
	}

	userManagementServer := server{
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
	pb.RegisterUserManagementServer(grpcServer, &userManagementServer)

	if err := grpcServer.Serve(listener); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
	log.Info("Closing socket")
	listener.Close()
	done <- true
	return nil
}

func handleErrors(errors []error, message string) (*pb.Success, error) {
	log.Println(message)
	printErrors(errors)
	return &pb.Success{Success: false}, errors[0]
}

func removeTrainID(slice pq.StringArray, s int) pq.StringArray {
	return append(slice[:s], slice[s+1:]...)
}

func printErrors(errors []error) {
	for _, err := range errors {
		fmt.Println(err)
	}
}

func int64FromString(text string) int64 {
	integer, _ := strconv.Atoi(text)
	return int64(integer)
}
