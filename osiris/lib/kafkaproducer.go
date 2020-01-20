package kafkaproducer

import (
	"time"
	"strings"
	"github.com/urfave/cli/v2"
	log "github.com/sirupsen/logrus"
	"github.com/Shopify/sarama"
)

var KafkaProducerCliOptions = []cli.Flag{
	&cli.StringFlag{
		Name: "kafka-brokers",
		Value: "localhost:29092",
		Aliases: []string{"brokers"},
		EnvVars: []string{"KAFKA_BROKERS", "BROKERS"},
		Usage: "Kafka bootstrap brokers to connect to, as a comma separated list",
	},
}

type KafkaProducerOptions struct {
	Brokers 	[]string
}

func (config KafkaProducerOptions) ParseCli(ctx *cli.Context) KafkaProducerOptions {
	return KafkaProducerOptions{
		Brokers: strings.Split(ctx.String("kafka-brokers"), ","),
	}
}

/*
type Message struct {
	Name	string
	Age		int32
}

func (m *Message) Length() int {
	encoded, _ := m.Encode()
	return len(encoded)
}

func (m *Message) Encode() ([]byte, error) {
	encoded, err := json.Marshal(m)
	return encoded, err
}
*/

/*
func WrappedProto struct {
	wrapped		string
}

func (m *Message) Length() int {
	encoded, _ := m.Encode()
	return len(encoded)
}

func (m *Message) Encode() ([]byte, error) {
	encoded, err := json.Marshal(m)
	return encoded, err
}
*/



type KafkaProducer struct {
	DataCollector     sarama.SyncProducer
	AccessLogProducer sarama.AsyncProducer
}

func (p KafkaProducer) ForBroker(brokerList []string) *KafkaProducer {
	return &KafkaProducer{
		DataCollector:     newDataCollector(brokerList),
		AccessLogProducer: newAccessLogProducer(brokerList),
	}
}

func newDataCollector(brokerList []string) sarama.SyncProducer {
	config := sarama.NewConfig()
	config.Producer.RequiredAcks = sarama.WaitForAll // Wait for all in-sync replicas to ack the message
	config.Producer.Retry.Max = 10                   // Retry up to 10 times to produce the message
	config.Producer.Return.Successes = true
	producer, err := sarama.NewSyncProducer(brokerList, config)
	if err != nil {
		log.Fatalf("Failed to start Sarama producer:", err)
	}

	return producer
}

func newAccessLogProducer(brokerList []string) sarama.AsyncProducer {
	config := sarama.NewConfig()
	config.Producer.RequiredAcks = sarama.WaitForLocal       // Only wait for the leader to ack
	config.Producer.Compression = sarama.CompressionSnappy   // Compress messages
	config.Producer.Flush.Frequency = 500 * time.Millisecond // Flush batches every 500ms

	producer, err := sarama.NewAsyncProducer(brokerList, config)
	if err != nil {
		log.Fatalf("Failed to start Sarama producer:", err)
	}
	go func() {
		for err := range producer.Errors() {
			log.Warnf("Failed to write access log entry:", err)
		}
	}()
	return producer
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