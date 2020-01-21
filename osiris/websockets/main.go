package main

import (
	"fmt"
	"net/http"
	"websockets/websocket"
)

//Duplicate code with kafkaproducer -- will be gone with the real kafka queue and protobuf anyway
type Message struct {
	Name string
	UID  int
}

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

func serveWs(pool *websocket.Pool, w http.ResponseWriter, r *http.Request) {
	fmt.Println("WebSocket Endpoint Hit")
	conn, err := websocket.Upgrade(w, r)
	if err != nil {
		fmt.Fprintf(w, "%+v\n", err)
	}

	client := &websocket.Client{
		Conn: conn,
		Pool: pool,
	}

	pool.Register <- client
	client.Read()
}

func setupRoutes() {
	pool := websocket.NewPool()
	go pool.Start()

	go connectKafkaConsumer(pool)

	http.HandleFunc("/ws", func(w http.ResponseWriter, r *http.Request) {
		serveWs(pool, w, r)
	})
}

func main() {
	fmt.Println("Websocket connection and subscription to Kafka")
	setupRoutes()
	http.ListenAndServe(":5555", nil)
}