package main

import (
	"context"
	"fmt"
	"io"
	"net"
	"net/http"
	"os"
	"os/signal"
	"reflect"
	"strings"
	"syscall"
	"time"

	"github.com/bptlab/cepta/ci/versioning"
	topics "github.com/bptlab/cepta/models/constants/topic"
	pb "github.com/bptlab/cepta/models/grpc/notification"
	usermgmtpb "github.com/bptlab/cepta/models/grpc/usermgmt"
	notificationpb "github.com/bptlab/cepta/models/internal/notifications/notification"
	"github.com/bptlab/cepta/models/internal/types/ids"
	"github.com/bptlab/cepta/models/internal/types/result"
	"github.com/bptlab/cepta/models/internal/types/users"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	kafkaconsumer "github.com/bptlab/cepta/osiris/lib/kafka/consumer"
	rmqc "github.com/bptlab/cepta/osiris/lib/rabbitmq/consumer"
	rmqp "github.com/bptlab/cepta/osiris/lib/rabbitmq/producer"
	"github.com/bptlab/cepta/osiris/notification/websocket"
	clivalues "github.com/romnnn/flags4urfavecli/values"

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

var (
	lruSize                  = 1000 // Cache up to 1000 transports
	lruMaxEntryLength        = 1000 // Cache up to 1000 subscribers per transport
	defaultNotificationTopic = topics.Topic_DELAY_NOTIFICATIONS.String()
)

// Endpoint ...
type Endpoint struct {
	Host string
	Port int
}

// TransportSubscribers ...
type TransportSubscribers struct {
	TransportID string
	Subscribers *[]*users.UserID
}

// NotificationServer ...
type NotificationServer struct {
	pb.UnimplementedNotificationServer

	transportCache *lru.Cache

	pool           *websocket.Pool
	usermgmtClient usermgmtpb.UserManagementClient
	grpcServer     *grpc.Server
	wsServer       *http.Server

	kafkacConfig kafkaconsumer.Config
	kc           *kafkaconsumer.Consumer

	producer   *rmqp.Producer
	rmqpConfig rmqp.Config

	usermgmtEndpoint Endpoint
}

// NewNotificationServer ...
func NewNotificationServer(kafkaConfig kafkaconsumer.Config, rmqProducerConfig rmqp.Config) NotificationServer {
	return NotificationServer{
		kafkacConfig: kafkaConfig,
		// rmqcConfig:   rmqConsumerConfig,
		rmqpConfig: rmqProducerConfig,
	}
}

// Shutdown ...
func (s *NotificationServer) Shutdown() {
	log.Info("Graceful shutdown")
	log.Info("Closing rabbitMQ connection")
	if s.producer != nil {
		s.producer.Close()
	}
	log.Info("Stopping websocket server")
	if s.wsServer != nil {
		s.wsServer.Shutdown(context.TODO())
	}
	log.Info("Stopping GRPC server")
	if s.grpcServer != nil {
		s.grpcServer.GracefulStop()
	}
}

// FillUserCache ...
func (s *NotificationServer) fillUserCache(ctx context.Context) {
	stream, err := s.usermgmtClient.GetUsers(ctx, &result.Empty{})
	if err != nil {
		log.Fatalf("Failed to receive users stream from usermgmt service: %v", err)
	}
	for {
		user, err := stream.Recv()
		if err == io.EOF || s.transportCache.Len() >= lruSize {
			break
		}
		if err != nil {
			log.Warnf("Failed to receive user from stream: %v", err)
			continue
		}
		log.Debugf("Adding transports for user %v", user)
		for _, transportID := range user.GetTransports() {
			log.Debugf("Adding transport %v to cache", transportID)
			s.addUsersToCache([]*users.UserID{user.GetId()}, transportID)
			/*
				if rawCachedSubscribers, ok := s.transportCache.Get(transport.GetId()); ok {
					if cachedSubscribers, ok := rawCachedSubscribers.(*TransportSubscribers); ok {
						*cachedSubscribers.Subscribers = append(*cachedSubscribers.Subscribers, user.GetId())
					}
				} else {
					subscriber := []*users.UserID{user.GetId()}
					s.transportCache.Add(transport.GetId(), &TransportSubscribers{Subscribers: &subscriber})
				}
			*/
		}
	}
}

// AddUserToCache ...
func (s *NotificationServer) addUsersToCache(values []*users.UserID, key *ids.CeptaTransportID) {
	if len(values) < 1 {
		return
	}
	log.Info(values)
	if rawCachedSubscribers, ok := s.transportCache.Get(key.GetId()); ok {
		if cachedSubscribers, ok := rawCachedSubscribers.(*TransportSubscribers); ok {
			if cachedSubscribers.Subscribers != nil {
				*cachedSubscribers.Subscribers = append(*cachedSubscribers.Subscribers, values...)
				return
			}
		}
	}
	s.transportCache.Add(key.GetId(), &TransportSubscribers{Subscribers: &values})
}

// GetSubscribersForTransport ...
func (s *NotificationServer) getSubscribersForTransport(key *ids.CeptaTransportID) ([]*users.UserID, bool) {
	if rawCachedSubscribers, ok := s.transportCache.Get(key.GetId()); ok {
		if cachedSubscribers, ok := rawCachedSubscribers.(*TransportSubscribers); ok {
			if cachedSubscribers.Subscribers != nil {
				if len(*cachedSubscribers.Subscribers) < 1 {
					log.Warnf("Found empty slice of transport subscribers for transport %v in cache", key)
				}
				return *cachedSubscribers.Subscribers, true
			}
			log.Warnf("Found nil slice of transport subscribers for transport %v in cache", key)
		} else {
			log.Errorf("Cache returned invalid type %v", reflect.TypeOf(rawCachedSubscribers))
		}
	}
	return []*users.UserID{}, false
}

func (s *NotificationServer) notifyUser(userID *users.UserID, message proto.Message) {
	rawMessage, err := proto.Marshal(message)
	if err != nil {
		log.Error("Failed to marshal proto: ", err)
		return
	}
	log.Infof("Sending notification to user %v", userID)
	s.pool.NotifyUser <- websocket.UserNotification{ID: userID, Msg: rawMessage}
	/*
		if err := s.producer.Publish(rawMessage, userID.GetId()); err != nil {
			log.Errorf("Failed to publish notification for user into queue %s: %v", userID.GetId(), err)
		}
	*/
}

// TODO: Notify here already (send into big buffered queue)
// When numbers exceeds limit do not set the cache item so it must be streamed every time again which is ok
// Change return type!
func (s *NotificationServer) notifySubscribersForTransport(ctx context.Context, transportID *ids.CeptaTransportID, message proto.Message) error {
	var count int
	var candidates []*users.UserID

	// CACHE HIT
	if subscribers, ok := s.getSubscribersForTransport(transportID); ok {
		// if rawCachedSubscribers, ok := s.transportCache.Get(*transportID); ok {
		log.Debug("Cache HIT")
		for _, user := range subscribers {
			s.notifyUser(user, message)
		}
		// CACHE MISS
	} else {
		log.Debug("Cache MISS")
		// Must query the user management service
		stream, err := s.usermgmtClient.GetSubscribersForTransport(ctx, &usermgmtpb.GetSubscribersRequest{
			TransportId: transportID,
		})
		if err != nil {
			return fmt.Errorf("Failed to query subscribers for transport %s: %v", transportID, err)
		}
		for {
			user, err := stream.Recv()
			if err == io.EOF {
				break
			}
			if err != nil {
				log.Warnf("Failed to receive user from stream: %v", err)
				continue
			}
			log.Debugf("Found subscriber: %v", user)
			// Add to cache eventually
			if count < lruMaxEntryLength {
				candidates = append(candidates, user.GetId())
			}

			// Notify user
			s.notifyUser(user.GetId(), message)
			count++
		}
		if count < lruMaxEntryLength {
			s.addUsersToCache(candidates, transportID)
		}
	}
	return nil
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

func (s *NotificationServer) handleKafkaMessages(ctx context.Context) {
	noopTicker := time.NewTicker(time.Second * 10)
	subscriberDone := make(chan bool, 1)
	stopSubscriber := make(chan bool, 1)
	go func() {
		defer func() { subscriberDone <- true }()
		for {
			select {
			case msg := <-s.kc.Messages:
				var notification notificationpb.Notification
				err := proto.Unmarshal(msg.Value, &notification)
				if err != nil {
					log.Errorf("unmarshaling error: ", err)
				}
				log.Debugf("Received notification: %v", notification)

				switch notification.GetNotification().(type) {
				case *notificationpb.Notification_Delay:
					if err := s.notifySubscribersForTransport(ctx, notification.GetDelay().GetTransportId(), notification.GetDelay().GetTransportId()); err != nil {
						log.Errorf("Failed notify subsribers of transport %d: err", notification.GetDelay().GetTransportId(), err)
					}
					break
				}
				break
			case <-noopTicker.C:
				// Noop, may be used for periodic pings
			case <-stopSubscriber:
				return
			}
		}
	}()
	<-subscriberDone
	// wg.Wait()
	noopTicker.Stop()
}

// Setup ...
func (s *NotificationServer) Setup(ctx context.Context, usermgmtConn *grpc.ClientConn) (err error) {
	s.usermgmtClient = usermgmtpb.NewUserManagementClient(usermgmtConn)

	// Fill the cache with recent transports
	s.transportCache, err = lru.New(lruSize)
	if err != nil {
		err = fmt.Errorf("Failed to initialize cache: %v", err)
		return
	}

	// Connect to RabbitMQ
	producer, err := s.rmqpConfig.CreateProducer()
	if err != nil {
		return
	}
	s.producer = &producer
	if err := s.producer.CreateQueue("test"); err != nil {
		panic(err)
	}
	if err := s.producer.Publish([]byte("Test"), "test"); err != nil {
		panic(err)
	}
	if _, err := s.producer.ConsumeQueue("test"); err != nil {
		panic(err)
	}
	log.Debug("Connected to rabbitMQ")

	log.Info("Filling cache")
	s.fillUserCache(ctx)

	s.pool = websocket.NewPool()
	s.pool.Rmq = s.producer

	// Connect to kafka
	if len(s.kafkacConfig.Topics) < 1 || s.kafkacConfig.Topics[0] == "" {
		s.kafkacConfig.Topics = []string{defaultNotificationTopic}
	}
	if s.kafkacConfig.Group == "" {
		s.kafkacConfig.Group = "DelayConsumerGroup"
	}
	log.Infof("Will consume topic %s from %s (group %s)", s.kafkacConfig.Topics, strings.Join(s.kafkacConfig.Brokers, ", "), s.kafkacConfig.Group)
	s.kc, _, err = kafkaconsumer.ConsumeGroup(ctx, s.kafkacConfig)
	if err != nil {
		log.Warnf("Failed to connect to kafka broker (%s) (group %s) on topic %s",
			strings.Join(s.kafkacConfig.Brokers, ", "), s.kafkacConfig.Group, s.kafkacConfig.Topics)
		log.Fatal(err)
	}

	go s.handleKafkaMessages(ctx)
	go s.pool.Start()
	return
}

// ConnectUsermgmt ...
func (s *NotificationServer) ConnectUsermgmt(ctx context.Context, usermgmtConnectionURI string) error {
	log.Info("Connecting to usermgmt service...")
	usermgmtConn, err := grpc.Dial(usermgmtConnectionURI, grpc.WithInsecure(), grpc.WithBlock())
	if err != nil {
		log.Fatalf("Failed to connect to the usermgmt service: %v", err)
	}
	s.Setup(ctx, usermgmtConn)
	return nil
}

func main() {
	var sources []string
	for t := range topics.Topic_value {
		sources = append(sources, t)
	}

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
		&cli.GenericFlag{
			Name: "notifications-topic",
			Value: &clivalues.EnumValue{
				Enum:    sources,
				Default: defaultNotificationTopic,
			},
			EnvVars: []string{"NOTIFICATIONS_TOPIC"},
			Usage:   "topic to subscribe for notifications",
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
				kafkacConfig: kafkaconsumer.Config{}.ParseCli(ctx),
				// rmqcConfig:       rmqc.Config{}.ParseCli(ctx),
				rmqpConfig:       rmqp.Config{}.ParseCli(ctx),
				usermgmtEndpoint: Endpoint{Host: ctx.String("usermgmt-host"), Port: ctx.Int("usermgmt-port")},
			}

			// Register shutdown routine
			setupCtx, cancelSetup := context.WithCancel(context.Background())
			shutdown := make(chan os.Signal)
			signal.Notify(shutdown, syscall.SIGINT, syscall.SIGTERM)
			go func() {
				<-shutdown
				cancelSetup()
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

			if err := server.ConnectUsermgmt(setupCtx, fmt.Sprintf("%s:%d", server.usermgmtEndpoint.Host, server.usermgmtEndpoint.Port)); err != nil {
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

	defer log.Info("Closing sockets")
	defer grpcListener.Close()
	defer wsListener.Close()

	wsDone := make(chan bool)
	grpcDone := make(chan bool)

	// Serve websocket endpoint
	go func() {
		s.wsServer = &http.Server{}
		http.HandleFunc("/ws/notifications", func(w http.ResponseWriter, r *http.Request) {
			s.serveWebsocket(s.pool, w, r)
		})

		if err := s.wsServer.Serve(wsListener); err != http.ErrServerClosed && err != nil {
			log.Error("Failed to serve websocket server: ", err)
		}
		wsDone <- true
	}()

	// Serve GRPC endpoint
	go func() {
		s.grpcServer = grpc.NewServer()
		pb.RegisterNotificationServer(s.grpcServer, s)
		if err := s.grpcServer.Serve(grpcListener); err != http.ErrServerClosed && err != nil {
			log.Error("Failed to serve grpc server: ", err)
		}
		grpcDone <- true
	}()
	<-wsDone
	<-grpcDone
	return nil
}
