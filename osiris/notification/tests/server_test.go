package main

import (
	"testing"
	"time"

	topics "github.com/bptlab/cepta/models/constants/topic"
	"github.com/bptlab/cepta/models/internal/delay"
	notificationpb "github.com/bptlab/cepta/models/internal/notifications/notification"
	"github.com/bptlab/cepta/models/internal/types/ids"
	"github.com/bptlab/cepta/models/internal/types/users"
	"github.com/golang/protobuf/proto"
	durationpb "github.com/golang/protobuf/ptypes/duration"
)

const parallel = true

func TestFillsCache(t *testing.T) {
	test := new(Test).setup(t)
	defer test.teardown()

	test.addUsersForTransport(t, map[*users.UserID][]*ids.CeptaTransportID{
		&users.UserID{Id: "1"}: []*ids.CeptaTransportID{{Id: "1"}, {Id: "2"}},
		&users.UserID{Id: "2"}: []*ids.CeptaTransportID{{Id: "2"}, {Id: "3"}},
		&users.UserID{Id: "3"}: []*ids.CeptaTransportID{{Id: "1"}, {Id: "4"}},
	})

	test.announceUsers(t, []*users.UserID{
		{Id: "1"}, {Id: "2"}, {Id: "4"},
	})

	test.produceEventsToKafka(t, topics.Topic_DELAY_NOTIFICATIONS, []proto.Message{
		newDelayNotification("1", 1200),
		newDelayNotification("1", 1200),
		newDelayNotification("2", 1200),
		newDelayNotification("3", 1200),
		newDelayNotification("2", 1200),
	})

	time.Sleep(40 * time.Second)

}

func newDelayNotification(tid string, secs int64) *notificationpb.Notification {
	return &notificationpb.Notification{
		Notification: &notificationpb.Notification_Delay{
			Delay: &notificationpb.DelayNotification{
				TransportId: &ids.CeptaTransportID{Id: tid},
				Delay:       &delay.Delay{Delta: &durationpb.Duration{Seconds: secs}},
			}}}
}

/*
func TestSendMessagesToUser(t *testing.T) {
    test := new(Test).setup(t)
    defer test.teardown()

    test.setupWebsocketConn(t)

   	// test.registerUser(t)
   	// test.userReceiveMessages(t)
}
*/
