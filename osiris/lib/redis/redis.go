package redis

import (
	"fmt"

	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	"github.com/urfave/cli/v2"
)

// Config ...
type Config struct {
	Host         string
	Port         int
	Password 	 string
	Database 	 int
	ConnectionTolerance libcli.ConnectionTolerance
}

// ConnectionURI ...
func (config Config) ConnectionURI() string {
	return fmt.Sprintf("%s:%d", config.Host, config.Port)
}

// ParseCli ...
func (config Config) ParseCli(ctx *cli.Context) Config {
	return Config{
		Host:         	ctx.String("redis-host"),
		Port:         	ctx.Int("redis-port"),
		Password:    	ctx.String("redis-password"),
		Database:       ctx.Int("redis-database"),
		ConnectionTolerance: libcli.ConnectionTolerance{}.ParseCli(ctx),
	}
}