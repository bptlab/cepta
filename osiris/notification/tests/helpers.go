package main

import (
	"net/url"
	"strings"
	"sync"
	"testing"
	"time"

	"context"

	"github.com/gorilla/websocket"

	"github.com/Shopify/sarama"
	"github.com/bptlab/cepta/models/internal/delay"
	durationpb "github.com/golang/protobuf/ptypes/duration"
	topics "github.com/bptlab/cepta/models/constants/topic"
	pb "github.com/bptlab/cepta/models/grpc/notification"
	usermgmtpb "github.com/bptlab/cepta/models/grpc/usermgmt"
	notificationpb "github.com/bptlab/cepta/models/internal/notifications/notification"
	"github.com/bptlab/cepta/models/internal/types/ids"
	"github.com/bptlab/cepta/models/internal/types/users"
	ws "github.com/bptlab/cepta/osiris/notification/websocket"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/golang/protobuf/proto"
	log "github.com/sirupsen/logrus"
)

type collector struct {
	responses 		map[*users.UserID]*notificationpb.AccumulatedNotifications
	shouldWrapUp  bool
	idleTime *time.Duration
	idleReaderChan chan *users.UserID
	userIDs []*users.UserID
	pool *ws.Pool
}

func (coll *collector) AwaitIdleFor(idleTime time.Duration) {
	coll.idleTime = &idleTime
	coll.shouldWrapUp = true
	coll.idleReaderChan = make(chan *users.UserID)
	// Wait until all readers have signaled to be idle
	idle := make(map[string]bool)
	for len(idle) < len(coll.userIDs) {
		idleUserId := <- coll.idleReaderChan
		idle[idleUserId.GetId()] = true
		log.Debugf("%s is now idle (%d/%d)", idleUserId, len(idle), len(coll.userIDs))
	}
	// All idle now
}

func (test *Test) announceUsers(t *testing.T, userIDs []*users.UserID) *collector {
	defaultIdleTime := 5 * time.Second
	coll := &collector{
		userIDs: userIDs,
		responses: make(map[*users.UserID]*notificationpb.AccumulatedNotifications),
		shouldWrapUp: false,
		idleReaderChan: make(chan *users.UserID),
		pool: test.notificationServer.Pool,
		idleTime: &defaultIdleTime,
	}
	var mux sync.Mutex
	for _, userID := range userIDs {
		go func(uid *users.UserID) {
			u := url.URL{Scheme: "ws", Host: test.websocketListener.Addr().String(), Path: "/ws/notifications"}

			c, _, err := websocket.DefaultDialer.Dial(u.String(), nil)
			if err != nil {
				t.Error("failed to dial websocket endpoint: ", err)
			}
			log.Debugf("Announcing user %v", uid)
			announcement := &pb.ClientMessage{Message: &pb.ClientMessage_Announcement{Announcement: &pb.Announcement{UserId: uid, Token: uid.GetId()}}}
			announcementMsg, err := proto.Marshal(announcement)
			if err != nil {
				t.Error("Failed to marshal proto: ", err)
			}
			err = c.WriteMessage(websocket.BinaryMessage, announcementMsg)
			if err != nil {
				t.Error("Failed to write websocket message:", err)
			}

			// Receive messages
			type WsMsg struct{messageType int; p []byte; err error}
			messageChan := make(chan WsMsg, 100)
			go func() {
				for {
					msgType, rawMessage, err := c.ReadMessage()
					log.Debugf("Received websocket message: %v (user %v)", rawMessage, uid)
					messageChan <- WsMsg{messageType: msgType, p: rawMessage, err: err}
				}
			}()

			for {
				select {
				case <-time.After(*coll.idleTime):
					log.Debugf("%v passed without message", *coll.idleTime)
					if coll.shouldWrapUp {
						// Did not receive new message, signal idle
						coll.idleReaderChan <- uid
					}
				case msg := <-messageChan:
					if msg.err != nil {
						t.Errorf("Failed to read from websocket connection: %v", msg.err)
					} else {
						var notification notificationpb.Notification
						if err := proto.Unmarshal(msg.p, &notification); err != nil {
							t.Errorf("Failed to parse websocket message as generic notification: %v", err)
						} else {
							mux.Lock()
							if _, ok := coll.responses[uid]; !ok {
								coll.responses[uid] = &notificationpb.AccumulatedNotifications{}
							}
							coll.responses[uid].Notifications =  append(coll.responses[uid].Notifications, &notification)
							coll.responses[uid].Total++
							mux.Unlock()
						}
					}
				}
			}
		}(userID)
	}
	return coll
}

func (test *Test) produceEventsToKafka(t *testing.T, topic topics.Topic, events []proto.Message) {
	log.Infof("Will produce to topic %s from %s", topic, strings.Join(test.kafkapConfig.Brokers, ", "))
	producer, err := kafkaproducer.Create(context.Background(), test.kafkapConfig)
	if err != nil {
		t.Fatal("Cannot produce events: ", err)
	}
	defer producer.Close()
	go func() {
		for err := range producer.AccessLogProducer.Errors() {
			t.Errorf("failed to write access log entry: %v", err)
		}
	}()
	for _, event := range events {
		eventBytes, err := proto.Marshal(event)
		if err != nil {
			t.Error("Failed to marshal proto: ", err)
			continue
		}
		log.Debugf("Sending %v", event)
		producer.Send(topic.String(), topic.String(), sarama.ByteEncoder(eventBytes))
		time.Sleep(100 * time.Millisecond)
	}
}

func (test *Test) addUsersForTransport(t *testing.T, data map[*users.User][]*ids.CeptaTransportID) map[string]*users.UserID {
	added := make(map[string]*users.UserID)
	for user, transportIds := range data {
		newUserReq := &usermgmtpb.AddUserRequest{User: &users.InternalUser{
			User:     &users.User{Email: user.GetEmail(), Transports: transportIds},
			Password: "hard-to-guess",
		}}
		addedUser, err := test.usermgmtClient.AddUser(context.Background(), newUserReq)
		if err != nil {
			t.Errorf("Failed to add new user: %v: %v", newUserReq.GetUser(), err)
		}
		added[addedUser.GetEmail()] = addedUser.GetId()
	}
	return added
}

func newDelayNotification(tid string, secs int64) *notificationpb.Notification {
	return &notificationpb.Notification{
		Notification: &notificationpb.Notification_Delay{
			Delay: &notificationpb.DelayNotification{
				TransportId: &ids.CeptaTransportID{Id: tid},
				Delay:       &delay.Delay{Delta: &durationpb.Duration{Seconds: secs}},
			}}}
}