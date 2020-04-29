package producer

import (
	"fmt"

	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	"github.com/bptlab/cepta/osiris/lib/rabbitmq"
	"github.com/streadway/amqp"
	"github.com/urfave/cli/v2"
)

// CliOptions ...
var CliOptions = libcli.CommonCliOptions(libcli.RabbitMQ)

// Config ...
type Config struct {
	rabbitmq.Config
}

// ParseCli ...
func (config Config) ParseCli(ctx *cli.Context) Config {
	return Config{
		Config: rabbitmq.Config{}.ParseCli(ctx),
	}
}

// Setup ...
func (config Config) Setup() (conn *amqp.Connection, ch *amqp.Channel, err error) {
	// 1. Create the connection to RabbitMQ
	conn, err = amqp.Dial(config.Config.ConnectionURI())
	if err != nil {
		err = fmt.Errorf("Failed to connect to RabbitMQ: %v", err)
		return
	}

	// Initialize a channel for the connection
	ch, err = conn.Channel()
	if err != nil {
		err = fmt.Errorf("Failed to open a channel: %v", err)
		return
	}

	// Configure the exchange for the channel
	if err = ch.ExchangeDeclare(
		config.ExchangeName, // name
		"direct",            // type
		true,                // durable
		false,               // auto-deleted
		false,               // internal
		false,               // no-wait
		nil,                 // arguments
	); err != nil {
		err = fmt.Errorf("Failed to declare an exchange: %v", err)
		return
	}
	return
}

// Publish ...
func (config Config) Publish(message []byte, ch *amqp.Channel) error {
	return ch.Publish(
		config.ExchangeName,       // exchange
		config.ExchangeRoutingKey, // routing key
		false,                     // mandatory
		false,                     // immediate
		amqp.Publishing{
			ContentType: "application/octet-stream",
			Body:        message,
		})
}
