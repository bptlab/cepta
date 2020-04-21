package main

import (
        "log"
        "encoding/json"

        "github.com/streadway/amqp"
	      delay "github.com/bptlab/cepta/models/events/traindelaynotificationevent"

)


func failOnError(err error, msg string) {
        if err != nil {
                log.Fatalf("%s: %s", msg, err)
        }
}

func main() {
        conn, err := amqp.Dial("amqp://guest:guest@localhost:5672")
        failOnError(err, "Failed to connect to RabbitMQ")
        defer conn.Close()

        ch, err := conn.Channel()
        failOnError(err, "Failed to open a channel")
        defer ch.Close()

        err = ch.ExchangeDeclare(
                "notification_exchange", // name
                "direct",      // type
                true,          // durable
                false,         // auto-deleted
                false,         // internal
                false,         // no-wait
                nil,           // arguments
        )
        failOnError(err, "Failed to declare an exchange")

        body := getBodyFrom()
        err = ch.Publish(
                "notification_exchange",         // exchange
                getUserID(), // routing key
                false, // mandatory
                false, // immediate
                amqp.Publishing{
                        ContentType: "text/plain",
                        Body:        []byte(body),
                })
        failOnError(err, "Failed to publish a message")

        log.Printf("Sent %s", body)
}

func getBodyFrom() string {
        // Receive TrainNotificationUpdate

        delayEvent := &delay.TrainDelayNotification{
          TrainId: 4322,
          LocationId: 80123456,
          Delay: 30,
        }
        delayEventMarshal, _ := json.Marshal(delayEvent)
        return string(delayEventMarshal)
}

func getUserID() string {
        // Receive UID to return  it
        return "2"
}