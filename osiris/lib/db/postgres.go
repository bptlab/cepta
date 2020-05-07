package db

import (
	"bytes"
	"fmt"
	"strings"
	"text/template"
	"time"

	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	"github.com/jinzhu/gorm"
	_ "github.com/jinzhu/gorm/dialects/postgres"
	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
)

// PostgresDB ...
type PostgresDB struct {
	DB *gorm.DB
}

// PostgresDBConfig ...
type PostgresDBConfig struct {
	Host                string
	Port                uint
	User                string
	Name                string
	Password            string
	SSLMode             string
	ConnectionTolerance libcli.ConnectionTolerance
}

// ParseCli ...
func (config PostgresDBConfig) ParseCli(ctx *cli.Context) PostgresDBConfig {
	return PostgresDBConfig{
		Host:                ctx.String("postgres-host"),
		Port:                uint(ctx.Int("postgres-port")),
		User:                ctx.String("postgres-user"),
		Name:                ctx.String("postgres-name"),
		Password:            ctx.String("postgres-password"),
		SSLMode:             ctx.String("postgres-ssl"),
		ConnectionTolerance: libcli.ConnectionTolerance{}.ParseCli(ctx),
	}
}

// PostgresDatabaseCliOptions ...
var PostgresDatabaseCliOptions = libcli.CommonCliOptions(libcli.Postgres)

// PostgresDatabase ...
func PostgresDatabase(config *PostgresDBConfig) (*PostgresDB, error) {
	options := []string{
		"host={{.Host}}",
		"port={{.Port}}",
		"user={{.User}}",
		"dbname={{.Name}}",
		"password={{.Password}}",
		"sslmode={{.SSLMode}}",
	}
	tmpl, err := template.New("config").Parse(strings.Join(options, " "))
	if err != nil {
		return nil, err
	}
	connectionOptions := &bytes.Buffer{}
	err = tmpl.Execute(connectionOptions, config)
	if err != nil {
		return nil, err
	}

	var attempt int
	for {
		db, err := gorm.Open("postgres", connectionOptions.String())
		if err != nil {
			if attempt >= config.ConnectionTolerance.MaxRetries {
				return nil, fmt.Errorf("failed to connect to postgres: %s", err.Error())
			}
			attempt++
			log.Infof("Failed to connect: %s. (Attempt %d of %d)", err.Error(), attempt, config.ConnectionTolerance.MaxRetries)
			time.Sleep(time.Duration(config.ConnectionTolerance.RetryIntervalSec) * time.Second)
			continue
		}
		return &PostgresDB{db}, nil
	}
}
