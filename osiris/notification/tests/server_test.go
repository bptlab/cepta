package main

import (
	"testing"
	"time"

	topics "github.com/bptlab/cepta/models/constants/topic"
	notificationpb "github.com/bptlab/cepta/models/internal/notifications/notification"
	"github.com/bptlab/cepta/models/internal/types/ids"
	"github.com/bptlab/cepta/models/internal/types/users"
	"github.com/golang/protobuf/proto"
)

const parallel = true

func TestBroadcast(t *testing.T) {
	// t.Skip()
	test := new(Test).setup(t)
	defer test.teardown()

	// Add some users
	addedUsersWithEmail := test.addUsersForTransport(t, map[*users.User][]*ids.CeptaTransportID{
		&users.User{Email: "1@test.com"}: {{Id: "1"}, {Id: "2"}},
		&users.User{Email: "2@test.com"}: {{Id: "2"}, {Id: "3"}},
		&users.User{Email: "3@test.com"}: {{Id: "1"}, {Id: "4"}},
	})

	userWithEmail := func(email string) *users.UserID {
		if user, ok := addedUsersWithEmail[email]; ok {
			return user
		}
		// This can never exist and that is just what we want
		return &users.UserID{Id: email}
	}

	// Simulate the users logging in via websocket connection
	collector := test.announceUsers(t, []*users.UserID{
		userWithEmail("1@test.com"), userWithEmail("2@test.com"), userWithEmail("4@test.com"),
	})

	systemNotification := &notificationpb.Notification{
		Notification: &notificationpb.Notification_System{
			System: &notificationpb.SystemNotification{
				Message: "Going offline",
			}}}
	systemNotification2 := proto.Clone(systemNotification).(*notificationpb.Notification)
	systemNotification2.GetSystem().Message = "Back online"

	// Produce system notifications to kafka
	test.produceEventsToKafka(t, topics.Topic_DELAY_NOTIFICATIONS, []proto.Message{
		systemNotification, systemNotification2,
	})

	// Allow for all websocket clients to finish up reading
	collector.AwaitIdleFor(5 * time.Second)

	expected := map[string][]*notificationpb.Notification{
		userWithEmail("1@test.com").GetId(): {systemNotification, systemNotification2},
		userWithEmail("2@test.com").GetId(): {systemNotification, systemNotification2},
		userWithEmail("4@test.com").GetId(): {systemNotification, systemNotification2},
	}
	compareNotifications(t, collector.responses, expected)
}

func TestNotifyOnlineUsers(t *testing.T) {
	// t.Skip()
	test := new(Test).setup(t)
	defer test.teardown()

	// Add some users
	addedUsersWithEmail := test.addUsersForTransport(t, map[*users.User][]*ids.CeptaTransportID{
		&users.User{Email: "1@test.com"}: {{Id: "1"}, {Id: "2"}},
		&users.User{Email: "2@test.com"}: {{Id: "2"}, {Id: "3"}},
		&users.User{Email: "3@test.com"}: {{Id: "1"}, {Id: "4"}},
	})

	userWithEmail := func(email string) *users.UserID {
		if user, ok := addedUsersWithEmail[email]; ok {
			return user
		}
		// This can never exist and that is just what we want
		return &users.UserID{Id: email}
	}

	// Simulate the users logging in via websocket connection
	collector := test.announceUsers(t, []*users.UserID{
		userWithEmail("1@test.com"), userWithEmail("2@test.com"), userWithEmail("4@test.com"),
	})

	dn2 := newDelayNotification("1", 2)
	dn4 := newDelayNotification("1", 4)
	dn6 := newDelayNotification("2", 6)
	dn8 := newDelayNotification("3", 8)
	dn10 := newDelayNotification("2", 10)
	test.produceEventsToKafka(t, topics.Topic_DELAY_NOTIFICATIONS, []proto.Message{
		dn2, dn4, dn6, dn8, dn10,
	})

	// Allow for all websocket clients to finish up reading
	collector.AwaitIdleFor(5 * time.Second)
	expected := map[string][]*notificationpb.Notification{
		userWithEmail("1@test.com").GetId(): {dn2, dn4, dn6, dn10},
		userWithEmail("2@test.com").GetId(): {dn6, dn8, dn10},
	}
	compareNotifications(t, collector.responses, expected)
}

func TestNotifyOfflineUsers(t *testing.T) {
	// t.Skip()
	test := new(Test).setup(t)
	defer test.teardown()

	// Add some users
	addedUsersWithEmail := test.addUsersForTransport(t, map[*users.User][]*ids.CeptaTransportID{
		&users.User{Email: "1@test.com"}: {{Id: "1"}, {Id: "2"}},
		&users.User{Email: "2@test.com"}: {{Id: "2"}, {Id: "3"}},
		&users.User{Email: "3@test.com"}: {{Id: "1"}, {Id: "4"}},
	})

	userWithEmail := func(email string) *users.UserID {
		if user, ok := addedUsersWithEmail[email]; ok {
			return user
		}
		// This can never exist and that is just what we want
		return &users.UserID{Id: email}
	}

	dn2 := newDelayNotification("1", 2)
	dn4 := newDelayNotification("1", 4)
	dn6 := newDelayNotification("2", 6)
	dn8 := newDelayNotification("3", 8)
	dn10 := newDelayNotification("2", 10)

	// Produce system notifications to kafka
	test.produceEventsToKafka(t, topics.Topic_DELAY_NOTIFICATIONS, []proto.Message{
		dn2, dn4, dn6, dn8, dn10,
	})

	// Simulate the users logging in via websocket connection
	time.Sleep(2 * time.Second)
	collector := test.announceUsers(t, []*users.UserID{
		userWithEmail("1@test.com"), userWithEmail("2@test.com"), userWithEmail("4@test.com"),
	})

	// Allow for all websocket clients to finish up reading
	collector.AwaitIdleFor(5 * time.Second)
	expected := map[string][]*notificationpb.Notification{
		userWithEmail("1@test.com").GetId(): {dn2, dn4, dn6, dn10},
		userWithEmail("2@test.com").GetId(): {dn6, dn8, dn10},
	}
	compareNotifications(t, collector.responses, expected)
}

func TestOfflineBufferLimitUsers(t *testing.T) {
	// t.Skip()
	test := new(Test).setup(t)
	defer test.teardown()

	// Lower the buffer limit of the server
	test.notificationServer.UserNotificationsBufferSize = 2
	test.notificationServer.Pool.BufferSize = 2

	// Add some users
	addedUsersWithEmail := test.addUsersForTransport(t, map[*users.User][]*ids.CeptaTransportID{
		&users.User{Email: "1@test.com"}: {{Id: "1"}, {Id: "2"}},
		&users.User{Email: "2@test.com"}: {{Id: "2"}, {Id: "3"}},
		&users.User{Email: "3@test.com"}: {{Id: "1"}, {Id: "4"}},
	})

	userWithEmail := func(email string) *users.UserID {
		if user, ok := addedUsersWithEmail[email]; ok {
			return user
		}
		// This can never exist and that is just what we want
		return &users.UserID{Id: email}
	}

	dn2 := newDelayNotification("1", 2)
	dn4 := newDelayNotification("1", 4)
	dn6 := newDelayNotification("2", 6)
	dn8 := newDelayNotification("3", 8)
	dn10 := newDelayNotification("2", 10)

	// Produce system notifications to kafka
	test.produceEventsToKafka(t, topics.Topic_DELAY_NOTIFICATIONS, []proto.Message{
		dn2, dn4, dn6, dn8, dn10,
	})

	// Simulate the users logging in via websocket connection
	time.Sleep(2 * time.Second)
	collector := test.announceUsers(t, []*users.UserID{
		userWithEmail("1@test.com"), userWithEmail("2@test.com"), userWithEmail("4@test.com"),
	})

	// Allow for all websocket clients to finish up reading
	collector.AwaitIdleFor(5 * time.Second)
	expected := map[string][]*notificationpb.Notification{
		userWithEmail("1@test.com").GetId(): {dn6, dn10},
		userWithEmail("2@test.com").GetId(): {dn8, dn10},
	}
	compareNotifications(t, collector.responses, expected)
}
