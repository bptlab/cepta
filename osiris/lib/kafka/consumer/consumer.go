package Consumer

import (
	"context"
	"errors"
	"fmt"
	"strings"
	"sync"
	"time"

	"github.com/Shopify/sarama"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	"github.com/bptlab/cepta/osiris/lib/kafka"
	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
)

// CliOptions ...
var CliOptions = libcli.CommonCliOptions(libcli.Kafka)

// Config ...
type Config struct {
	kafka.Config
	Group  string
	Topics []string
}

// ParseCli ...
func (config Config) ParseCli(ctx *cli.Context) Config {
	return Config{
		Config: kafka.Config{}.ParseCli(ctx),
		Group:  ctx.String("kafka-group"),
		Topics: strings.Split(ctx.String("kafka-topics"), ","),
	}
}

// Consumer ...
type Consumer struct {
	ready    chan error
	Messages chan *sarama.ConsumerMessage
	client   sarama.ConsumerGroup
}

// Cleanup ...
func (consumer *Consumer) Cleanup(sarama.ConsumerGroupSession) error {
	return nil
}

// ConsumeClaim ...
func (consumer *Consumer) ConsumeClaim(session sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {
	for message := range claim.Messages() {
		log.Debugf("Message claimed: value = %s, timestamp = %v, topic = %s", string(message.Value), message.Timestamp, message.Topic)
		consumer.Messages <- message
		session.MarkMessage(message, "")
	}
	return nil
}

// Setup ...
func (consumer *Consumer) Setup(sarama.ConsumerGroupSession) error {
	consumer.ready <- nil
	return nil
}

// Close ...
func (consumer *Consumer) Close() error {
	return consumer.client.Close()
}

func newConsumerGroup(options Config, config *sarama.Config) (sarama.ConsumerGroup, error) {
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

// ConsumeGroup ...
func ConsumeGroup(ctx context.Context, options Config) (*Consumer, *sync.WaitGroup, error) {
	config := sarama.NewConfig()
	// config.Consumer.Return.Errors = true

	c := &Consumer{
		ready:    make(chan error, 2),
		Messages: make(chan *sarama.ConsumerMessage),
	}

	version, err := sarama.ParseKafkaVersion(options.Version)
	if err != nil {
		return c, nil, fmt.Errorf("Error parsing Kafka version: %v", err)
	}
	config.Version = version

	c.client, err = newConsumerGroup(options, config)
	if err != nil {
		return c, nil, fmt.Errorf("Error creating consumer group client: %v", err)
	}

	wg := &sync.WaitGroup{}
	wg.Add(1)
	go func() {
		defer wg.Done()
		for {
			// Consume should be called inside an infinite loop, when a
			// server-side rebalance happens, the consumer session will need to be
			// recreated to get the new claims
			if err := c.client.Consume(ctx, options.Topics, c); err != nil {
				c.ready <- err
			}
			// check if context was cancelled, signaling that the consumer should stop
			if ctx.Err() != nil {
				log.Debug("Context canceled")
				c.ready <- err
				break
			}
			c.ready = make(chan error, 2)
		}
		log.Debug("Consumer loop exiting")
	}()

	// Wait until the consumer has been set up
	if err = <-c.ready; err != nil {
		return c, wg, fmt.Errorf("Error setting up consumer: %v", err)
	}

	log.Debug("Sarama consumer up and running!...")
	return c, wg, nil
}

func newConsumer(options Config, config *sarama.Config) (sarama.Consumer, error) {
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

// Consume ...
func Consume(ctx context.Context, options Config) (sarama.PartitionConsumer, error) {
	config := sarama.NewConfig()
	version, err := sarama.ParseKafkaVersion(options.Version)
	if err != nil {
		return nil, fmt.Errorf("Error parsing Kafka version: %s", err.Error())
	}
	config.Version = version

	if len(options.Topics) != 1 {
		return nil, errors.New("Need exactly one topic")
	}

	consumer := Consumer{
		ready: make(chan error),
	}

	client, err := newConsumer(options, config)
	if err != nil {
		return nil, fmt.Errorf("Error creating consumer group client: %s", err.Error())
	}

	var Consumer sarama.PartitionConsumer
	wg := &sync.WaitGroup{}
	wg.Add(1)
	go func() {
		defer wg.Done()
		for {
			if Consumer, err = client.ConsumePartition(options.Topics[0], 0, sarama.OffsetOldest); err != nil {
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
	return Consumer, nil
}
