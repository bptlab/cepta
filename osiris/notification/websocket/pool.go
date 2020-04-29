package websocket

import (
	"github.com/bptlab/cepta/models/types/users"
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
	NotifyUser    chan UserNotification
	Broadcast     chan []byte
	Clients       map[*Client]bool
	ClientMapping map[*users.UserID]*Client
}

// NewPool ...
func NewPool() *Pool {
	return &Pool{
		Register:      make(chan *Client),
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
			pool.Clients[client] = true
			log.Debugf("connection pool size: %v", len(pool.Clients))
			break
		case client := <-pool.Unregister:
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
