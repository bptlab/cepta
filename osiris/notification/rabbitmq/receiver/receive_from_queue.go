package main

import (
        "log"

        "github.com/streadway/amqp"
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

        args := make(amqp.Table)
        args["x-max-length"] = 5

        q, err := ch.QueueDeclare(
                "user-notifications",    // name
                true, // durable
                false, // delete when unused
                false,  // exclusive
                false, // no-wait
                args,   // arguments
        )
        failOnError(err, "Failed to declare a queue")

        routingKey := getUserID()

        log.Printf("Binding queue %s to exchange %s with routing key %s",
                q.Name, "notification_exchange", routingKey)
        err = ch.QueueBind(
                q.Name,        // queue name
                routingKey,             // routing key
                "notification_exchange", // exchange
                false,
                nil)
        failOnError(err, "Failed to bind a queue")

        msgs, err := ch.Consume(
                q.Name, // queue
                "User" + getUserID(),     // consumer
                false,   // auto ack
                false,  // exclusive
                false,  // no local
                false,  // no wait
                nil,    // args
        )
        failOnError(err, "Failed to register a consumer")

        forever := make(chan bool)

        go func() {
                for d := range msgs {
                        log.Printf("%s", d.Body)
                }
        }()

        log.Printf("Waiting for notifications. To exit press CTRL+C")
        <-forever
}

func getUserID() string {
        // Receive UID to return  it
        return "2"
}