package testing

import (
	"context"
	"fmt"
	"log"
	"strings"
	"sync"
	"time"

	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	"github.com/bptlab/cepta/osiris/lib/kafka/consumer"
	"github.com/bptlab/cepta/osiris/lib/kafka/producer"
	dockerclient "github.com/docker/docker/client"
	"github.com/docker/go-connections/nat"
	"github.com/phayes/freeport"
	"github.com/testcontainers/testcontainers-go"
)

var portMux sync.Mutex

const (
	// Outside ...
	Outside = iota
	// Inside ...
	Inside
)

const brokerPort = Inside

// KafkaConnectionOptions ...
type KafkaConnectionOptions interface {
	GetBrokers() []string
	GetConnectionTolerance() libcli.ConnectionTolerance
}

// KafkaContainerConnectionConfig ...
type KafkaContainerConnectionConfig struct {
	Brokers             []string
	ConnectionTolerance libcli.ConnectionTolerance
	log                 *LogCollector
}

// ProducerConfig ...
func (kccc KafkaContainerConnectionConfig) ProducerConfig() producer.KafkaProducerOptions {
	return producer.KafkaProducerOptions{
		Brokers:             kccc.Brokers,
		ConnectionTolerance: kccc.ConnectionTolerance,
	}
}

// ConsumerConfig ...
func (kccc KafkaContainerConnectionConfig) ConsumerConfig() consumer.KafkaConsumerOptions {
	return consumer.KafkaConsumerOptions{
		Brokers:             kccc.Brokers,
		ConnectionTolerance: kccc.ConnectionTolerance,
		Version:             "2.4.1",
	}
}

func getFreePort() int {
	portMux.Lock()
	defer portMux.Unlock()
	var attempts int
	for {
		port, err := freeport.GetFreePort()
		if err == nil {
			return port
		}
		attempts++
		if attempts >= 200 {
			log.Fatalf("Failed to allocate port after 200 attempts")
		}
	}
}

// StartKafkaContainer ...
func StartKafkaContainer(options ContainerOptions) (testcontainers.Container, testcontainers.Container, KafkaContainerConnectionConfig, error) {
	var config KafkaContainerConnectionConfig
	ctx := context.Background()

	// Start zookeeper first
	zkC, zkConfig, err := StartZookeeperContainer(options)
	if err != nil {
		return nil, nil, config, fmt.Errorf("Failed to start zookeeper container: %v", err)
	}

	kafkaPort, err := nat.NewPort("", "9093")
	// kafkaPort, err := nat.NewPort("", "9092")
	if err != nil {
		return nil, nil, config, err
	}

	starterScript := "/testcontainers_start.sh"
	/*
		kafkaPortInside, err := nat.NewPort("", "9092")
		if err != nil {
			return nil, nil, config, err
		}
	*/
	// getFreePort
	req := testcontainers.ContainerRequest{
		// Image: "wurstmeister/kafka",
		Image: "confluentinc/cp-kafka:5.2.1",
		// Cmd:   []string{"tail", "-f", "/dev/null"},
		Cmd: []string{"/bin/bash", "-c", fmt.Sprintf("while [ ! -f %s ]; do sleep 0.1; done; cat %s && %s", starterScript, starterScript, starterScript)},
		Env: map[string]string{

			"KAFKA_LISTENERS":                        fmt.Sprintf("PLAINTEXT://0.0.0.0:%d,BROKER://0.0.0.0:9092", kafkaPort.Int()),
			"KAFKA_LISTENER_SECURITY_PROTOCOL_MAP":   "BROKER:PLAINTEXT,PLAINTEXT:PLAINTEXT",
			"KAFKA_INTER_BROKER_LISTENER_NAME":       "BROKER",
			"KAFKA_BROKER_ID":                        "1",
			"KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR": "1",
			"KAFKA_OFFSETS_TOPIC_NUM_PARTITIONS":     "1",
			// "KAFKA_LOG_FLUSH_INTERVAL_MESSAGES":      fmt.Sprintf("%d", int(^uint(0)>>1)),
			"KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS": "0",

			// "KAFKA_DELETE_TOPIC_ENABLE":  "true",
			// "KAFKA_ADVERTISED_HOST_NAME": "kafka",
			// "KAFKA_ADVERTISED_PORT":      "9092",
			// "HOSTNAME_COMMAND":        "route -n | awk '/UG[ \t]/{print $$2}'",
			// "PORT_COMMAND":            "docker port $$(hostname) 9092/tcp | cut -d: -f2",
			// "KAFKA_ZOOKEEPER_CONNECT": fmt.Sprintf("%s:%d", zkConfig.Host, zkConfig.Port),
			// "KAFKA_ADVERTISED_LISTENERS":           "INSIDE://kafka:9092,OUTSIDE://localhost:29092",
			// "KAFKA_LISTENERS":                      "INSIDE://:9092,OUTSIDE://:29092",
		},
		ExposedPorts: []string{
			string(kafkaPort), // "29092",
			// string(kafkaPortInside), string(kafkaPortOutside),
		},
		BindMounts: map[string]string{
			"/var/run/docker.sock": "/var/run/docker.sock",
		},
		// WaitingFor: wait.ForListeningPort("9092/tcp"),
		// WaitingFor: wait.ForLog("started (kafka.server.KafkaServer)"),
	}
	if options.Network != "" {
		req.Networks = []string{
			options.Network,
		}

		req.NetworkAliases = map[string][]string{
			options.Network: []string{"kafka"},
		}

	}
	/*
		p, err := testcontainers.NewDockerProvider()
		if err != nil {
			return nil, nil, config, err
		}
	*/
	// kafkaC, err := p.CreateContainer(ctx, req)

	kafkaC, err := testcontainers.GenericContainer(ctx, testcontainers.GenericContainerRequest{
		ContainerRequest: req,
		Started:          true,
	})

	// start-kafka.sh

	if err != nil {
		return nil, nil, config, err
	}
	host, err := kafkaC.Host(ctx)
	if err != nil {
		return nil, nil, config, err
	}

	var port nat.Port
	/*
		if brokerPort == Inside {
			port, err = kafkaC.MappedPort(ctx, kafkaPortInside)
			if err != nil {
				return nil, nil, config, err
			}
		} else {
			port, err = kafkaC.MappedPort(ctx, kafkaPortOutside)
			if err != nil {
				return nil, nil, config, err
			}
		}
	*/

	port, err = kafkaC.MappedPort(ctx, kafkaPort)
	if err != nil {
		return nil, nil, config, err
	}

	bootstrapServer := fmt.Sprintf("PLAINTEXT://%s:%d", host, port.Int())
	listeners := []string{bootstrapServer}

	// Get docker client
	client, err := dockerclient.NewClientWithOpts(dockerclient.FromEnv, dockerclient.WithAPIVersionNegotiation())
	if err != nil {
		return nil, nil, config, err
	}
	inspect, err := client.ContainerInspect(context.Background(), kafkaC.GetContainerID())
	if err != nil {
		return nil, nil, config, err
	}
	/*
		networks, err := kafkaC.Networks(ctx)
		if err != nil {
			return nil, nil, config, err
		}
		for _, net := range networks {
			if endpoint, ok := inspect.NetworkSettings.Networks[net]; ok {
				listeners = append(listeners, fmt.Sprintf("INSIDE://%s", endpoint.IPAddress))
			}
		}
	*/
	for _, endpoint := range inspect.NetworkSettings.Networks {
		listeners = append(listeners, fmt.Sprintf("BROKER://%s:9092", endpoint.IPAddress))
	}

	// bootstrapServer := fmt.Sprintf("OUTSIDE://%s", endpoint.IPAddress)

	// networks := []string{bootstrapServer}
	// Networks -> GetNetwork -> NetworkResource ->
	// Want: NetworkSettings -> Networks -> EndpointSettings -> IPAddress
	/*
		for _, net := range req.Networks {
			// networkInfo := p.GetNetwork(ctx, testcontainers.NetworkRequest{Name: net})
			networks = append(networks, fmt.Sprintf("BROKER://%s:9092", ))
		}
	*/

	// Build script
	script := "#!/bin/bash \\n"
	script += fmt.Sprintf("export KAFKA_ZOOKEEPER_CONNECT=\"%s:%d\" && ", zkConfig.Host, zkConfig.Port)
	script += fmt.Sprintf("export KAFKA_ADVERTISED_LISTENERS=\"%s\" && ", strings.Join(listeners, ","))
	script += ". /etc/confluent/docker/bash-config && "
	script += "/etc/confluent/docker/configure && "
	script += "/etc/confluent/docker/launch"

	/*
		script := "#!/bin/bash \\n"
		// script += fmt.Sprintf("export KAFKA_ADVERTISED_HOST_NAME=\"%s\" && ", ip)
		// script += "export KAFKA_ADVERTISED_HOST_NAME=\"localhost\" && "
		// script += fmt.Sprintf("export KAFKA_ADVERTISED_PORT=\"%d\" && ", port.Int())
		// script += "export KAFKA_ADVERTISED_PORT=\"9092\" && "
		script += fmt.Sprintf("export KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=\"INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT\" && ")
		// script += "export KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=\"INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT\" && "
		script += fmt.Sprintf("export KAFKA_INTER_BROKER_LISTENER_NAME=\"INSIDE\" && ")
		// script += "export KAFKA_INTER_BROKER_LISTENER_NAME=\"INSIDE\" && "
		// script += fmt.Sprintf("export KAFKA_ADVERTISED_LISTENERS=\"INSIDE://kafka:9092,OUTSIDE://%s:%d\" && ", ip, port.Int())
		script += fmt.Sprintf("export KAFKA_ADVERTISED_LISTENERS=\"%s\" && ", strings.Join(listeners, ","))
		// script += "export KAFKA_ADVERTISED_LISTENERS=\"INSIDE://kafka:9092,OUTSIDE://localhost:29092\" && "
		script += fmt.Sprintf("export KAFKA_LISTENERS=\"INSIDE://0.0.0.0:9092,OUTSIDE://0.0.0.0:%d\" && ", port.Int())
		// script += "export KAFKA_LISTENERS=\"INSIDE://:9092,OUTSIDE://:29092\" && "
		script += "start-kafka.sh"
	*/

	/*
		script := "#!/bin/bash \\n"
		script += "export KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=\"INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT\" && "
		script += fmt.Sprintf("export KAFKA_LISTENERS=\"INSIDE://:19092,OUTSIDE://:%d\" && ", port.Int())
		script += fmt.Sprintf("export KAFKA_ADVERTISED_LISTENERS=\"INSIDE://kafka:19092,%s\" && ", strings.Join(listeners, ","))
		script += "start-kafka.sh"
	*/

	// exitCode, err := kafkaC.Exec(ctx, []string{fmt.Sprintf("KAFKA_ADVERTISED_HOST_NAME=%s KAFKA_ADVERTISED_PORT=%d start-kafka.sh", ip, port.Int())})
	cmd := []string{"/bin/bash", "-c", fmt.Sprintf("printf '%s' > %s && chmod 700 %s", script, starterScript, starterScript)}
	// cmd := []string{"/bin/bash", "-c", fmt.Sprintf("printf '%s' > %s && chmod 700 %s && iconv -f ISO-8859-1 -t UTF-8 %s -o %s", script, starterScript, starterScript, starterScript, starterScript)}

	fmt.Println(cmd)
	// log.Fatal(cmd)

	exitCode, err := kafkaC.Exec(ctx, cmd)
	if err != nil {
		return nil, nil, config, err
	}

	if exitCode != 0 {
		return nil, nil, config, fmt.Errorf("Failed with code %d", exitCode)
	}

	config = KafkaContainerConnectionConfig{
		Brokers: []string{fmt.Sprintf("%s:%d", host, port.Int())},
		// Brokers: []string{bootstrapServer},
		ConnectionTolerance: libcli.ConnectionTolerance{
			TimeoutSec: 20,
		},
	}

	if options.Log || true {
		config.log = &LogCollector{}

		err := kafkaC.StartLogProducer(ctx)
		if err != nil {
			return kafkaC, zkC, config, fmt.Errorf("Failed to start log producer: %v", err)
		}

		kafkaC.FollowOutput(config.log)
		// User must call StopLogProducer() himself

		go func() {
			for {
				time.Sleep(1 * time.Second)
				/*
					for _, m := range config.log.Messages {
						fmt.Println(m)
					}
				*/
				config.log.Messages = []string{}
			}
		}()
	}

	// log.Fatal(config)

	return kafkaC, zkC, config, nil
}
