package rabbitmq

import (
	"fmt"

	"github.com/urfave/cli"
)

// Config ...
type Config struct {
	Host               string
	Port               int64
	ExchangeName       string
	ExchangeRoutingKey string
}

// ConnectionURI ...
func (config Config) ConnectionURI() string {
	return fmt.Sprintf("amqp://guest:guest@%s:%d", config.Host, config.Port)
}

// ParseCli ...
func (config Config) ParseCli(ctx *cli.Context) Config {
	return Config{
		Host:               ctx.String("rabbitmq-host"),
		Port:               ctx.Int64("rabbitmq-port"),
		ExchangeName:       ctx.String("rabbitmq-exchange-name"),
		ExchangeRoutingKey: ctx.String("rabbitmq-exchange-routing-key"),
	}
}
