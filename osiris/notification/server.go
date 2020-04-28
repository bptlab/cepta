package main

import (
	"context"
	"fmt"
	"net/http"
	"os"
	"strings"

	"time"

	"github.com/bptlab/cepta/ci/versioning"
	"github.com/bptlab/cepta/models/constants/topic"
	delay "github.com/bptlab/cepta/models/internal/notifications/notification"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	kafkaconsumer "github.com/bptlab/cepta/osiris/lib/kafka/consumer"
	"github.com/bptlab/cepta/osiris/notification/websocket"
	"github.com/golang/protobuf/jsonpb"
	"github.com/golang/protobuf/proto"

	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
)

// Version will be injected at build time
var Version string = "Unknown"

// BuildTime will be injected at build time
var BuildTime string = ""

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

func subscribeKafkaToPool(ctx context.Context, pool *websocket.Pool, options kafkaconsumer.Config) {
	if !(len(options.Topics) == 1 && len(options.Topics[0]) > 0) {
		options.Topics = []string{topic.Topic_DELAY_NOTIFICATIONS.String()}
	}
	if options.Group == "" {
		options.Group = "DelayConsumerGroup"
	}
	log.Infof("Will consume topic %s from %s (group %s)", options.Topics, strings.Join(options.Brokers, ", "), options.Group)
	// TODO: Implement wg
	kafkaConsumer, _, err := kafkaconsumer.ConsumeGroup(ctx, options)
	if err != nil {
		log.Warnf("Failed to connect to kafka broker (%s) (group %s) on topic %s",
			strings.Join(options.Brokers, ", "), options.Group, options.Topics)
		log.Fatal(err.Error())
	}

	noopTicker := time.NewTicker(time.Second * 10)
	subscriberDone := make(chan bool, 1)
	stopSubscriber := make(chan bool, 1)
	go func() {
		defer func() { subscriberDone <- true }()
		for {
			select {
			case msg := <-kafkaConsumer.Messages:
				delayEvent := &delay.Notification{}
				err = proto.Unmarshal(msg.Value, delayEvent)
				if err != nil {
					log.Errorf("unmarshaling error: ", err)
				}
				log.Info(delayEvent)
				pool.Broadcast <- msg.Value
				break

			case <-noopTicker.C:
				// Noop
			case <-stopSubscriber:
				return
			}
		}
	}()
	<-subscriberDone
	noopTicker.Stop()
}

func serve(cliCtx *cli.Context) error {
	ctx, _ := context.WithCancel(context.Background()) // cancel
	kafkaOptions := kafkaconsumer.Config{}.ParseCli(cliCtx)
	pool := websocket.NewPool()
	go pool.Start()
	go subscribeKafkaToPool(ctx, pool, kafkaOptions)

	http.HandleFunc("/ws/userdata", func(w http.ResponseWriter, r *http.Request) {
		serveWebsocket(pool, w, r)
	})
	port := fmt.Sprintf(":%d", cliCtx.Int("port"))
	log.Printf("Server ready at %s", port)
	log.Fatal(http.ListenAndServe(port, nil))
	return nil
}

func main() {
	cliFlags := []cli.Flag{}
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServicePort, libcli.ServiceLogLevel)...)
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceConnectionTolerance)...)
	cliFlags = append(cliFlags, kafkaconsumer.CliOptions...)

	app := &cli.App{
		Name:    "CEPTA Notification service",
		Version: versioning.BinaryVersion(Version, BuildTime),
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
