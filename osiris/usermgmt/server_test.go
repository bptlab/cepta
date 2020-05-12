package main

import (
	"context"
	"fmt"
	"github.com/bptlab/cepta/models/internal/types/result"
	"github.com/bptlab/cepta/models/internal/types/users"
	"github.com/bptlab/cepta/models/internal/types/ids"
	"github.com/golang/protobuf/proto"
	"net"
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

	pb "github.com/bptlab/cepta/models/grpc/usermgmt"
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

func setUpUserMgmtServer(t *testing.T, listener *bufconn.Listener, mongoConfig libdb.MongoDBConfig) (*UserMgmtServer, error) {
	server := NewUserMgmtServer(mongoConfig)
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
		server.Serve(listener)
	}()
	return &server, nil
}

func teardownServer(server interface{ Shutdown() }) {
	server.Shutdown()
}

type Test struct {
	mongoC       testcontainers.Container
	userEndpoint *grpc.ClientConn
	userServer   *UserMgmtServer
	userClient   pb.UserManagementClient
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
	test.userServer, err = setUpUserMgmtServer(t, userListener, libdb.MongoDBConfig{
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

	test.userClient = pb.NewUserManagementClient(test.userEndpoint)
	return test
}

func (test *Test) teardown() {
	test.mongoC.Terminate(context.Background())
	test.userEndpoint.Close()
	teardownServer(test.userServer)
}

func (test *Test) countUsers(t *testing.T) int64 {
	dbCollection := test.userServer.DB.DB.Collection(test.userServer.UserCollection)
	opts := options.Count().SetMaxTime(2 * time.Second)
	count, err := dbCollection.CountDocuments(context.TODO(), bson.D{}, opts)
	if err != nil {
		t.Fatalf("Failed to count users in database: %v", err)
	}
	return count
}

func TestDefaultUser(t *testing.T) {
	test := new(Test).setup(t)
	defer test.teardown()

	// Assert exactly one user is in the database
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
}

func TestRemoveUser(t *testing.T) {
	test := new(Test).setup(t)
	defer test.teardown()

	// Assert exactly one user is in the database
	if count := test.countUsers(t); count != 1 {
		t.Fatalf("Initial database has %d users (expected 1)", count)
	}

	defaultUser, err := test.userClient.GetUser(context.Background(), &pb.GetUserRequest{
		Email: test.userServer.DefaultUser.User.Email,
	})
	if err != nil {
		t.Fatalf("Failed to get default user: %v", err)
	}

	removeDefaultUser := func() error {
		_, err := test.userClient.RemoveUser(context.Background(), &pb.RemoveUserRequest{
			UserId: defaultUser.Id,
		})
		return err
	}

	// Assert removing the default user fails when there is no other user
	if err := removeDefaultUser(); err == nil {
		t.Fatal("Expected error when trying to delete only admin user left")
	}

	// Assert still one user in the database
	if count := test.countUsers(t); count != 1 {
		t.Fatalf("Default user has been deleted. Have %d users now.", count)
	}

	// Add new user
	newAdmin := &users.InternalUser{
		User:     &users.User{Email: "my-email@web.de"},
		Password: "hard-to-guess",
	}
	if _, err := test.userClient.AddUser(context.Background(), &pb.AddUserRequest{User: newAdmin}); err != nil {
		t.Fatalf("Failed to add new user: %v: %v", newAdmin, err)
	}

	// Assert two users in the database
	if count := test.countUsers(t); count != 2 {
		t.Fatalf("Assumed default user and a new user in the database. Have %d users.", count)
	}

	// Assert removing the default works now
	if err := removeDefaultUser(); err != nil {
		t.Fatal("Expected to be able to delete default user when there is another admin left: %v", err)
	}

	// Assert only new user in the database now
	if count := test.countUsers(t); count != 1 {
		t.Fatalf("Assumed only one user in the database after the default user has been deleted. Have %d users.", count)
	}
}

func TestAddUser(t *testing.T) {
	test := new(Test).setup(t)
	defer test.teardown()

	// Assert exactly one user is in the database
	if count := test.countUsers(t); count != 1 {
		t.Fatalf("Initial database has %d users (expected 1)", count)
	}

	// Assert adding a valid new user succeeds
	newUserReq := &pb.AddUserRequest{User: &users.InternalUser{
		User:     &users.User{Email: "my-email@mail.de"},
		Password: "hard-to-guess",
	}}
	addedUser, err := test.userClient.AddUser(context.Background(), newUserReq)
	if err != nil {
		t.Fatalf("Failed to add new user: %v: %v", newUserReq.User, err)
	}

	// Assert adding the same user again fails because emails clash
	if _, err := test.userClient.AddUser(context.Background(), newUserReq); err == nil {
		t.Fatal("Expected error when adding the same user")
	}

	// Assert adding the same user with another password fails because emails clash
	modUserReq := newUserReq
	modUserReq.User.Password = "different"
	if _, err := test.userClient.AddUser(context.Background(), modUserReq); err == nil {
		t.Fatal("Expected error when adding a user with same email but different password")
	}

	// Assert adding a new user assigns a new unique ID
	newUserWithIDReq := &pb.AddUserRequest{User: &users.InternalUser{
		User: &users.User{
			Email: "different-email@mail.de",
			Id:    &users.UserID{Id: "custom-id"},
		},
		Password: "hard-to-guess",
	}}
	addedUser2, err := test.userClient.AddUser(context.Background(), newUserWithIDReq)
	if err != nil {
		t.Fatal("Failed to add new user: %v: %v", newUserWithIDReq.User, err)
	}
	if addedUser2.Id.Id == "custom-id" {
		t.Fatal("Expected custom ids to be overridde for new users")
	}
	if addedUser2.Id.Id == addedUser.Id.Id {
		t.Fatal("Expected different user ids to be generated for each user")
	}
}

func TestGetUser(t *testing.T) {
	test := new(Test).setup(t)
	defer test.teardown()

	// Add a new user
	newUserReq := &pb.AddUserRequest{User: &users.InternalUser{
		User: &users.User{
			Email:      "email@mail.de",
			Transports: []*ids.CeptaTransportID{{Id: "14"}},
		},
		Password: "hard-to-guess",
	}}
	added, err := test.userClient.AddUser(context.Background(), newUserReq)
	if err != nil {
		t.Fatal("Failed to add new user: %v: %v", newUserReq.User, err)
	}

	assertCanFindUser := func(req *pb.GetUserRequest) (*users.User, error) {
		found, err := test.userClient.GetUser(context.Background(), req)
		if err != nil {
			t.Fatalf("Failed to get user by querying for %v: %v", req, err)
		}
		if found == nil {
			t.Fatalf("Failed to get user by querying for %v", req)
		}
		if equal := proto.Equal(found, added); !equal {
			t.Fatalf("Found user: %v doesn't match expected: %v", found, added)
		}
		return found, err
	}

	// Assert user is found by ID
	assertCanFindUser(&pb.GetUserRequest{
		UserId: added.Id,
	})

	// Assert user is found by ID and Email and ID takes precedence
	assertCanFindUser(&pb.GetUserRequest{
		UserId: added.Id,
		Email:  "not the real email",
	})

	// Assert user is found by email
	assertCanFindUser(&pb.GetUserRequest{
		Email: newUserReq.User.User.Email,
	})


	// Assert user is found by Email when ID is not found
	assertCanFindUser(&pb.GetUserRequest{
		UserId: &users.UserID{Id: "dumb-id"},
		Email:  newUserReq.User.User.Email,
	})
}

func TestGetUsers(t *testing.T) {
	test := new(Test).setup(t)
	defer test.teardown()

	// Add a new user
	newUserReq := &pb.AddUserRequest{User: &users.InternalUser{
		User: &users.User{
			Email:      "example@gmail.com",
			Transports: []*ids.CeptaTransportID{{Id: "13"}},
		},
		Password: "hard-to-guess",
	}}
	added, err := test.userClient.AddUser(context.Background(), newUserReq)
	if err != nil {
		t.Fatal("Failed to add new user: %v: %v", newUserReq.User, err)
	}

	assertCanFindAllUsers := func() {
	  // 2 Users must be in the database
	  count := test.countUsers(t)
	  var receivedUserCount int64 = 0
	  var lastUser *users.User

		stream, err := test.userClient.GetUsers(context.Background(), &result.Empty{})
    if err != nil {
      t.Fatalf("Failed to receive stream from the usermgmt service %v", err)
    }

    for {
      user, err := stream.Recv()
      if err == io.EOF {
        break
      }
      if err != nil {
        t.Fatalf("Failed to receive user from our stream: %v", err)
      }
      receivedUserCount++
      lastUser = user
    }

    if count != receivedUserCount {
      t.Fatalf("Failed to receive all users in our Database. Received: %d, Expected: %d", receivedUserCount, count)
    }

    if equal := proto.Equal(lastUser, added); !equal {
        t.Fatal("Last added user in the database doesn't match last returned user")
      }
  }

	assertCanFindAllUsers()
}
