package kafkaproducer

import (
	"context"
	"fmt"
	"strings"
	"time"

	"github.com/Shopify/sarama"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
)

var KafkaProducerCliOptions = libcli.CommonCliOptions(libcli.KafkaBroker)

type KafkaProducerConfig struct {
	Brokers             []string
	ConnectionTolerance libcli.ConnectionTolerance
}

func (config KafkaProducerConfig) GetBrokers() []string {
	return config.Brokers
}

func (config KafkaProducerConfig) GetConnectionTolerance() libcli.ConnectionTolerance {
	return config.ConnectionTolerance
}

func (config KafkaProducerConfig) ParseCli(ctx *cli.Context) KafkaProducerConfig {
	return KafkaProducerConfig{
		Brokers:             strings.Split(ctx.String("kafka-brokers"), ","),
		ConnectionTolerance: libcli.ConnectionTolerance{}.ParseCli(ctx),
	}
}

type KafkaProducer struct {
	DataCollector     sarama.SyncProducer
	AccessLogProducer sarama.AsyncProducer
}

func (p KafkaProducer) forBroker(brokerList []string) (*KafkaProducer, error) {
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

func (p KafkaProducer) Create(ctx context.Context, options KafkaProducerConfig) (*KafkaProducer, error) {
	var attempt int
	if options.ConnectionTolerance.MaxRetries < 1 && options.ConnectionTolerance.TimeoutSec > 0 {
		options.ConnectionTolerance.MaxRetries = options.ConnectionTolerance.TimeoutSec
		options.ConnectionTolerance.RetryIntervalSec = 1
	}
	for {
		select {
		case <-ctx.Done():
			return nil, fmt.Errorf("Failed to start kafka producer after %d attempts", attempt)
		default:
			producer, err := p.forBroker(options.Brokers)
			if err != nil {
				if attempt >= options.ConnectionTolerance.MaxRetries {
					return nil, fmt.Errorf("Failed to start kafka producer: %v", err)
				}
				attempt++
				log.Infof("Failed to connect: %v. (Attempt %d of %d)", err, attempt, options.ConnectionTolerance.MaxRetries)
				time.Sleep(time.Duration(options.ConnectionTolerance.RetryIntervalSec) * time.Second)
			} else {
				return producer, nil
			}
		}
	}
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
