package main

import (
	pb "github.com/bptlab/cepta/models/grpc/user_management"
	libdb "github.com/bptlab/cepta/osiris/lib/db"

	"github.com/sirupsen/logrus"
	"google.golang.org/grpc"
)

var grpcServer *grpc.Server
var done = make(chan bool, 1)
var db *libdb.DB
var log *logrus.Logger

type server struct {
	pb.UnimplementedUserManagementServer
	active bool
}

/*
rpc SetEmail(UserId, string) returns (Success) {}
rpc AddTrain(UserId, Train) returns (Success) {}
rpc RemoveTrain(UserId, Train) returns (Success) {}
rpc AddUser(User) returns (Success) {}
rpc RemoveUser(UserId) returns (Success) {}

rpc GetUser(UserId) returns (User) {}
rpc GetTrains(UserId) returns (Trains) {}
*/

// SetEmail sets a users email
func SetEmail(email string) (*pb.Success, error) {
	return &pb.Success{Success: true}, nil
}
