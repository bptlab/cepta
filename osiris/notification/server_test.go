package main

import (
	"context"
	"fmt"
	"net"
	"testing"
	"time"

	pb "github.com/bptlab/cepta/models/grpc/notification"
	usermgmtpb "github.com/bptlab/cepta/models/grpc/usermgmt"
	"github.com/bptlab/cepta/models/types/users"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/bptlab/cepta/osiris/lib/kafka"
	kafkaconsumer "github.com/bptlab/cepta/osiris/lib/kafka/consumer"
	rmqc "github.com/bptlab/cepta/osiris/lib/rabbitmq/consumer"
	rmqp "github.com/bptlab/cepta/osiris/lib/rabbitmq/producer"
	usermgmt "github.com/bptlab/cepta/osiris/usermgmt"
	tc "github.com/romnnn/testcontainers"
	log "github.com/sirupsen/logrus"
	"github.com/testcontainers/testcontainers-go"
	"google.golang.org/grpc"
	"google.golang.org/grpc/test/bufconn"
)

const (
	logLevel       = log.ErrorLevel
	bufSize        = 1024 * 1024
	userCollection = "mock-users"
	parallel       = true
)

type DialerFunc = func(string, time.Duration) (net.Conn, error)

func dialerFor(listener *bufconn.Listener) DialerFunc {
	return func(string, time.Duration) (net.Conn, error) {
		return listener.Dial()
	}
}

func setUpNotificationServer(t *testing.T, grpcListener *bufconn.Listener, wsListener *bufconn.Listener, kafkacConfig kafkaconsumer.Config, rmqcConfig rmqc.Config, rmqpConfig rmqp.Config) (*NotificationServer, error) {
	server := NewNotificationServer(kafkacConfig, rmqcConfig, rmqpConfig)
	if err := server.Setup(context.Background()); err != nil {
		t.Fatalf("Failed to setup replayer server: %v", err)
	}
	go func() {
		if err := server.Serve(grpcListener, wsListener); err != nil {
			t.Fatalf("Failed to serve the replayer: %v", err)
		}
	}()
	return &server, nil
}

func setUpUsermgmtServer(t *testing.T, listener *bufconn.Listener, mongoConfig libdb.MongoDBConfig) (*usermgmt.UserMgmtServer, error) {
	server := usermgmt.NewUserMgmtServer(mongoConfig)
	server.UserCollection = userCollection
	server.DefaultUser = users.InternalUser{
		User: &users.User{
			Email: "default-user@web.de",
		},
		Password: "admins-have-the-best-passwords",
	}
	server.Setup()
	go func() {
		server.Serve(listener)
	}()
	return &server, nil
}

func teardownServer(server interface{ Shutdown() }) {
	server.Shutdown()
}

type Test struct {
	Net    testcontainers.Network
	MongoC testcontainers.Container
	KafkaC testcontainers.Container
	ZkC    testcontainers.Container
	RmqC   testcontainers.Container

	KafkaConsumerConfig kafkaconsumer.Config
	rmqcConfig          rmqc.Config
	MongoConfig         libdb.MongoDBConfig
	rmqpConfig          rmqp.Config

	notificationEndpoint *grpc.ClientConn
	notificationServer   *NotificationServer
	notificationClient   pb.NotificationClient

	usermgmtEndpoint *grpc.ClientConn
	usermgmtServer   *usermgmtpb.UserManagementServer
	usermgmtClient   usermgmtpb.UserManagementClient
}

// Setup ...
func (test *Test) Setup(t *testing.T) *Test {
	var err error
	log.SetLevel(logLevel)
	if parallel {
		t.Parallel()
	}

	networkName := fmt.Sprintf("test-network-%s", tc.UniqueID())
	test.Net, err = tc.CreateNetwork(testcontainers.NetworkRequest{
		Driver:         "bridge",
		Name:           networkName,
		Attachable:     true,
		CheckDuplicate: true,
	}, 5)
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
	test.KafkaConsumerConfig = kafkaconsumer.Config{
		Config: kafka.Config{
			Brokers:             kafkaConfig.Brokers,
			Version:             kafkaConfig.KafkaVersion,
			ConnectionTolerance: libcli.ConnectionTolerance{TimeoutSec: 20},
		},
		Group: fmt.Sprintf("TestConsumerGroup-%s", tc.UniqueID()),
	}

	var grpcListener = bufconn.Listen(bufSize)
	var websocketListener = bufconn.Listen(bufSize)

	// Create endpoint
	test.notificationEndpoint, err = grpc.DialContext(context.Background(), "bufnet", grpc.WithContextDialer(func(context.Context, string) (net.Conn, error) {
		return grpcListener.Dial()
	}), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
		return test
	}

	// Start the GRPC server
	test.notificationServer, err = setUpNotificationServer(t, grpcListener, websocketListener, test.KafkaConsumerConfig, test.rmqcConfig, test.rmqpConfig)
	if err != nil {
		t.Fatalf("Failed to setup the replayer service: %v", err)
		return test
	}

	test.notificationClient = pb.NewNotificationClient(test.notificationEndpoint)
	return test
}

// Teardown ...
func (test *Test) Teardown() {
	test.notificationServer.Shutdown()
	test.notificationEndpoint.Close()
	test.MongoC.Terminate(context.Background())
	test.KafkaC.Terminate(context.Background())
	test.ZkC.Terminate(context.Background())
	test.Net.Remove(context.Background())
}
