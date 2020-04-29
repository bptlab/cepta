package main

import (
	"testing"
	"time"

	topics "github.com/bptlab/cepta/models/constants/topic"
	delay "github.com/bptlab/cepta/models/events/traindelaynotificationevent"
	"github.com/bptlab/cepta/models/types/transports"
	"github.com/bptlab/cepta/models/types/users"
	"github.com/golang/protobuf/proto"
)

const parallel = true

func TestFillsCache(t *testing.T) {
	test := new(Test).setup(t)
	defer test.teardown()

	test.addUsersForTransport(t, &transports.TransportID{Id: "1"}, []*users.UserID{
		{Id: "1"}, {Id: "2"}, {Id: "3"},
	})

	time.Sleep(10 * time.Second)

	test.produceEventsToKafka(t, topics.Topic_DELAY_NOTIFICATIONS, []proto.Message{
		&delay.TrainDelayNotification{Delay: 12},
		&delay.TrainDelayNotification{Delay: 12},
		&delay.TrainDelayNotification{Delay: 12},
		&delay.TrainDelayNotification{Delay: 12},
	})

}

func TestWebsocket(t *testing.T) {
    test := new(Test).setup(t)
    defer test.teardown()

    test.websocketHelper(t)
}
