package testing

import (
	"context"
	"fmt"

	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/wait"
)

// ZookeeperConfig ...
type ZookeeperConfig struct {
	Host string
	Port uint
	log  *LogCollector
}

func (zkc ZookeeperConfig) String() string {
	return fmt.Sprintf("%s:%d", zkc.Host, zkc.Port)
}

// StartZookeeperContainer ...
func StartZookeeperContainer(options ContainerOptions) (testcontainers.Container, ZookeeperConfig, error) {
	var config ZookeeperConfig
	ctx := context.Background()
	req := testcontainers.ContainerRequest{
		Image: "bitnami/zookeeper",
		Env: map[string]string{
			"ALLOW_ANONYMOUS_LOGIN": "yes",
		},
		WaitingFor: wait.ForLog("binding to port"),
	}
	if options.Network != "" {
		req.Networks = []string{
			options.Network,
		}
		req.NetworkAliases = map[string][]string{
			options.Network: []string{"zookeeper"},
		}
	}
	zkC, err := testcontainers.GenericContainer(ctx, testcontainers.GenericContainerRequest{
		ContainerRequest: req,
		Started:          true,
	})
	if err != nil {
		return nil, config, err
	}

	config = ZookeeperConfig{
		Host: "zookeeper",
		Port: uint(2181),
	}

	if options.Log {
		config.log = &LogCollector{}

		err := zkC.StartLogProducer(ctx)
		if err != nil {
			return zkC, config, fmt.Errorf("Failed to start log producer: %v", err)
		}

		zkC.FollowOutput(config.log)
		// User must call StopLogProducer() himself
	}

	return zkC, config, nil
}
