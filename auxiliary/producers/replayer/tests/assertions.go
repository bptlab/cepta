package main

import (
	"context"
	"testing"
	"time"

	"github.com/Shopify/sarama"
	eventpb "github.com/bptlab/cepta/models/events/event"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	"github.com/bptlab/cepta/models/internal/types/result"
	kafkaconsumer "github.com/bptlab/cepta/osiris/lib/kafka/consumer"
	"github.com/bptlab/cepta/osiris/lib/utils"
	"github.com/romnnn/deepequal"
)

// AssertStatusIs ...
func (test *Test) AssertStatusIs(t *testing.T, expected *pb.ReplayStatus) {
	status, err := test.ReplayerClient.GetStatus(context.Background(), &result.Empty{})
	if err != nil {
		t.Fatalf("Failed to get status of the replayer: %v", err)
	}
	if equal, err := deepequal.DeepEqual(status, expected); !equal {
		t.Fatalf("Expected status of the replayer to be %v but got: %v: %v", expected, status, err)
	}
}

// AssertHasOptions ...
func (test *Test) AssertHasOptions(t *testing.T, expected *pb.ReplayStartOptions) {
	status, err := test.ReplayerClient.GetOptions(context.Background(), &result.Empty{})
	if err != nil {
		t.Fatalf("Failed to get options of the replayer: %v", err)
	}
	if equal, err := deepequal.DeepEqual(status, expected); !equal {
		t.Fatalf("Expected options of the replayer to be %q but got: %q: %v", expected, status, err)
	}
}

type intervalOptions struct{ Expected time.Duration }

func assertReplayInterval(t *testing.T, consumer *kafkaconsumer.Consumer, options intervalOptions) {
	var lastEventTimestamp time.Time
	firstEvents{10, consumer}.do(func(index int, event eventpb.Event, raw *sarama.ConsumerMessage, err error) {
		if err != nil {
			t.Error(err)
		} else {
			if index > 2 && !lastEventTimestamp.IsZero() {
				// expected := time.Duration(ingestionTimeIntervalSec/speedupFactor) * time.Second
				if diff := int64(raw.Timestamp.Sub(lastEventTimestamp) - options.Expected); !utils.InPlusMinusRange(0, int64(1*time.Second)/2, diff) { // Allow .5 second fluctuations
					t.Errorf("Expected +%v interval since the last event but got %v (diff=%v)", options.Expected, raw.Timestamp.Sub(lastEventTimestamp), time.Duration(diff))
				}
			}
			lastEventTimestamp = raw.Timestamp
		}
	})
}
