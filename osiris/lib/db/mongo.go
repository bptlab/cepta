package db

import (
	"context"
	"fmt"
	"log"
	"time"

	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	"github.com/urfave/cli/v2"

	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"go.mongodb.org/mongo-driver/mongo/readpref"
)

// MongoDB ...
type MongoDB struct {
	DB *mongo.Client
}

// MongoDBConfig ...
type MongoDBConfig struct {
	Host     string
	Port     uint
	User     string
	Database string
	Password string
}

// ParseCli ...
func (config MongoDBConfig) ParseCli(ctx *cli.Context) MongoDBConfig {
	return MongoDBConfig{
		Host:     ctx.String("mongodb-host"),
		Port:     uint(ctx.Int("mongodb-port")),
		User:     ctx.String("mongodb-user"),
		Database: ctx.String("mongodb-database"),
		Password: ctx.String("db-password"),
	}
}

// MongoDatabaseCliOptions ...
var MongoDatabaseCliOptions = libcli.CommonCliOptions(libcli.Mongo)

// MongoDatabase ...
func MongoDatabase(config *MongoDBConfig) (*MongoDB, error) {
	databaseName := config.Database
	databaseConnectionURI := fmt.Sprintf("mongodb://%s:%d", config.Host, config.Port)
	client, err := mongo.NewClient(options.Client().ApplyURI(databaseConnectionURI).SetReplicaSet("rs"))
	if err != nil {
		log.Fatalf("Failed to create database client: %v (%s:%s)", err, databaseConnectionURI, databaseName)
	}
	mctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	client.Connect(mctx)
	err = client.Ping(mctx, readpref.Primary())
	if err != nil {
		log.Fatalf("Could not ping database within 10 seconds: %s (%s:%s)", err.Error(), databaseConnectionURI, databaseName)
	}
	// database := client.Database(databaseName)
	// return &MongoDB{DB: database}, nil
	return &MongoDB{DB: client}, nil
}
