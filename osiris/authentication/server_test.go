package main

import (
	"context"
	"fmt"
	"net"
	"testing"
	"time"

	pb "github.com/bptlab/cepta/models/grpc/authentication"
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
var expectedValidation *pb.Validation = &pb.Validation{Value: true}
var user *pb.User = &pb.User{Id: userIDProto, Email: emailParam, Password: passwordParam}
var userStringResponseDB map[string]interface{} = map[string]interface{}{"id": "1", "email": emailParam, "password": passwordParam}

const bufSize = 1024 * 1024

var lis *bufconn.Listener

func SetUpAll() {
	SetUpDatabase()
	SetUpServerConnection()
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
func SetUpServerConnection() {
	lis = bufconn.Listen(bufSize)
	s := grpc.NewServer()
	pb.RegisterAuthenticationServer(s, &Server{DB: ldb})
	go func() {
		if err := s.Serve(lis); err != nil {
			fmt.Printf("Server exited with error: %v", err)
		}
	}()
}

func TestAddUser(t *testing.T) {
	SetUpAll()
	ctx := context.Background()
	conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
	}
	defer conn.Close()
	client := pb.NewAuthenticationClient(conn)
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

func TestLogin(t *testing.T) {
	SetUpAll()
	ctx := context.Background()
	conn, err := grpc.DialContext(ctx, "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
	}
	defer conn.Close()
	client := pb.NewAuthenticationClient(conn)

	request := &pb.UserId{
		Value: 1,
	}

	userReply := []map[string]interface{}{userStringResponseDB}
	mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "users"  WHERE "users"."deleted_at" IS NULL AND (("users"."id" = 1)) ORDER BY "users"."id" ASC LIMIT 1`).WithReply(userReply)

	response, err := client.Login(context.Background(), request)
	if err != nil {
		t.Errorf("Login should work without error. Got: %v", err)
	}
	if !equalValidation(response, expectedValidation) {
		t.Errorf("Login should return a successful validation %v, but it was %v", expectedValidation, response)
	}
}

func bufDialer(string, time.Duration) (net.Conn, error) {
	return lis.Dial()
}
