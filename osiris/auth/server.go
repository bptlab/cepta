package main

import (
	"context"
	"crypto/rand"
	"crypto/rsa"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"io/ioutil"
	"math/big"
	"net"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/google/uuid"

	"github.com/bptlab/cepta/ci/versioning"
	pb "github.com/bptlab/cepta/models/grpc/auth"
	"github.com/bptlab/cepta/models/internal/types/users"
	lib "github.com/bptlab/cepta/osiris/auth/lib"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/dgrijalva/jwt-go"
	"github.com/lestrrat-go/jwx/jwk"

	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

// Version will be injected at build time
var Version string = "Unknown"

// BuildTime will be injected at build time
var BuildTime string = ""

var server AuthenticationServer

// Claims ...
type Claims struct {
	UserID string `json:"userid"`
	jwt.StandardClaims
}

// AuthenticationServer ...
type AuthenticationServer struct {
	pb.UnimplementedAuthenticationServer
	grpcServer 		*grpc.Server
	MongoConfig    libdb.MongoDBConfig
	DB             *libdb.MongoDB
	SignKey        *rsa.PrivateKey
	JwkSet         *jwk.Set
	UserCollection string
	DisableAuth    bool
	ExpireSeconds  int64
}

// NewAuthServer ...
func NewAuthServer(mongoConfig libdb.MongoDBConfig) AuthenticationServer {
	return AuthenticationServer{
		MongoConfig: mongoConfig,
	}
}

// Shutdown ...
func (s *AuthenticationServer) Shutdown() {
	log.Info("Graceful shutdown")
	log.Info("Stopping GRPC server")
	s.grpcServer.Stop()
}

func (s *AuthenticationServer) findUser(email string) (*users.InternalUser, error) {
	return lib.FindUser(s.DB.DB.Collection(s.UserCollection), email)
}

// Validate checks a token if it is valid (e.g. has not expired)
func (s *AuthenticationServer) Validate(ctx context.Context, in *pb.TokenValidationRequest) (*pb.TokenValidationResult, error) {
	if s.DisableAuth {
		return &pb.TokenValidationResult{Valid: true}, nil
	}
	token, err := jwt.ParseWithClaims(in.Token, &Claims{}, func(token *jwt.Token) (interface{}, error) {
		keyID, ok := token.Header["kid"].(string)
		if !ok {
			return nil, errors.New("expecting JWT header to have string kid")
		}

		if key := s.JwkSet.LookupKeyID(keyID); len(key) == 1 {
			return key[0].Materialize()
		}
		return nil, fmt.Errorf("unable to find key %q", keyID)
	})
	if err != nil {
		return &pb.TokenValidationResult{Valid: false}, status.Error(codes.Internal, "Failed to parse token")
	}
	if _, ok := token.Claims.(*Claims); ok && token.Valid {
		return &pb.TokenValidationResult{Valid: true}, nil
	}
	return &pb.TokenValidationResult{Valid: false}, nil
}

func (s *AuthenticationServer) signJwt(userID *users.UserID) (string, error) {
	if userID == nil || userID.Id == "" {
		return "", errors.New("User has no ID")
	}
	expirationTime := time.Now().Add(time.Duration(s.ExpireSeconds) * time.Second)
	// Create the JWT claims, which includes the user ID and expiry time
	claims := &Claims{
		UserID: userID.Id,
		StandardClaims: jwt.StandardClaims{
			// In JWT, the expiry time is expressed as unix milliseconds
			ExpiresAt: expirationTime.Unix(),
			Issuer:    "ceptaproject@gmail.com",
			Audience:  "https://bptlab.github.io/cepta",
		},
	}

	// Declare the token with the algorithm used for signing, and the claims
	token := jwt.NewWithClaims(jwt.SigningMethodRS256, claims)
	token.Header["kid"] = "0"
	token.Header["alg"] = "RS256"

	return token.SignedString(s.SignKey)
}

// Login logs in a user
func (s *AuthenticationServer) Login(ctx context.Context, in *pb.UserLoginRequest) (*pb.AuthenticationToken, error) {
	if s.DisableAuth {
		if userID, err := uuid.NewRandom(); err == nil {
			id := &users.UserID{Id: userID.String()}
			if token, err := s.signJwt(id); err == nil {
				return &pb.AuthenticationToken{
					Token:      token,
					Email: 		in.GetEmail(),
					UserId: 	id,
					Expiration: s.ExpireSeconds,
				}, nil
			}
		}
		return nil, status.Error(codes.Unauthenticated, "Login failed")
	}

	user, err := s.findUser(in.Email)
	if err != nil {
		return nil, status.Error(codes.Unauthenticated, "Unauthorized")
	}

	if user.Password != in.Password {
		return nil, status.Error(codes.Unauthenticated, "Unauthorized")
	}

	if token, err := s.signJwt(user.User.Id); err == nil {
		return &pb.AuthenticationToken{
			Token:      token,
			Email: 		user.GetUser().GetEmail(),
			UserId: 	user.GetUser().GetId(),
			Expiration: s.ExpireSeconds,
		}, nil
	}
	// If there is an error in creating the JWT return an internal server error
	return nil, status.Error(codes.Internal, "Internal error")
}

func main() {
	shutdown := make(chan os.Signal)
	signal.Notify(shutdown, syscall.SIGINT, syscall.SIGTERM)
	go func() {
		<-shutdown
		server.Shutdown()
	}()

	cliFlags := []cli.Flag{}
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServicePort, libcli.ServiceLogLevel)...)
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceConnectionTolerance)...)
	cliFlags = append(cliFlags, libdb.MongoDatabaseCliOptions...)
	cliFlags = append(cliFlags, []cli.Flag{
		&cli.PathFlag{
			Name:    "key",
			Aliases: []string{"public-key", "signing-key"},
			EnvVars: []string{"PRIVATE_KEY", "KEY", "SIGNING_KEY"},
			Usage:   "private key to sign the tokens with",
		},
		&cli.PathFlag{
			Name:    "jwks",
			Aliases: []string{"jwks-json", "jwk-set"},
			EnvVars: []string{"JWKS", "JWK_SET", "JWKS_JSON"},
			Usage:   "jwk set json file containing the public keys",
		},
		&cli.StringFlag{
			Name:    "collection",
			Value:   "users",
			Aliases: []string{"mongodb-collection"},
			EnvVars: []string{"MONGO_COLLECTION", "COLLECTION"},
			Usage:   "the mongo collection containing the user data",
		},
		&cli.BoolFlag{
			Name:    "no-auth",
			Value:   false,
			Aliases: []string{"development", "test", "dev"},
			EnvVars: []string{"NO_AUTH", "DEV", "TEST"},
			Usage:   "disable user database lookup and accept every login request",
		},
		&cli.IntFlag{
			Name:    "expire-sec",
			Value:   7 * 24 * 60 * 60,
			Aliases: []string{"expire"},
			EnvVars: []string{"EXPIRATION_SEC"},
			Usage:   "number of seconds until a user token expires",
		},
	}...)

	app := &cli.App{
		Name:    "CEPTA authentication microservice",
		Version: versioning.BinaryVersion(Version, BuildTime),
		Usage:   "Handles basic JWT authentication",
		Flags:   cliFlags,
		Action: func(ctx *cli.Context) error {
			level, err := log.ParseLevel(ctx.String("log"))
			if err != nil {
				log.Warnf("Log level '%s' does not exist.")
				level = log.InfoLevel
			}
			log.SetLevel(level)

			server = AuthenticationServer{
				MongoConfig:    libdb.MongoDBConfig{}.ParseCli(ctx),
				UserCollection: ctx.String("collection"),
				ExpireSeconds:  int64(ctx.Int("expire-sec")),
			}

			if ctx.String("jwks") != "" && ctx.String("key") != "" {
				// Load from files
				if err := server.LoadKeys(ctx); err != nil {
					return fmt.Errorf("failed to load keys: %v", err)
				}
			} else {
				log.Info("No jwk set file and private key specified. Generating both.")
				if err := server.GenerateKeys(); err != nil {
					return fmt.Errorf("failed to generate keys: %v", err)
				}
			}

			port := fmt.Sprintf(":%d", ctx.Int("port"))
			listener, err := net.Listen("tcp", port)
			if err != nil {
				return fmt.Errorf("failed to listen: %v", err)
			}

			if err := server.Setup(); err != nil {
				return err
			}
			return server.Serve(listener)
		},
	}
	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}

func toJWKS(pub *rsa.PublicKey) (string, error) {
	// See https://github.com/golang/crypto/blob/master/acme/jws.go#L90
	// https://tools.ietf.org/html/rfc7518#section-6.3.1
	n := pub.N
	e := big.NewInt(int64(pub.E))
	// Field order is important.
	// See https://tools.ietf.org/html/rfc7638#section-3.3 for details.
	return fmt.Sprintf(`{"e":"%s","kty":"RSA","n":"%s"}`,
		base64.RawURLEncoding.EncodeToString(e.Bytes()),
		base64.RawURLEncoding.EncodeToString(n.Bytes()),
	), nil
}

// LoadKeys loads a private key and jwk set from files
func (s *AuthenticationServer) LoadKeys(ctx *cli.Context) error {
	// Load the key
	log.Infof("Loading signing key from %s", ctx.String("key"))
	data, err := ioutil.ReadFile(ctx.String("key"))
	if err != nil {
		return fmt.Errorf("Failed to read %s: %v", ctx.String("key"), err)
	}

	// Parse the PEM private key
	key, err := jwt.ParseRSAPrivateKeyFromPEM(data)
	if err != nil {
		return fmt.Errorf("Failed to parse private key PEM file: %v", err)
	}
	s.SignKey = key

	// Load the jwk set
	log.Infof("Loading jwk set from %s", ctx.String("jwks"))
	jwksData, err := ioutil.ReadFile(ctx.String("jwks"))
	if err != nil {
		return fmt.Errorf("Failed to read %s: %v", ctx.String("jwks"), err)
	}

	// Load the jwk set
	s.JwkSet = new(jwk.Set)
	err = s.JwkSet.UnmarshalJSON(jwksData)
	if err != nil {
		return fmt.Errorf("Failed to parse the jwk set at %s: %v", ctx.String("jwks"), err)
	}
	return nil
}

// GenerateKeys generates private and public keys for jwt validation
func (s *AuthenticationServer) GenerateKeys() error {
	key, err := rsa.GenerateKey(rand.Reader, 4096)
	if err != nil {
		return err
	}
	s.SignKey = key
	jwkJSON, err := toJWKS(key.Public().(*rsa.PublicKey))
	if err != nil {
		return err
	}

	var jwk0 map[string]interface{}
	json.Unmarshal([]byte(jwkJSON), &jwk0)
	jwk0["kid"] = "0"
	jwk0["alg"] = "RS256"
	jwks := map[string]interface{}{
		"keys": []interface{}{jwk0},
	}
	s.JwkSet = new(jwk.Set)
	if err := s.JwkSet.ExtractMap(jwks); err != nil {
		return err
	}
	return nil
}

// Setup prepares the service
func (s *AuthenticationServer) Setup() error {
	mongo, err := libdb.MongoDatabase(&s.MongoConfig)
	if err != nil {
		return err
	}
	s.DB = mongo

	if s.UserCollection == "" {
		return errors.New("Need to specify a valid collection name")
	}

	if s.SignKey == nil {
		return errors.New("Need to specify a valid private key for signing")
	}
	return nil
}

// Serve starts the service
func (s *AuthenticationServer) Serve(listener net.Listener) error {
	log.Infof("Authentication service ready at %s", listener.Addr())
	s.grpcServer = grpc.NewServer()
	pb.RegisterAuthenticationServer(s.grpcServer, s)
	if err := s.grpcServer.Serve(listener); err != nil {
		return err
	}
	log.Info("Closing socket")
	listener.Close()
	return nil
}
