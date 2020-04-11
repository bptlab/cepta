package kafkaconsumer

import (
	"context"
	"fmt"
	"strings"
	"sync"

	"github.com/Shopify/sarama"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
)

var KafkaConsumerCliOptions = libcli.CommonCliOptions(libcli.Kafka)

type KafkaConsumerOptions struct {
	Brokers []string
	Group   string
	Version string
	Topics  []string
}

func (config KafkaConsumerOptions) ParseCli(ctx *cli.Context) KafkaConsumerOptions {
	return KafkaConsumerOptions{
		Brokers: strings.Split(ctx.String("kafka-brokers"), ","),
		Group:   ctx.String("kafka-group"),
		Version: ctx.String("kafka-version"),
		Topics:  strings.Split(ctx.String("kafka-topics"), ","),
	}
}

type KafkaConsumer struct {
	ready    chan bool
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

func ConsumeKafkaGroup(ctx context.Context, options KafkaConsumerOptions) (KafkaConsumer, error) {
	config := sarama.NewConfig()
	version, err := sarama.ParseKafkaVersion(options.Version)
	if err != nil {
		log.Panicf("Error parsing Kafka version: %v", err)
	}
	config.Version = version

	consumer := KafkaConsumer{
		ready:    make(chan bool),
		Messages: make(chan *sarama.ConsumerMessage),
	}

	client, err := sarama.NewConsumerGroup(options.Brokers, options.Group, config)
	if err != nil {
		log.Panicf("Error creating consumer group client: %v", err)
	}

	wg := &sync.WaitGroup{}
	wg.Add(1)
	go func() {
		defer wg.Done()
		for {
			if err := client.Consume(ctx, options.Topics, &consumer); err != nil {
				log.Panicf("Error from consumer: %v", err)
			}
			// check if context was cancelled, signaling that the consumer should stop
			if ctx.Err() != nil {
				return
			}
			consumer.ready = make(chan bool)
		}
	}()

	<-consumer.ready // Await till the consumer has been set up
	log.Debug("Sarama consumer up and running!...")
	return consumer, err
}

func ConsumeKafka(ctx context.Context, options KafkaConsumerOptions) (sarama.PartitionConsumer, error) {
	config := sarama.NewConfig()
	version, err := sarama.ParseKafkaVersion(options.Version)
	if err != nil {
		log.Panicf("Error parsing Kafka version: %v", err)
	}
	config.Version = version

	if len(options.Topics) != 1 {
		return nil, fmt.Errorf("Need exactly one topic!")
	}

	consumer := KafkaConsumer{
		ready: make(chan bool),
	}

	client, err := sarama.NewConsumer(options.Brokers, config)
	if err != nil {
		log.Panicf("Error creating consumer group client: %v", err)
	}

	var kafkaConsumer sarama.PartitionConsumer
	wg := &sync.WaitGroup{}
	wg.Add(1)
	go func() {
		defer wg.Done()
		for {
			if kafkaConsumer, err = client.ConsumePartition(options.Topics[0], 0, sarama.OffsetOldest); err != nil {
				log.Panicf("Error from consumer: %v", err)
			}
			// check if context was cancelled, signaling that the consumer should stop
			if ctx.Err() != nil {
				return
			}
			consumer.ready = make(chan bool)
		}
	}()

	<-consumer.ready // Await till the consumer has been set up
	log.Debug("Sarama consumer up and running!...")
	return kafkaConsumer, err
}
