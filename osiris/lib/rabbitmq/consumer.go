package consumer

import (
        "log"
        "fmt"

	      libcli "github.com/bptlab/cepta/osiris/lib/cli"
	      "github.com/urfave/cli/v2"
        "github.com/streadway/amqp"
)

var RabbitMQConsumerCliOptions = libcli.CommonCliOptions(libcli.RabbitMQ)

type RabbitMQConsumerOptions struct {
	Host                string
	Port                int64
	ExchangeName        string
	QueueName           string
	ExchangeRoutingKey  string
	QueueLength         int64
}

func (config RabbitMQConsumerOptions) ParseCli(ctx *cli.Context) RabbitMQConsumerOptions {
	return RabbitMQConsumerOptions{
		Host:               ctx.String("rabbitmq-host"),
		Port:               ctx.Int64("rabbitmq-port"),
		ExchangeName:       ctx.String("rabbitmq-exchange-name"),
		QueueName:          ctx.String("rabbitmq-queue-name"),
		ExchangeRoutingKey: ctx.String("rabbitmq-exchange-routing-key"),
		QueueLength:        ctx.Int64("rabbitmq-queue-length"),
	}
}

func failOnError(err error, msg string) {
        if err != nil {
                log.Fatalf("%s: %s", msg, err)
        }
}

func Setup(options RabbitMQConsumerOptions) (*amqp.Connection, *amqp.Channel){
  // 1. Create the connection to RabbitMQ
  url := fmt.Sprintf("amqp://guest:guest@%s:%d", options.Host, options.Port)
  conn, err := amqp.Dial(url)
  failOnError(err, "Failed to connect to RabbitMQ")

  // Initialize a channel for the connection
  ch, err := conn.Channel()
  failOnError(err, "Failed to open a channel")

  // Configure the exchange for the channel
  err = ch.ExchangeDeclare(
          options.ExchangeName, // name
          "direct",      // type
          true,          // durable
          false,         // auto-deleted
          false,         // internal
          false,         // no-wait
          nil,           // arguments
  )
  failOnError(err, "Failed to declare an exchange")

  // Define the arguments to configure a queue
  args := make(amqp.Table)
  // Set a maximum queue length
  args["x-max-length"] = options.QueueLength
  // Configure the Queue
  q, err := ch.QueueDeclare(
          options.QueueName,    // name
          true,                                 // durable
          false,                                // delete when unused
          false,                                // exclusive
          false,                                // no-wait
          args,                                 // arguments
  )
  failOnError(err, "Failed to declare a queue")

  // Binding queue to specified exchange with routing key
  err = ch.QueueBind(
          q.Name,               // queue name
          options.ExchangeRoutingKey,      // routing key
          options.ExchangeName,            // exchange
          false,
          nil)
  failOnError(err, "Failed to bind a queue")

  return conn, ch
}

func Consume(ch *amqp.Channel, conn *amqp.Connection, options RabbitMQConsumerOptions) {
  // Close Connection and Channel when exciting
  defer conn.Close()
  defer ch.Close()

  msgs, err := ch.Consume(
          options.QueueName, // queue
          "User" + options.ExchangeRoutingKey,     // consumer
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