package rabbitmq

import (
	"fmt"

	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	"github.com/urfave/cli/v2"
)

// Config ...
type Config struct {
	Host         string
	Port         int64
	ExchangeName string
	ConnectionTolerance libcli.ConnectionTolerance
}

// ConnectionURI ...
func (config Config) ConnectionURI() string {
	return fmt.Sprintf("amqp://guest:guest@%s:%d", config.Host, config.Port)
}

// ParseCli ...
func (config Config) ParseCli(ctx *cli.Context) Config {
	return Config{
		Host:         ctx.String("rabbitmq-host"),
		Port:         ctx.Int64("rabbitmq-port"),
		ExchangeName: ctx.String("rabbitmq-exchange-name"),
		ConnectionTolerance: libcli.ConnectionTolerance{}.ParseCli(ctx),
	}
}
