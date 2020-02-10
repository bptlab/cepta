package cli

import (
	libutils "github.com/bptlab/cepta/osiris/lib/utils"
	"github.com/urfave/cli/v2"
)

const (
	// Kafka options
	Kafka    	= iota
	KafkaBroker
	KafkaGroup
	KafkaVersion
	KafkaTopics
	
	Postgres
	PostgresHost
	PostgresPort
	PostgresUser
	PostgresDatabase
	PostgresPassword
	PostgresSSL

	// Postgres options
	Thursday
	Friday
	Saturday

	// Service options
	ServicePort
	ServiceLogLevel
)

func CommonCliOptions(options ...int) []cli.Flag {
	flags := []cli.Flag{}
	uniqueOptions := libutils.Unique(options)
	for _, option := range uniqueOptions {
		newOptions := []cli.Flag{}
		switch option {
		case Kafka:
			newOptions = CommonCliOptions(KafkaBroker, KafkaGroup, KafkaVersion, KafkaTopics)
		case KafkaBroker:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name: "kafka-brokers",
				Value: "localhost:29092",
				Aliases: []string{"brokers"},
				EnvVars: []string{"KAFKA_BROKERS", "BROKERS"},
				Usage: "Kafka bootstrap brokers to connect to, as a comma separated list",
			}}
		case KafkaGroup:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name: "kafka-group",
				Aliases: []string{"group"},
				EnvVars: []string{"KAFKA_GROUP", "GROUP"},
				Usage: "Kafka consumer group definition",
			}}
		case KafkaVersion:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name: "kafka-version",
				Value: "2.1.1",
				Aliases: []string{"kafka"},
				EnvVars: []string{"KAFKA_VERSION", "KAFKA"},
				Usage: "Kafka cluster version",
			}}
		case KafkaTopics:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name: "kafka-topics",
				Value: "news_for_leo",
				Aliases: []string{"topics"},
				EnvVars: []string{"TOPICS", "KAFKA_TOPICS"},
				Usage: "Kafka topics to be consumed, as a comma seperated list",
			}}
		
		case Postgres:
			newOptions = CommonCliOptions(PostgresHost, PostgresPort, PostgresUser, PostgresDatabase, PostgresPassword, PostgresSSL)
		case PostgresHost:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name: "db-host",
				Value: "localhost",
				Aliases: []string{"db-hostname"},
				EnvVars: []string{"DB_HOST", "DB_HOSTNAME"},
				Usage: "Postgres database host",
			}}
		case PostgresPort:
			newOptions = []cli.Flag{&cli.IntFlag{
				Name: "db-port",
				Value: 5432,
				EnvVars: []string{"DB_PORT"},
				Usage: "Postgres database port",
			}}
		case PostgresUser:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name: "db-user",
				Value: "postgres",
				Aliases: []string{"db-username"},
				EnvVars: []string{"DB_USER", "DB_USERNAME"},
				Usage: "Postgres database user",
			}}
		case PostgresDatabase:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name: "db-name",
				Value: "postgres",
				Aliases: []string{"db", "database"},
				EnvVars: []string{"DB_NAME", "DB_DATABASE_NAME"},
				Usage: "Postgres database name",
			}}
		case PostgresPassword:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name: "db-password",
				Value: "example",
				Aliases: []string{"db-pass"},
				EnvVars: []string{"DB_PASSWORD", "DB_PASS"},
				Usage: "Postgres database password",
			}}
		case PostgresSSL:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name: "db-ssl",
				Value: "disable",
				Aliases: []string{"db-sslmode", "ssl-mode", "ssl"},
				EnvVars: []string{"DB_SSL", "DB_SSL_MODE", "SSL", "SSLMODE"},
				Usage: "Postgres database ssl mode",
			}}
		
		case ServiceLogLevel:
			newOptions = []cli.Flag{&cli.GenericFlag{
				Name: "log",
				Value: &EnumValue{
					Enum:    []string{"info", "debug", "warn", "fatal", "trace", "error", "panic"},
					Default: "info",
				},
				Aliases: []string{"log-level"},
				EnvVars: []string{"LOG", "LOG_LEVEL"},
				Usage:   "Log level",
			}}
		case ServicePort:
			newOptions = []cli.Flag{&cli.IntFlag{
				Name:    "port",
				Value:   80,
				Aliases: []string{"p"},
				EnvVars: []string{"PORT"},
				Usage:   "grpc service port",
			}}
		default:
			// Do not add any
			continue
		}
		flags = append(flags, newOptions...)
	}
	return flags
}