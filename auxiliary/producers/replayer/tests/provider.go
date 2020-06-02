package main

import (
	"context"
	"fmt"
	"io"
	"net"
	"strconv"
	"sync"
	"testing"
	"time"

	"github.com/Shopify/sarama"
	topics "github.com/bptlab/cepta/models/constants/topic"
	eventpb "github.com/bptlab/cepta/models/events/event"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	kafka "github.com/bptlab/cepta/osiris/lib/kafka"
	kafkaconsumer "github.com/bptlab/cepta/osiris/lib/kafka/consumer"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/bptlab/cepta/osiris/lib/utils"
	"github.com/golang/protobuf/proto"
	tspb "github.com/golang/protobuf/ptypes/timestamp"
	"github.com/golang/protobuf/ptypes/wrappers"
	"github.com/romnnn/bsonpb"
	tc "github.com/romnnn/testcontainers"
	"github.com/romnnn/testcontainers-go"
	tckafka "github.com/romnnn/testcontainers/kafka"
	tcmongo "github.com/romnnn/testcontainers/mongo"
	"github.com/sirupsen/logrus"
	log "github.com/sirupsen/logrus"
	"go.mongodb.org/mongo-driver/mongo"
	"google.golang.org/grpc"
	"google.golang.org/grpc/test/bufconn"
)

const logLevel = logrus.ErrorLevel
const bufSize = 1024 * 1024

type dialerFunc = func(string, time.Duration) (net.Conn, error)

func dailerFor(listener *bufconn.Listener) dialerFunc {
	return func(string, time.Duration) (net.Conn, error) {
		return listener.Dial()
	}
}

func setUpReplayerServer(t *testing.T, listener *bufconn.Listener, mongoConfig libdb.MongoDBConfig, kafkaConfig kafkaproducer.Config) (*ReplayerServer, error) {
	r := NewReplayerServer(mongoConfig, kafkaConfig)
	if err := r.Setup(context.Background()); err != nil {
		t.Fatalf("Failed to setup replayer server: %v", err)
	}
	go func() {
		if err := r.Serve(listener); err != nil {
			t.Fatalf("Failed to serve the replayer: %v", err)
		}
	}()
	return &r, nil
}

// Test ...
type Test struct {
	Net                 testcontainers.Network
	MongoC              testcontainers.Container
	KafkaC              testcontainers.Container
	ZkC                 testcontainers.Container
	KafkaProducerConfig kafkaproducer.Config
	KafkaConsumerConfig kafkaconsumer.Config
	MongoConfig         libdb.MongoDBConfig
	ReplayerEndpoint    *grpc.ClientConn
	ReplayerServer      *ReplayerServer
	ReplayerClient      pb.ReplayerClient
}

// Setup ...
func (test *Test) Setup(t *testing.T) *Test {
	var err error
	log.SetLevel(logLevel)
	if parallel {
		t.Parallel()
	}

	networkName := fmt.Sprintf("test-network-%s", tc.UniqueID())
	test.Net, err = tc.CreateNetwork(testcontainers.NetworkRequest{
		Driver:         "bridge",
		Name:           networkName,
		Attachable:     true,
		CheckDuplicate: true,
	}, 5)
	if err != nil {
		t.Fatalf("Failed to create the docker test network: %v", err)
		return test
	}
	defer test.Net.Remove(context.Background())

	containerOptions := tc.ContainerOptions{
		ContainerRequest: testcontainers.ContainerRequest{
			Networks: []string{networkName},
		},
	}

	kafkaContainerOptions := containerOptions
	/*
		kafkaContainerOptions.ContainerRequest.Resources = &testcontainers.ContainerResourcers{
			Memory:     1000 * 1024 * 1024, // max. 1GB
			MemorySwap: -1,               // Unlimited swap
		}
	*/

	// Start mongodb container
	var mongoConfig tcmongo.DBConfig
	test.MongoC, mongoConfig, err = tcmongo.StartMongoContainer(tcmongo.ContainerOptions{ContainerOptions: containerOptions})
	if err != nil {
		t.Fatalf("Failed to start the mongodb container: %v", err)
		return test
	}
	test.MongoConfig = libdb.MongoDBConfig{
		Host:                mongoConfig.Host,
		Port:                mongoConfig.Port,
		User:                mongoConfig.User,
		Database:            fmt.Sprintf("mockdatabase-%s", tc.UniqueID()),
		Password:            mongoConfig.Password,
		ConnectionTolerance: libcli.ConnectionTolerance{TimeoutSec: 60},
	}

	// Start kafka container
	var kafkaConfig *tckafka.ContainerConnectionConfig
	test.KafkaC, kafkaConfig, test.ZkC, _, err = tckafka.StartKafkaContainer(tckafka.ContainerOptions{ContainerOptions: kafkaContainerOptions})
	if err != nil {
		t.Fatalf("Failed to start the kafka container: %v", err)
		return test
	}
	test.KafkaConsumerConfig = kafkaconsumer.Config{
		Config: kafka.Config{
			Brokers:             kafkaConfig.Brokers,
			Version:             kafkaConfig.KafkaVersion,
			ConnectionTolerance: libcli.ConnectionTolerance{TimeoutSec: 60},
		},
		Group: fmt.Sprintf("TestConsumerGroup-%s", tc.UniqueID()),
	}
	test.KafkaProducerConfig = kafkaproducer.Config{
		Config: kafka.Config{
			Brokers:             kafkaConfig.Brokers,
			Version:             kafkaConfig.KafkaVersion,
			ConnectionTolerance: libcli.ConnectionTolerance{TimeoutSec: 60},
		},
	}

	var replayerListener = bufconn.Listen(bufSize)

	// Create endpoint
	test.ReplayerEndpoint, err = grpc.DialContext(context.Background(), "bufnet", grpc.WithContextDialer(func(context.Context, string) (net.Conn, error) {
		return replayerListener.Dial()
	}), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("Failed to dial bufnet: %v", err)
		return test
	}

	// Start the GRPC server
	test.ReplayerServer, err = setUpReplayerServer(t, replayerListener, test.MongoConfig, test.KafkaProducerConfig)
	if err != nil {
		t.Fatalf("Failed to setup the replayer service: %v", err)
		return test
	}

	test.ReplayerClient = pb.NewReplayerClient(test.ReplayerEndpoint)
	return test
}

// Teardown ...
func (test *Test) Teardown() {
	test.ReplayerServer.Shutdown()
	_ = test.ReplayerEndpoint.Close()
	_ = test.MongoC.Terminate(context.Background())
	_ = test.KafkaC.Terminate(context.Background())
	_ = test.ZkC.Terminate(context.Background())
	test.Net.Remove(context.Background())
}

func (test *Test) startWithSpeed(t *testing.T, speedup int, mode pb.ReplayMode) {
	_, err := test.ReplayerClient.Start(context.Background(), &pb.ReplayStartOptions{
		Sources: []*pb.SourceReplay{
			{Source: topics.Topic_LIVE_TRAIN_DATA, Options: &pb.ReplayOptions{Speed: &pb.Speed{Speed: int32(speedup)}, Mode: mode, Repeat: &wrappers.BoolValue{Value: true}}},
		},
	})
	if err != nil {
		t.Fatalf("Failed to start the replayer: %v", err)
	}
}

func (test *Test) startConsumer(t *testing.T) (*kafkaconsumer.Consumer, context.CancelFunc, *sync.WaitGroup) {
	ctx, cancel := context.WithCancel(context.Background())
	options := test.KafkaConsumerConfig
	options.Topics = []string{topics.Topic_LIVE_TRAIN_DATA.String()}
	kafkaConsumer, wg, err := kafkaconsumer.ConsumeGroup(ctx, options)
	if err != nil {
		t.Fatalf("Failed to start the kafka consumer: %v", err)
	}
	return kafkaConsumer, cancel, wg
}

func (test *Test) streamResults(t *testing.T, request *pb.QueryOptions) (results []*pb.ReplayedEvent) {
	stream, err := test.ReplayerClient.Query(context.Background(), request)
	if err != nil {
		t.Fatalf("Failed to query replayer: %v", err)
		return
	}
	for {
		result, err := stream.Recv()
		if err == io.EOF {
			break
		}
		if err != nil {
			t.Errorf("Error during receive of stream: %v", err)
		}
		results = append(results, result)
	}
	return
}

func mapLiveTrainQueryIds(results []*pb.ReplayedEvent) []string {
	var observed []string
	for _, r := range results {
		observed = append(observed, strconv.Itoa(int(r.GetEvent().GetLiveTrain().GetTrainId())))
	}
	return observed
}

func mapLiveTrainQueryTimes(results []*pb.ReplayedEvent) []time.Time {
	var observed []time.Time
	for _, r := range results {
		observed = append(observed, fromProtoTime(r.GetReplayTimestamp()))
	}
	return observed
}

func (test *Test) insertForTopic(topic topics.Topic, entries []proto.Message) error {
	mongoDB, err := libdb.MongoDatabase(&test.MongoConfig)
	if err != nil {
		return fmt.Errorf("Failed to initialize mongo database: %v", err)
	}
	defer mongoDB.Close()

	// Get collection for selected topic
	var collection *mongo.Collection
	for _, r := range test.ReplayerServer.Replayers {
		if r.Topic == topic {
			collection = mongoDB.DB.Collection(r.SourceName)
			break
		}
	}
	if collection == nil {
		return fmt.Errorf("No collection for source %s", topic.String())
	}

	var docs []interface{}
	marshaler := bsonpb.Marshaler{}
	for _, e := range entries {
		bson, err := marshaler.Marshal(e)
		if err != nil {
			return fmt.Errorf("Failed to marshal with error: %v", err)
		}
		docs = append(docs, bson)
	}

	log.Debugf("%d docs into collection %s", len(docs), collection.Name())
	_, err = collection.InsertMany(context.Background(), docs)
	if err != nil {
		return err
	}
	return nil
}

func toProtoTime(t time.Time) *tspb.Timestamp {
	tt, err := utils.ToProtoTime(t)
	if err != nil {
		panic(err)
	}
	return tt
}

func fromProtoTime(t *tspb.Timestamp) time.Time {
	tt, err := utils.FromProtoTime(t)
	if err != nil {
		panic(err)
	}
	return tt
}

type firstEvents struct {
	count    int
	consumer *kafkaconsumer.Consumer
}

func (e firstEvents) do(handler func(index int, event eventpb.Event, raw *sarama.ConsumerMessage, err error)) {
	done := make(chan bool)
	var i int
	go func() {
		for {
			if i == e.count {
				done <- true
			}
			msg := <-e.consumer.Messages
			if i < e.count {
				var event eventpb.Event
				err := proto.Unmarshal(msg.Value, &event)
				handler(i, event, msg, err)
			}
			i++
		}
	}()
	<-done
}
