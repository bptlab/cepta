package websocket

import (
	"fmt"
	"github.com/gorilla/websocket"
	"log"
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
			log.Println(err)
			return
		}

		if messageType == 1 {
			c.ID, _ = strconv.Atoi(string(p))
			fmt.Printf("UserId: %+v\n", c.ID)
		}

		message := Message{Type: messageType, Body: string(p)}
		c.Pool.Broadcast <- message
	}
}
