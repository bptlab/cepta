package main

import (
	"context"
	"fmt"
	"github.com/bptlab/cepta/models/internal/types/result"
	"github.com/bptlab/cepta/models/internal/types/users"
	"github.com/bptlab/cepta/models/internal/types/ids"
	"github.com/bptlab/cepta/models/internal/transport"
	"github.com/golang/protobuf/proto"
	"net"
	"net/http"
	"testing"
	"time"
	"io"

	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/sirupsen/logrus"
	log "github.com/sirupsen/logrus"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo/options"
	"google.golang.org/grpc"
	"google.golang.org/grpc/test/bufconn"

	pb "github.com/bptlab/cepta/models/grpc/bookkeeper"
	tc "github.com/romnnn/testcontainers"
	tcmongo "github.com/romnnn/testcontainers/mongo"
	"github.com/romnnn/testcontainers-go"
)

const parallel = true

const logLevel = logrus.ErrorLevel
const bufSize = 1024 * 1024
const userCollection = "mock_users"

type DialerFunc = func(string, time.Duration) (net.Conn, error)

func dailerFor(listener *bufconn.Listener) DialerFunc {
	return func(string, time.Duration) (net.Conn, error) {
		return listener.Dial()
	}
}

func setUpBookkeeperServer(t *testing.T, listener *bufconn.Listener, mongoConfig libdb.MongoDBConfig) (*BookkeeperServer, error) {
	server := NewBookkeeperServer(mongoConfig)
	server.TransportsCollection = userCollection
	if err := server.Setup(); err != nil {
		t.Fatalf("Failed to setup user management server: %v", err)
	}
	go func() {
		if err := server.Serve(listener); err != nil {
			t.Fatalf("Bookkeeper server failed to serve: %v", err)
		}
	}()
	return &server, nil
}

func teardownServer(server interface{ Shutdown() }) {
	server.Shutdown()
}

type Test struct {
	mongoC       testcontainers.Container
	userEndpoint *grpc.ClientConn
	userServer   *BookkeeperServer
	userClient   pb.BookkeeperServerClient
}

func (test *Test) setup(t *testing.T) *Test {
	var err error
	var mongoConfig tcmongo.DBConfig
	log.SetLevel(logLevel)
	if parallel {
		t.Parallel()
	}

	// Start mongodb container
	test.mongoC, mongoConfig, err = tcmongo.StartMongoContainer(tcmongo.ContainerOptions{})
	if err != nil {
		t.Fatalf("Failed to start the mongodb container: %v", err)
		return test
	}

	// Create endpoint
	userListener := bufconn.Listen(bufSize)
	test.userEndpoint, err = grpc.DialContext(context.Background(), "bufnet", grpc.WithDialer(dailerFor(userListener)), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
		return test
	}

	// Start the GRPC server
	test.userServer, err = setUpBookkeeperServer(t, userListener, libdb.MongoDBConfig{
		Host:                mongoConfig.Host,
		Port:                mongoConfig.Port,
		User:                mongoConfig.User,
		Database:            fmt.Sprintf("mockdatabase-%s", tc.UniqueID()),
		Password:            mongoConfig.Password,
		ConnectionTolerance: libcli.ConnectionTolerance{TimeoutSec: 60},
	})
	if err != nil {
		t.Fatalf("Failed to setup the user management service: %v", err)
		return test
	}

	test.userClient = pb.NewBookkeeperClient(test.userEndpoint)
	return test
}

func (test *Test) teardown() {
	_ = test.mongoC.Terminate(context.Background())
	_ = test.userEndpoint.Close()
	teardownServer(test.userServer)
}

// Test ideas
// - Test subscribe to kafka and correct updating of archived plan data
// - Adding of a simple plan
// - Full plan vs partial plan

func insertTransports(t *testing.T, transports []*transport.Transport) {
	// TODO: Use bookkeeping service here to insert the transports
}

func TestSingleTransport(t *testing.T) {
	test := new(Test).setup(t)
	defer test.teardown()

	// Add some mock transports

	/* Assert exactly one user is in the database
	count := test.countUsers(t)
	if count != 1 {
		t.Fatalf("Initial database has %d users (expected 1)", count)
	}

	// Assert calling setup again will not add a user
	err := test.userServer.Setup()
	if err != nil {
		t.Fatal(err)
	}
	if newCount := test.countUsers(t); newCount != 1 {
		t.Fatalf("Calling Setup() again did change the number of users (from %d to %d). Expected 1.", count, newCount)
	}

	// Assert user can be found
	defaultUser, err := test.userClient.GetUser(context.Background(), &pb.GetUserRequest{
		Email: test.userServer.DefaultUser.User.Email,
	})
	if err != nil {
		t.Fatalf("Failed to get default user: %v", err)
	}
	if defaultUser.Email != test.userServer.DefaultUser.User.Email {
		t.Fatalf("Received default user has unexpected email (%s != %s)", defaultUser.Email, test.userServer.DefaultUser.User.Email)
	}
	 */
}
