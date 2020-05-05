package websocket

import (
	"github.com/go-redis/redis"
	"github.com/bptlab/cepta/models/internal/types/users"
	"github.com/gorilla/websocket"
	log "github.com/sirupsen/logrus"
)

// UserNotification ...
type UserNotification struct {
	ID  *users.UserID
	Msg []byte
}

// Pool ...
type Pool struct {
	Register      chan *Client
	Unregister    chan *Client
	Login         chan *Client
	NotifyUser    chan UserNotification
	Broadcast     chan []byte
	Clients       map[*Client]bool
	ClientMapping map[*users.UserID]*Client
	Rclient		*redis.Client
}

// NewPool ...
func NewPool() *Pool {
	return &Pool{
		Register:      make(chan *Client),
		Login:         make(chan *Client),
		Unregister:    make(chan *Client),
		NotifyUser:    make(chan UserNotification),
		Broadcast:     make(chan []byte),
		Clients:       make(map[*Client]bool),
		ClientMapping: make(map[*users.UserID]*Client),
	}
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
			pool.ClientMapping[client.ID] = client
			/*
			if err := pool.Rmq.CreateQueue(client.ID.GetId()); err != nil {
				log.Error("Failed to create queue for user")
			}
			var err error
			client.notificationChan, err = pool.Rmq.ConsumeQueue(client.ID.GetId())
			if err != nil {
				log.Error("Failed to create notification queue for user")
			}
			log.Debug("Created rabbitmq consumer for user %s", client.ID.GetId())
			go func() {
				select {
				case notification := <-client.notificationChan:
					log.Debug("Sending websocket notification to user %s", client.ID.GetId())
					pool.NotifyUser <- UserNotification{ID: client.ID, Msg: notification.Body}
				case <-client.done:
					break
				}
			}()
			*/
			break
		case client := <-pool.Unregister:
			client.done <- true
			delete(pool.Clients, client)
			log.Debugf("connection pool size: %v", len(pool.Clients))
			break
		case notifyUserRequest := <-pool.NotifyUser:
			if client, ok := pool.ClientMapping[notifyUserRequest.ID]; ok {
				if err := client.Conn.WriteMessage(websocket.BinaryMessage, notifyUserRequest.Msg); err != nil {
					log.Errorf("Sending message to client %v failed: %v", client, err)
				}
			} else {
				log.Errorf("Sending message to client %v without active connection", client)
			}
		case message := <-pool.Broadcast:
			log.Debug("Sending message to all clients in Pool")
			for client := range pool.Clients {
				if err := client.Conn.WriteMessage(websocket.BinaryMessage, message); err != nil {
					log.Errorf("Broadcasting message to client %v failed: %v", client, err)
				}
			}
		}
	}
}
