package testing

import (
	"context"
	"fmt"

	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	"github.com/bptlab/cepta/osiris/lib/kafka/consumer"
	"github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/docker/go-connections/nat"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/wait"
)

// KafkaConnectionOptions ...
type KafkaConnectionOptions interface {
	GetBrokers() []string
	GetConnectionTolerance() libcli.ConnectionTolerance
}

// KafkaContainerConnectionConfig ...
type KafkaContainerConnectionConfig struct {
	Brokers             []string
	ConnectionTolerance libcli.ConnectionTolerance
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
	}
}

// StartKafkaContainer ...
func StartKafkaContainer() (testcontainers.Container, KafkaContainerConnectionConfig, error) {
	var config KafkaContainerConnectionConfig
	ctx := context.Background()
	kafkaPort, err := nat.NewPort("", "9092")
	if err != nil {
		return nil, config, err
	}
	req := testcontainers.ContainerRequest{
		Image: "wurstmeister/kafka",
		Env: map[string]string{
			"KAFKA_DELETE_TOPIC_ENABLE":            "true",
			"KAFKA_ADVERTISED_HOST_NAME":           "kafka",
			"KAFKA_ADVERTISED_PORT":                "9092",
			"KAFKA_ZOOKEEPER_CONNECT":              "zookeeper:${CEPTA_ZOOKEEPER_PORT}",
			"KAFKA_ADVERTISED_LISTENERS":           "INSIDE://kafka:9092,OUTSIDE://localhost:29092",
			"KAFKA_LISTENERS":                      "INSIDE://:9092,OUTSIDE://:29092",
			"KAFKA_LISTENER_SECURITY_PROTOCOL_MAP": "INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT",
			"KAFKA_INTER_BROKER_LISTENER_NAME":     "INSIDE",
		},
		ExposedPorts: []string{
			string(kafkaPort),
		},
		WaitingFor: wait.ForLog("waiting for connections on port"),
	}
	kafkaC, err := testcontainers.GenericContainer(ctx, testcontainers.GenericContainerRequest{
		ContainerRequest: req,
		Started:          true,
	})
	if err != nil {
		return nil, config, err
	}
	ip, err := kafkaC.Host(ctx)
	if err != nil {
		return nil, config, err
	}
	port, err := kafkaC.MappedPort(ctx, kafkaPort)
	if err != nil {
		return nil, config, err
	}

	config = KafkaContainerConnectionConfig{
		Brokers: []string{fmt.Sprintf("%s:%d", ip, port.Int())},
		ConnectionTolerance: libcli.ConnectionTolerance{
			TimeoutSec: 20,
		},
	}

	return kafkaC, config, nil
}
