// inspired by tutorialedge.net webmaster Elliot Frobes

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
	Conn  *websocket.Conn
	Pool  *Pool
	ID    *users.UserID
	Token string
	mu    sync.Mutex
}

func (c *Client) Read() {
	defer func() {
		c.Pool.Unregister <- c
		c.Conn.Close()
	}()

	for {
		messageType, message, err := c.Conn.ReadMessage()
		if err != nil {
			log.Error(err)
			return
		}

    switch messageType {
		case int(pb.MessageType_USER_ANNOUNCEMENT):
			// Attempt to decode UserAnnouncementMessage
			var announcement pb.UserAnnouncementMessage
			err = proto.Unmarshal(message, &announcement)
			if err != nil {
				log.Errorf("unmarshaling error: %v", err)
			}
			clientID := announcement.GetUserId()
			if clientID == nil || clientID.GetId() == "" {
				log.Warnf("Received invalid user announcement: %v", announcement)
				break
			}
			log.Infof("User registered with ID %s", clientID)
			c.ID = clientID
			c.Pool.ClientMapping[c.ID] = c
			break
		default:
			log.Warnf("Unkown message type: %v", messageType)
		}
	}
}
