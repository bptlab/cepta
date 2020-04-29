package main

import (
	"testing"
	//"time"

	//topics "github.com/bptlab/cepta/models/constants/topic"
	//notificationpb "github.com/bptlab/cepta/models/internal/notifications/notification"
	//"github.com/bptlab/cepta/models/internal/types/ids"
	//"github.com/bptlab/cepta/models/internal/delay"
	//"github.com/bptlab/cepta/models/internal/types/users"
	//"github.com/golang/protobuf/proto"
	//durationpb "github.com/golang/protobuf/ptypes/duration"
)

const parallel = true

/*
func TestFillsCache(t *testing.T) {
	test := new(Test).setup(t)
	defer test.teardown()

	test.addUsersForTransport(t, &ids.CeptaTransportID{Id: "1"}, []*users.UserID{
		{Id: "1"}, {Id: "2"}, {Id: "3"},
	})

	time.Sleep(10 * time.Second)

	test.produceEventsToKafka(t, topics.Topic_DELAY_NOTIFICATIONS, []proto.Message{
		newDelayNotification(12),
		newDelayNotification(12),
		newDelayNotification(12),
		newDelayNotification(12),
	})

}

func newDelayNotification(secs int64) *notificationpb.Notification {
	return &notificationpb.Notification{
		Notification: &notificationpb.Notification_Delay{
			Delay: &notificationpb.DelayNotification{
				Delay: &delay.Delay{Delta: &durationpb.Duration{Seconds: secs}},
			}}}
}
*/

func TestSendMessagesToUser(t *testing.T) {
    test := new(Test).setup(t)
    defer test.teardown()

    test.setupWebsocketConn(t)

   // test.registerUser(t)
   // test.userReceiveMessages(t)
}
