package kafkaproducer

import (
	"strings"
	"time"

	"github.com/Shopify/sarama"
	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
)

var KafkaProducerCliOptions = libcli.CommonCliOptions(libcli.KafkaBroker)

type KafkaProducerOptions struct {
	Brokers []string
}

func (config KafkaProducerOptions) ParseCli(ctx *cli.Context) KafkaProducerOptions {
	return KafkaProducerOptions{
		Brokers: strings.Split(ctx.String("kafka-brokers"), ","),
	}
}

type KafkaProducer struct {
	DataCollector     sarama.SyncProducer
	AccessLogProducer sarama.AsyncProducer
}

func (p KafkaProducer) ForBroker(brokerList []string) (*KafkaProducer, error) {
	collector, err := newDataCollector(brokerList)
	if err != nil {
		return nil, err
	}
	producer, err := newAccessLogProducer(brokerList)
	if err != nil {
		return nil, err
	}
	return &KafkaProducer{
		DataCollector:     collector,
		AccessLogProducer: producer,
	}, nil
}

func newDataCollector(brokerList []string) (sarama.SyncProducer, error) {
	config := sarama.NewConfig()
	config.Producer.RequiredAcks = sarama.WaitForAll // Wait for all in-sync replicas to ack the message
	config.Producer.Retry.Max = 10                   // Retry up to 10 times to produce the message
	config.Producer.Return.Successes = true
	return sarama.NewSyncProducer(brokerList, config)
}

func newAccessLogProducer(brokerList []string) (sarama.AsyncProducer, error) {
	config := sarama.NewConfig()
	config.Producer.RequiredAcks = sarama.WaitForLocal       // Only wait for the leader to ack
	config.Producer.Compression = sarama.CompressionSnappy   // Compress messages
	config.Producer.Flush.Frequency = 500 * time.Millisecond // Flush batches every 500ms
	producer, err := sarama.NewAsyncProducer(brokerList, config)
	if err != nil {
		return nil, err
	}
	go func() {
		for err := range producer.Errors() {
			log.Warnf("Failed to write access log entry:", err)
		}
	}()
	return producer, err
}

func (s *KafkaProducer) Close() error {
	if err := s.DataCollector.Close(); err != nil {
		log.Fatalf("Failed to shut down data collector cleanly", err)
	}
	if err := s.AccessLogProducer.Close(); err != nil {
		log.Fatalf("Failed to shut down access log producer cleanly", err)
	}
	return nil
}

func (s *KafkaProducer) Send(topic string, pkey string, entry sarama.Encoder) {
	s.AccessLogProducer.Input() <- &sarama.ProducerMessage{
		Topic: topic,
		Key:   sarama.StringEncoder(pkey),
		Value: entry,
	}
}
