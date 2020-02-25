package main

import (
	"context"
	"net"
	"testing"
	"time"

	pb "github.com/bptlab/cepta/models/grpc/user_management"
	"github.com/grpc/grpc-go/test/bufconn"
	"github.com/jinzhu/gorm"
	mocket "github.com/selvatico/go-mocket"
	"google.golang.org/grpc"
	// "google.golang.org/grpc/test/bufconn"
)

var successMessage *pb.Success = &pb.Success{Success: true}
var DB *gorm.DB

const bufSize = 1024 * 1024

var lis *bufconn.Listener

func SetUpDatabase() {
	mocket.Catcher.Register()
	db, err := gorm.Open(mocket.DriverName, "connection_string") // Can be any connection string
	if err != nil {
		print(err)
	}
	DB = db
}
func SetUpServerConnection() {
	lis = bufconn.Listen(bufSize)
	s := grpc.NewServer()
	pb.RegisterUserManagementServer(s, &server{})
	go func() {
		if err := s.Serve(lis); err != nil {
			log.Fatalf("Server exited with error: %v", err)
		}
	}()
}

func bufDialer(string, time.Duration) (net.Conn, error) {
	return lis.Dial()
}

func TestGetUser(t *testing.T) {}

func TestAddUser(t *testing.T) {
	SetUpServerConnection()
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
	if response != successMessage {
		t.Errorf("AddUser should return success message.")
	}
}
