package main

import (
	"context"
	"fmt"
	"io"
	"net"
	"net/http"
	"os"
	"os/signal"
	"strconv"
	"strings"
	"syscall"
	"time"

	"github.com/bptlab/cepta/ci/versioning"
	"github.com/bptlab/cepta/models/constants/topic"
	delay "github.com/bptlab/cepta/models/events/traindelaynotificationevent"
	pb "github.com/bptlab/cepta/models/grpc/notification"
	usermgmtpb "github.com/bptlab/cepta/models/grpc/usermgmt"
	"github.com/bptlab/cepta/models/types/result"
	"github.com/bptlab/cepta/models/types/transports"
	"github.com/bptlab/cepta/models/types/users"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	kafkaconsumer "github.com/bptlab/cepta/osiris/lib/kafka/consumer"
	rmqc "github.com/bptlab/cepta/osiris/lib/rabbitmq/consumer"
	rmqp "github.com/bptlab/cepta/osiris/lib/rabbitmq/producer"
	"github.com/bptlab/cepta/osiris/notification/websocket"

	"github.com/golang/protobuf/proto"
	lru "github.com/hashicorp/golang-lru"
	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
	"google.golang.org/grpc"
)

// Version will be injected at build time
var Version string = "Unknown"

// BuildTime will be injected at build time
var BuildTime string = ""

const (
	lruSize = 1000
)

// Endpoint ...
type Endpoint struct {
	Host string
	Port int
}

// NotificationServer ...
type NotificationServer struct {
	pb.UnimplementedNotificationServer

	transportCache *lru.Cache
	pool           *websocket.Pool

	usermgmtClient usermgmtpb.UserManagementClient
	grpcServer     *grpc.Server

	kafkacConfig kafkaconsumer.Config
	rmqcConfig   rmqc.Config
	rmqpConfig   rmqp.Config

	usermgmtEndpoint Endpoint
}

// NewNotificationServer ...
func NewNotificationServer(kafkaConfig kafkaconsumer.Config, rmqConsumerConfig rmqc.Config, rmqProducerConfig rmqp.Config) NotificationServer {
	return NotificationServer{
		kafkacConfig: kafkaConfig,
		rmqcConfig:   rmqConsumerConfig,
		rmqpConfig:   rmqProducerConfig,
	}
}

// Shutdown ...
func (s *NotificationServer) Shutdown() {
	log.Info("Graceful shutdown")
	log.Info("Stopping GRPC server")
	if s.grpcServer != nil {
		s.grpcServer.Stop()
	}
}

// FillUserCache ...
func (s *NotificationServer) fillUserCache() {
	stream, err := s.usermgmtClient.GetAllUser(context.Background(), &result.Empty{})
	if err != nil {
		log.Fatalf("Failed to receive stream from usermgmt service %v", err)
	}

	for {
		user, err := stream.Recv()
		if err == io.EOF || s.transportCache.Len() == lruSize {
			break
		}
		if err != nil {
			log.Fatalf("Failed to receive user from stream: %v", err)
		}
		log.Debug(user)

		for _, train := range user.Transports {
			log.Debug(train)
			evicted := s.transportCache.Add(train.Id, user.Id)
			log.Debug(evicted)
		}
	}
	return
}

func (s *NotificationServer) findUser(ctx context.Context, trainID int64) (*users.User, error) {
	userExists := s.transportCache.Contains(trainID)

	if userExists {
		uid, _ := s.transportCache.Get(trainID)
		return uid.(*users.User), nil
	} else {
		userRequest := &usermgmtpb.GetUserRequest{
			TrainId: &transports.TransportID{
				Id: strconv.Itoa(int(trainID)),
			},
		}
		usr, err := s.usermgmtClient.GetUser(ctx, userRequest)
		if err != nil {
			return nil, fmt.Errorf("Failed to receive user to the trainID with error message: %v", err)
		}
		evicted := s.transportCache.Add(trainID, usr.Id)
		log.Debug(evicted)
		return usr, nil
	}
}

func (s *NotificationServer) serveRabbitMQConsumer(options rmqc.Config) {
	conn, ch := options.Setup()

	// Consume Messages from the Queue
	options.Consume(ch, conn)
}

func (s *NotificationServer) serveWebsocket(pool *websocket.Pool, w http.ResponseWriter, r *http.Request) {
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

func (s *NotificationServer) subscribeKafkaToPool(ctx context.Context, pool *websocket.Pool) {
	if !(len(s.kafkacConfig.Topics) == 1 && len(s.kafkacConfig.Topics[0]) > 0) {
		s.kafkacConfig.Topics = []string{topic.Topic_DELAY_NOTIFICATIONS.String()}
	}
	if s.kafkacConfig.Group == "" {
		s.kafkacConfig.Group = "DelayConsumerGroup"
	}
	log.Infof("Will consume topic %s from %s (group %s)", s.kafkacConfig.Topics, strings.Join(s.kafkacConfig.Brokers, ", "), s.kafkacConfig.Group)
	// TODO: Implement wg
	kafkaConsumer, _, err := kafkaconsumer.ConsumeGroup(ctx, s.kafkacConfig)
	if err != nil {
		log.Warnf("Failed to connect to kafka broker (%s) (group %s) on topic %s",
			strings.Join(s.kafkacConfig.Brokers, ", "), s.kafkacConfig.Group, s.kafkacConfig.Topics)
		log.Fatal(err.Error())
	}

	// Connect to RabbitMQ and define channel
	rabbitMqConnection, rabbitMqChannel := s.rmqpConfig.Setup()
	defer rabbitMqConnection.Close()
	defer rabbitMqChannel.Close()

	noopTicker := time.NewTicker(time.Second * 10)
	subscriberDone := make(chan bool, 1)
	stopSubscriber := make(chan bool, 1)
	go func() {
		defer func() { subscriberDone <- true }()
		for {
			select {
			case msg := <-kafkaConsumer.Messages:
				delayEvent := delay.TrainDelayNotification{}
				err = proto.Unmarshal(msg.Value, &delayEvent)
				if err != nil {
					log.Errorf("unmarshaling error: ", err)
				}
				log.Info(delayEvent)

				uid, err := s.findUser(ctx, delayEvent.TrainId)
				if err != nil {
					log.Fatal(err)
				}
				s.rmqcConfig.ExchangeRoutingKey = uid.Id.Id
				s.rmqpConfig.Publish(delayEvent, rabbitMqChannel)

				s.pool.Broadcast <- msg.Value
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

// Setup ...
func (s *NotificationServer) Setup(ctx context.Context, usermgmtConn *grpc.ClientConn) error {
	s.usermgmtClient = usermgmtpb.NewUserManagementClient(usermgmtConn)

	// Fill the cache with recent transports
	var err error
	s.transportCache, err = lru.New(lruSize)
	if err != nil {
		log.Fatalf("Failed to initialize cache: %v", err)
	}

	log.Info("Filling cache")
	go s.fillUserCache()
	s.pool = websocket.NewPool()
	go s.pool.Start()

	// TODO: We need to serve more than just one user and therefore need more than one rabbitmqConsumer
	go s.serveRabbitMQConsumer(s.rmqcConfig)
	go s.subscribeKafkaToPool(ctx, s.pool)

	return nil
}

// ConnectUsermgmt ...
func (s *NotificationServer) ConnectUsermgmt(ctx context.Context, usermgmtConnectionURI string) error {
	log.Info("Connecting to usermgmt service")
	usermgmtConn, err := grpc.Dial(usermgmtConnectionURI, grpc.WithInsecure(), grpc.WithBlock())
	if err != nil {
		log.Fatalf("Failed to connect to the usermgmt service: %v", err)
	}
	s.Setup(ctx, usermgmtConn)
	return nil
}

func main() {

	cliFlags := []cli.Flag{}
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceLogLevel)...)
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceConnectionTolerance)...)
	cliFlags = append(cliFlags, kafkaconsumer.CliOptions...)
	cliFlags = append(cliFlags, rmqc.CliOptions...)
	cliFlags = append(cliFlags, []cli.Flag{
		// gRPC endpoint
		&cli.IntFlag{
			Name:    "grpc-port",
			Value:   6000,
			EnvVars: []string{"GRPC_PORT"},
			Usage:   "grpc port",
		},
		// websocket endpoint
		&cli.IntFlag{
			Name:    "ws-port",
			Value:   5555,
			EnvVars: []string{"WS_PORT"},
			Usage:   "webocket port",
		},
		// External user managemnt service config
		&cli.StringFlag{
			Name:    "usermgmt-host",
			Value:   "localhost",
			EnvVars: []string{"USERMGMT_HOST"},
			Usage:   "usermgmt microservice host",
		},
		&cli.IntFlag{
			Name:    "usermgmt-port",
			Value:   5557,
			EnvVars: []string{"USERMGMT_PORT"},
			Usage:   "usermgmt microservice port",
		},
	}...)

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

			server := NotificationServer{
				kafkacConfig:     kafkaconsumer.Config{}.ParseCli(ctx),
				rmqcConfig:       rmqc.Config{}.ParseCli(ctx),
				rmqpConfig:       rmqp.Config{}.ParseCli(ctx),
				usermgmtEndpoint: Endpoint{Host: ctx.String("usermgmt-host"), Port: ctx.Int("usermgmt-port")},
			}

			// Register shutdown routine
			shutdown := make(chan os.Signal)
			signal.Notify(shutdown, syscall.SIGINT, syscall.SIGTERM)
			go func() {
				<-shutdown
				server.Shutdown()
			}()

			grpcListener, err := net.Listen("tcp", fmt.Sprintf(":%d", ctx.Int("grpc-port")))
			if err != nil {
				return fmt.Errorf("failed to listen: %v", err)
			}

			wsListener, err := net.Listen("tcp", fmt.Sprintf(":%d", ctx.Int("ws-port")))
			if err != nil {
				return fmt.Errorf("failed to listen: %v", err)
			}

			if err := server.ConnectUsermgmt(context.Background(), fmt.Sprintf("%s:%d", server.usermgmtEndpoint.Host, server.usermgmtEndpoint.Port)); err != nil {
				return err
			}

			return server.Serve(grpcListener, wsListener)
		},
	}

	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}

// Serve starts the service
func (s *NotificationServer) Serve(grpcListener net.Listener, wsListener net.Listener) error {
	log.Infof("Notification grpc service ready at %s", grpcListener.Addr())
	log.Infof("Notification websocket service ready at %s", wsListener.Addr())
	defer grpcListener.Close()
	defer wsListener.Close()

	wsDone := make(chan bool)
	grpcDone := make(chan bool)

	// Serve websocket endpoint
	go func() {
		http.HandleFunc("/ws/notifications", func(w http.ResponseWriter, r *http.Request) {
			s.serveWebsocket(s.pool, w, r)
		})
		if err := http.Serve(wsListener, nil); err != nil {
			log.Error("Failed to serve: ", err)
		}
		wsDone <- true
	}()

	// Serve GRPC endpoint
	go func() {
		s.grpcServer = grpc.NewServer()
		pb.RegisterNotificationServer(s.grpcServer, s)
		if err := s.grpcServer.Serve(grpcListener); err != nil {
			log.Error("Failed to serve: ", err)
		}
		grpcDone <- true
	}()
	<-wsDone
	<-grpcDone

	log.Info("Closing sockets")
	return nil
}
