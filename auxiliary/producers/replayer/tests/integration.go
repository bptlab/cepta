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
	integrationtesting "github.com/bptlab/cepta/osiris/lib/testing"
	"github.com/grpc/grpc-go/test/bufconn"
	"github.com/sirupsen/logrus"
	"github.com/testcontainers/testcontainers-go"
	"google.golang.org/grpc"
)

const logLevel = logrus.ErrorLevel
const bufSize = 1024 * 1024
const userCollection = "mock_users"

type DialerFunc = func(string, time.Duration) (net.Conn, error)

func dailerFor(listener *bufconn.Listener) DialerFunc {
	return func(string, time.Duration) (net.Conn, error) {
		return listener.Dial()
	}
}

func defaultReplayerServer(mongoConfig libdb.MongoDBConfig) ReplayerServer {
	KafkaConfig := kafkaproducer.KafkaProducerOptions{
		Brokers: []string{"localhost:9092"},
		ConnectionTolerance: libcli.ConnectionTolerance{
			TimeoutSec: 20,
		},
	}
	server := NewReplayerServer(mongoConfig, KafkaConfig)
	server.StartOptions.Options = &pb.ReplayOptions{Limit: 100}
	return server
}

func setUpReplayerServer(t *testing.T, listener *bufconn.Listener, mongoConfig libdb.MongoDBConfig) (*ReplayerServer, error) {
	replayer := defaultReplayerServer(mongoConfig)
	if err := replayer.Setup(); err != nil {
		t.Fatalf("Failed to setup user management server: %v", err)
	}
	go func() {
		log = logrus.New()
		log.SetLevel(logLevel)
		if err := replayer.Serve(listener, log, []string{}, []string{}); err != nil {
			t.Fatalf("Failed to serve the replayer: %v", err)
		}
	}()
	return &server, nil
}

type Test struct {
	mongoC           testcontainers.Container
	replayerEndpoint *grpc.ClientConn
	replayerServer   *ReplayerServer
	replayerClient   pb.ReplayerClient
}

func (test *Test) setup(t *testing.T) *Test {
	var err error
	var dbConn libdb.MongoDBConfig

	// Start mongodb container
	test.mongoC, dbConn, err = integrationtesting.StartMongoContainer()
	if err != nil {
		t.Fatalf("Failed to start the mongodb container: %v", err)
		return test
	}

	// Create endpoint
	replayerListener := bufconn.Listen(bufSize)
	test.replayerEndpoint, err = grpc.DialContext(context.Background(), "bufnet", grpc.WithDialer(dailerFor(replayerListener)), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
		return test
	}

	// Start the GRPC server
	test.replayerServer, err = setUpReplayerServer(t, replayerListener, dbConn)
	if err != nil {
		t.Fatalf("Failed to setup the user management service: %v", err)
		return test
	}

	test.replayerClient = pb.NewReplayerClient(test.replayerEndpoint)
	return test
}

func (test *Test) teardown() {
	test.mongoC.Terminate(context.Background())
	test.replayerEndpoint.Close()
	test.replayerServer.Shutdown()
}

// Test ideas:
// - Test constant and proportional time replay with different speed levels (replay and query)
// - Test query finds correct items
// - Test start / stop resumes
// - Test repeat repeats
// - Test extracted time (replay and query)
// - Test Reset
// - Test get and set options

// TestQuery ...
func TestQuery(t *testing.T) {
	test := new(Test).setup(t)
	defer test.teardown()
	request := &pb.QueryOptions{Sources: []*pb.SourceQueryOptions{
		{
			Source:  topics.Topic_GPS_TRIP_UPDATE_DATA,
			Options: &pb.ReplayOptions{Limit: 100},
		},
	}}
	stream, err := test.replayerClient.Query(context.Background(), request)
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
			t.Errorf("%v.Query(_) = _, %v", test.replayerClient, err)
		}
		c++
	}
	if c != 100 {
		t.Errorf("Query returned wrong number of results")
	}
}
