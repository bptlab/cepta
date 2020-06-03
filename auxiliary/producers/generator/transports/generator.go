package main

import (
	"context"
	"fmt"
	"math/rand"
	"os"
	"os/signal"
	"strings"
	"sync"
	"syscall"
	"time"

	"github.com/Shopify/sarama"
	"github.com/golang/protobuf/proto"

	"github.com/bptlab/cepta/ci/versioning"
	"github.com/bptlab/cepta/models/internal/delay"
	notificationpb "github.com/bptlab/cepta/models/internal/notifications/notification"
	"github.com/bptlab/cepta/models/internal/station"
	"github.com/bptlab/cepta/models/internal/types/ids"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	durationpb "github.com/golang/protobuf/ptypes/duration"
	"github.com/google/uuid"
	"github.com/k0kubun/pp"
	"github.com/yourbasic/graph"

	topics "github.com/bptlab/cepta/models/constants/topic"

	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
)

// Version will be injected at build time
var Version string = "Unknown"

// BuildTime will be injected at build time
var BuildTime string = ""

// Config ....
type Config struct {
	Speedup      int64
	JitterSecs   int
	TransportIDs []string
	Nodes        int64
	Edges        int64
}

// Generator ....
type Generator struct {
	Config
	ProducerConfig kafkaproducer.Config

	graph       *graph.Mutable
	stations    []*station.Station
	producer    *kafkaproducer.Producer
	done        chan bool
	cancelSetup context.Context
}

func (gen *Generator) generateUniqueSourceID() string {
	for {
		if id, err := uuid.NewRandom(); err != nil {
			return id.String()
		}
	}
}

func (gen *Generator) generateTransportNetwork() {
	// Generate stations
	gen.stations = make([]*station.Station, gen.Nodes)
	for si := range gen.stations {
		gen.stations[si] = &station.Station{
			// TODO: Add more params
			Name: fmt.Sprintf("Station %d", si),
		}
	}

	// https://link.springer.com/content/pdf/10.1007/978-3-642-36763-2_41.pdf
	// https://i11www.iti.kit.edu/extra/publications/n-asamm-14.pdf
	g := graph.New(int(gen.Nodes))
	g.AddBoth(0, 1) //  0 -- 1
	g.AddBoth(0, 2) //  |    |
	g.AddBoth(2, 3) //  2 -- 3
	g.AddBoth(1, 3)
}

func (gen *Generator) worker() {
	// time.AfterFunc(4*time.Hour, func() { destroyObject("idofobject") })
	//
}

func (gen *Generator) Serve(ctx context.Context) (err error) {
	log.Info("Connecting to Kafka...")
	gen.producer, err = kafkaproducer.Create(ctx, gen.ProducerConfig)
	if err != nil {
		return fmt.Errorf("failed to create kafka producer: %v", err)
	}
	log.Info("Connected")

	var wg sync.WaitGroup
	for _, tid := range gen.TransportIDs {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for {
				pause := time.Duration(time.Second)
				if gen.JitterSecs > 0 {
					pause = pause + (time.Duration(rand.Intn(gen.JitterSecs)) * time.Second)
				}
				select {
				case <-time.After(pause):
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
					gen.producer.Send(topics.Topic_DELAY_NOTIFICATIONS), topics.Topic_DELAY_NOTIFICATIONS.String(), sarama.ByteEncoder(eventBytes))
					log.Debugf("Sent notification with delay=%d to kafka topic=%s", notification.GetDelay().GetDelay().GetDelta().GetSeconds(), topics.Topic_DELAY_NOTIFICATIONS.String())
				case <-gen.done:
					return
				}
			}
		}()
	}

	wg.Wait()
	return nil
}

// Shutdown ...
func (gen *Generator) Shutdown() {
	log.Info("Graceful shutdown")
	for _ = range gen.TransportIDs {
		gen.done <- true
	}
	if gen.producer != nil {
		_ = gen.producer.Close()
	}
}

func main() {
	var cliFlags []cli.Flag
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceLogLevel)...)
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceConnectionTolerance)...)
	cliFlags = append(cliFlags, kafkaproducer.CliOptions...)
	cliFlags = append(cliFlags, []cli.Flag{
		&cli.Int64Flag{
			Name:    "speed",
			Value:   1,
			Aliases: []string{"speedup", "factor"},
			EnvVars: []string{"SPEEDUP", "FACTOR"},
			Usage:   "speedup factor (1x=realtime, 2x=twice as fast as realtime, 0x=benchmark)",
		},
		&cli.IntFlag{
			Name:    "jitter",
			Value:   0, // No jitter
			Aliases: []string{"variance", "jitter-seconds"},
			EnvVars: []string{"JITTER", "VARIANCE"},
			Usage:   "random lag in seconds to be added to scheduled events to simulate transmission jitter",
		},
		&cli.StringFlag{
			Name:    "transport-ids",
			EnvVars: []string{"TRANSPORT_IDS"},
			Usage:   "comma-separated list of transport ids to be guaranteed to be included in a timely fashion",
		},
		&cli.Int64Flag{
			Name:    "nodes",
			Value:   10_000,
			Aliases: []string{"node-count", "num-nodes", "network-size"},
			EnvVars: []string{"NODES", "NODE_COUNT", "NETWORK_SIZE"},
			Usage:   "number of nodes in the generated transport network",
		},
		&cli.Int64Flag{
			Name:    "edges",
			Aliases: []string{"edge-count", "num-edges", "routes"},
			EnvVars: []string{"NODES", "NODE_COUNT", "NETWORK_SIZE"},
			Usage:   "number of edges (routes) in the generated transport network (default: nodes*nodes/2)",
		},
	}...)

	app := &cli.App{
		Name:    "CEPTA mock transport data generator",
		Version: versioning.BinaryVersion(Version, BuildTime),
		Usage:   "Generates fake source transport events on a randomly generated transportation network for benchmarking purposes",
		Flags:   cliFlags,
		Action: func(ctx *cli.Context) error {
			if logLevel, err := log.ParseLevel(ctx.String("log")); err == nil {
				log.SetLevel(logLevel)
			}
			gen := Generator{
				ProducerConfig: kafkaproducer.Config{}.ParseCli(ctx),
				Config: Config{
					Speedup:      ctx.Int64("speed"),
					JitterSecs:   ctx.Int("jitter"),
					TransportIDs: []string{},
					Nodes:        ctx.Int64("nodes"),
					Edges:        ctx.Int64("edges"),
				},
				done: make(chan bool),
			}

			if gen.Config.Edges < 1 {
				gen.Config.Edges = gen.Config.Nodes * gen.Config.Nodes / 2
			}

			for _, tid := range strings.Split(ctx.String("transport-ids"), ",") {
				if trimmed := strings.TrimSpace(tid); trimmed != "" {
					gen.Config.TransportIDs = append(gen.Config.TransportIDs, trimmed)
				}
			}

			log.Info(pp.Sprintln(gen.Config))

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
