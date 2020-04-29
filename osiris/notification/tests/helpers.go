package main

import (
	"net/http"
  "net/http/httptest"
  "strings"
  "testing"

	// clientWebsocket "github.com/bptlab/cepta/osiris/notification/websocket"
	// log "github.com/sirupsen/logrus"
	"github.com/gorilla/websocket"
)

var numberUsers int = 5
var upgrader = websocket.Upgrader{}

func (test *Test) websocketHelper(t *testing.T) {
  // Create test server with the echo handler.
  s := httptest.NewServer(http.HandlerFunc(echo))
  defer s.Close()

  // Convert http://127.0.0.1 to ws://127.0.0.
  u := "ws" + strings.TrimPrefix(s.URL, "http")

  // Connect to the server
  ws, _, err := websocket.DefaultDialer.Dial(u, nil)
  if err != nil {
      t.Fatalf("%v", err)
  }
  defer ws.Close()

  // Send message to server, read response and check to see if it's what we expect.
  for i := 0; i < 10; i++ {
      if err := ws.WriteMessage(websocket.TextMessage, []byte("hello")); err != nil {
          t.Fatalf("%v", err)
      }
      _, p, err := ws.ReadMessage()
      if err != nil {
          t.Fatalf("%v", err)
      }
      if string(p) != "hello" {
          t.Fatalf("bad message")
      }
  }

/*
  test.NotificationServer.pool = clientWebsocket.NewPool()
	if err != nil {
		log.Errorf("%+v\n", err)
	}
  userList := getmockUsers(numberUsers, test.NotificationServer.pool, c)

  done := make(chan bool)
  go test.notificationServer.pool.Start()

  for _, client := range userList {
    test.NotificationServer.pool.Register <- client
	  client.Read()
  }

  go publishMessages(test.NotificationServer.pool, done)
  */
}

/*
//Generates <number> many UserIds
func getmockUsers(number int, pool *clientWebsocket.Pool, conn *websocket.Conn) ([]*clientWebsocket.Client){
  var userList []*clientWebsocket.Client

  for i := 0; i < number; i++ {
    client := &clientWebsocket.Client{
      Conn: conn,
    }
    userList = append(userList, client)
  }
  return userList
}

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

func echo(w http.ResponseWriter, r *http.Request) {
    c, err := upgrader.Upgrade(w, r, nil)
    if err != nil {
        return
    }

    // TODO bind Pool to client
    // userList := getmockUsers(numberUsers, c)
    // log.Fatal(userList)

    defer c.Close()
    for {
        mt, message, err := c.ReadMessage()
        if err != nil {
            break
        }
        err = c.WriteMessage(mt, message)
        if err != nil {
            break
        }
    }
}
