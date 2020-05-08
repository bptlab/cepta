package main

import (
	"context"
	"fmt"
	"net"
	"testing"

	pb "github.com/bptlab/cepta/models/grpc/notification"
	usermgmtpb "github.com/bptlab/cepta/models/grpc/usermgmt"
	"github.com/bptlab/cepta/models/internal/types/users"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libredis "github.com/bptlab/cepta/osiris/lib/redis"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/bptlab/cepta/osiris/lib/kafka"
	kafkaconsumer "github.com/bptlab/cepta/osiris/lib/kafka/consumer"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	usermgmt "github.com/bptlab/cepta/osiris/usermgmt"
	tc "github.com/romnnn/testcontainers"
	tcmongo "github.com/romnnn/testcontainers/mongo"
	tckafka "github.com/romnnn/testcontainers/kafka"
	tcredis "github.com/romnnn/testcontainers/redis"
	log "github.com/sirupsen/logrus"
	"github.com/romnnn/testcontainers-go"
	"google.golang.org/grpc"
	"google.golang.org/grpc/test/bufconn"
)

const (
	logLevel       = log.WarnLevel
	bufSize        = 1024 * 1024
	userCollection = "mock-users"
)

// Test ...
type Test struct {
	Net    testcontainers.Network
	MongoC testcontainers.Container
	KafkaC testcontainers.Container
	ZkC    testcontainers.Container
	RedisC   testcontainers.Container

	kafkacConfig kafkaconsumer.Config
	kafkapConfig kafkaproducer.Config
	mongoConfig  libdb.MongoDBConfig
	redisConfig  libredis.Config

	notificationEndpoint *grpc.ClientConn
	notificationServer   *NotificationServer
	notificationClient   pb.NotificationClient

	usermgmtEndpoint *grpc.ClientConn
	usermgmtServer   *usermgmt.UserMgmtServer
	usermgmtClient   usermgmtpb.UserManagementClient

	websocketListener net.Listener
}

func setUpUserMgmtServer(t *testing.T, listener *bufconn.Listener, mongoConfig libdb.MongoDBConfig) (*usermgmt.UserMgmtServer, error) {
	server := usermgmt.NewUserMgmtServer(mongoConfig)
	server.UserCollection = userCollection
	server.DefaultUser = users.InternalUser{
		User: &users.User{
			Email: "default-user@web.de",
		},
		Password: "admins-have-the-best-passwords",
	}
	if err := server.Setup(); err != nil {
		t.Fatalf("Failed to setup user management server: %v", err)
	}
	go func() {
		if err := server.Serve(listener); err != nil {
			t.Fatal("Failed to serve user management service")
		}
	}()
	return &server, nil
}

func setUpNotificationServer(t *testing.T, grpcListener *bufconn.Listener, wsListener net.Listener, kafkacConfig kafkaconsumer.Config, usermgmtEndpoint *grpc.ClientConn, redisConfig libredis.Config) (*NotificationServer, error) {
	server := NewNotificationServer(kafkacConfig, redisConfig)
	if err := server.Setup(context.Background(), usermgmtEndpoint); err != nil {
		t.Fatalf("Failed to setup replayer server: %v", err)
	}
	go func() {
		if err := server.Serve(grpcListener, wsListener); err != nil {
			t.Fatalf("Failed to serve the replayer: %v", err)
		}
	}()
	return &server, nil
}

func (test *Test) setup(t *testing.T) *Test {
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
	var mongoConfig tcmongo.DBConfig
	test.MongoC, mongoConfig, err = tcmongo.StartMongoContainer(tcmongo.ContainerOptions{ContainerOptions: containerOptions})
	if err != nil {
		t.Fatalf("Failed to start the mongodb container: %v", err)
		return test
	}
	test.mongoConfig = libdb.MongoDBConfig{
		Host:                mongoConfig.Host,
		Port:                mongoConfig.Port,
		User:                mongoConfig.User,
		Database:            fmt.Sprintf("mockdatabase-%s", tc.UniqueID()),
		Password:            mongoConfig.Password,
		ConnectionTolerance: libcli.ConnectionTolerance{TimeoutSec: 60},
	}

	// Start kafka container
	var kafkaConfig *tckafka.ContainerConnectionConfig
	test.KafkaC, kafkaConfig, test.ZkC, _, err = tckafka.StartKafkaContainer(tckafka.ContainerOptions{ContainerOptions: containerOptions})
	if err != nil {
		t.Fatalf("Failed to start the kafka container: %v", err)
		return test
	}
	baseKafkaConfig := kafka.Config{
		Brokers:             kafkaConfig.Brokers,
		Version:             kafkaConfig.KafkaVersion,
		ConnectionTolerance: libcli.ConnectionTolerance{MaxRetries: 20, RetryIntervalSec: 5},
	}
	test.kafkacConfig = kafkaconsumer.Config{
		Config: baseKafkaConfig,
		Group:  fmt.Sprintf("TestConsumerGroup-%s", tc.UniqueID()),
	}
	test.kafkapConfig = kafkaproducer.Config{
		Config: baseKafkaConfig,
	}

	// Start redis container
	var redisConConfig tcredis.Config
	test.RedisC, redisConConfig, err = tcredis.StartRedisContainer(tcredis.ContainerOptions{ContainerOptions: containerOptions})
	if err != nil {
		t.Fatalf("Failed to start the rabbitmq container: %v", err)
		return test
	}
	test.redisConfig = libredis.Config{
		Host:     redisConConfig.Host,
		Port:     int(redisConConfig.Port),
		Password: redisConConfig.Password,
	}

	var grpcListener = bufconn.Listen(bufSize)
	var usermgmtListener = bufconn.Listen(bufSize)
	// Choose a random free port for the websocket listener
	test.websocketListener, err = net.Listen("tcp", "127.0.0.1:0")
	if err != nil {
		t.Fatalf("Failed to create listener for the websocket connection")
	}

	// User management service
	test.usermgmtEndpoint, err = grpc.DialContext(context.Background(), "bufnet", grpc.WithContextDialer(func(context.Context, string) (net.Conn, error) {
		return usermgmtListener.Dial()
	}), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
		return test
	}

	test.usermgmtServer, err = setUpUserMgmtServer(t, usermgmtListener, test.mongoConfig)
	if err != nil {
		t.Fatalf("Failed to setup the user management service: %v", err)
		return test
	}

	test.usermgmtClient = usermgmtpb.NewUserManagementClient(test.usermgmtEndpoint)

	// Notification service
	test.notificationEndpoint, err = grpc.DialContext(context.Background(), "bufnet", grpc.WithContextDialer(func(context.Context, string) (net.Conn, error) {
		return grpcListener.Dial()
	}), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
		return test
	}
	test.notificationServer, err = setUpNotificationServer(t, grpcListener, test.websocketListener, test.kafkacConfig, test.usermgmtEndpoint, test.redisConfig)
	if err != nil {
		t.Fatalf("Failed to setup the replayer service: %v", err)
		return test
	}
	test.notificationClient = pb.NewNotificationClient(test.notificationEndpoint)

	return test
}

func (test *Test) teardown() {
	test.notificationServer.Shutdown()
	test.usermgmtServer.Shutdown()
	_ = test.notificationEndpoint.Close()
	_ = test.usermgmtEndpoint.Close()
	_ = test.MongoC.Terminate(context.Background())
	_ = test.KafkaC.Terminate(context.Background())
	_ = test.ZkC.Terminate(context.Background())
	_ = test.Net.Remove(context.Background())
}
