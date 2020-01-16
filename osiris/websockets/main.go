package main

import (
	"log"
	"net/http"
  "fmt"
  "github.com/linkedin/goavro/v2"
	"github.com/gorilla/websocket"
)

/*
type Traindata struct {
	StationID int    `json:"id"`
	Station   string `json:"station"`
	oldETA    string `json:"old"`
	Delay     string `json:"delay"`
	Cause     string `json:"cause"`
	newETA    string `json:"new"`
}
*/


// Need to define an Upgrade
var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
}

func wsEndpoint(w http.ResponseWriter, r *http.Request) {
	// Allow all incoming connections
	upgrader.CheckOrigin = func(r *http.Request) bool { return true }

	// upgrade this connection to a WebSocket
	// connection
	ws, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Println(err)
	}

	log.Println("Client Connected")
	if err != nil {
		log.Println(err)
	}

	// listen indefinitely for new messages coming
	// through on our WebSocket connection
	reader(ws)
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

    /*
    codec, err := goavro.NewCodec(`
              {
                "namespace": "org.bptlab.cepta",
                "type": "record",
                "name": "TrainDelayNotification",
                "fields": [
                  {"name": "train_id", "type": "int"},
                  {"name": "location_id", "type": "int"},
                  {"name": "delay", "type": "string"}
                  ]
              }
              `)
    */
     
    textual := []byte(`{"train_id":"10", "location_id":"12", "delay":"30min"}`)

    // Convert textual Avro data (in Avro JSON format) to native Go form
    native, _, err := codec.NativeFromTextual(textual)
    fmt.Println(native)

		// send data to the client
		conn.WriteMessage(1, []byte(`{"next":{"LongList":{}}}`))

		if err != nil {
			log.Println(string(messageType))
			log.Println(string(p))
			return
		}

	}
}

func setupRoutes() {
	http.HandleFunc("/ws", wsEndpoint)
}

func main() {
	setupRoutes()
	log.Fatal(http.ListenAndServe(":5555", nil))
}
