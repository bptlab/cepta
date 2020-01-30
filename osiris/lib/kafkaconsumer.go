package kafkaconsumer

import (
	"os/signal"
	"strings"
	"context"
	"sync"
	"os"
	"syscall"

	"github.com/urfave/cli/v2"
	log "github.com/sirupsen/logrus"
	"github.com/Shopify/sarama"
)

var KafkaConsumerCliOptions = []cli.Flag{
	&cli.StringFlag{
		Name: "kafka-brokers",
		Value: "localhost:29092",
		Aliases: []string{"brokers"},
		EnvVars: []string{"KAFKA_BROKERS", "BROKERS"},
		Usage: "Kafka bootstrap brokers to connect to, as a comma separated list",
	},
	&cli.StringFlag{
		Name: "kafka-group",
		// Value: "",
		Aliases: []string{"group"},
		EnvVars: []string{"KAFKA_GROUP", "GROUP"},
		Usage: "Kafka consumer group definition",
	},
	&cli.StringFlag{
		Name: "kafka-version",
		Value: "2.1.1",
		Aliases: []string{"kafka"},
		EnvVars: []string{"KAFKA_VERSION", "KAFKA"},
		Usage: "Kafka cluster version",
	},
	&cli.StringFlag{
		Name: "kafka-topics",
		Value: "news_for_leo",
		Aliases: []string{"topics"},
		EnvVars: []string{"TOPICS", "KAFKA_TOPICS"},
		Usage: "Kafka topics to be consumed, as a comma seperated list",
	},
}

type KafkaConsumerOptions struct {
	Brokers 	[]string
	Group		string
	Version		string
	Topics		[]string
}

func (config KafkaConsumerOptions) ParseCli(ctx *cli.Context) KafkaConsumerOptions {
	return KafkaConsumerOptions{
		Brokers: strings.Split(ctx.String("kafka-brokers"), ","),
		Group: ctx.String("kafka-group"),
		Version: ctx.String("kafka-version"),
		Topics: strings.Split(ctx.String("kafka-topics"), ","),
	}
}

type KafkaConsumer struct {
	ready chan bool
}

func (consumer *KafkaConsumer) Cleanup(sarama.ConsumerGroupSession) error {
	return nil
}

func (consumer *KafkaConsumer) ConsumeClaim(session sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {
	for message := range claim.Messages() {
		log.Debugf("Message claimed: value = %s, timestamp = %v, topic = %s", string(message.Value), message.Timestamp, message.Topic)
		session.MarkMessage(message, "")
	}
	return nil
}

func (consumer *KafkaConsumer) Setup(sarama.ConsumerGroupSession) error {
	close(consumer.ready)
	return nil
}

func consumeKafka(options KafkaConsumerOptions) error {
	config := sarama.NewConfig()
	version, err := sarama.ParseKafkaVersion(options.Version)
	if err != nil {
		log.Panicf("Error parsing Kafka version: %v", err)
	}
	config.Version = version
	
	consumer := KafkaConsumer{
		ready: make(chan bool),
	}

	ctx, cancel := context.WithCancel(context.Background())
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

	sigterm := make(chan os.Signal, 1)
	signal.Notify(sigterm, syscall.SIGINT, syscall.SIGTERM)
	select {
	case <-ctx.Done():
		log.Info("terminating: context cancelled")
	case <-sigterm:
		log.Info("terminating: via signal")
	}
	cancel()
	wg.Wait()
	if err = client.Close(); err != nil {
		log.Panicf("Error closing client: %v", err)
	}
	return nil
}