package main

import (
  "time"
  "encoding/json"
  log "github.com/sirupsen/logrus"
  "github.com/Shopify/sarama"
)

type Message struct {
  Name  string
  Age   time.Time
}

func (m *Message) Length() int {
  encoded, _ := m.Encode()
  return len(encoded)
}

func (m *Message) Encode() ([]byte, error) {
  encoded, err := json.Marshal(m)
  return encoded, err
}

type Server struct {
  DataCollector     sarama.SyncProducer
  AccessLogProducer sarama.AsyncProducer
}

func newDataCollector(brokerList []string) sarama.SyncProducer {

  // For the data collector, we are looking for strong consistency semantics.
  // Because we don't change the flush settings, sarama will try to produce messages
  // as fast as possible to keep latency low.
  config := sarama.NewConfig()
  config.Producer.RequiredAcks = sarama.WaitForAll // Wait for all in-sync replicas to ack the message
  config.Producer.Retry.Max = 10                   // Retry up to 10 times to produce the message
  config.Producer.Return.Successes = true
  /*
  tlsConfig := createTlsConfiguration()
  if tlsConfig != nil {
    config.Net.TLS.Config = tlsConfig
    config.Net.TLS.Enable = true
  }
  */

  // On the broker side, you may want to change the following settings to get
  // stronger consistency guarantees:
  // - For your broker, set unclean.leader.election.enable to false
  // - For the topic, you could increase min.insync.replicas.

  producer, err := sarama.NewSyncProducer(brokerList, config)
  if err != nil {
    log.Fatalln("Failed to start Sarama producer:", err)
  }

  return producer
}

func newAccessLogProducer(brokerList []string) sarama.AsyncProducer {

  // For the access log, we are looking for AP semantics, with high throughput.
  // By creating batches of compressed messages, we reduce network I/O at a cost of more latency.
  config := sarama.NewConfig()
  /*
  tlsConfig := createTlsConfiguration()
  if tlsConfig != nil {
    config.Net.TLS.Enable = true
    config.Net.TLS.Config = tlsConfig
  }
  */
  config.Producer.RequiredAcks = sarama.WaitForLocal       // Only wait for the leader to ack
  config.Producer.Compression = sarama.CompressionSnappy   // Compress messages
  config.Producer.Flush.Frequency = 500 * time.Millisecond // Flush batches every 500ms

  producer, err := sarama.NewAsyncProducer(brokerList, config)
  if err != nil {
    log.Fatalln("Failed to start Sarama producer:", err)
  }

  // We will just log to STDOUT if we're not able to produce messages.
  // Note: messages will only be returned here after all retry attempts are exhausted.
  go func() {
    for err := range producer.Errors() {
      log.Println("Failed to write access log entry:", err)
    }
  }()

  return producer
}

func (s *Server) Close() error {
  if err := s.DataCollector.Close(); err != nil {
    log.Println("Failed to shut down data collector cleanly", err)
  }

  if err := s.AccessLogProducer.Close(); err != nil {
    log.Println("Failed to shut down access log producer cleanly", err)
  }

  return nil
}

func (s *Server) Produce() error {
  log.Info("Producing")
  for {
    entry := &Message{
      Name:  "Romanski",
      Age:  time.Now(),
    }

    // We will use the client's IP address as key. This will cause
    // all the access log entries of the same IP address to end up
    // on the same partition.
    s.AccessLogProducer.Input() <- &sarama.ProducerMessage{
      Topic: "news_for_leo",
      Key:   sarama.StringEncoder("leo"),
      Value: entry,
    }
    log.Info("Produced a message and will sleep for 2 seconds")
    time.Sleep(2 * time.Second)
  }
  return nil
}

func main() {
  brokerList := []string{"localhost:29092"}
  server := &Server{
    DataCollector:     newDataCollector(brokerList),
    AccessLogProducer: newAccessLogProducer(brokerList),
  }
  defer func() {
    if err := server.Close(); err != nil {
      log.Println("Failed to close server", err)
    }
  }()
  log.Fatal(server.Produce())
}