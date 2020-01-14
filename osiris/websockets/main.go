package main

import (
  "fmt"
  "log"
  "net/http"
  "github.com/gorilla/websocket"
)

func homePage(w http.ResponseWriter, r *http.Request) {
  fmt.Fprintf(w, "Home Page")
}

func wsEndpoint(w http.ResponseWriter, r *http.Request) {
  // remove the previous fmt statement
  // fmt.Fprintf(w, "Hello World")
  // Allow all incoming connections
  upgrader.CheckOrigin = func(r *http.Request) bool { return true }

  // upgrade this connection to a WebSocket
  // connection
  ws, err := upgrader.Upgrade(w, r, nil)
  if err != nil {
    log.Println(err)
  }

  log.Println("Client Connected")
  err = ws.WriteMessage(1, []byte("Hi Client!"))
  if err != nil {
    log.Println(err)
  }
  // listen indefinitely for new messages coming
  // through on our WebSocket connection
  reader(ws)
}

// We'll need to define an Upgrader
// this will require a Read and Write buffer size
var upgrader = websocket.Upgrader{
  ReadBufferSize:  1024,
  WriteBufferSize: 1024,
}

// define a reader which will listen for
// new messages being sent to our WebSocket
// endpoint
func reader(conn *websocket.Conn) {
  for {
    // read in a message
    messageType, p, err := conn.ReadMessage()
    if err != nil {
      log.Println(err)
      return
    }
    // print out that message for clarity
    fmt.Println(string(p))

    if err := conn.WriteMessage(messageType, p); err != nil {
      log.Println(err)
      return
    }

  }
}

func setupRoutes() {
  http.HandleFunc("/", homePage)
  http.HandleFunc("/ws", wsEndpoint)
}

func main() {
  fmt.Println("Hello World")
  setupRoutes()
  log.Fatal(http.ListenAndServe(":8080", nil))
}
