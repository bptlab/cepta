package main

import (
	"context"
	"io"
	"testing"
	"time"

	// "github.com/bptlab/cepta/auxiliary/producers/replayer/tests/provider"
	topics "github.com/bptlab/cepta/models/constants/topic"
	livetrainpb "github.com/bptlab/cepta/models/events/livetraindataevent"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	"github.com/bptlab/cepta/models/types/result"
	kafkaconsumer "github.com/bptlab/cepta/osiris/lib/kafka/consumer"
	"github.com/bptlab/cepta/osiris/lib/utils"
	"github.com/golang/protobuf/proto"
	tspb "github.com/golang/protobuf/ptypes/timestamp"
)

// Test ideas:
// - Test constant and proportional time replay with different speed levels (replay and query)
// - Test query finds correct items
// - Test start / stop resumes
// - Test repeat repeats
// - Test extracted time (replay and query)
// - Test Reset

// TestStatus ...
func TestStatus(t *testing.T) {
	return
	test := new(Test).Setup(t)
	defer test.Teardown()

	// Assert status is inactive after startup
	test.AssertStatusIs(t, &pb.ReplayStatus{Active: false})

	// Assert status is active after starting with one source
	_, err := test.ReplayerClient.Start(context.Background(), &pb.ReplayStartOptions{
		Sources: []*pb.SourceQueryOptions{
			{Source: topics.Topic_GPS_TRIP_UPDATE_DATA},
		},
	})
	if err != nil {
		t.Fatalf("Failed to start the replayer: %v", err)
	}
	test.AssertStatusIs(t, &pb.ReplayStatus{Active: true})

	// Assert status is inactive after stopping
	_, err = test.ReplayerClient.Stop(context.Background(), &result.Empty{})
	if err != nil {
		t.Fatalf("Failed to stop the replayer: %v", err)
	}
	test.AssertStatusIs(t, &pb.ReplayStatus{Active: false})

	// Assert status is active after starting with no source (will default to replaying all sources)
	_, err = test.ReplayerClient.Start(context.Background(), &pb.ReplayStartOptions{
		Sources: []*pb.SourceQueryOptions{},
	})
	if err != nil {
		t.Fatalf("Failed to start the replayer: %v", err)
	}
	test.AssertStatusIs(t, &pb.ReplayStatus{Active: true})
}

func toProtoTime(t time.Time) *tspb.Timestamp {
	tt, err := utils.ToProtoTime(t)
	if err != nil {
		panic(err)
	}
	return tt
}

// TestReset ...
func TestReset(t *testing.T) {
	// return
	test := new(Test).Setup(t)
	defer test.Teardown()

	// Insert test data
	test.InsertForTopic(topics.Topic_LIVE_TRAIN_DATA, []proto.Message{
		&livetrainpb.LiveTrainData{TrainId: 1, IngestionTime: toProtoTime(time.Now().Add(5 * time.Second))},
		&livetrainpb.LiveTrainData{TrainId: 2, IngestionTime: toProtoTime(time.Now().Add(10 * time.Second))},
		&livetrainpb.LiveTrainData{TrainId: 3, IngestionTime: toProtoTime(time.Now().Add(15 * time.Second))},
		&livetrainpb.LiveTrainData{TrainId: 4, IngestionTime: toProtoTime(time.Now().Add(20 * time.Second))},
	})

	// Start the consumer
	options := test.KafkaConsumerConfig
	options.Topics = []string{topics.Topic_LIVE_TRAIN_DATA.String()}
	kafkaConsumer, err := kafkaconsumer.KafkaConsumer{}.ConsumeGroup(context.Background(), options)
	if err != nil {
		t.Fatalf("Failed to start the kafka consumer: %v", err)
	}

	// Start the replayer for Topic_LIVE_TRAIN_DATA
	_, err = test.ReplayerClient.Start(context.Background(), &pb.ReplayStartOptions{
		Sources: []*pb.SourceQueryOptions{
			{Source: topics.Topic_LIVE_TRAIN_DATA, Options: &pb.ReplayOptions{Speed: &pb.Speed{Speed: 2000}, Mode: pb.ReplayMode_CONSTANT}}, // 2 Secs
		},
	})
	if err != nil {
		t.Fatalf("Failed to start the replayer: %v", err)
	}

	// Test reset after a few receives
	t.Error("Waiting for messages YAY!")
	for {
		msg := <-kafkaConsumer.Messages
		t.Error(msg)
	}

}

// TestOptions ...
func TestOptions(t *testing.T) {
	return
	test := new(Test).Setup(t)
	defer test.Teardown()

	// time.Sleep(200 * time.Second)

	// Test initially there are no options
	test.AssertHasOptions(t, &pb.ReplayStartOptions{
		Options: &pb.ReplayOptions{},
	})

	// Test setting of new options when starting the replay
	startOptions := &pb.ReplayStartOptions{
		Sources: []*pb.SourceQueryOptions{
			{Source: topics.Topic_GPS_TRIP_UPDATE_DATA},
			{Source: topics.Topic_LIVE_TRAIN_DATA},
		},
		Options: &pb.ReplayOptions{Speed: &pb.Speed{Speed: 20}},
	}
	expectedOptions := &pb.ReplayStartOptions{
		Sources: []*pb.SourceQueryOptions{
			{Source: topics.Topic_GPS_TRIP_UPDATE_DATA, Options: &pb.ReplayOptions{}},
			{Source: topics.Topic_LIVE_TRAIN_DATA, Options: &pb.ReplayOptions{}},
		},
		Options: &pb.ReplayOptions{Speed: &pb.Speed{Speed: 20}},
	}
	_, err := test.ReplayerClient.Start(context.Background(), startOptions)
	if err != nil {
		t.Fatalf("Failed to start the replayer: %v", err)
	}
	test.AssertHasOptions(t, expectedOptions)

	// Test setting options during replay
	expectedOptions = &pb.ReplayStartOptions{
		Sources: []*pb.SourceQueryOptions{
			{Source: topics.Topic_GPS_TRIP_UPDATE_DATA, Options: &pb.ReplayOptions{Speed: &pb.Speed{Speed: 10}}},
			{Source: topics.Topic_LIVE_TRAIN_DATA, Options: &pb.ReplayOptions{}},
		},
		Options: &pb.ReplayOptions{Speed: &pb.Speed{Speed: 30}},
	}
	_, err = test.ReplayerClient.SetOptions(context.Background(), &pb.ReplaySetOptionsRequest{
		Sources: []*pb.ActiveReplayOptions{
			{Source: topics.Topic_GPS_TRIP_UPDATE_DATA, Speed: &pb.Speed{Speed: 10}},
		},
		Options: &pb.ActiveReplayOptions{Speed: &pb.Speed{Speed: 30}},
	})
	if err != nil {
		t.Fatalf("Failed to start the replayer: %v", err)
	}
	test.AssertHasOptions(t, expectedOptions)
}

// TestQuery ...
func TestQuery(t *testing.T) {
	return
	test := new(Test).Setup(t)
	defer test.Teardown()
	request := &pb.QueryOptions{Sources: []*pb.SourceQueryOptions{
		{
			Source:  topics.Topic_GPS_TRIP_UPDATE_DATA,
			Options: &pb.ReplayOptions{Limit: 100},
		},
	}}
	stream, err := test.ReplayerClient.Query(context.Background(), request)
	if err != nil {
		t.Errorf("Failed to query replayer: %s", err.Error())
	}
	var c int
	for {
		_, err := stream.Recv()
		if err == io.EOF {
			break
		}
		if err != nil {
			t.Errorf("%v.Query(_) = _, %v", test.ReplayerClient, err)
		}
		c++
	}
	if c != 100 {
		// t.Errorf("Query returned wrong number of results")
	}
}
