package main

import (
	"context"
	"fmt"
	"github.com/Shopify/sarama"
	"github.com/golang/protobuf/proto"
	"math/rand"
	"os"
	"os/signal"
	"strings"
	"sync"
	"syscall"
	"time"

	"github.com/bptlab/cepta/ci/versioning"
	"github.com/bptlab/cepta/models/internal/delay"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	notificationpb "github.com/bptlab/cepta/models/internal/notifications/notification"
	durationpb "github.com/golang/protobuf/ptypes/duration"
	"github.com/bptlab/cepta/models/internal/types/ids"

	topics "github.com/bptlab/cepta/models/constants/topic"

	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
)

// Version will be injected at build time
var Version string = "Unknown"

// BuildTime will be injected at build time
var BuildTime string = ""

const (
	defaultNotificationsTopic = topics.Topic_DELAY_NOTIFICATIONS
)

type Generator struct {
	ProducerConfig 	kafkaproducer.Config
	NotificationsTopic topics.Topic
	Pause time.Duration
	JitterSecs int
	TransportIDs []string

	producer 		*kafkaproducer.Producer
	done              chan bool
	cancelSetup       context.Context
}

func (g *Generator) Serve(ctx context.Context) (err error) {
	log.Info("Connecting to Kafka...")
	g.producer, err = kafkaproducer.Create(ctx, g.ProducerConfig)
	if err != nil {
		return fmt.Errorf("failed to create kafka producer: %v", err)
	}
	log.Info("Connected")

	var wg sync.WaitGroup
	for _, tid := range g.TransportIDs {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for {
				pause := g.Pause
				if g.JitterSecs > 0 {
					pause = pause + (time.Duration(rand.Intn(g.JitterSecs)) * time.Second)
				}
				select {
				case <- time.After(pause):
					notification := &notificationpb.Notification{
						Notification: &notificationpb.Notification_Delay{
							Delay: &notificationpb.DelayNotification{
								TransportId: &ids.CeptaTransportID{Id: tid}, // strconv.Itoa(rand.Intn(10000))
								Delay:       &delay.Delay{Delta: &durationpb.Duration{Seconds: int64(rand.Intn(60 * 60 * 60))}},
							}}}
					eventBytes, err := proto.Marshal(notification)
					if err != nil {
						log.Error("Failed to marshal notification: %v", err)
						break
					}
					g.producer.Send(g.NotificationsTopic.String(), g.NotificationsTopic.String(), sarama.ByteEncoder(eventBytes))
					log.Debugf("Sent notification with delay=%d to kafka tropic=%s", notification.GetDelay().GetDelay().GetDelta().GetSeconds(), g.NotificationsTopic)
				case <- g.done:
					return
				}
			}
		}()
	}

	wg.Wait()
	return nil
}

// Shutdown ...
func (g *Generator) Shutdown() {
	log.Info("Graceful shutdown")
	for _ = range g.TransportIDs {
		g.done <- true
	}
	if g.producer != nil {
		_ = g.producer.Close()
	}
}

func main() {
	var cliFlags []cli.Flag
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceLogLevel)...)
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceConnectionTolerance)...)
	cliFlags = append(cliFlags, kafkaproducer.CliOptions...)
	cliFlags = append(cliFlags, []cli.Flag{
		&cli.IntFlag{
			Name: "per-minute",
			Value: 60, // 1 per second
			Aliases: []string{"frequency", "notifications-per-minute"},
			EnvVars: []string{"PER_MINUTE", "FREQUENCY", "NOTIFICATIONS_PER_MINUTE"},
			Usage:   "number of notifications per minute",
		},
		&cli.IntFlag{
			Name:    "jitter",
			Value:   0, // No jitter
			Aliases: []string{"variance", "jitter-seconds",},
			EnvVars: []string{"JITTER", "VARIANCE"},
			Usage:   "range of random value to be added to the pause value in seconds",
		},
		&cli.StringFlag{
			Name:    "transport-ids",
			EnvVars: []string{"TRANSPORT_IDS"},
			Usage:   "comma-separated list of transport ids to generate notifications for",
		},
	}...)

	app := &cli.App{
		Name:    "CEPTA mock notification generator",
		Version: versioning.BinaryVersion(Version, BuildTime),
		Usage:   "Generates fake notifications for testing purposes only",
		Flags:   cliFlags,
		Action: func(ctx *cli.Context) error {
			if logLevel, err := log.ParseLevel(ctx.String("log")); err == nil {
				log.SetLevel(logLevel)
			}
			gen := Generator{
				ProducerConfig: kafkaproducer.Config{}.ParseCli(ctx),
				NotificationsTopic: defaultNotificationsTopic,
				Pause: time.Duration(60 / ctx.Int("per-minute")) * time.Second,
				JitterSecs: ctx.Int("jitter"),
				done: make(chan bool),
			}
			log.Infof("pause=%v: jitter=%vs", gen.Pause, gen.JitterSecs)

			for _, tid := range strings.Split(ctx.String("transport-ids"), ",") {
				if trimmed := strings.TrimSpace(tid); trimmed != "" {
					gen.TransportIDs = append(gen.TransportIDs, trimmed)
				}
			}

			if len(gen.TransportIDs) < 1 {
				return fmt.Errorf("must specify transport ids with --transport-ids")
			}

			log.Infof("transports=%v", gen.TransportIDs)

			// Register shutdown routine
			setupCtx, cancelSetup := context.WithCancel(context.Background())
			shutdown := make(chan os.Signal)
			signal.Notify(shutdown, syscall.SIGINT, syscall.SIGTERM)
			go func() {
				<-shutdown
				cancelSetup()
				gen.Shutdown()
			}()

			return gen.Serve(setupCtx)
		},
	}
	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}
