package main

import (
	"context"
	"io"
	"net"
	"testing"
	"time"

	topics "github.com/bptlab/cepta/models/constants/topic"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/grpc/grpc-go/test/bufconn"
	"github.com/sirupsen/logrus"
	"google.golang.org/grpc"
)

// const logLevel = logrus.DebugLevel
const logLevel = logrus.ErrorLevel
const bufSize = 1024 * 1024

var listener *bufconn.Listener

func bufDialer(string, time.Duration) (net.Conn, error) {
	return listener.Dial()
}

func defaultReplayerServer() ReplayerServer {
	mongoConfig := libdb.MongoDBConfig{
		Host:     "localhost",
		Port:     27017,
		User:     "root",
		Password: "example",
		Database: "replay",
		ConnectionTolerance: libcli.ConnectionTolerance{
			TimeoutSec: 20,
		},
	}
	KafkaConfig := kafkaproducer.KafkaProducerOptions{
		Brokers: []string{"localhost:9092"},
		ConnectionTolerance: libcli.ConnectionTolerance{
			TimeoutSec: 20,
		},
	}
	server := NewReplayerServer(mongoConfig, KafkaConfig)
	server.Limit = 100
	return server
}

func setUpServer(t *testing.T) ReplayerServer {
	listener = bufconn.Listen(bufSize)
	replayer := defaultReplayerServer()
	replayer.Setup()
	go func() {
		log = logrus.New()
		log.SetLevel(logLevel)
		if err := replayer.Serve(listener, log); err != nil {
			t.Fatalf("Failed to serve the replayer: %v", err)
		}
	}()
	return replayer
}

func teardownServer(server ReplayerServer, t *testing.T) {
	server.Shutdown()
}

// TestQuery ...
func TestQuery(t *testing.T) {
	server := setUpServer(t)
	defer func() {
		teardownServer(server, t)
	}()
	conn, err := grpc.DialContext(context.Background(), "bufnet", grpc.WithDialer(bufDialer), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
	}
	defer conn.Close()
	client := pb.NewReplayerClient(conn)
	request := &pb.QueryOptions{Sources: []topics.Topic{topics.Topic_GPS_TRIP_UPDATE_DATA}, Limit: 100}
	stream, err := client.Query(context.Background(), request)
	if err != nil {
		t.Errorf("Failed to query replayer: %s", err.Error())
	}
	var c int
	for {
		_, err := stream.Recv()
		if err == io.EOF {
			break
		}
		if err != nil {
			t.Errorf("%v.Query(_) = _, %v", client, err)
		}
		c++
	}
	if c != 100 {
		t.Errorf("Query returned wrong number of results")
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
