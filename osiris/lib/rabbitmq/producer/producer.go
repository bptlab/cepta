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
	QueueLength int64
}

// ParseCli ...
func (config Config) ParseCli(ctx *cli.Context) Config {
	return Config{
		Config: rabbitmq.Config{}.ParseCli(ctx),
	}
}

// Producer ...
type Producer struct {
	Config Config
	Conn   *amqp.Connection
	ProdCh *amqp.Channel
	ConCh  *amqp.Channel
}

// CreateProducer ...
func (config Config) CreateProducer() (p Producer, err error) {
	// 1. Create the connection to RabbitMQ
	p.Config = config
	p.Conn, err = amqp.Dial(config.Config.ConnectionURI())
	if err != nil {
		err = fmt.Errorf("Failed to connect to RabbitMQ: %v", err)
		return
	}

	// Initialize a channel for the connection
	p.ProdCh, err = p.Conn.Channel()
	if err != nil {
		err = fmt.Errorf("Failed to open a channel: %v", err)
		return
	}

	p.ConCh, err = p.Conn.Channel()
	if err != nil {
		err = fmt.Errorf("Failed to open a channel: %v", err)
		return
	}

	// Configure the exchange for the channel
	if err = p.ProdCh.ExchangeDeclare(
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

// CreateQueue ...
func (p *Producer) CreateQueue(queueName string) (err error) {
	// Define the arguments to configure a queue
	args := make(amqp.Table)
	// Set a maximum queue length
	args["x-max-length"] = p.Config.QueueLength
	_, err = p.ProdCh.QueueDeclare(
		queueName, // name, leave empty to generate a unique name
		true,      // durable
		false,     // delete when unused
		false,     // exclusive
		false,     // no-wait
		args,      // arguments
	)
	if err != nil {
		return
	}

	err = p.ProdCh.QueueBind(
		queueName,             // name of the queue
		queueName,             // bindingKey
		p.Config.ExchangeName, // sourceExchange
		false,                 // noWait
		nil,                   // arguments
	)
	return
}

// ConsumeQueue ...
func (p *Producer) ConsumeQueue(queueName string) (resultChan <-chan amqp.Delivery, err error) {
	resultChan, err = p.ConCh.Consume(
		queueName, // queue
		queueName, // consumer
		false,     // auto ack
		false,     // exclusive
		false,     // no local
		false,     // no wait
		nil,       // args
	)
	return
}

// Publish ...
func (p *Producer) Publish(message []byte, routingKey string) error {
	return p.ProdCh.Publish(
		p.Config.ExchangeName, // exchange
		routingKey,            // routing key
		false,                 // mandatory
		false,                 // immediate
		amqp.Publishing{
			ContentType: "application/octet-stream",
			Body:        message,
		})
}

// Close ...
func (p *Producer) Close() {
	p.Conn.Close()
}
