package main

import (
	"context"
	"io"
	"net"
	"testing"
	"time"

	pb "github.com/bptlab/cepta/models/grpc/replayer"
	"github.com/sirupsen/logrus"

	// libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/grpc/grpc-go/test/bufconn"
	// mocket "github.com/selvatico/go-mocket"
	"google.golang.org/grpc"
)

const bufSize = 1024 * 1024

var listener *bufconn.Listener

func bufDialer(string, time.Duration) (net.Conn, error) {
	return listener.Dial()
}

func setUpServer(t *testing.T) {
	listener = bufconn.Listen(bufSize)
	// grpcServer := grpc.NewServer()
	replayer := server{
		// Must set the logger
	}
	// pb.RegisterReplayerServer(grpcServer, &replayerServer)
	go func() {
		log = logrus.New()
		log.SetLevel(logrus.ErrorLevel)
		replayer.serve(listener, log)
		listen(listener)
		/*
			if err := grpcServer.Serve(listener); err != nil {
				t.Errorf("Server exited with error: %v", err)
			}
		*/
	}()
}

func TestQuery(t *testing.T) {
	setUpServer(t)
	conn, err := grpc.DialContext(context.Background(), "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
	}
	defer conn.Close()
	client := pb.NewReplayerClient(conn)
	request := &pb.QueryOptions{}
	stream, err := client.Query(context.Background(), request)
	if err != nil {
		t.Errorf("Failed to query replayer: %s", err.Error())
	}
	for {
		event, err := stream.Recv()
		if err == io.EOF {
			break
		}
		if err != nil {
			t.Errorf("%v.Query(_) = _, %v", client, err)
		}
		t.Fatal(event)
	}
}

/*
func stopTestForReal(t *testing.T) {
	conn, err := grpc.Dial("localhost:8080", grpc.WithInsecure())
	if err != nil {
		panic(err)
		return
	}
	client := pb.NewReplayerClient(conn)
	_, err2 := client.Query(&pb.QueryOptions{})
	if err2 != nil {
		panic(err2)
		return
	}
	defer conn.Close()
}
*/

/*
import (
	"context"
	"fmt"
	"net"
	"testing"
	"time"

	auth "github.com/bptlab/cepta/models/grpc/authentication"
	pb "github.com/bptlab/cepta/models/grpc/usermgmt"
	authserv "github.com/bptlab/cepta/osiris/authentication"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/grpc/grpc-go/test/bufconn"
	"github.com/jinzhu/gorm"
	mocket "github.com/selvatico/go-mocket"
	"google.golang.org/grpc"
)

var successMessage *pb.Success = &pb.Success{Success: true}
var gormDB *gorm.DB
var ldb *libdb.PostgresDB

// constants for testing
var emailParam string = "example@mail.de"
var passwordParam string = "password"
var userIDProto *pb.UserId = &pb.UserId{Value: 1}
var userWithoutTrainsProto *pb.User = &pb.User{Id: userIDProto, Email: emailParam, Password: passwordParam, Trains: nil}
var userWithTrainsProto *pb.User = &pb.User{Id: userIDProto, Email: emailParam, Password: passwordParam, Trains: trainIdsProto}
var userWithoutTrainsStringResponseDB map[string]interface{} = map[string]interface{}{"id": "1", "email": emailParam, "password": passwordParam}
var userWithTrainsStringResponseDB map[string]interface{} = map[string]interface{}{"id": "1", "email": emailParam, "password": passwordParam, "train_ids": "{1,2}"} // no space in the array!
var trainIDProto *pb.TrainId = &pb.TrainId{Value: 1}
var trainIdsProto *pb.TrainIds = &pb.TrainIds{Ids: []*pb.TrainId{&pb.TrainId{Value: 1}, &pb.TrainId{Value: 2}}}

const bufSize = 1024 * 1024

var lis *bufconn.Listener

func SetUpAll(t *testing.T) {
	SetUpDatabase()
	SetUpServerConnection(t)
}
func SetUpDatabase() {
	mocket.Catcher.Register()
	// uncomment to log all catcher things. add to test to log only things happening there
	// mocket.Catcher.Logging = true
	db, err := gorm.Open(mocket.DriverName, "connection_string") // Can be any connection string
	if err != nil {
		print(err)
	}
	gormDB = db
	// uncomment to log all queries asked to mock db. add to test to log only things happening there
	// gormDB.LogMode(true)
	ldb = &libdb.PostgresDB{DB: gormDB}
}
func SetUpServerConnection(t *testing.T) {
	lis = bufconn.Listen(bufSize)
	s := grpc.NewServer()
	ctx := context.Background()
	auth.RegisterAuthenticationServer(s, &authserv.Server{DB: ldb})
	pb.RegisterUserManagementServer(s, &server{
		db: ldb,
		authClient: func(inconn *grpc.ClientConn) auth.AuthenticationClient {
			conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
			if err != nil {
				t.Fatalf("Failed to dial bufnet: %v", err)
			}
			return auth.NewAuthenticationClient(conn)
		},
	})
	go func() {
		if err := s.Serve(lis); err != nil {
			fmt.Printf("Server exited with error: %v", err)
		}
	}()
}

func TestAddTrain(t *testing.T) {
	SetUpAll(t)
	ctx := context.Background()
	conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
	}
	defer conn.Close()
	client := pb.NewUserManagementClient(conn)
	userReply := []map[string]interface{}{userWithoutTrainsStringResponseDB}
	mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "users"  WHERE "users"."deleted_at" IS NULL AND (("users"."id" = 1)) ORDER BY "users"."id" ASC LIMIT 1`).WithReply(userReply)
	request := &pb.UserIdTrainIdInput{
		UserId:  userIDProto,
		TrainId: trainIDProto,
	}
	response, err := client.AddTrain(context.Background(), request)
	if err != nil {
		t.Errorf("AddTrain() should work without error. Error: %v", err)
	}
	if response.Success != true {
		t.Errorf("AddTrain() should return success message, but it was %v", response)
	}
}

func TestAddUser(t *testing.T) {
	SetUpAll(t)
	ctx := context.Background()
	conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	// conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
	}
	defer conn.Close()
	client := pb.NewUserManagementClient(conn)
	request := &pb.User{
		Id:       userIDProto,
		Email:    emailParam,
		Password: passwordParam,
	}
	response, err := client.AddUser(context.Background(), request)
	if err != nil {
		t.Errorf("AddUser() should work without error. Error: %v", err)
	}
	if response.Success != true {
		t.Errorf("AddUser should return success message. It was %v", response)
	}
}

func TestGetTrainIds(t *testing.T) {
	SetUpAll(t)
	ctx := context.Background()
	conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
	}
	defer conn.Close()
	client := pb.NewUserManagementClient(conn)

	request := userIDProto

	userReply := []map[string]interface{}{userWithTrainsStringResponseDB}
	mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "users"  WHERE "users"."deleted_at" IS NULL AND (("users"."id" = 1)) ORDER BY "users"."id" ASC LIMIT 1`).WithReply(userReply)
	response, err := client.GetTrainIds(context.Background(), request)
	if err != nil {
		t.Errorf("GetTrainIds should work without error. Got: %v", err)
	}
	if !equalTrainIDs(response, trainIdsProto) {
		t.Errorf("GetTrainIds should return the user's train ids: %v, but it was %v", trainIdsProto, response)
	}
}

func TestGetUserWithoutTrains(t *testing.T) {
	SetUpAll(t)
	ctx := context.Background()
	conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
	}
	defer conn.Close()
	client := pb.NewUserManagementClient(conn)

	request := &pb.UserId{
		Value: 1,
	}

	userReply := []map[string]interface{}{userWithoutTrainsStringResponseDB}
	mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "users"  WHERE "users"."deleted_at" IS NULL AND (("users"."id" = 1)) ORDER BY "users"."id" ASC LIMIT 1`).WithReply(userReply)

	response, err := client.GetUser(context.Background(), request)
	if err != nil {
		t.Errorf("GetUser should work without error. Got: %v", err)
	}
	if !equalUser(response, userWithoutTrainsProto) {
		t.Errorf("GetUser should return the user information: %v, but it was %v", userWithoutTrainsProto, response)
	}
}
func TestGetUserWithTrains(t *testing.T) {
	SetUpAll(t)
	ctx := context.Background()
	conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
	}
	defer conn.Close()
	client := pb.NewUserManagementClient(conn)

	request := &pb.UserId{
		Value: 1,
	}

	userReply := []map[string]interface{}{userWithTrainsStringResponseDB}
	mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "users"  WHERE "users"."deleted_at" IS NULL AND (("users"."id" = 1)) ORDER BY "users"."id" ASC LIMIT 1`).WithReply(userReply)

	response, err := client.GetUser(context.Background(), request)
	if err != nil {
		t.Errorf("GetUser should work without error. Got: %v", err)
	}
	if !equalUser(response, userWithTrainsProto) {
		t.Errorf("GetUser should return the user information: %v, but it was %v", userWithTrainsProto, response)
	}
}

func TestRemoveTrain(t *testing.T) {
	SetUpAll(t)
	ctx := context.Background()
	conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
	}
	defer conn.Close()
	client := pb.NewUserManagementClient(conn)
	userReply := []map[string]interface{}{userWithTrainsStringResponseDB}
	mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "users"  WHERE "users"."deleted_at" IS NULL AND (("users"."id" = 1)) ORDER BY "users"."id" ASC LIMIT 1`).WithReply(userReply)
	request := &pb.UserIdTrainIdInput{
		UserId:  userIDProto,
		TrainId: trainIDProto,
	}
	response, err := client.RemoveTrain(context.Background(), request)
	if err != nil {
		t.Errorf("RemoveTrain should work without error. Error: %v", err)
	}
	if response.Success != true {
		t.Errorf("RemoveTrain should return success message, but it was %v", response)
	}
}

func TestRemoveUser(t *testing.T) {
	SetUpAll(t)
	ctx := context.Background()
	conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	// conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
	}
	defer conn.Close()
	client := pb.NewUserManagementClient(conn)
	request := userIDProto
	response, err := client.RemoveUser(context.Background(), request)
	if err != nil {
		t.Errorf("RemoveUser() should work without error. Error: %v", err)
	}
	if response.Success != true {
		t.Errorf("RemoveUser should return success message. It was %v", response)
	}
}

func bufDialer(string, time.Duration) (net.Conn, error) {
	return lis.Dial()
}
*/
