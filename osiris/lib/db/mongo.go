package db

import (
	"context"
	"fmt"
	"time"

	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	"github.com/urfave/cli/v2"

	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"go.mongodb.org/mongo-driver/mongo/readpref"
)

// MongoDB ...
type MongoDB struct {
	Client *mongo.Client
	DB     *mongo.Database
}

// Close ...
func (db *MongoDB) Close() error {
	return db.Client.Disconnect(context.Background())
}

// MongoDBConfig ...
type MongoDBConfig struct {
	Host                string
	Port                uint
	User                string
	Database            string
	Password            string
	ConnectionTolerance libcli.ConnectionTolerance
}

// ParseCli ...
func (config MongoDBConfig) ParseCli(ctx *cli.Context) MongoDBConfig {
	return MongoDBConfig{
		Host:                ctx.String("mongodb-host"),
		Port:                uint(ctx.Int("mongodb-port")),
		User:                ctx.String("mongodb-user"),
		Database:            ctx.String("mongodb-database"),
		Password:            ctx.String("mongodb-password"),
		ConnectionTolerance: libcli.ConnectionTolerance{}.ParseCli(ctx),
	}
}

// MongoDatabaseCliOptions ...
var MongoDatabaseCliOptions = libcli.CommonCliOptions(libcli.Mongo)

// MongoDatabase ...
func MongoDatabase(config *MongoDBConfig) (*MongoDB, error) {
	databaseName := config.Database
	var databaseAuth string
	if config.User != "" && config.Password != "" {
		databaseAuth = fmt.Sprintf("%s:%s@", config.User, config.Password)
	}
	databaseHost := fmt.Sprintf("%s:%d", config.Host, config.Port)
	databaseConnectionURI := fmt.Sprintf("mongodb://%s%s/?connect=direct", databaseAuth, databaseHost)
	client, err := mongo.NewClient(options.Client().ApplyURI(databaseConnectionURI))
	if err != nil {
		return nil, fmt.Errorf("Failed to create database client: %v (%s:%s)", err, databaseConnectionURI, databaseName)
	}
	mctx, cancel := context.WithTimeout(context.Background(), time.Duration(config.ConnectionTolerance.Timeout())*time.Second)
	defer cancel()
	client.Connect(mctx)

	err = client.Ping(mctx, readpref.Primary())
	if err != nil {
		return nil, fmt.Errorf("Could not ping database within %d seconds: %s (%s:%s)", config.ConnectionTolerance.Timeout(), err.Error(), databaseConnectionURI, databaseName)
	}
	database := client.Database(databaseName)
	return &MongoDB{DB: database, Client: client}, nil
}
