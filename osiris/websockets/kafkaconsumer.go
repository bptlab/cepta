package main

import (
	"encoding/json"
	"github.com/Shopify/sarama"
	"time"
	"websockets/websocket"
)

func connectKafkaConsumer(pool *websocket.Pool) {

	config := sarama.NewConfig()
	config.Consumer.Return.Errors = true

	// Specify brokers address
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
	
	consumer, err := master.ConsumePartition(topic, 0, sarama.OffsetOldest)
	if err != nil {
		panic(err)
	}

	// start consumer, emit to ws
	noopTicker := time.NewTicker(time.Second * 5)
	subscriberDone := make(chan bool, 1)
	stopSubscriber := make(chan bool, 1)

	go func() {
		defer func() { subscriberDone <- true }()

		for {
			select {
			case msg := <-consumer.Messages():
				payload := msg.Value

				// Converting []byte payload to JSON
				var message Message
				json.Unmarshal(payload, &message)

				for client, _ := range pool.Clients {
					if client.ID == message.UID {
						client.Conn.WriteJSON(websocket.Message{Type: 4, Body: string(payload)})
					}
				}
				break

			case <-noopTicker.C:
				message := websocket.Message{Type: 2, Body: "ping"}
				pool.Broadcast <- message
			case <-stopSubscriber:
				return
			}
		}
	}()
	<-subscriberDone
	noopTicker.Stop()
}