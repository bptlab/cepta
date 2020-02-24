package main

import (
	pb "github.com/bptlab/cepta/models/grpc/user_management"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/sirupsen/logrus"
)

type UserManagement struct {
	Ctrl    chan pb.InternalControlMessageType
	Active  *bool
	Db      *libdb.DB
	log     *logrus.Entry
	running bool
}
