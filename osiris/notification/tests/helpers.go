package main

import (
	"net/http"
  "net/http/httptest"
  "strings"
  "testing"

	clientWebsocket "github.com/bptlab/cepta/osiris/notification/websocket"
	"github.com/gorilla/websocket"
	"context"
	"fmt"

	"github.com/Shopify/sarama"
	topics "github.com/bptlab/cepta/models/constants/topic"
	usermgmtpb "github.com/bptlab/cepta/models/grpc/usermgmt"
	"github.com/bptlab/cepta/models/internal/types/users"
	"github.com/bptlab/cepta/models/internal/types/ids"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/golang/protobuf/proto"
	log "github.com/sirupsen/logrus"
)

var numberUsers int = 5
var upgrader = websocket.Upgrader{}

func (test *Test) setupWebsocketConn(t *testing.T) (){
  var err error
  // Create test server with the echo handler.
  test.websocketServer = httptest.NewServer(http.HandlerFunc(test.setClient))

  // Convert http://127.0.0.1 to ws://127.0.0.
  url := "ws" + strings.TrimPrefix(test.websocketServer.URL, "http")

  // Connect to the server
  test.websocketConnection, _, err = websocket.DefaultDialer.Dial(url, nil)
  if err != nil {
      t.Fatalf("%v", err)
  }

  // Create new client pool
  test.notificationServer.pool = clientWebsocket.NewPool()
	if err != nil {
		log.Errorf("%+v\n", err)
	}
  userList := getmockUsers(numberUsers, test.notificationServer.pool, test.websocketClient)

  go test.notificationServer.pool.Start()

  for _, client := range userList {
    test.notificationServer.pool.Register <- client
	  go client.Read()
  }

  test.websocketConnection
  // go publishMessages(test.notificationServer.pool, done)

  // test.writeToWebsocket(t)
}

//Generates <number> many UserIds
func getmockUsers(number int, pool *clientWebsocket.Pool, clientConn *websocket.Conn) ([]*clientWebsocket.Client){
  var userList []*clientWebsocket.Client

  for i := 0; i < number; i++ {
    client := &clientWebsocket.Client{
      Pool: pool,
      Conn: clientConn,
    }
    userList = append(userList, client)
  }
  return userList
}
/*
func publishMessages(pool *clientWebsocket.Pool, done chan bool) {
  ticker := time.NewTicker(time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-done:
			return
		case t := <-ticker.C:
		  pool.Broadcast <- t
		  done <- true
		  return
	  }
  }
}

*/
func (test *Test) setClient(w http.ResponseWriter, r *http.Request) {
    var err error
    test.websocketClient, err = upgrader.Upgrade(w, r, nil)
    if err != nil {
        return
    }

    // test.echoMessages()
}

func (test *Test) echoMessages() {
  for {
        mt, message, err := test.websocketClient.ReadMessage()
        if err != nil {
            break
        }
        err = test.websocketClient.WriteMessage(mt, message)
        if err != nil {
            break
        }
    }
}

func (test *Test) writeToWebsocket(t *testing.T) {
 // Send message to server, read response and check to see if it's what we expect.
  for i := 0; i < 10; i++ {
      if err := test.websocketConnection.WriteMessage(websocket.TextMessage, []byte("hello")); err != nil {
          t.Fatalf("%v", err)
      }
      _, p, err := test.websocketConnection.ReadMessage()
      if err != nil {
          t.Fatalf("%v", err)
      }
      if string(p) != "hello" {
          t.Fatalf("bad message")
      }
  }
}


func (test *Test) produceEventsToKafka(t *testing.T, topic topics.Topic, events []proto.Message) {
	producer, err := kafkaproducer.Create(context.Background(), test.kafkapConfig)
	if err != nil {
		t.Fatal("Cannot produce events: ", err)
	}
	defer producer.Close()
	for _, event := range events {
		eventBytes, err := proto.Marshal(event)
		if err != nil {
			t.Error("Failed to marshal proto: ", err)
			continue
		}
		log.Debug("Sending ", event)
		producer.Send(topic.String(), topic.String(), sarama.ByteEncoder(eventBytes))
	}
}

func (test *Test) addUsersForTransport(t *testing.T, transportID *ids.CeptaTransportID, userIDs []*users.UserID) {
	for _, userID := range userIDs {
		newUserReq := &usermgmtpb.AddUserRequest{User: &users.InternalUser{
			User:     &users.User{Email: fmt.Sprintf("%s@cepta.org", userID.GetId()), Id: userID},
			Password: "hard-to-guess",
		}}
		_, err := test.usermgmtClient.AddUser(context.Background(), newUserReq)
		if err != nil {
			t.Errorf("Failed to add new user: %v: %v", newUserReq.GetUser(), err)
		}
	}
}
