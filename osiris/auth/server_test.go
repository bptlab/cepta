package main

import (
	"context"
	"fmt"
	"net"
	"testing"
	"time"

	authpb "github.com/bptlab/cepta/models/grpc/auth"
	usermgmtpb "github.com/bptlab/cepta/models/grpc/usermgmt"
	"github.com/bptlab/cepta/models/internal/types/users"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	usermgmt "github.com/bptlab/cepta/osiris/usermgmt"
	"github.com/dgrijalva/jwt-go"
	tc "github.com/romnnn/testcontainers"
	tcmongo "github.com/romnnn/testcontainers/mongo"
	"github.com/sirupsen/logrus"
	log "github.com/sirupsen/logrus"
	"github.com/romnnn/testcontainers-go"
	"google.golang.org/grpc"
	"google.golang.org/grpc/test/bufconn"
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

func setUpAuthServer(t *testing.T, listener *bufconn.Listener, mongoConfig libdb.MongoDBConfig) (*AuthenticationServer, error) {
	server := NewAuthServer(mongoConfig)
	server.UserCollection = userCollection
	if err := server.GenerateKeys(); err != nil {
		t.Fatalf("Failed to generate keys: %v", err)
	}
	if err := server.Setup(); err != nil {
		t.Fatalf("Failed to setup auth server: %v", err)
	}
	go func() {
		if err := server.Serve(listener); err != nil {
			t.Fatalf("Failed to serve auth service: %v", err)
		}
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
	mongoC       testcontainers.Container
	authEndpoint *grpc.ClientConn
	userEndpoint *grpc.ClientConn
	authServer   *AuthenticationServer
	userServer   *usermgmt.UserMgmtServer
	authClient   authpb.AuthenticationClient
	userClient   usermgmtpb.UserManagementClient
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

	// Create endpoints
	authListener := bufconn.Listen(bufSize)
	test.authEndpoint, err = grpc.DialContext(context.Background(), "bufnet", grpc.WithDialer(dailerFor(authListener)), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
		return test
	}

	userListener := bufconn.Listen(bufSize)
	test.userEndpoint, err = grpc.DialContext(context.Background(), "bufnet", grpc.WithDialer(dailerFor(userListener)), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
		return test
	}

	// Start the GRPC servers
	mc := libdb.MongoDBConfig{
		Host:                mongoConfig.Host,
		Port:                mongoConfig.Port,
		User:                mongoConfig.User,
		Database:            fmt.Sprintf("mockdatabase-%s", tc.UniqueID()),
		Password:            mongoConfig.Password,
		ConnectionTolerance: libcli.ConnectionTolerance{TimeoutSec: 60},
	}
	test.authServer, err = setUpAuthServer(t, authListener, mc)
	if err != nil {
		t.Fatalf("Failed to setup the authentication service: %v", err)
		return test
	}

	test.userServer, err = setUpUserMgmtServer(t, userListener, mc)
	if err != nil {
		t.Fatalf("Failed to setup the user management service: %v", err)
		return test
	}

	test.authClient = authpb.NewAuthenticationClient(test.authEndpoint)
	test.userClient = usermgmtpb.NewUserManagementClient(test.userEndpoint)
	return test
}

func (test *Test) teardown() {
	_ = test.mongoC.Terminate(context.Background())
	_ = test.authEndpoint.Close()
	_ = test.userEndpoint.Close()
	test.authServer.Shutdown()
	test.userServer.Shutdown()
}

func TestLogin(t *testing.T) {
	test := new(Test).setup(t)
	defer test.teardown()

	// 1. Invalid login because no such user has been added
	assertLoginFails(t, test.authClient, &authpb.UserLoginRequest{
		Email:    "test@example.com",
		Password: "secret",
	})

	// 2. Valid user login because it was set as default admin user
	assertSuccessfulLogin(t, test.authClient, &authpb.UserLoginRequest{
		Email:    test.userServer.DefaultUser.User.Email,
		Password: test.userServer.DefaultUser.Password,
	})

	// 3. Invalid user login because of typos
	assertLoginFails(t, test.authClient, &authpb.UserLoginRequest{
		Email:    test.userServer.DefaultUser.User.Email,
		Password: test.userServer.DefaultUser.Password + "'",
	})
	assertLoginFails(t, test.authClient, &authpb.UserLoginRequest{
		Email:    test.userServer.DefaultUser.User.Email + " ",
		Password: test.userServer.DefaultUser.Password,
	})

	// 4. Add the user from 1. to make sure login succeeds now
	addedUser, addUserErr := test.userClient.AddUser(context.Background(), &usermgmtpb.AddUserRequest{
		User: &users.InternalUser{User: &users.User{Email: "test@example.com"}, Password: "secret"},
	})
	if addUserErr != nil {
		t.Errorf("Failed to add user: %v", addUserErr)
	}

	assertSuccessfulLogin(t, test.authClient, &authpb.UserLoginRequest{
		Email:    "test@example.com",
		Password: "secret",
	})

	// 5. Remove the user from 1. to make sure login fails again
	_, removeUserErr := test.userClient.RemoveUser(context.Background(), &usermgmtpb.RemoveUserRequest{
		UserId: addedUser.Id,
	})
	if removeUserErr != nil {
		t.Errorf("Failed to remove user: %v", removeUserErr)
	}

	assertLoginFails(t, test.authClient, &authpb.UserLoginRequest{
		Email:    "test@example.com",
		Password: "secret",
	})

	// 6. Sanity check empty values
	assertLoginFails(t, test.authClient, &authpb.UserLoginRequest{
		Email:    test.userServer.DefaultUser.User.Email,
		Password: "",
	})
	assertLoginFails(t, test.authClient, &authpb.UserLoginRequest{
		Email:    "",
		Password: test.userServer.DefaultUser.Password,
	})
	assertLoginFails(t, test.authClient, &authpb.UserLoginRequest{
		Email:    "",
		Password: "",
	})
}

func TestValidation(t *testing.T) {
	test := new(Test).setup(t)
	defer test.teardown()
	email := "test@example.com"
	pw := "secret"
	addedUser, addUserErr := test.userClient.AddUser(context.Background(), &usermgmtpb.AddUserRequest{
		User: &users.InternalUser{User: &users.User{Email: email}, Password: pw},
	})
	if addUserErr != nil {
		t.Errorf("Failed to add user: %v", addUserErr)
	}

	// 1. Get the token from a valid user
	response, err := assertSuccessfulLogin(t, test.authClient, &authpb.UserLoginRequest{
		Email:    email,
		Password: pw,
	})
	if err != nil {
		t.Fatal(err)
	}
	assertIsValidToken(t, test.authClient, &authpb.TokenValidationRequest{Token: response.Token})

	// 2. Delete the valid user and make sure the token is valid until it expires
	_, removeUserErr := test.userClient.RemoveUser(context.Background(), &usermgmtpb.RemoveUserRequest{
		UserId: addedUser.Id,
	})
	if removeUserErr != nil {
		t.Fatal("Failed to remove user: %v", removeUserErr)
	}
	assertIsValidToken(t, test.authClient, &authpb.TokenValidationRequest{Token: response.Token})
	at(time.Now().Add(time.Duration(test.authServer.ExpireSeconds+200)*time.Second), func() {
		assertIsInvalidToken(t, test.authClient, &authpb.TokenValidationRequest{Token: response.Token})
	})

	// 3. Test for a malformed token (invalid format)
	badToken := "12.adbs."
	if _, err := test.authClient.Validate(context.Background(), &authpb.TokenValidationRequest{Token: badToken}); err == nil {
		t.Fatal("Bad jwt \"%s\"did not cause internal parse error", badToken)
	}
}

func TestDebugMode(t *testing.T) {
	test := new(Test).setup(t)
	defer test.teardown()
	test.authServer.DisableAuth = true
	email := "test@example.com"
	pw := "secret"
	// Test if arbitrary users can still login
	response, err := assertSuccessfulLogin(t, test.authClient, &authpb.UserLoginRequest{
		Email:    email,
		Password: pw,
	})
	if err != nil {
		t.Fatal("New user could not login in debug mode: %v", err)
	}
	assertIsValidToken(t, test.authClient, &authpb.TokenValidationRequest{Token: response.Token})
}

// Override time value for tests.  Restore default value after.
// Source: https://github.com/dgrijalva/jwt-go/blob/master/example_test.go#L81
func at(t time.Time, f func()) {
	jwt.TimeFunc = func() time.Time { return t }
	f()
	jwt.TimeFunc = time.Now
}

func assertSuccessfulLogin(t *testing.T, client authpb.AuthenticationClient, login *authpb.UserLoginRequest) (*authpb.AuthenticationToken, error) {
	loginResult, err := client.Login(context.Background(), login)
	if err != nil {
		t.Errorf("Login for user %v failed unexpectedly: %v", login, err)
	}
	return loginResult, err
}

func assertLoginFails(t *testing.T, client authpb.AuthenticationClient, login *authpb.UserLoginRequest) {
	loginResult, err := client.Login(context.Background(), login)
	if err == nil {
		t.Errorf("Login for user %v succeeded unexpectedly with token %v", loginResult.Token)
	}
}

func assertIsValidToken(t *testing.T, client authpb.AuthenticationClient, request *authpb.TokenValidationRequest) {
	valid, err := client.Validate(context.Background(), request)
	if err != nil || (valid != nil && !valid.Valid) {
		t.Errorf("Validation for token %s yielded invalid unexpectedly", request.Token)
	}
}

func assertIsInvalidToken(t *testing.T, client authpb.AuthenticationClient, request *authpb.TokenValidationRequest) {
	valid, err := client.Validate(context.Background(), request)
	if err == nil || (valid != nil && valid.Valid) {
		t.Errorf("Validation for token %s yielded valid unexpectedly", request.Token)
	}
}
