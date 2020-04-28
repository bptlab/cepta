package consumer

import (
	"log"

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
	QueueName   string
	QueueLength int64
}

// ParseCli ...
func (config Config) ParseCli(ctx *cli.Context) Config {
	return Config{
		Config:      rabbitmq.Config{}.ParseCli(ctx),
		QueueName:   ctx.String("rabbitmq-queue-name"),
		QueueLength: ctx.Int64("rabbitmq-queue-length"),
	}
}

func failOnError(err error, msg string) {
	if err != nil {
		log.Fatalf("%s: %s", msg, err)
	}
}

// Setup ...
func (config Config) Setup() (*amqp.Connection, *amqp.Channel) {
	conn, err := amqp.Dial(config.ConnectionURI())
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

	// Define the arguments to configure a queue
	args := make(amqp.Table)
	// Set a maximum queue length
	args["x-max-length"] = config.QueueLength
	// Configure the Queue
	q, err := ch.QueueDeclare(
		config.QueueName, // name
		true,             // durable
		false,            // delete when unused
		false,            // exclusive
		false,            // no-wait
		args,             // arguments
	)
	failOnError(err, "Failed to declare a queue")

	// Binding queue to specified exchange with routing key
	err = ch.QueueBind(
		q.Name,                    // queue name
		config.ExchangeRoutingKey, // routing key
		config.ExchangeName,       // exchange
		false,
		nil)
	failOnError(err, "Failed to bind a queue")

	return conn, ch
}

// Consume ...
func (config Config) Consume(ch *amqp.Channel, conn *amqp.Connection) {
	defer conn.Close()
	defer ch.Close()

	msgs, err := ch.Consume(
		config.QueueName,                 // queue
		"User"+config.ExchangeRoutingKey, // consumer
		false,                            // auto ack
		false,                            // exclusive
		false,                            // no local
		false,                            // no wait
		nil,                              // args
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
