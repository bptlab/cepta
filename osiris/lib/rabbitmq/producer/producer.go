package producer

import (
	"encoding/json"
	"log"

	delay "github.com/bptlab/cepta/models/events/traindelaynotificationevent"
	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	"github.com/bptlab/cepta/osiris/lib/rabbitmq"
	"github.com/streadway/amqp"
	"github.com/urfave/cli/v2"
)

// C
var CliOptions = libcli.CommonCliOptions(libcli.RabbitMQ)

// Config ...
type Config struct {
	rabbitmq.Config
}

func (config Config) ParseCli(ctx *cli.Context) Config {
	return Config{
		Config: rabbitmq.Config{}.ParseCli(ctx),
	}
}

func failOnError(err error, msg string) {
	if err != nil {
		log.Fatalf("%s: %s", msg, err)
	}
}

func (config Config) Setup() (*amqp.Connection, *amqp.Channel) {
	// 1. Create the connection to RabbitMQ
	conn, err := amqp.Dial(config.Config.ConnectionURI())
	failOnError(err, "Failed to connect to RabbitMQ")

	// Initialize a channel for the connection
	ch, err := conn.Channel()
	failOnError(err, "Failed to open a channel")

	// Configure the exchange for the channel
	err = ch.ExchangeDeclare(
		config.ExchangeName, // name
		"direct",            // type
		true,                // durable
		false,               // auto-deleted
		false,               // internal
		false,               // no-wait
		nil,                 // arguments
	)
	failOnError(err, "Failed to declare an exchange")

	return conn, ch
}

func (config Config) Publish(delayEvent delay.TrainDelayNotification, ch *amqp.Channel) {
	delayEventMarshal, _ := json.Marshal(delayEvent)
	body := string(delayEventMarshal)

	err := ch.Publish(
		config.ExchangeName,       // exchange
		config.ExchangeRoutingKey, // routing key
		false,                     // mandatory
		false,                     // immediate
		amqp.Publishing{
			ContentType: "text/plain",
			Body:        []byte(body),
		})
	failOnError(err, "Failed to publish a message")

	log.Printf("Sent %s", body)
}
