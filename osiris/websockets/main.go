package main

import (
	"log"
	"net/http"

	"github.com/gorilla/websocket"
	"github.com/Shopify/sarama"
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

var consumer sarama.PartitionConsumer = connectKafkaConsumer()

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
		panic(err)
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
		_, _, err := conn.ReadMessage()
		if err != nil {
			log.Println(err)
			return
		}

		// Consume Kafka messages
		message := writeOutput(consumer)

		// send data to the client
		textual := message
		conn.WriteMessage(1, []byte(textual))
	}
}

func setupRoutes() {
	http.HandleFunc("/ws", wsEndpoint)
}

func main() {
	setupRoutes()
	log.Fatal(http.ListenAndServe(":5555", nil))
}
