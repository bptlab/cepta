package db

import (
	"bytes"
	"strings"
	"text/template"

	libcli "github.com/bptlab/cepta/osiris/lib/cli"
	"github.com/jinzhu/gorm"
	_ "github.com/jinzhu/gorm/dialects/postgres"
	"github.com/urfave/cli/v2"
)

// PostgresDB ...
type PostgresDB struct {
	DB *gorm.DB
}

// PostgresDBConfig ...
type PostgresDBConfig struct {
	Host     string
	Port     uint
	User     string
	Name     string
	Password string
	SSLMode  string
}

// ParseCli ...
func (config PostgresDBConfig) ParseCli(ctx *cli.Context) PostgresDBConfig {
	return PostgresDBConfig{
		Host:     ctx.String("postgres-host"),
		Port:     uint(ctx.Int("postgres-port")),
		User:     ctx.String("postgres-user"),
		Name:     ctx.String("postgres-name"),
		Password: ctx.String("postgres-password"),
		SSLMode:  ctx.String("postgres-ssl"),
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
		panic(err)
	}
	connectionOptions := &bytes.Buffer{}
	err = tmpl.Execute(connectionOptions, config)
	if err != nil {
		panic(err)
	}
	db, err := gorm.Open("postgres", connectionOptions.String())
	return &PostgresDB{db}, err
}
