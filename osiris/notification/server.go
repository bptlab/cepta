package main

import (
	"context"
	"fmt"
	"net/http"
	"os"
	"strings"

	// "syscall"
	// "os/signal"
	// "encoding/json"
	"time"

	// "github.com/Shopify/sarama"
	"github.com/bptlab/cepta/ci/versioning"
	"github.com/bptlab/cepta/constants"
	delay "github.com/bptlab/cepta/models/events/traindelaynotificationevent"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	kafkaconsumer "github.com/bptlab/cepta/osiris/lib/kafka/consumer"
	"github.com/bptlab/cepta/osiris/notification/websocket"
	"github.com/golang/protobuf/jsonpb"
	"github.com/golang/protobuf/proto"

	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
)

var marshaler = &jsonpb.Marshaler{EmitDefaults: true}

// TODO: Protobuf schema will replace this test Message with actual message send through the kafka queue
type Message struct {
	UID     int
	Message string
}

func serveWebsocket(pool *websocket.Pool, w http.ResponseWriter, r *http.Request) {
	log.Debug("WebSocket Endpoint Request")
	conn, err := websocket.Upgrade(w, r)
	if err != nil {
		log.Error(w, "%+v\n", err)
	}

	client := &websocket.Client{
		Conn: conn,
		Pool: pool,
	}

	pool.Register <- client
	client.Read()
}

func subscribeKafkaToPool(ctx context.Context, pool *websocket.Pool, options kafkaconsumer.KafkaConsumerOptions) {
	if !(len(options.Topics) == 1 && len(options.Topics[0]) > 0) {
		options.Topics = []string{constants.Topics_DELAY_NOTIFICATIONS.String()}
	}
	if options.Group == "" {
		options.Group = "DelayConsumerGroup"
	}
	log.Infof("Will consume topic %s from %s (group %s)", options.Topics, strings.Join(options.Brokers, ", "), options.Group)
	kafkaConsumer, err := kafkaconsumer.ConsumeKafkaGroup(ctx, options)
	if err != nil {
		log.Fatalf("Failed to connect to kafka broker (%s) (group %s) on topic %s: %s",
			strings.Join(options.Brokers, ", "), options.Group, options.Topics, err.Error())
	}

	noopTicker := time.NewTicker(time.Second * 10)
	subscriberDone := make(chan bool, 1)
	stopSubscriber := make(chan bool, 1)
	go func() {
		defer func() { subscriberDone <- true }()
		for {
			select {
			case msg := <-kafkaConsumer.Messages: // kafkaConsumer.Messages():
				// payload := msg.Value
				// log.Info("Got delay message")
				// log.Info(string(msg.Value))
				// Converting []byte payload to JSON
				// var message Message
				// json.Unmarshal(payload, &message)

				delayEvent := &delay.TrainDelayNotification{}
				err = proto.Unmarshal(msg.Value, delayEvent)
				if err != nil {
					log.Errorf("unmarshaling error: ", err)
				}
				log.Info(delayEvent)
				pool.Broadcast <- msg.Value

				/*
					jsonMessage, err := marshaler.MarshalToString(delayEvent)
					if err != nil {
						log.Errorf("json marshal error: ", err)
					} else {
						pool.Broadcast <- jsonMessage
					}
				*/
				/*
					for client, _ := range pool.Clients {
						if client.ID == message.UID {
							client.Conn.WriteJSON(websocket.Message{Type: 4, Body: string(payload)})
						}
					}
				*/
				break

			case <-noopTicker.C:
				// message := websocket.Message{Type: 2, Body: "ping"}
				// pool.Broadcast <- message
			case <-stopSubscriber:
				return
			}
		}
	}()
	<-subscriberDone
	noopTicker.Stop()

	/*
		consumer, err := kafkaClient.Consume(ctx, topics, 0, sarama.OffsetOldest)
		if err != nil {
			log.Fatalf("Failed to consume topic %s: %s", topic, err.Error())
		}
	*/

	/*
		config := sarama.NewConfig()
		config.Consumer.Return.Errors = true

		// Specify brokers address
		// brokers := []string{"localhost:29092"}

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
		noopTicker := time.NewTicker(time.Second * 10)
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
	*/
}

func serve(cliCtx *cli.Context) error {
	ctx, _ := context.WithCancel(context.Background()) // cancel
	kafkaOptions := kafkaconsumer.KafkaConsumerOptions{}.ParseCli(cliCtx)
	pool := websocket.NewPool()
	go pool.Start()
	go subscribeKafkaToPool(ctx, pool, kafkaOptions)

	http.HandleFunc("/ws/userdata", func(w http.ResponseWriter, r *http.Request) {
		serveWebsocket(pool, w, r)
	})
	port := fmt.Sprintf(":%d", cliCtx.Int("port"))
	log.Printf("Server ready at %s", port)
	log.Fatal(http.ListenAndServe(port, nil))

	/*
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
	*/
	return nil
}

func main() {
	cliFlags := []cli.Flag{}
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServicePort, libcli.ServiceLogLevel)...)
	cliFlags = append(cliFlags, kafkaconsumer.KafkaConsumerCliOptions...)

	app := &cli.App{
		Name:    "CEPTA Notification service",
		Version: versioning.GetVersion(),
		Usage:   "The service sets up the websocket connection and subscription to kafka",
		Flags:   cliFlags,
		Action: func(ctx *cli.Context) error {
			level, err := log.ParseLevel(ctx.String("log"))
			if err != nil {
				log.Warnf("Log level '%s' does not exist.")
				level = log.InfoLevel
			}
			log.SetLevel(level)
			ret := serve(ctx)
			return ret
		},
	}

	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}
