package kafka

import (
	"strings"

	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	"github.com/urfave/cli/v2"
)

// Config ...
type Config struct {
	Brokers             []string
	Version             string
	ConnectionTolerance libcli.ConnectionTolerance
}

// ParseCli ...
func (config Config) ParseCli(ctx *cli.Context) Config {
	return Config{
		Brokers:             strings.Split(ctx.String("kafka-brokers"), ","),
		Version:             ctx.String("kafka-version"),
		ConnectionTolerance: libcli.ConnectionTolerance{}.ParseCli(ctx),
	}
}
