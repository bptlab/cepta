package producer

import (
        "log"
        "encoding/json"
        "fmt"

        "github.com/streadway/amqp"
	      libcli "github.com/bptlab/cepta/osiris/lib/cli"
	      "github.com/urfave/cli/v2"
	      delay "github.com/bptlab/cepta/models/events/traindelaynotificationevent"
)

var RabbitMQProducerCliOptions = libcli.CommonCliOptions(libcli.RabbitMQ)

type RabbitMQProducerOptions struct {
	Host                string
	Port                int64
	ExchangeName        string
	ExchangeRoutingKey  string
}

func (config RabbitMQProducerOptions) ParseCli(ctx *cli.Context) RabbitMQProducerOptions {
	return RabbitMQProducerOptions{
		Host:               ctx.String("rabbitmq-host"),
		Port:               ctx.Int64("rabbitmq-port"),
		ExchangeName:       ctx.String("rabbitmq-exchange-name"),
		ExchangeRoutingKey: ctx.String("rabbitmq-exchange-routing-key"),
	}
}

func failOnError(err error, msg string) {
        if err != nil {
                log.Fatalf("%s: %s", msg, err)
        }
}

func Setup(options RabbitMQProducerOptions) (*amqp.Connection, *amqp.Channel){
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

  return conn, ch
}

func Publish(delayEvent delay.TrainDelayNotification, options RabbitMQProducerOptions, ch *amqp.Channel) {
        delayEventMarshal, _ := json.Marshal(delayEvent)
        body := string(delayEventMarshal)

        err := ch.Publish(
                options.ExchangeName,         // exchange
                options.ExchangeRoutingKey, // routing key
                false, // mandatory
                false, // immediate
                amqp.Publishing{
                        ContentType: "text/plain",
                        Body:        []byte(body),
                })
        failOnError(err, "Failed to publish a message")

        log.Printf("Sent %s", body)
}
