package main

import (
	"fmt"
	"net/http"
	"os"

	"github.com/bptlab/cepta/osiris/notification/websocket"

	"github.com/sirupsen/logrus"
	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
)

// TODO: Protobuf schema will replace this test Message with actual message send through the kafka queue
type Message struct {
	UID     int
	Message string
}

func serveWs(pool *websocket.Pool, w http.ResponseWriter, r *http.Request) {
	log.Debug("WebSocket Endpoint Hit")
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

func setupRoutes() {
	pool := websocket.NewPool()
	go pool.Start()

	go connectKafkaConsumer(pool)

	http.HandleFunc("/ws/userdata", func(w http.ResponseWriter, r *http.Request) {
		serveWs(pool, w, r)
	})
}

func serve(ctx *cli.Context) error {
	port := fmt.Sprintf(":%d", ctx.Int("port"))
	log.Printf("Server ready at %s", port)
	log.Fatal(http.ListenAndServe(port, nil))
	return nil
}

func main() {
	setupRoutes()
	app := &cli.App{
		Name:  "CEPTA Notification service",
		Usage: "The service sets up the websocket connection and subscription to kafka",
		Flags: []cli.Flag{
			&cli.StringFlag{
				Name:    "log",
				Value:   "info",
				Aliases: []string{"log-level"},
				EnvVars: []string{"LOG", "LOG_LEVEL"},
				Usage:   "Set the log level for the microservice",
			},
			&cli.IntFlag{
				Name:    "port",
				Value:   5555,
				Aliases: []string{"p"},
				EnvVars: []string{"PORT"},
				Usage:   "Notification server port",
			},
		},
		Action: func(ctx *cli.Context) error {
			level, err := logrus.ParseLevel(ctx.String("log"))
			if err != nil {
				log.Warnf("Log level '%s' does not exist.")
				level = logrus.InfoLevel
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
