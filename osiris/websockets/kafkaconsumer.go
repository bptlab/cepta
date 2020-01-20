package main

import (
	"fmt"

	"github.com/Shopify/sarama"
)

func connectKafkaConsumer() (sarama.PartitionConsumer) {
	config := sarama.NewConfig()
	config.Consumer.Return.Errors = true

	// Specify brokers address. This is default one
	brokers := []string{"localhost:29092"}

	// Create new consumer
	master, err := sarama.NewConsumer(brokers, config)
	if err != nil {
		panic(err)
	}

	defer func() {
		if err := master.Close(); err != nil {
			panic(err)
		}
	}()

	topic := "news_for_leo"
	// How to decide partition, is it fixed value...?
	consumer, err := master.ConsumePartition(topic, 0, sarama.OffsetOldest)
	if err != nil {
		panic(err)
	}

	return consumer
}

func writeOutput(consumer sarama.PartitionConsumer) (msg string) {
	var message string = ""

	select {
	case err := <-consumer.Errors():
		fmt.Println(err)
	case msg := <-consumer.Messages():
		message = string(msg.Value)
		fmt.Println("Received messages", string(msg.Key), string(msg.Value))
	}

	return message
}
