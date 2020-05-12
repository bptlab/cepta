package websocket

import (
	"fmt"
	"github.com/go-redis/redis"
	"github.com/bptlab/cepta/models/internal/types/users"
	notificationpb "github.com/bptlab/cepta/models/internal/notifications/notification"
	"github.com/golang/protobuf/proto"
	"github.com/gorilla/websocket"
	log "github.com/sirupsen/logrus"
)

// Notification
type Notification interface {
	GetScore() float64
	GetMessage() []byte
	IsResend() bool
}

// UserNotification ...
type UserNotification struct {
	ID  *users.UserID
	Score float64
	Msg []byte
	Resend bool
}

func (un UserNotification) GetScore() float64 {
	return un.Score
}

func (un UserNotification) GetMessage() []byte {
	return un.Msg
}

func (un UserNotification) IsResend() bool {
	return un.Resend
}

// BroadcastNotification ...
type BroadcastNotification struct {
	Score float64
	Msg []byte
	Resend bool
}

func (bn BroadcastNotification) GetScore() float64 {
	return bn.Score
}

func (bn BroadcastNotification) GetMessage() []byte {
	return bn.Msg
}

func (bn BroadcastNotification) IsResend() bool {
	return bn.Resend
}

// Pool ...
type Pool struct {
	Register      chan *Client
	Unregister    chan *Client
	Login         chan *Client
	NotifyUser    chan UserNotification
	Broadcast     chan BroadcastNotification
	Clients       map[*Client]bool
	ClientMapping map[string]*Client
	Rclient		*redis.Client
	BufferSize int64
}

// NewPool ...
func NewPool(size int64) *Pool {
	return &Pool{
		Register:      make(chan *Client),
		Login:         make(chan *Client),
		Unregister:    make(chan *Client),
		NotifyUser:    make(chan UserNotification, 100),
		Broadcast:     make(chan BroadcastNotification, 100),
		Clients:       make(map[*Client]bool),
		ClientMapping: make(map[string]*Client),
		BufferSize: 	size,
	}
}

func (pool *Pool) bufPush(userID *users.UserID, n Notification) error {
	if n.IsResend() {
		// Notification has already been acknowledged or failed to be delivered multiple times
		// In this case the notification is dropped to avoid round trips
		return nil
	}
	// Warning: This is likely to be a performance killer :(
	// Count items
	count, err := pool.Rclient.ZCount(fmt.Sprintf("%s-buffered", userID.GetId()), "-inf", "+inf").Result()
	if err != nil {
		return err
	}
	log.Debugf("redis buffer [user=%v count=%d buffer-size=%d]", userID, count, pool.BufferSize)

	pipe := pool.Rclient.Pipeline()
	if count >= pool.BufferSize {
		// Pop oldest notification
		if err := pipe.ZPopMin(fmt.Sprintf("%s-buffered", userID.GetId()), 1).Err(); err != nil {
			return err
		}
	}
	// Add new notification
	log.Debugf("Buffering notification with score=%d for user %v", n.GetScore(), userID)
	if _, err := pipe.ZAdd(fmt.Sprintf("%s-buffered", userID.GetId()), redis.Z{Score: n.GetScore(), Member: n.GetMessage()}).Result(); err != nil {
		return err
	}
	// Execute pipeline
	if _, err := pipe.Exec(); err != nil {
		return err
	}
	return nil
}

func (pool *Pool) bufUpdateScore(userID *users.UserID, acknowledged []byte) error {
	// Update score to 0=acknowledged
	return pool.Rclient.ZAddXX(fmt.Sprintf("%s-buffered", userID.GetId()), redis.Z{Score: 0, Member: acknowledged}).Err()
}

func (pool *Pool) bufGet(userID *users.UserID) (*notificationpb.AccumulatedNotifications, error) {
	return pool.bufGetMissedByScore(userID, "-inf", "+inf")
}

func (pool *Pool) bufGetUnacknowledged(userID *users.UserID) (*notificationpb.AccumulatedNotifications, error) {
	return pool.bufGetMissedByScore(userID, "(1", "+inf")
}

func (pool *Pool) bufGetUnacknowledgedRaw(userID *users.UserID) ([][]byte, error) {
	return pool.bufGetMissedByScoreRaw(userID, "(1", "+inf")
}

func (pool *Pool) bufGetMissedByScore(userID *users.UserID, min string, max string) (*notificationpb.AccumulatedNotifications, error) {
	raw, err := pool.bufGetMissedByScoreRaw(userID, min, max)
	if err != nil {
		return nil, err
	}
	return unmarshalRawNotifications(raw)
}

func (pool *Pool) bufGetMissedByScoreRaw(userID *users.UserID, min string, max string) (raw [][]byte, err error) {
	unseen, err := pool.Rclient.ZRangeByScoreWithScores(fmt.Sprintf("%s-buffered", userID.GetId()), redis.ZRangeBy{Min: min, Max: max}).Result()
	if err != nil {
		return nil, err
	}
	for _, e := range unseen {
		raw = append(raw, []byte(e.Member.(string)))
	}
	return
}

func (pool *Pool) bufCount(userID *users.UserID) (int64, error) {
	return pool.Rclient.ZCount(fmt.Sprintf("%s-buffered", userID.GetId()), "-inf", "+inf").Result()
}

func unmarshalRawNotifications(raw [][]byte) (*notificationpb.AccumulatedNotifications, error) {
	var notifications notificationpb.AccumulatedNotifications
	for _, r := range raw {
		var notification notificationpb.Notification
		if err := proto.Unmarshal(r, &notification); err != nil {
			log.Errorf("Failed to unmarshal buffered notification for user: %v", err)
			continue
		}
		notifications.Notifications = append(notifications.Notifications, &notification)
	}
	return &notifications, nil
}

// Start ...
func (pool *Pool) Start() {
	for {
		select {
		case client := <-pool.Register:
			client.done = make(chan bool)
			pool.Clients[client] = true
			log.Debugf("connection pool size: %v", len(pool.Clients))
			break
		case client := <-pool.Login:
			pool.ClientMapping[client.ID.GetId()] = client
			log.Debugf("User %v logged in", client.ID)
			notifications, err := pool.bufGetUnacknowledgedRaw(client.ID)
			if err != nil {
				log.Errorf("Failed to get buffered notifications for logged in user %v: %v", client.ID, err)
			} else {
				for _, n := range notifications {
					pool.NotifyUser <- UserNotification{ID: client.ID, Msg: n, Resend: true}
				}
			}
			break
		case client := <-pool.Unregister:
			client.done <- true
			delete(pool.Clients, client)
			log.Infof("connection pool size: %v", len(pool.Clients))
			break
		case notifyUserRequest := <-pool.NotifyUser:
			if client, ok := pool.ClientMapping[notifyUserRequest.ID.GetId()]; ok {
				if err := client.Conn.WriteMessage(websocket.BinaryMessage, notifyUserRequest.Msg); err != nil {
					log.Errorf("Sending notification to client %v failed: %v", client, err)
					break
				}
				log.Debug("Sent websocket message for user: %v", notifyUserRequest.ID.GetId())
			} else {
				log.Debugf("Buffering notification for user %v without active connection", notifyUserRequest.ID)
				if err := pool.bufPush(notifyUserRequest.ID, notifyUserRequest); err != nil {
					log.Warningf("Failed to buffer notification for user %v", notifyUserRequest.ID)
				}
			}
			break
		case broadcastRequest := <-pool.Broadcast:
			log.Debug("Broadcasting message to all clients in pool")
			for client := range pool.Clients {
				if err := client.Conn.WriteMessage(websocket.BinaryMessage, broadcastRequest.Msg); err != nil {
					log.Warningf("Broadcasting message to client %v failed: %v", client, err)
				}
			}
			break
		}
	}
}
