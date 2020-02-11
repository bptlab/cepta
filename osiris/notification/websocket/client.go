// inspired by tutorialedge.net webmaster Elliot Frobes

package websocket

import (
	"github.com/gorilla/websocket"
	log "github.com/sirupsen/logrus"
	"strconv"
	"sync"
)

type Client struct {
	ID   int
	Conn *websocket.Conn
	Pool *Pool
	mu   sync.Mutex
}

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

		if messageType == 1 {
			c.ID, _ = strconv.Atoi(string(p))
			log.Info("UserId: %+v\n", c.ID)
		}

		/*
		message := Message{Type: messageType, Body: string(p)}
		c.Pool.Broadcast <- message
		*/
	}
}
