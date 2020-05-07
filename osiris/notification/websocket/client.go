package websocket

import (
	"sync"

	pb "github.com/bptlab/cepta/models/grpc/notification"
	"github.com/bptlab/cepta/models/internal/types/users"
	"github.com/golang/protobuf/proto"
	"github.com/gorilla/websocket"
	log "github.com/sirupsen/logrus"
)

// Client ...
type Client struct {
	Conn             *websocket.Conn
	Pool             *Pool
	ID               *users.UserID
	Token            string
	done             chan bool
	mu               sync.Mutex
}

func (c *Client) Read() {
	defer func() {
		c.Pool.Unregister <- c
		_ = c.Conn.Close()
	}()

	for {
		messageType, message, err := c.Conn.ReadMessage()
		if err != nil {
			log.Errorf("Error reading message from websocket connection: %v", err)
			return
		}

		switch messageType {
		case websocket.BinaryMessage:
			// Attempt to decode client message
			var clientMessage pb.ClientMessage
			err = proto.Unmarshal(message, &clientMessage)
			if err != nil {
				log.Errorf("unmarshal error: %v", err)
			}
			switch clientMessage.GetMessage().(type) {
			case *pb.ClientMessage_Announcement:
				clientID := clientMessage.GetAnnouncement().GetUserId()
				clientToken := clientMessage.GetAnnouncement().GetToken()
				if clientID == nil || clientID.GetId() == "" || clientToken == "" {
					log.Warnf("Received invalid user announcement: %v", clientMessage.GetAnnouncement())
					break
				}
				// TODO: Check auth!
				log.Infof("User registered with ID %s", clientID)
				c.ID = clientID
				c.Token = clientToken
				c.Pool.Login <- c
				break
			default:
				log.Warnf("Received client message of unknown type: %v", clientMessage)
			}
			break
		default:
			log.Warnf("Received non binary websocket message of type %v", messageType)
		}
	}
}
