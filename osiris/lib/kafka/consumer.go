package consumer

import (
	"context"
	"fmt"
	"strings"
	"sync"
	"time"

	"github.com/Shopify/sarama"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
)

var KafkaConsumerCliOptions = libcli.CommonCliOptions(libcli.Kafka)

type KafkaConsumerOptions struct {
	Brokers             []string
	Group               string
	Version             string
	Topics              []string
	ConnectionTolerance libcli.ConnectionTolerance
}

func (config KafkaConsumerOptions) GetBrokers() []string {
	return config.Brokers
}

func (config KafkaConsumerOptions) GetConnectionTolerance() libcli.ConnectionTolerance {
	return config.ConnectionTolerance
}

func (config KafkaConsumerOptions) ParseCli(ctx *cli.Context) KafkaConsumerOptions {
	return KafkaConsumerOptions{
		Brokers:             strings.Split(ctx.String("kafka-brokers"), ","),
		Group:               ctx.String("kafka-group"),
		Version:             ctx.String("kafka-version"),
		Topics:              strings.Split(ctx.String("kafka-topics"), ","),
		ConnectionTolerance: libcli.ConnectionTolerance{}.ParseCli(ctx),
	}
}

type KafkaConsumer struct {
	ready    chan error
	Messages chan *sarama.ConsumerMessage
}

func (consumer *KafkaConsumer) Cleanup(sarama.ConsumerGroupSession) error {
	return nil
}

func (consumer *KafkaConsumer) ConsumeClaim(session sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {
	for message := range claim.Messages() {
		log.Debugf("Message claimed: value = %s, timestamp = %v, topic = %s", string(message.Value), message.Timestamp, message.Topic)
		consumer.Messages <- message
		session.MarkMessage(message, "")
	}
	return nil
}

func (consumer *KafkaConsumer) Setup(sarama.ConsumerGroupSession) error {
	close(consumer.ready)
	return nil
}

func newConsumerGroup(options KafkaConsumerOptions, config *sarama.Config) (sarama.ConsumerGroup, error) {
	var attempt int
	for {
		client, err := sarama.NewConsumerGroup(options.Brokers, options.Group, config)
		if err != nil {
			if attempt >= options.ConnectionTolerance.MaxRetries {
				return nil, fmt.Errorf("Failed to start kafka consumer: %s", err.Error())
			}
			attempt++
			log.Infof("Failed to connect: %s. (Attempt %d of %d)", err.Error(), attempt, options.ConnectionTolerance.MaxRetries)
			time.Sleep(time.Duration(options.ConnectionTolerance.RetryIntervalSec) * time.Second)
			continue
		}
		return client, nil
	}
}

func (c KafkaConsumer) ConsumeGroup(ctx context.Context, options KafkaConsumerOptions) (KafkaConsumer, error) {
	config := sarama.NewConfig()

	consumer := KafkaConsumer{
		ready:    make(chan error),
		Messages: make(chan *sarama.ConsumerMessage),
	}

	version, err := sarama.ParseKafkaVersion(options.Version)
	if err != nil {
		return consumer, fmt.Errorf("Error parsing Kafka version: %s", err.Error())
	}
	config.Version = version

	client, err := newConsumerGroup(options, config)
	if err != nil {
		return consumer, fmt.Errorf("Error creating consumer group client: %s", err.Error())
	}

	wg := &sync.WaitGroup{}
	wg.Add(1)
	go func() {
		defer wg.Done()
		for {
			if err := client.Consume(ctx, options.Topics, &consumer); err != nil {
				consumer.ready <- err
			}
			// check if context was cancelled, signaling that the consumer should stop
			if err := ctx.Err(); err != nil {
				consumer.ready <- err
			}
			// All set up
			consumer.ready <- nil
		}
	}()

	// Wait until the consumer has been set up
	if err := <-consumer.ready; err != nil {
		return consumer, fmt.Errorf("Error setting up consumer: %s", err.Error())
	}
	log.Debug("Sarama consumer up and running!...")
	return consumer, nil
}

func newConsumer(options KafkaConsumerOptions, config *sarama.Config) (sarama.Consumer, error) {
	var attempt int
	for {
		client, err := sarama.NewConsumer(options.Brokers, config)
		if err != nil {
			if attempt >= options.ConnectionTolerance.MaxRetries {
				return nil, fmt.Errorf("Failed to start kafka consumer: %s", err.Error())
			}
			attempt++
			log.Infof("Failed to connect: %s. (Attempt %d of %d)", err.Error(), attempt, options.ConnectionTolerance.MaxRetries)
			time.Sleep(time.Duration(options.ConnectionTolerance.RetryIntervalSec) * time.Second)
			continue
		}
		return client, nil
	}
}

func (c KafkaConsumer) Consume(ctx context.Context, options KafkaConsumerOptions) (sarama.PartitionConsumer, error) {
	config := sarama.NewConfig()
	version, err := sarama.ParseKafkaVersion(options.Version)
	if err != nil {
		return nil, fmt.Errorf("Error parsing Kafka version: %s", err.Error())
	}
	config.Version = version

	if len(options.Topics) != 1 {
		return nil, fmt.Errorf("Need exactly one topic!")
	}

	consumer := KafkaConsumer{
		ready: make(chan error),
	}

	client, err := newConsumer(options, config)
	if err != nil {
		return nil, fmt.Errorf("Error creating consumer group client: %s", err.Error())
	}

	var kafkaConsumer sarama.PartitionConsumer
	wg := &sync.WaitGroup{}
	wg.Add(1)
	go func() {
		defer wg.Done()
		for {
			if kafkaConsumer, err = client.ConsumePartition(options.Topics[0], 0, sarama.OffsetOldest); err != nil {
				consumer.ready <- err
			}
			// check if context was cancelled, signaling that the consumer should stop
			if err := ctx.Err(); err != nil {
				consumer.ready <- err
			}
			consumer.ready <- nil
		}
	}()

	// Wait until the consumer has been set up
	if err := <-consumer.ready; err != nil {
		return nil, fmt.Errorf("Error setting up consumer: %s", err.Error())
	}
	log.Debug("Sarama consumer up and running!...")
	return kafkaConsumer, nil
}
