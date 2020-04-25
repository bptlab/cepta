package main

import (
	"context"
	"fmt"
	"net"
	"testing"
	"time"

	topics "github.com/bptlab/cepta/models/constants/topic"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	"github.com/bptlab/cepta/models/types/result"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	kafkaconsumer "github.com/bptlab/cepta/osiris/lib/kafka/consumer"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/golang/protobuf/proto"
	"github.com/grpc/grpc-go/test/bufconn"
	"github.com/romnnn/bsonpb"
	"github.com/romnnn/deepequal"
	tc "github.com/romnnn/testcontainers"
	"github.com/sirupsen/logrus"
	log "github.com/sirupsen/logrus"
	"github.com/testcontainers/testcontainers-go"
	"go.mongodb.org/mongo-driver/mongo"
	"google.golang.org/grpc"
)

const logLevel = logrus.ErrorLevel
const bufSize = 1024 * 1024
const userCollection = "mock_users"

type dialerFunc = func(string, time.Duration) (net.Conn, error)

func dailerFor(listener *bufconn.Listener) dialerFunc {
	return func(string, time.Duration) (net.Conn, error) {
		return listener.Dial()
	}
}

func setUpReplayerServer(t *testing.T, listener *bufconn.Listener, mongoConfig libdb.MongoDBConfig, kafkaConfig kafkaproducer.KafkaProducerConfig) (*ReplayerServer, error) {
	r := NewReplayerServer(mongoConfig, kafkaConfig)
	if err := r.Setup(context.Background()); err != nil {
		t.Fatalf("Failed to setup replayer server: %v", err)
	}
	go func() {
		logger := logrus.New()
		logger.SetLevel(logLevel)
		if err := r.Serve(listener, logger, []string{}, []string{}); err != nil {
			t.Fatalf("Failed to serve the replayer: %v", err)
		}
	}()
	return &r, nil
}

// Test ...
type Test struct {
	Net                 testcontainers.Network
	MongoC              testcontainers.Container
	KafkaC              testcontainers.Container
	ZkC                 testcontainers.Container
	KafkaProducerConfig kafkaproducer.KafkaProducerConfig
	KafkaConsumerConfig kafkaconsumer.KafkaConsumerConfig
	MongoConfig         libdb.MongoDBConfig
	ReplayerEndpoint    *grpc.ClientConn
	ReplayerServer      *ReplayerServer
	ReplayerClient      pb.ReplayerClient
}

// Setup ...
func (test *Test) Setup(t *testing.T) *Test {
	var err error
	log.SetLevel(logLevel)

	networkName := "test-network"
	test.Net, err = testcontainers.GenericNetwork(context.Background(), testcontainers.GenericNetworkRequest{
		NetworkRequest: testcontainers.NetworkRequest{
			Driver:         "bridge",
			Name:           networkName,
			Attachable:     true,
			CheckDuplicate: true,
		},
	})
	if err != nil {
		t.Fatalf("Failed to create the docker test network: %v", err)
		return test
	}
	defer test.Net.Remove(context.Background())

	containerOptions := tc.ContainerOptions{
		ContainerRequest: testcontainers.ContainerRequest{
			Networks: []string{networkName},
		},
	}

	// Start mongodb container
	var mongoConfig tc.MongoDBConfig
	test.MongoC, mongoConfig, err = tc.StartMongoContainer(tc.MongoContainerOptions{ContainerOptions: containerOptions})
	if err != nil {
		t.Fatalf("Failed to start the mongodb container: %v", err)
		return test
	}
	test.MongoConfig = libdb.MongoDBConfig{
		Host:                mongoConfig.Host,
		Port:                mongoConfig.Port,
		User:                mongoConfig.User,
		Database:            fmt.Sprintf("mockdatabase-%s", tc.UniqueID()),
		Password:            mongoConfig.Password,
		ConnectionTolerance: libcli.ConnectionTolerance{TimeoutSec: 20},
	}

	// Start kafka container
	var kafkaConfig *tc.KafkaContainerConnectionConfig
	test.KafkaC, kafkaConfig, test.ZkC, _, err = tc.StartKafkaContainer(tc.KafkaContainerOptions{ContainerOptions: containerOptions})
	if err != nil {
		t.Fatalf("Failed to start the kafka container: %v", err)
		return test
	}
	test.KafkaConsumerConfig = kafkaconsumer.KafkaConsumerConfig{
		Group:               fmt.Sprintf("TestConsumerGroup-%s", tc.UniqueID()),
		Brokers:             kafkaConfig.Brokers,
		Version:             kafkaConfig.KafkaVersion,
		ConnectionTolerance: libcli.ConnectionTolerance{TimeoutSec: 20},
	}
	test.KafkaProducerConfig = kafkaproducer.KafkaProducerConfig{
		Brokers:             kafkaConfig.Brokers,
		ConnectionTolerance: libcli.ConnectionTolerance{TimeoutSec: 20},
	}

	// Create endpoint
	replayerListener := bufconn.Listen(bufSize)
	test.ReplayerEndpoint, err = grpc.DialContext(context.Background(), "bufnet", grpc.WithDialer(dailerFor(replayerListener)), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
		return test
	}

	// Start the GRPC server
	test.ReplayerServer, err = setUpReplayerServer(t, replayerListener, test.MongoConfig, test.KafkaProducerConfig)
	if err != nil {
		t.Fatalf("Failed to setup the replayer service: %v", err)
		return test
	}

	test.ReplayerClient = pb.NewReplayerClient(test.ReplayerEndpoint)
	return test
}

// Teardown ...
func (test *Test) Teardown() {
	test.MongoC.Terminate(context.Background())
	test.KafkaC.Terminate(context.Background())
	test.ZkC.Terminate(context.Background())
	test.Net.Remove(context.Background())
	test.ReplayerEndpoint.Close()
	test.ReplayerServer.Shutdown()
}

// AssertStatusIs ...
func (test *Test) AssertStatusIs(t *testing.T, expected *pb.ReplayStatus) {
	status, err := test.ReplayerClient.GetStatus(context.Background(), &result.Empty{})
	if err != nil {
		t.Fatalf("Failed to get status of the replayer: %v", err)
	}
	if equal, err := deepequal.DeepEqual(status, expected); !equal {
		t.Fatalf("Expected status of the replayer to be %v but got: %v: %v", expected, status, err)
	}
}

// AssertHasOptions ...
func (test *Test) AssertHasOptions(t *testing.T, expected *pb.ReplayStartOptions) {
	status, err := test.ReplayerClient.GetOptions(context.Background(), &result.Empty{})
	if err != nil {
		t.Fatalf("Failed to get options of the replayer: %v", err)
	}
	if equal, err := deepequal.DeepEqual(status, expected); !equal {
		t.Fatalf("Expected options of the replayer to be %q but got: %q: %v", expected, status, err)
	}
}

// InsertForTopic ...
func (test *Test) InsertForTopic(topic topics.Topic, entries []proto.Message) error {
	mongoDB, err := libdb.MongoDatabase(&test.MongoConfig)
	if err != nil {
		return fmt.Errorf("Failed to initialize mongo database: %v", err)
	}
	defer mongoDB.Close()

	// Get collection for selected topic
	var collection *mongo.Collection
	for _, r := range test.ReplayerServer.Replayers {
		if r.Topic == topic {
			collection = mongoDB.DB.Collection(r.SourceName)
			break
		}
	}
	if collection == nil {
		return fmt.Errorf("No collection for source %s", topic.String())
	}

	var docs []interface{}
	marshaler := bsonpb.Marshaler{}
	for _, e := range entries {
		bson, err := marshaler.Marshal(e)
		if err != nil {
			return fmt.Errorf("Failed to marshal with error: %v", err)
		}
		docs = append(docs, bson)
	}
	if len(docs) > 0 {
		_, err := collection.InsertMany(context.Background(), docs)
		if err != nil {
			return err
		}
	}
	return nil
}
