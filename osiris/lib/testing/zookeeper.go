package testing

import (
	"context"
	"fmt"

	"github.com/docker/go-connections/nat"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/wait"
)

// ZookeeperConfig ...
type ZookeeperConfig struct {
	Host string
	Port uint
}

func (zkc ZookeeperConfig) String() string {
	return fmt.Sprintf("%s:%d", zkc.Host, zkc.Port)
}

// StartZookeeperContainer ...
func StartZookeeperContainer() (testcontainers.Container, ZookeeperConfig, error) {
	var config ZookeeperConfig
	ctx := context.Background()
	zkPort, err := nat.NewPort("", "2181")
	if err != nil {
		return nil, config, err
	}
	req := testcontainers.ContainerRequest{
		Image: "bitnami/zookeeper",
		Env: map[string]string{
			"ALLOW_ANONYMOUS_LOGIN": "yes",
		},
		ExposedPorts: []string{string(zkPort)},
		WaitingFor:   wait.ForLog("waiting for connections on port"),
	}
	zkC, err := testcontainers.GenericContainer(ctx, testcontainers.GenericContainerRequest{
		ContainerRequest: req,
		Started:          true,
	})
	if err != nil {
		return nil, config, err
	}
	ip, err := zkC.Host(ctx)
	if err != nil {
		return nil, config, err
	}
	port, err := zkC.MappedPort(ctx, zkPort)
	if err != nil {
		return nil, config, err
	}

	config = ZookeeperConfig{
		Host: ip,
		Port: uint(port.Int()),
	}
	return zkC, config, nil
}
