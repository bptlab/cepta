package main

import (
	"context"
	"fmt"
	"github.com/bptlab/cepta/osiris/lib/utils"
	"github.com/pkg/errors"
	"io"
	"net"
	"net/http"
	"os"
	"os/signal"
	"reflect"
	"strings"
	"syscall"
	"time"

	"github.com/go-redis/redis"
	"github.com/bptlab/cepta/ci/versioning"
	topics "github.com/bptlab/cepta/models/constants/topic"
	pb "github.com/bptlab/cepta/models/grpc/notification"
	usermgmtpb "github.com/bptlab/cepta/models/grpc/usermgmt"
	notificationpb "github.com/bptlab/cepta/models/internal/notifications/notification"
	"github.com/bptlab/cepta/models/internal/types/ids"
	"github.com/bptlab/cepta/models/internal/types/result"
	"github.com/bptlab/cepta/models/internal/types/users"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libredis "github.com/bptlab/cepta/osiris/lib/redis"
	kafkaconsumer "github.com/bptlab/cepta/osiris/lib/kafka/consumer"
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
	defaultLruSize                  = 1000 // Cache up to 1000 transports
	defaultLruMaxEntryLength        = 1000 // Cache up to 1000 subscribers per transport
	defaultUserNotificationsBufferSize        = int64(200) // Store the most recent 200 notifications for each user (not persistent)
	defaultNotificationTopic = topics.Topic_DELAY_NOTIFICATIONS
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
	Pool           *websocket.Pool

	usermgmtConn *grpc.ClientConn
	usermgmtClient usermgmtpb.UserManagementClient
	grpcServer     *grpc.Server
	wsServer       *http.Server

	kafkacConfig kafkaconsumer.Config
	kc           *kafkaconsumer.Consumer

	redisConfig libredis.Config
	rclient *redis.Client

	usermgmtEndpoint Endpoint

	LruSize int
	LruMaxEntryLength int
	UserNotificationsBufferSize int64
	NotificationTopic topics.Topic
}

// NewNotificationServer ...
func NewNotificationServer(kafkaConfig kafkaconsumer.Config, redisConfig libredis.Config) NotificationServer {
	return NotificationServer{
		kafkacConfig: kafkaConfig,
		redisConfig: redisConfig,
		// Use defaults
		LruSize: defaultLruSize,
		LruMaxEntryLength: defaultLruMaxEntryLength,
		UserNotificationsBufferSize: defaultUserNotificationsBufferSize,
		NotificationTopic: defaultNotificationTopic,
	}
}

// Shutdown ...
func (s *NotificationServer) Shutdown() {
	log.Info("Graceful shutdown")
	if s.rclient != nil {
		log.Info("Closing redis connection")
		if err := s.rclient.Close(); err != nil {
			log.Warnf("Failed to close redis cleanly: %v", err)
		}
	}
	if s.usermgmtConn != nil {
		log.Info("Closing user management client connection")
		if err := s.usermgmtConn.Close(); err != nil {
			log.Warnf("Failed to close user management client connection cleanly: %v", err)
		}
	}
	if s.wsServer != nil {
		log.Info("Stopping websocket server")
		if err := s.wsServer. Shutdown(context.TODO()); err != nil {
			log.Warnf("Failed to close websocket server cleanly: %v", err)
		}
	}
	if s.grpcServer != nil {
		log.Info("Stopping GRPC server")
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
		if err == io.EOF || s.transportCache.Len() >= s.LruSize {
			break
		}
		if err != nil {
			log.Warnf("Failed to receive user from streams: %v", err)
			continue
		}
		log.Debugf("Adding transports for user %v", user)
		for _, transportID := range user.GetTransports() {
			log.Debugf("Adding transport %v to cache", transportID)
			s.addUsersToCache([]*users.UserID{user.GetId()}, transportID)
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

func (s *NotificationServer) notifyUser(userID *users.UserID, notification *notificationpb.Notification) {
	rawMessage, err := proto.Marshal(notification)
	if err != nil {
		log.Error("Failed to marshal proto: ", err)
		return
	}
	log.Debugf("Sending notification to user %v with score %v", userID, float64(getNotificationTime(notification).UnixNano()))
	s.Pool.NotifyUser <- websocket.UserNotification{ID: userID, Msg: rawMessage, Score: float64(getNotificationTime(notification).UnixNano())}
}

func (s *NotificationServer) broadcast(notification *notificationpb.Notification) {
	rawMessage, err := proto.Marshal(notification)
	if err != nil {
		log.Errorf("Failed to marshal notification: %v", err)
		return
	}
	log.Info("Sending broadcast notification to all users")
	s.Pool.Broadcast <- websocket.BroadcastNotification{Msg: rawMessage, Score: float64(getNotificationTime(notification).UnixNano())}
}

func getNotificationTime(notification *notificationpb.Notification) (occurred time.Time) {
	if o, err := utils.FromProtoTime(notification.GetOccurred()); err == nil && !o.IsZero() {
		occurred = o
	} else {
		occurred = time.Now()
	}
	return
}

// When numbers exceeds limit do not set the cache item so it must be streamed every time again which is ok
// Change return type!
func (s *NotificationServer) notifySubscribersForTransport(ctx context.Context, transportID *ids.CeptaTransportID, message *notificationpb.Notification) error {
	var count int
	var candidates []*users.UserID

	log.Debugf("Will notify users for transport %v (cached transports: %v)", transportID, s.transportCache.Keys())
	if subscribers, ok := s.getSubscribersForTransport(transportID); ok {
		log.Debug("Cache HIT: subscribers=%v", subscribers)
		for _, user := range subscribers {
			s.notifyUser(user, message)
		}
	} else {
		log.Debugf("Cache MISS: subscribers=%v", subscribers)
		// Must query the user management service
		stream, err := s.usermgmtClient.GetSubscribersForTransport(ctx, &usermgmtpb.GetSubscribersRequest{
			TransportId: transportID,
		})
		if err != nil {
			return fmt.Errorf("failed to query subscribers for transport %v: %v", transportID, err)
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
			if count < s.LruMaxEntryLength {
				candidates = append(candidates, user.GetId())
			}

			// Notify user
			s.notifyUser(user.GetId(), message)
			count++
		}
		if count < s.LruMaxEntryLength {
			s.addUsersToCache(candidates, transportID)
		}
	}
	return nil
}

func (s *NotificationServer) serveWebsocket(pool *websocket.Pool, w http.ResponseWriter, r *http.Request) {
	conn, err := websocket.Upgrade(w, r)
	if err != nil {
		log.Errorf("Failed to upgrade to websocket connection in response handler %v: %v", w, err)
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
					log.Errorf("unmarshal error: %v", err)
				}
				log.Debugf("Received notification: %v (%v)", notification, msg.Timestamp)

				// Set occurrence time as a best effort
				if occurred := notification.GetOccurred(); occurred.GetSeconds() + int64(occurred.GetNanos()) < 1 && !msg.Timestamp.IsZero() {
					if occurredProto, err := utils.ToProtoTime(msg.Timestamp); err != nil {
						notification.Occurred = occurredProto
					}
				}

				switch notification.GetNotification().(type) {
				case *notificationpb.Notification_Delay:
					// Just for testing porpuses
					// s.broadcast(&notification)
					// Users need to be assigned to transports to do this

					if err := s.notifySubscribersForTransport(ctx, notification.GetDelay().GetTransportId(), &notification); err != nil {
						log.Errorf("Failed to notify subscribers of transport %v: %v", notification.GetDelay().GetTransportId(), err)
					}
					break
				case *notificationpb.Notification_System:
					s.broadcast(&notification)
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
	s.usermgmtConn = usermgmtConn
	s.usermgmtClient = usermgmtpb.NewUserManagementClient(usermgmtConn)

	// Fill the cache with recent transports
	s.transportCache, err = lru.New(s.LruSize)
	if err != nil {
		err = fmt.Errorf("failed to initialize cache: %v", err)
		return
	}

	// Connect to redis
	log.Info("Connecting to redis...")
	connected := make(chan error)
	go func() {
		s.rclient = redis.NewClient(&redis.Options{
			Addr:     s.redisConfig.ConnectionURI(),
			Password: s.redisConfig.Password,
			DB:       s.redisConfig.Database,
		})
		_, err := s.rclient.Ping().Result()
		connected <- err
	}()

	select {
	case <-ctx.Done():
		err = errors.New("Setup was cancelled")
		return
	case err := <-connected:
		if err != nil {
			return fmt.Errorf("failed to connect to redis: %v", err)
		}
		break
	}

	log.Info("Filling cache")
	s.fillUserCache(ctx)

	s.Pool = websocket.NewPool(s.UserNotificationsBufferSize)
	s.Pool.Rclient = s.rclient

	// Connect to kafka
	if len(s.kafkacConfig.Topics) < 1 || s.kafkacConfig.Topics[0] == "" {
		s.kafkacConfig.Topics = []string{s.NotificationTopic.String()}
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
	go s.Pool.Start()
	return
}

// ConnectUsermgmt ...
func (s *NotificationServer) ConnectUsermgmt(ctx context.Context, usermgmtConnectionURI string) error {
	connected := make(chan error)
	go func() {
		log.Info("Connecting to usermgmt service...")
		usermgmtConn, err := grpc.Dial(usermgmtConnectionURI, grpc.WithInsecure())
		if err != nil {
			connected <- fmt.Errorf("Failed to connect to the usermgmt service: %v", err)
			return
		}
		connected <- s.Setup(ctx, usermgmtConn)
	}()
	select {
	case <-ctx.Done():
		return errors.New("Setup was cancelled")
	case err := <-connected:
		return err
	}
}

func main() {
	var sources []string
	for t := range topics.Topic_value {
		sources = append(sources, t)
	}

	cliFlags := []cli.Flag{}
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceLogLevel)...)
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceConnectionTolerance)...)
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.Redis)...)
	cliFlags = append(cliFlags, kafkaconsumer.CliOptions...)
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
				Default: defaultNotificationTopic.String(),
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

			server := NewNotificationServer(kafkaconsumer.Config{}.ParseCli(ctx), libredis.Config{}.ParseCli(ctx))
			server.usermgmtEndpoint = Endpoint{Host: ctx.String("usermgmt-host"), Port: ctx.Int("usermgmt-port")}

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
		mux := http.NewServeMux()
		s.wsServer = &http.Server{Handler: mux}
		mux.HandleFunc("/ws/notifications", func(w http.ResponseWriter, r *http.Request) {
			s.serveWebsocket(s.Pool, w, r)
		})

		if err := s.wsServer.Serve(wsListener); err != http.ErrServerClosed && err != nil {
			log.Errorf("Failed to serve websocket server: %v", err)
		}
		wsDone <- true
	}()

	// Serve GRPC endpoint
	go func() {
		s.grpcServer = grpc.NewServer()
		pb.RegisterNotificationServer(s.grpcServer, s)
		if err := s.grpcServer.Serve(grpcListener); err != http.ErrServerClosed && err != nil {
			log.Errorf("Failed to serve GRPC server: %v", err)
		}
		grpcDone <- true
	}()
	<-wsDone
	<-grpcDone
	return nil
}
