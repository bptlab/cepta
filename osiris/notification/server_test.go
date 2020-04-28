package main

import (
	"context"
	"net"
	"testing"
	"time"

	notificationpb "github.com/bptlab/cepta/models/grpc/notification"
	usermanagementpb "github.com/bptlab/cepta/models/grpc/usermgmt"
	"github.com/bptlab/cepta/models/types/users"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	rmqProducer "github.com/bptlab/cepta/osiris/lib/rabbitmq/producer"
	rmqConsumer "github.com/bptlab/cepta/osiris/lib/rabbitmq/consumer"
	integrationtesting "github.com/bptlab/cepta/osiris/lib/testing"
	"github.com/grpc/grpc-go/test/bufconn"
	log "github.com/sirupsen/logrus"
	tc "github.com/romnnn/testcontainers"
	"github.com/testcontainers/testcontainers-go"
	"google.golang.org/grpc"
)

const (
  logLevel = log.ErrorLevel
  bufSize = 1024 * 1024
  userCollection = "mock_users"
)

type DialerFunc = func(string, time.Duration) (net.Conn, error)

func dialerFor(listener *bufconn.Listener) DialerFunc {
	return func(string, time.Duration) (net.Conn, error) {
		return listener.Dial()
	}
}

func setUpNotificationServer(t *testing.T,listener *bufconn.Listener,
      kafkaConsumerConfig kafkaconsumer.KafkaConsumerOptions, rmqConsumerConfig rmqConsumer.RabbitMQConsumerOptions,
      rmqProducerConfig rmqConsumer.RabbitMQProducerOptions) (*NotificationServer, error) {
	server := NewNotificationServer(kafkaConsumerConfig, rmqConsumerConfig, rmqProducerConfig)
	server.Setup()
	go func() {
		server.Serve(listener)
	}()
	return &server, nil
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
	mongoC                testcontainers.Container
	rmqC                  testcontainers.Container
	kafkaC                testcontainers.Container
	notificationEndpoint  *grpc.ClientConn
	notificationServer    *NotificationServer
	notificationClient    notificationpb.NotificationClient

	usermgmtEndpoint      *grpc.ClientConn
	userServer            *usermgmt.UserMgmtServer
	userClient            usermgmtpb.UserManagementClient
}

func (test *Test) setup(t *testing.T) *Test {
	var err error
	var dbConn libdb.MongoDBConfig
	log.SetLevel(logLevel)

	// Start mongodb container
	test.mongoC, dbConn, err = tc.StartMongoContainer()
	if err != nil {
		t.Fatalf("Failed to start the mongodb container: %v", err)
		return test
	}

	// Start rabbitmq container
	test.mongoC, dbConn, err = tc.StartMongoContainer()
	if err != nil {
		t.Fatalf("Failed to start the rabbitMQ container: %v", err)
		return test
	}

	// Start kafka container
	test.kafkaC, kafkaConfig, zkC, net, err = tc.StartKafkaContainer()
	if err != nil {
		t.Fatalf("Failed to start the Kafka container: %v", err)
		return test
	}

	// Create endpoints
	notificationListener := bufconn.Listen(bufSize)
	test.notificationEndpoint, err = grpc.DialContext(context.Background(), "bufnet", grpc.WithDialer(dailerFor(notificationListener)), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
		return test
	}

	// Start the GRPC servers
	test.notificationServer, err = setUpNotificationsServer(t, notificationListener, dbConn)
	if err != nil {
		t.Fatalf("Failed to setup the Notification service: %v", err)
		return test
	}

	test.notificationClient = notificationpb.NewNotificationClient(test.notificationEndpoint)
	return test
}

func (test *Test) teardown() {
	test.mongoC.Terminate(context.Background())
	test.notificationEndpoint.Close()
	teardownServer(test.notificationServer)
}


/*
Old Tests that are not needed anymore

func TestSettingNotificationNumber(t *testing.T) {
	test := new(Test).setup(t)
	defer test.teardown()

	notificationNumber := 500

  assertSuccessfulSettingNotificationNumber(t, test.notificationClient, &notificationpb.UserNotificationNumberRequest{
    Number: notificationNumber
  })

  assertFailingSettingNotificationNumber(t, test.notificationClient, &notificationpb.UserNotificationNumberRequest{
    Number: 0
  })
}

func  assertSuccessfulSettingNotificationNumber(t *testing.T, client .notificationClient, &notificationpb.UserNotificationNumberRequest)
*/