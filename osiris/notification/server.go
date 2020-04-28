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
	pbUsermgmt "github.com/bptlab/cepta/models/grpc/usermgmt"
	"github.com/bptlab/cepta/models/types/result"
	"github.com/bptlab/cepta/models/types/transports"
	"github.com/bptlab/cepta/models/types/users"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	kafkaconsumer "github.com/bptlab/cepta/osiris/lib/kafka/consumer"
	rmqConsumer "github.com/bptlab/cepta/osiris/lib/rabbitmq/consumer"
	rmqProducer "github.com/bptlab/cepta/osiris/lib/rabbitmq/producer"
	"github.com/bptlab/cepta/osiris/notification/websocket"

	"github.com/golang/protobuf/jsonpb"
	"github.com/golang/protobuf/proto"
	lru "github.com/hashicorp/golang-lru"
	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
	"google.golang.org/grpc"
)

var (
	Version   string = "Unknown"
	BuildTime string = ""
	marshaler        = &jsonpb.Marshaler{EmitDefaults: true}

	server         NotificationServer
	usermgmtClient pbUsermgmt.UserManagementClient
	grpcServer     *grpc.Server
	userCache      *lru.Cache
)

const (
	lruSize = 1000
)

type NotificationServer struct {
	pb.UnimplementedNotificationServer
	KafkaConsumerConfig    kafkaconsumer.Config
	RabbitMQConsumerConfig rmqConsumer.RabbitMQConsumerOptions
	RabbitMQProducerConfig rmqProducer.RabbitMQProducerOptions
}

func NewNotificationServer(kafkaConfig kafkaconsumer.Config, rmqConsumerConfig rmqConsumer.RabbitMQConsumerOptions, rmqProducerConfig rmqProducer.RabbitMQProducerOptions) NotificationServer {
	return NotificationServer{
		KafkaConsumerConfig:    kafkaConfig,
		RabbitMQConsumerConfig: rmqConsumerConfig,
		RabbitMQProducerConfig: rmqProducerConfig,
	}
}

func fillUserCache() {
	stream, err := usermgmtClient.GetAllUser(context.Background(), &result.Empty{})
	if err != nil {
		log.Fatalf("Failed to receive stream from the UserManagement Service %v", err)
	}

	for {
		user, err := stream.Recv()
		if err == io.EOF || userCache.Len() == lruSize {
			break
		}
		if err != nil {
			log.Fatalf("Failed to receive user from our stream: %v", err)
		}
		log.Debug(user)

		for _, train := range user.Transports {
			log.Debug(train)
			evicted := userCache.Add(train.Id, user.Id)
			log.Debug(evicted)
		}
	}
	return
}

func findUser(ctx context.Context, trainID int64) (*users.User, error) {
	userExists := userCache.Contains(trainID)

	if userExists {
		uid, _ := userCache.Get(trainID)
		return uid.(*users.User), nil
	} else {
		userRequest := &pbUsermgmt.GetUserRequest{
			TrainId: &transports.TransportID{
				Id: strconv.Itoa(int(trainID)),
			},
		}
		usr, err := usermgmtClient.GetUser(ctx, userRequest)
		if err != nil {
			return nil, fmt.Errorf("Failed to receive user to the trainID with error message: %v", err)
		}
		evicted := userCache.Add(trainID, usr.Id)
		log.Debug(evicted)
		return usr, nil
	}
}

func serveRabbitMQConsumer(options rmqConsumer.RabbitMQConsumerOptions) {
	conn, ch := rmqConsumer.Setup(options)

	//Consume Messages from the Queue
	rmqConsumer.Consume(ch, conn, options)
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

func subscribeKafkaToPool(ctx context.Context, pool *websocket.Pool, options kafkaconsumer.Config, rabbitMqOptions rmqProducer.RabbitMQProducerOptions) {
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

	// Connect to RabbitMQ and define channel
	rabbitMqConnection, rabbitMqChannel := rmqProducer.Setup(rabbitMqOptions)
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

				uid, err := findUser(ctx, delayEvent.TrainId)
				if err != nil {
					log.Fatal(err)
				}
				rabbitMqOptions.ExchangeRoutingKey = uid.Id.Id
				rmqProducer.Publish(delayEvent, rabbitMqOptions, rabbitMqChannel)

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
	_, err := lru.New(lruSize)
	if err != nil {
		log.Fatalf("Failed to initialize cache: %v", err)
	}
	kafkaOptions := kafkaconsumer.Config{}.ParseCli(cliCtx)
	rabbitmqProducerOptions := rmqProducer.RabbitMQProducerOptions{}.ParseCli(cliCtx)
	rabbitmqConsumerOptions := rmqConsumer.RabbitMQConsumerOptions{}.ParseCli(cliCtx)
	pool := websocket.NewPool()

	// connect to Usermgmt Service
	usermgmtPort := 5555
	usermgmtHost := "localhost"
	usermgmtAddress := fmt.Sprintf("%s:%d", usermgmtHost, usermgmtPort)
	usermgmtConn, err := grpc.Dial(usermgmtAddress)
	if err != nil {
		log.Fatalf("Failed to connect to the User-management service: %v", err)
	}
	defer usermgmtConn.Close()

	usermgmtClient = pbUsermgmt.NewUserManagementClient(usermgmtConn)

	// Fill our cache with users and there transports
	go fillUserCache()
	go pool.Start()

	// TODO: We need to serve more than just one user and therefore need more than one rabbitmqConsumer
	go serveRabbitMQConsumer(rabbitmqConsumerOptions)
	go subscribeKafkaToPool(ctx, pool, kafkaOptions, rabbitmqProducerOptions)

	http.HandleFunc("/ws/userdata", func(w http.ResponseWriter, r *http.Request) {
		serveWebsocket(pool, w, r)
	})
	port := fmt.Sprintf(":%d", cliCtx.Int("port"))
	log.Printf("Server ready at %s", port)
	log.Fatal(http.ListenAndServe(port, nil))
	return nil
}

func main() {
	shutdown := make(chan os.Signal)
	signal.Notify(shutdown, syscall.SIGINT, syscall.SIGTERM)
	go func() {
		<-shutdown
		server.Shutdown()
	}()

	cliFlags := []cli.Flag{}
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServicePort, libcli.ServiceLogLevel)...)
	cliFlags = append(cliFlags, libcli.CommonCliOptions(libcli.ServiceConnectionTolerance)...)
	cliFlags = append(cliFlags, kafkaconsumer.CliOptions...)
	cliFlags = append(cliFlags, rmqConsumer.RabbitMQConsumerCliOptions...)

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

			server = NotificationServer{
				KafkaConsumerConfig:    kafkaconsumer.Config{}.ParseCli(ctx),
				RabbitMQConsumerConfig: rmqConsumer.RabbitMQConsumerOptions{}.ParseCli(ctx),
				RabbitMQProducerConfig: rmqProducer.RabbitMQProducerOptions{}.ParseCli(ctx),
			}

			port := fmt.Sprintf(":%d", ctx.Int("port"))
			listener, err := net.Listen("tcp", port)
			if err != nil {
				return fmt.Errorf("failed to listen: %v", err)
			}

			if err := server.Setup(); err != nil {
				return err
			}

			if err := server.Serve(listener); err != nil {
				return err
			}

			return serve(ctx)
		},
	}

	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}

func (s *NotificationServer) Setup() error {
	return nil
}

func (s *NotificationServer) Serve(listener net.Listener) error {
	log.Infof("Notification service ready at %s", listener.Addr())
	grpcServer = grpc.NewServer()
	pb.RegisterNotificationServer(grpcServer, s)
	if err := grpcServer.Serve(listener); err != nil {
		return err
	}
	log.Info("Closing socket")
	listener.Close()
	return nil
}

func (s *NotificationServer) Shutdown() {
	log.Info("Graceful shutdown")
	log.Info("Stopping GRPC server")
	grpcServer.Stop()
}
