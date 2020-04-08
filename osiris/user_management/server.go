package main

import (
	"context"
	"fmt"
	"net"
	"os"
	"os/signal"
	"strconv"
	"syscall"

	auth "github.com/bptlab/cepta/models/grpc/authentication"
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
var db *libdb.PostgresDB

// AuthClientFunc is fancy
type AuthClientFunc func(*grpc.ClientConn) auth.AuthenticationClient

type server struct {
	pb.UnimplementedUserManagementServer
	active     bool
	authClient AuthClientFunc
	db         *libdb.PostgresDB
}

// User is a struct to rep user account
type User struct {
	gorm.Model                // adds the fields ID, CreatedAt, UpdatedAt, DeletedAt automatically
	Email      string         `json:"email"`
	TrainIds   pq.StringArray `gorm:"type:int[]"`
	Password   string         `json:"password"`
	Token      string         `json:"token";sql:"-"`
}

// AddTrain adds a train to a user
func (server *server) AddTrain(ctx context.Context, in *pb.UserIdTrainIdInput) (*pb.Success, error) {
	var userID int = int(in.GetUserId().GetValue())
	var trainID int = int(in.GetTrainId().GetValue())
	var user User
	err := server.db.DB.First(&user, userID).Error
	if err != nil {
		return &pb.Success{Success: false}, err
	}
	var trains []string = append(user.TrainIds, strconv.Itoa(trainID))
	err = server.db.DB.Model(&user).Update("TrainIds", trains).Error
	if err != nil {
		return &pb.Success{Success: false}, err
	}
	return &pb.Success{Success: true}, nil
}

// AddUser adds a new user
func (server *server) AddUser(ctx context.Context, in *pb.User) (*pb.Success, error) {
	user := User{
		//ID:       int(in.GetId().GetValue()),
		Email:    in.GetEmail(),
		Password: in.GetPassword(),
		TrainIds: toStringArray(in.GetTrains()),
	}
	server.db.DB.NewRecord(user)
	err := server.db.DB.Create(&user).Error
	if err != nil {
		return &pb.Success{Success: false}, err
	}

	// send user to auth microservice
	conn, err := grpc.Dial("localhost:8080", grpc.WithInsecure())
	if err != nil {
		return &pb.Success{Success: false}, err
	}
	client := server.authClient(conn) //auth.NewAuthenticationClient(conn)
	_, errr := client.AddUser(ctx, &auth.User{Email: in.GetEmail(), Password: in.GetPassword()})
	if errr != nil {
		return &pb.Success{Success: false}, errr
	}
	defer conn.Close()
	return &pb.Success{Success: true}, err
}

func toStringArray(trains *pb.TrainIds) pq.StringArray {
	var array pq.StringArray
	for _, id := range trains.GetIds() {
		array = append(array, int64ToString(id.GetValue()))
	}
	return array
}

// GetTrains fetches all train ids to a user
func (server *server) GetTrainIds(ctx context.Context, in *pb.UserId) (*pb.TrainIds, error) {
	var user User
	err := server.db.DB.First(&user, int(in.GetValue())).Error
	if err != nil {
		return &pb.TrainIds{}, err
	}
	var ids *pb.TrainIds = toProtoTrainIds(user.TrainIds)
	return ids, nil
}

// GetUser fetches all information to a user
func (server *server) GetUser(ctx context.Context, in *pb.UserId) (*pb.User, error) {
	var user User
	err := server.db.DB.First(&user, int(in.GetValue())).Error
	if err != nil {
		return &pb.User{
			Id:       nil,
			Email:    "",
			Password: "",
			Trains:   nil}, nil
	}
	var ids *pb.TrainIds = toProtoTrainIds(user.TrainIds)
	return &pb.User{
		Id:       &pb.UserId{Value: int64(user.ID)},
		Email:    user.Email,
		Password: user.Password,
		Trains:   ids}, nil
}

// RemoveTrain removes a train from a user
func (server *server) RemoveTrain(ctx context.Context, in *pb.UserIdTrainIdInput) (*pb.Success, error) {
	var userID int = int(in.GetUserId().GetValue())
	var trainID int = int(in.GetTrainId().GetValue())
	var user User
	err := server.db.DB.First(&user, userID).Error
	if err != nil {
		return &pb.Success{Success: false}, err
	}

	var trains []string = removeElementFromStringArray(user.TrainIds, strconv.Itoa(trainID))

	err = server.db.DB.Model(&user).Update("TrainIds", trains).Error
	if err != nil {
		return &pb.Success{Success: false}, err
	}
	return &pb.Success{Success: true}, nil
}

// RemoveUser removes a user
func (server *server) RemoveUser(ctx context.Context, in *pb.UserId) (*pb.Success, error) {
	var user User
	err := server.db.DB.First(&user, int(in.GetValue())).Error
	if err != nil {
		return &pb.Success{Success: false}, err
	}
	err = server.db.DB.Delete(&user).Error
	if err != nil {
		return &pb.Success{Success: false}, err
	}

	// send user removal to auth microservice
	conn, err := grpc.Dial("localhost:8080", grpc.WithInsecure())
	if err != nil {
		return &pb.Success{Success: false}, err
	}
	client := server.authClient(conn)
	_, errr := client.RemoveUser(ctx, &auth.UserId{Value: in.GetValue()})
	if errr != nil {
		return &pb.Success{Success: false}, errr
	}
	defer conn.Close()

	return &pb.Success{Success: true}, nil
}

// SetEmail sets a users email
func (server *server) SetEmail(ctx context.Context, in *pb.UserIdEmailInput) (*pb.Success, error) {
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

	// send email to auth microservice
	conn, err := grpc.Dial("localhost:8080", grpc.WithInsecure())
	if err != nil {
		return &pb.Success{Success: false}, err
	}
	client := auth.NewAuthenticationClient(conn)
	_, errr := client.SetEmail(ctx, &auth.UserIdEmailInput{UserId: &auth.UserId{Value: in.GetUserId().GetValue()}, Email: in.GetEmail()})
	if errr != nil {
		return &pb.Success{Success: false}, errr
	}
	defer conn.Close()

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

	userManagementServer := server{
		active: true,
		db:     db,
		authClient: func(conn *grpc.ClientConn) auth.AuthenticationClient {
			return auth.NewAuthenticationClient(conn)
		},
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

// returns true if the given users have the same attribute values
func equalUser(u1 *pb.User, u2 *pb.User) bool {
	if !equalUserID(u1.Id, u2.Id) || u1.Email != u2.Email || u1.Password != u2.Password || !equalTrainIDs(u1.Trains, u2.Trains) {
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

// equalTrainIDs returns true if the given repeated train ids have the same attribute values
func equalTrainIDs(ids1 *pb.TrainIds, ids2 *pb.TrainIds) bool {
	if ids1.GetIds() == nil && ids2.GetIds() == nil {
		return true
	} else if ids1.GetIds() == nil || ids2.GetIds() == nil {
		return false
	} else if ids1 == nil && ids2 == nil {
		return true
	} else if ids1 == nil || ids2 == nil {
		return false
	}
	for _, id1 := range ids1.GetIds() {
		var found bool
		for _, id2 := range ids2.GetIds() {
			found = false
			if equalTrainID(id1, id2) {
				found = true
				break
			}
		}
		if !found {
			return false
		}
	}
	return true
}

// equalTrainID returns true if the given train ids have the same attribute values
func equalTrainID(id1 *pb.TrainId, id2 *pb.TrainId) bool {
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

func toProtoTrainIds(trainIds pq.StringArray) *pb.TrainIds {
	if trainIds == nil {
		return &pb.TrainIds{}
	}
	var ids []*pb.TrainId
	for _, id := range trainIds {
		ids = append(ids, &pb.TrainId{Value: int64FromString(id)})
	}
	return &pb.TrainIds{Ids: ids}
}
