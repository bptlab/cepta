package main

import (
	"context"
	"net"
	"strconv"
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
var idParam int64 = 1
var userIDParam *pb.UserId = &pb.UserId{Value: idParam}
var userParam *pb.User = &pb.User{Id: userIDParam, Email: emailParam, Password: passwordParam, Trains: nil}
var userStringResponseDB map[string]interface{} = map[string]interface{}{"id": strconv.Itoa(int(idParam)), "email": emailParam, "password": passwordParam}

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

func bufDialer(string, time.Duration) (net.Conn, error) {
	return lis.Dial()
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

	userReply := []map[string]interface{}{userStringResponseDB}
	gormDB.LogMode(true)
	mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "users"  WHERE "users"."deleted_at" IS NULL AND (("users"."id" = 1)) ORDER BY "users"."id" ASC LIMIT 1`).WithReply(userReply)

	response, err := client.GetUser(context.Background(), request)
	if err != nil {
		t.Errorf("GetUser should work without error. Got: %v", err)
	}
	if !EqualUser(response, userParam) {
		t.Errorf("GetUser should return the user information: %v, but it was %v", userParam, response)
	}
	// t.Errorf("expected: %v, got: %v", userParam, response)
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

	request := &pb.User{
		Id:       &pb.UserId{Value: 1},
		Email:    "test@user.com",
		Password: "password",
	}
	response, err := client.AddUser(context.Background(), request)
	if err != nil {
		t.Errorf("AddUser() should work without error.")
	}
	if response.Success != true {
		t.Errorf("AddUser should return success message. It was %v", response)
	}
}
