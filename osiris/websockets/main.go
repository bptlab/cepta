package main

import (
	"fmt"
	"net/http"
	"websockets/websocket"
)

func connectKafkaConsumer() () {

	config := sarama.NewConfig()
	config.Consumer.Return.Errors = true

	// Specify brokers address. This is default one
	brokers := []string{"localhost:29092"}

	// Create new consumer
	master, err := sarama.NewConsumer(brokers, config)
	if err != nil {
		panic(err)
	}

	defer func() {
		if err := master.Close(); err != nil {
			panic(err)
		}
	}()

	topic := "news_for_leo"
	// How to decide partition, is it fixed value...?
	consumer, err := master.ConsumePartition(topic, 0, sarama.OffsetOldest)
	if err != nil {
		panic(err)
	}
	
	signals := make(chan os.Signal, 1)
	signal.Notify(signals, os.Interrupt)

	// Count how many message processed
	msgCount := 0
	var message string = ""

	// Get signal for finish
	doneCh := make(chan struct{})
	go func() {
		for {
			select {
			case err := <-consumer.Errors():
				fmt.Println(err)
			case msg := <-consumer.Messages():
				msgCount++
				fmt.Println("Received messages", string(msg.Key), string(msg.Value))
			case <-signals:
				fmt.Println("Interrupt is detected")
				doneCh <- struct{}{}
			}
		}
	}()
	<-doneCh
	fmt.Println("Processed", msgCount, "messages")



	/*
	// start consumer, emit to ws
	noopTicker := time.NewTicker(time.Second * 5)
	subscriberDone := make(chan bool, 1)
	stopSubscriber := make(chan bool, 1)

	go func() {
		defer func() { subscriberDone <- true }()
		var (
			payload []byte
			msgs    = sub.Start()
		)
		for {
			select {
			case msg := <-msgs:
				payload = msg.Message()
				err = ws.SetWriteDeadline(time.Now().Add(time.Second * 30))
				if err != nil {
					server.LogWithFields(r).WithField("error", err).Error("unable to write deadline")
				}
				err = ws.WriteMessage(websocket.TextMessage, payload)
				if err != nil {
					server.LogWithFields(r).WithField("error", err).Error("unable to write ws message")
				}
				err = msg.Done()
				if err != nil {
					server.LogWithFields(r).WithField("error", err).Error("unable to mark gizmo message as done")
				}
			case <-noopTicker.C:
				err = ws.SetWriteDeadline(time.Now().Add(time.Second * 30))
				if err != nil {
					server.LogWithFields(r).WithField("error", err).Error("unable to write deadline")
				}
				if err := ws.WriteMessage(websocket.PingMessage, []byte("ping")); err != nil {
					server.LogWithFields(r).WithField("error", err).Error("error writing ws message")
					return
				}
			case <-stopSubscriber:
				return
			}
		}
	}()

	<-subscriberDone
	noopTicker.Stop()
	*/
}

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

	// edited
	go connectKafkaConsumer()

	http.HandleFunc("/ws", func(w http.ResponseWriter, r *http.Request) {
		serveWs(pool, w, r)
	})
}

func main() {
	fmt.Println("Websocket connection and subscription to Kafka")
	setupRoutes()
	http.ListenAndServe(":5555", nil)
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


// var consumer sarama.PartitionConsumer = connectKafkaConsumer()

// Need to define an Upgrade
var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
}

func connectKafkaConsumer() () {

	config := sarama.NewConfig()
	config.Consumer.Return.Errors = true

	// Specify brokers address. This is default one
	brokers := []string{"localhost:29092"}

	// Create new consumer
	master, err := sarama.NewConsumer(brokers, config)
	if err != nil {
		panic(err)
	}

	defer func() {
		if err := master.Close(); err != nil {
			panic(err)
		}
	}()

	topic := "news_for_leo"
	// How to decide partition, is it fixed value...?
	consumer, err := master.ConsumePartition(topic, 0, sarama.OffsetOldest)
	if err != nil {
		panic(err)
	}

	signals := make(chan os.Signal, 1)
	signal.Notify(signals, os.Interrupt)

	// Count how many message processed
	msgCount := 0
	var message string = ""

	// Get signnal for finish
	doneCh := make(chan struct{})
	go func() (string) {
		for {
			select {
			case err := <-consumer.Errors():
				fmt.Println(err)
			case msg := <-consumer.Messages():
				msgCount++
				message <-string(msg.Value)
				fmt.Println("Received messages", string(msg.Key), string(msg.Value))
			case <-signals:
				fmt.Println("Interrupt is detected")
				doneCh <- struct{}{}
			}
		}
	}()
	<-doneCh
	fmt.Println("Processed", msgCount, "messages")
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
		//message := writeOutput(consumer)

		// send data to the client
		textual := <-connectKafkaConsumer()
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



*/

/*

(sarama.PartitionConsumer) {
	config := sarama.NewConfig()
	config.Consumer.Return.Errors = true

	// Specify brokers address. This is default one
	brokers := []string{"localhost:29092"}

	// Create new consumer
	master, err := sarama.NewConsumer(brokers, config)
	if err != nil {
		panic(err)
	}

	defer func() {
		if err := master.Close(); err != nil {
			panic(err)
		}
	}()

	topic := "news_for_leo"
	// How to decide partition, is it fixed value...?
	consumer, err := master.ConsumePartition(topic, 0, sarama.OffsetOldest)
	if err != nil {
		panic(err)
	}

	return consumer
}

func writeOutput(consumer sarama.PartitionConsumer) (msg string) {
	var message string = ""

	select {
	case err := <-consumer.Errors():
		fmt.Println(err)
	case msg := <-consumer.Messages():
		message = string(msg.Value)
		fmt.Println("Received messages", string(msg.Key), string(msg.Value))
	}

	return message
}
*/
