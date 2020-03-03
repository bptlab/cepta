package main

import (
	"context"
	"net"
	"testing"
	"time"

	pb "github.com/bptlab/cepta/models/grpc/user_management"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/grpc/grpc-go/test/bufconn"
	"github.com/jinzhu/gorm"
	mocket "github.com/selvatico/go-mocket"
	"google.golang.org/grpc"
)

var successMessage *pb.Success = &pb.Success{Success: true}
var gormDB *gorm.DB
var ldb *libdb.DB

// constants for testing
var emailParam string = "example@mail.de"
var passwordParam string = "password"
var userIDProto *pb.UserId = &pb.UserId{Value: 1}
var userWithoutTrainsProto *pb.User = &pb.User{Id: userIDProto, Email: emailParam, Password: passwordParam, Trains: nil}
var userWithoutTrainsStringResponseDB map[string]interface{} = map[string]interface{}{"id": "1", "email": emailParam, "password": passwordParam}
var userWithTrainsStringResponseDB map[string]interface{} = map[string]interface{}{"id": "1", "email": emailParam, "password": passwordParam, "trains": []int{1, 2}}
var trainIDProto *pb.TrainId = &pb.TrainId{Value: 1}
var trainIdsProto *pb.TrainIds = &pb.TrainIds{Ids: []*pb.TrainId{&pb.TrainId{Value: 1}, &pb.TrainId{Value: 2}}}

const bufSize = 1024 * 1024

var lis *bufconn.Listener

func SetUpAll() {
	SetUpDatabase()
	SetUpServerConnection()
}
func SetUpDatabase() {
	mocket.Catcher.Register()
	// mocket.Catcher.Logging = true
	db, err := gorm.Open(mocket.DriverName, "connection_string") // Can be any connection string
	if err != nil {
		print(err)
	}
	gormDB = db
	ldb = &libdb.DB{DB: gormDB}
}
func SetUpServerConnection() {
	lis = bufconn.Listen(bufSize)
	s := grpc.NewServer()
	pb.RegisterUserManagementServer(s, &server{db: ldb})
	go func() {
		if err := s.Serve(lis); err != nil {
			log.Fatalf("Server exited with error: %v", err)
		}
	}()
}

func TestAddTrain(t *testing.T) {
	SetUpAll()
	ctx := context.Background()
	conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
	}
	defer conn.Close()
	client := pb.NewUserManagementClient(conn)

	//gormDB.LogMode(true)
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
	SetUpAll()
	ctx := context.Background()
	conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
	}
	defer conn.Close()
	client := pb.NewUserManagementClient(conn)
	// gormDB.LogMode(true)
	request := &pb.User{
		Id:       userIDProto,
		Email:    emailParam,
		Password: passwordParam,
	}
	response, err := client.AddUser(context.Background(), request)
	if err != nil {
		t.Errorf("AddUser() should work without error.")
	}
	if response.Success != true {
		t.Errorf("AddUser should return success message. It was %v", response)
	}
}

func TestGetTrainIds(t *testing.T) {
	SetUpAll()
	ctx := context.Background()
	conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
	}
	defer conn.Close()
	client := pb.NewUserManagementClient(conn)

	request := userIDProto

	userReply := []map[string]interface{}{userWithTrainsStringResponseDB}
	mocket.Catcher.Logging = true
	//gormDB.LogMode(true)
	mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "users"  WHERE "users"."deleted_at" IS NULL AND (("users"."id" = 1)) ORDER BY "users"."id" ASC LIMIT 1`).WithReply(userReply)
	mocket.Catcher.NewMock().WithQuery(`SELECT * FROM "users"  WHERE "users"."deleted_at" IS NULL AND ((id = 1)) ORDER BY "users"."id" ASC LIMIT 1`).WithReply(userReply)
	// SELECT * FROM "users"  WHERE "users"."deleted_at" IS NULL AND ((id = 1)) ORDER BY "users"."id" ASC LIMIT 1
	response, err := client.GetTrainIds(context.Background(), request)
	if err != nil {
		t.Errorf("GetTrainIds should work without error. Got: %v", err)
	}
	if !EqualTrainIDs(response, trainIdsProto) {
		t.Errorf("GetTrainIds should return the user's train ids: %v, but it was %v", trainIdsProto, response)
	}
}

func TestGetUser(t *testing.T) {
	SetUpAll()
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
	// gormDB.LogMode(true)
	mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "users"  WHERE "users"."deleted_at" IS NULL AND (("users"."id" = 1)) ORDER BY "users"."id" ASC LIMIT 1`).WithReply(userReply)

	response, err := client.GetUser(context.Background(), request)
	if err != nil {
		t.Errorf("GetUser should work without error. Got: %v", err)
	}
	if !EqualUser(response, userWithoutTrainsProto) {
		t.Errorf("GetUser should return the user information: %v, but it was %v", userWithoutTrainsProto, response)
	}
	// t.Errorf("expected: %v, got: %v", userWithoutTrainsProto, response)
}

func bufDialer(string, time.Duration) (net.Conn, error) {
	return lis.Dial()
}
