package websocket

import (
	"strconv"
	"sync"

	"github.com/gorilla/websocket"
	log "github.com/sirupsen/logrus"
)

type Client struct {
	ID   int
	Conn *websocket.Conn
	Pool *Pool
	mu   sync.Mutex
}

// TODO: send protobuf message
type Message struct {
	Type int    `json:"type"`
	Body string `json:"body"`
}

func (c *Client) Read() {
	defer func() {
		c.Pool.Unregister <- c
		c.Conn.Close()
	}()

	for {
		messageType, p, err := c.Conn.ReadMessage()
		if err != nil {
			log.Error(err)
			return
		}

		if messageType == UserMessage {
			c.ID, _ = strconv.Atoi(string(p))
			log.Infof("UserId: %+v\n", c.ID)
		}

		message := Message{Type: messageType, Body: string(p)}
		c.Pool.Broadcast <- message
	}
}
