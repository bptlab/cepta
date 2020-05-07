package main

import (
	"context"
	"reflect"
	"testing"
	"time"

	"github.com/Shopify/sarama"
	topics "github.com/bptlab/cepta/models/constants/topic"
	eventpb "github.com/bptlab/cepta/models/events/event"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	"github.com/bptlab/cepta/models/internal/types/result"
	kafkaconsumer "github.com/bptlab/cepta/osiris/lib/kafka/consumer"
	"github.com/golang/protobuf/ptypes/wrappers"
	"github.com/romnnn/deepequal"
	log "github.com/sirupsen/logrus"
)

const parallel = true

// TestReplayStatus tests if the status is updated as the replay is started and stopped
func TestReplayStatus(t *testing.T) {
	// t.Skip()
	test := new(Test).Setup(t)
	defer test.Teardown()

	// Assert status is inactive after startup
	test.AssertStatusIs(t, &pb.ReplayStatus{Active: false})

	// Assert status is active after starting with one source
	_, err := test.ReplayerClient.Start(context.Background(), &pb.ReplayStartOptions{
		Sources: []*pb.SourceReplay{
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
		Sources: []*pb.SourceReplay{},
	})
	if err != nil {
		t.Fatalf("Failed to start the replayer: %v", err)
	}
	test.AssertStatusIs(t, &pb.ReplayStatus{Active: true})
}

// TestReplayRepeatAndReset tests repeat and reset. Repeat is implemented as a reset after the entire dataset has been replayed
func TestReplayRepeatAndReset(t *testing.T) {
	// t.Skip()
	test := new(Test).Setup(t)
	defer test.Teardown()

	// Insert test data
	if err := test.insertForTopic(topics.Topic_LIVE_TRAIN_DATA, ascendingLiveTrainEvents(4, 5)); err != nil {
		t.Fatalf("Failed to insert mock data for topic %s: %v", topics.Topic_LIVE_TRAIN_DATA, err)
	}

	// Start the consumer
	options := test.KafkaConsumerConfig
	options.Topics = []string{topics.Topic_LIVE_TRAIN_DATA.String()}
	ctx, cancel := context.WithCancel(context.Background())
	kafkaConsumer, wg, err := kafkaconsumer.ConsumeGroup(ctx, options)
	if err != nil {
		t.Fatalf("Failed to start the kafka consumer: %v", err)
	}

	// Start the replayer for Topic_LIVE_TRAIN_DATA
	_, err = test.ReplayerClient.Start(context.Background(), &pb.ReplayStartOptions{
		Sources: []*pb.SourceReplay{
			{Source: topics.Topic_LIVE_TRAIN_DATA, Options: &pb.ReplayOptions{Speed: &pb.Speed{Speed: 200}, Mode: pb.ReplayMode_CONSTANT, Repeat: &wrappers.BoolValue{Value: true}}}, // 200 MSec
		},
	})
	if err != nil {
		t.Fatalf("Failed to start the replayer: %v", err)
	}

	expected := []int64{1, 2, 3, 4, 1, 2, 3, 4}
	var observed []int64
	firstEvents{len(expected), kafkaConsumer}.do(func(index int, event eventpb.Event, raw *sarama.ConsumerMessage, err error) {
		if err != nil {
			t.Error(err)
		} else {
			observed = append(observed, event.GetLiveTrain().GetTrainId())
		}
	})

	// TODO: Detect pattern of resetting all the time, works only with long pause or with limit
	/*
		if i > 6 {
			_, err = test.ReplayerClient.Start(context.Background(), &pb.ReplayStartOptions{
				Sources: []*pb.SourceQueryOptions{
					{Source: topics.Topic_LIVE_TRAIN_DATA, Options: &pb.ReplayOptions{Speed: &pb.Speed{Speed: 200}, Mode: pb.ReplayMode_CONSTANT, Repeat: &wrappers.BoolValue{Value: true}}}, // 200 MSec
				},
			})
		}
	*/

	if equal, err := deepequal.DeepEqual(expected, observed); !equal {
		t.Fatalf("Expected order of events to be %v but got %v: %v", expected, observed, err)
	}

	cancel()
	wg.Wait()
}

// TestReplayStartStop tests starting and stopping the replay. Replayer should resume and the order of events should remain the same unless reset.
func TestReplayStartStop(t *testing.T) {
	// t.Skip()
	test := new(Test).Setup(t)
	defer test.Teardown()

	// Insert test data
	if err := test.insertForTopic(topics.Topic_LIVE_TRAIN_DATA, ascendingLiveTrainEvents(100, 5)); err != nil {
		t.Fatalf("Failed to insert mock data for topic %s: %v", topics.Topic_LIVE_TRAIN_DATA, err)
	}

	speed := 1000
	consumer, cancel, wg := test.startConsumer(t)
	test.startWithSpeed(t, speed, pb.ReplayMode_CONSTANT)
	var expected, observed []int64
	for i := int64(1); i < 90; i++ {
		expected = append(expected, i)
	}
	firstEvents{len(expected), consumer}.do(func(index int, event eventpb.Event, raw *sarama.ConsumerMessage, err error) {
		if err != nil {
			t.Error(err)
		} else {
			observed = append(observed, event.GetLiveTrain().GetTrainId())
			if index == 10 {
				go func() {
					if _, err := test.ReplayerClient.Stop(context.Background(), &result.Empty{}); err != nil {
						t.Fatalf("Failed to stop the replayer: %v", err)
					}
					test.startWithSpeed(t, speed, pb.ReplayMode_CONSTANT)
				}()
			}
		}
	})
	if equal, err := deepequal.DeepEqual(expected, observed); !equal {
		t.Fatalf("Expected order of events to be %v but got %v: %v", expected, observed, err)
	}
	cancel()
	wg.Wait()
}

// TestReplayConstantMode tests the constant speed mode. Events should be published to kafka with constant intervals
func TestReplayConstantMode(t *testing.T) {
	// t.Skip()
	test := new(Test).Setup(t)
	defer test.Teardown()

	// Insert test data
	if err := test.insertForTopic(topics.Topic_LIVE_TRAIN_DATA, ascendingLiveTrainEvents(15, 5)); err != nil {
		t.Fatalf("Failed to insert mock data for topic %s: %v", topics.Topic_LIVE_TRAIN_DATA, err)
	}

	var intervalSecs int
	consumer, cancel, wg := test.startConsumer(t)
	intervalSecs = 2
	test.startWithSpeed(t, intervalSecs*1000, pb.ReplayMode_CONSTANT)
	assertReplayInterval(t, consumer, intervalOptions{Expected: time.Duration(intervalSecs) * time.Second})
	cancel()
	wg.Wait()

	// Test different speed level
	if _, err := test.ReplayerClient.Stop(context.Background(), &result.Empty{}); err != nil {
		t.Fatalf("Failed to stop the replayer: %v", err)
	}
	if _, err := test.ReplayerClient.Reset(context.Background(), &result.Empty{}); err != nil {
		t.Fatalf("Failed to reset the replayer: %v", err)
	}
	consumer, cancel, wg = test.startConsumer(t)
	intervalSecs = 1 // 5x speedup
	test.startWithSpeed(t, intervalSecs*1000, pb.ReplayMode_CONSTANT)
	assertReplayInterval(t, consumer, intervalOptions{Expected: time.Duration(intervalSecs) * time.Second})
	cancel()
	wg.Wait()
}

// TestReplayProportionalMode tests the proportional speed mode. Events should be published to kafka with intervals determined by their original event time where possible
func TestReplayProportionalMode(t *testing.T) {
	// t.Skip()
	test := new(Test).Setup(t)
	defer test.Teardown()

	// Insert test data
	ingestionTimeIntervalSec := 5
	if err := test.insertForTopic(topics.Topic_LIVE_TRAIN_DATA, ascendingLiveTrainEvents(15, ingestionTimeIntervalSec)); err != nil {
		t.Fatalf("Failed to insert mock data for topic %s: %v", topics.Topic_LIVE_TRAIN_DATA, err)
	}

	// Start the replayer
	var speedupFactor int
	consumer, cancel, wg := test.startConsumer(t)
	speedupFactor = 1 // No speedup
	test.startWithSpeed(t, speedupFactor, pb.ReplayMode_PROPORTIONAL)
	assertReplayInterval(t, consumer, intervalOptions{Expected: time.Duration(ingestionTimeIntervalSec/speedupFactor) * time.Second})
	cancel()
	wg.Wait()

	// Test different speed level
	if _, err := test.ReplayerClient.Stop(context.Background(), &result.Empty{}); err != nil {
		t.Fatalf("Failed to stop the replayer: %v", err)
	}
	if _, err := test.ReplayerClient.Reset(context.Background(), &result.Empty{}); err != nil {
		t.Fatalf("Failed to reset the replayer: %v", err)
	}
	consumer, cancel, wg = test.startConsumer(t)
	speedupFactor = 5 // 5x speedup
	test.startWithSpeed(t, speedupFactor, pb.ReplayMode_PROPORTIONAL)
	assertReplayInterval(t, consumer, intervalOptions{Expected: time.Duration(ingestionTimeIntervalSec/speedupFactor) * time.Second})
	cancel()
	wg.Wait()
}

// TestConfigMerge tests if configs are merged correctly
func TestConfigMerge(t *testing.T) {
	// t.Skip()
	log.SetLevel(logLevel)
	if parallel {
		t.Parallel()
	}

	isZeroOfUnderlyingType := func(x interface{}) bool {
		return reflect.DeepEqual(x, reflect.Zero(reflect.TypeOf(x)).Interface())
	}

	if isZeroOfUnderlyingType(&wrappers.BoolValue{Value: false}) {
		t.Error("*wrappers.BoolValue{Value: false} should not be a zero type")
	}

	// TODO: Add more config merge examples
	startOptions := pb.ReplayStartOptions{
		Options: &pb.ReplayOptions{
			Repeat: &wrappers.BoolValue{Value: true},
			Speed:  &pb.Speed{Speed: 20},
		},
	}
	if err := applyActiveOptions(&pb.ActiveReplayOptions{
		Repeat: &wrappers.BoolValue{Value: false},
		Speed:  &pb.Speed{Speed: 100},
	}, startOptions.Options); err != nil {
		t.Errorf("Merging replay options failed: %v", err)
	}
	if r := startOptions.GetOptions().GetRepeat().Value; r {
		t.Errorf("Expected repeat=%v but got repeat=%v", false, r)
	}
	if s := startOptions.GetOptions().GetSpeed().GetSpeed(); s != 100 {
		t.Errorf("Expected speed=%v but got speed=%v", 100, s)
	}
}

// TestReplayOptions tests if options are correctly applied to the replayer both on startup and during replay
func TestReplayOptions(t *testing.T) {
	// t.Skip()
	test := new(Test).Setup(t)
	defer test.Teardown()

	// Test initially there are no options
	test.AssertHasOptions(t, &pb.ReplayStartOptions{
		Options: &pb.ReplayOptions{},
	})

	// Test setting of new options when starting the replay
	startOptions := &pb.ReplayStartOptions{
		Sources: []*pb.SourceReplay{
			{Source: topics.Topic_GPS_TRIP_UPDATE_DATA},
			{Source: topics.Topic_LIVE_TRAIN_DATA},
		},
		Options: &pb.ReplayOptions{Speed: &pb.Speed{Speed: 20}},
	}
	// Default values are added
	expectedOptions := &pb.ReplayOptions{Speed: &pb.Speed{Speed: 20}, Repeat: &wrappers.BoolValue{Value: true}}
	expected := &pb.ReplayStartOptions{
		Sources: []*pb.SourceReplay{
			{Source: topics.Topic_GPS_TRIP_UPDATE_DATA, Options: expectedOptions},
			{Source: topics.Topic_LIVE_TRAIN_DATA, Options: expectedOptions},
		},
		Options: expectedOptions,
	}
	_, err := test.ReplayerClient.Start(context.Background(), startOptions)
	if err != nil {
		t.Fatalf("Failed to start the replayer: %v", err)
	}
	test.AssertHasOptions(t, expected)

	// Test setting options during replay
	expected = &pb.ReplayStartOptions{
		Sources: []*pb.SourceReplay{
			{Source: topics.Topic_GPS_TRIP_UPDATE_DATA, Options: &pb.ReplayOptions{Speed: &pb.Speed{Speed: 10}, Repeat: &wrappers.BoolValue{Value: false}}},
			{Source: topics.Topic_LIVE_TRAIN_DATA, Options: &pb.ReplayOptions{Speed: &pb.Speed{Speed: 30}, Repeat: &wrappers.BoolValue{Value: true}}},
		},
		Options: &pb.ReplayOptions{Speed: &pb.Speed{Speed: 30}, Repeat: &wrappers.BoolValue{Value: true}},
	}
	_, err = test.ReplayerClient.SetOptions(context.Background(), &pb.ReplaySetOptionsRequest{
		Sources: []*pb.ActiveReplayOptions{
			{Source: topics.Topic_GPS_TRIP_UPDATE_DATA, Speed: &pb.Speed{Speed: 10}, Repeat: &wrappers.BoolValue{Value: false}},
		},
		Options: &pb.ActiveReplayOptions{Speed: &pb.Speed{Speed: 30}},
	})
	if err != nil {
		t.Fatalf("Failed to start the replayer: %v", err)
	}
	test.AssertHasOptions(t, expected)
}

// TestQuery tests if the query interface finds correct items
func TestQuery(t *testing.T) {
	// t.Skip()
	test := new(Test).Setup(t)
	defer test.Teardown()

	// Insert test data
	numTestData := 20
	if err := test.insertForTopic(topics.Topic_LIVE_TRAIN_DATA, ascendingLiveTrainEvents(int64(numTestData), 5)); err != nil {
		t.Fatalf("Failed to insert mock data for topic %s: %v", topics.Topic_LIVE_TRAIN_DATA, err)
	}
	if err := test.insertForTopic(topics.Topic_PLANNED_TRAIN_DATA, ascendingPlannedTrainEvents(int64(numTestData), 5)); err != nil {
		t.Fatalf("Failed to insert mock data for topic %s: %v", topics.Topic_PLANNED_TRAIN_DATA, err)
	}

	// Test finds all live inserted if limit is large enough
	results := test.streamResults(t, &pb.QueryOptions{Sources: []*pb.SourceQuery{
		{Source: topics.Topic_LIVE_TRAIN_DATA, Options: &pb.SourceQueryOptions{Limit: 100}},
	}})
	if len(results) != numTestData {
		t.Errorf("Query returned wrong number of results (%d but should be %d)", results, numTestData)
	}

	// Test finds all without exceeding limit
	limit := 10
	results = test.streamResults(t, &pb.QueryOptions{Sources: []*pb.SourceQuery{
		{Source: topics.Topic_LIVE_TRAIN_DATA, Options: &pb.SourceQueryOptions{Limit: int32(limit)}},
	}})
	if len(results) != limit {
		t.Errorf("Query returned wrong number of results (%d but should be %d)", results, limit)
	}

	// Test finds all live with trainId < 10 and converts the ids to the correct type
	ids := []string{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}
	results = test.streamResults(t, &pb.QueryOptions{Sources: []*pb.SourceQuery{
		{Source: topics.Topic_LIVE_TRAIN_DATA, Ids: ids},
	}})
	observedIds := mapLiveTrainQueryIds(results)
	if equal, err := deepequal.DeepEqual(ids, observedIds); !equal {
		t.Errorf("Expected results to be %v but got %v: %v", ids, observedIds, err)
	}

	// Test finds all live with trainId < 10 and all planned
	results = test.streamResults(t, &pb.QueryOptions{Sources: []*pb.SourceQuery{
		{Source: topics.Topic_LIVE_TRAIN_DATA, Ids: ids},
		{Source: topics.Topic_PLANNED_TRAIN_DATA},
	}})
	if len(results) != numTestData+len(ids) {
		t.Errorf("Query returned wrong number of results (%d but should be %d)", results, numTestData+len(ids))
	}
}

// TestQueryReplayTimes tests if query result's replay timestamps are streamed in ascending order
func TestQueryReplayTimes(t *testing.T) {
	test := new(Test).Setup(t)
	defer test.Teardown()

	// Insert test data
	numTestData := 20
	liveEventInterval, plannedEventInterval := 5, 10
	if err := test.insertForTopic(topics.Topic_LIVE_TRAIN_DATA, ascendingLiveTrainEvents(int64(numTestData), liveEventInterval)); err != nil {
		t.Fatalf("Failed to insert mock data for topic %s: %v", topics.Topic_LIVE_TRAIN_DATA, err)
	}
	if err := test.insertForTopic(topics.Topic_PLANNED_TRAIN_DATA, ascendingPlannedTrainEvents(int64(numTestData), plannedEventInterval)); err != nil {
		t.Fatalf("Failed to insert mock data for topic %s: %v", topics.Topic_PLANNED_TRAIN_DATA, err)
	}

	// Test events are streamed in correct order by replay time
	results := test.streamResults(t, &pb.QueryOptions{
		Sources: []*pb.SourceQuery{
			{Source: topics.Topic_LIVE_TRAIN_DATA},
			{Source: topics.Topic_PLANNED_TRAIN_DATA},
		},
		Sort: true,
	})
	observedTimes := mapLiveTrainQueryTimes(results)
	var lastReplayTime time.Time
	for _, replayTime := range observedTimes {
		if !lastReplayTime.IsZero() {
			if diff := replayTime.Sub(lastReplayTime); int(diff) < 0 {
				t.Errorf("Query returned event with wrong order: replayTime=%v < lastReplayTime=%v (diff %v)", replayTime, lastReplayTime, diff)
			}
		}
		lastReplayTime = replayTime
	}
}
