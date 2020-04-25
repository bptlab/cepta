package testing

import (
	"context"
	"log"

	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/docker/go-connections/nat"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/wait"
)

// StartMongoContainer ...
func StartMongoContainer(options ContainerOptions) (testcontainers.Container, libdb.MongoDBConfig, error) {
	var config libdb.MongoDBConfig
	ctx := context.Background()
	mongoPort, err := nat.NewPort("", "27017")
	if err != nil {
		return nil, config, err
	}
	user := "root"
	password := "example"
	req := testcontainers.ContainerRequest{
		Image: "mongo",
		Env: map[string]string{
			"MONGO_INITDB_ROOT_USERNAME": user,
			"MONGO_INITDB_ROOT_PASSWORD": password,
		},
		ExposedPorts: []string{string(mongoPort)},
		WaitingFor:   wait.ForLog("waiting for connections on port"),
	}
	if options.Network != "" {
		req.Networks = []string{
			options.Network,
		}
	}
	mongoC, err := testcontainers.GenericContainer(ctx, testcontainers.GenericContainerRequest{
		ContainerRequest: req,
		Started:          true,
	})
	if err != nil {
		return nil, config, err
	}
	ip, err := mongoC.Host(ctx)
	if err != nil {
		return nil, config, err
	}
	port, err := mongoC.MappedPort(ctx, mongoPort)
	if err != nil {
		return nil, config, err
	}

	containerIP, err := mongoC.ContainerIP(ctx)
	if err != nil {
		return nil, config, err
	}
	log.Printf("Container IP=%s\n", containerIP)

	config = libdb.MongoDBConfig{
		Host:     ip,
		Port:     uint(port.Int()),
		User:     user,
		Password: password,
		Database: "mockdatabase",
		ConnectionTolerance: libcli.ConnectionTolerance{
			TimeoutSec: 20,
		},
	}

	return mongoC, config, nil
}
