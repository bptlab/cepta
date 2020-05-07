package consumer

import (
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
		// QueueName:   ctx.String("rabbitmq-queue-name"),
		// QueueLength: ctx.Int64("rabbitmq-queue-length"),
	}
}

// Consumer ...
type Consumer struct {
	Config Config
	Conn   *amqp.Connection
	Ch     *amqp.Channel
}

// CreateConsumer ...
func (config Config) CreateConsumer() (c Consumer, err error) {
	c.Config = config
	c.Conn, err = amqp.Dial(config.ConnectionURI())
	if err != nil {
		return
	}
	return WithConnection(c.Conn)
}

// WithConnection ...
func WithConnection(conn *amqp.Connection) (c Consumer, err error) {
	c.Conn = conn
	c.Ch, err = c.Conn.Channel()
	return
}

// ConsumeQueue ...
func (c *Consumer) ConsumeQueue(queueName string) (resultChan <-chan amqp.Delivery, err error) {
	resultChan, err = c.Ch.Consume(
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

// Close ...
func (c *Consumer) Close() {
	c.Conn.Close()
}
