package cli

import (
	libutils "github.com/bptlab/cepta/osiris/lib/utils"
	"github.com/urfave/cli/v2"
)

const (
	// Kafka options
	Kafka = iota
	KafkaBroker
	KafkaGroup
	KafkaVersion
	KafkaTopics

	// Postgres options
	Postgres
	PostgresHost
	PostgresPort
	PostgresUser
	PostgresDatabase
	PostgresPassword
	PostgresSSL

	// Mongodb options
	Mongo
	MongoHost
	MongoPort
	MongoDatabase
	MongoUser
	MongoPassword

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
				Name:    "kafka-brokers",
				Value:   "localhost:29092",
				Aliases: []string{"brokers"},
				EnvVars: []string{"KAFKA_BROKERS", "BROKERS"},
				Usage:   "Kafka bootstrap brokers to connect to, as a comma separated list",
			}}
		case KafkaGroup:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "kafka-group",
				Aliases: []string{"group"},
				EnvVars: []string{"KAFKA_GROUP", "GROUP"},
				Usage:   "Kafka consumer group definition",
			}}
		case KafkaVersion:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "kafka-version",
				Value:   "2.1.1",
				Aliases: []string{"kafka"},
				EnvVars: []string{"KAFKA_VERSION", "KAFKA"},
				Usage:   "Kafka cluster version",
			}}
		case KafkaTopics:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "kafka-topics",
				Aliases: []string{"topics"},
				EnvVars: []string{"TOPICS", "KAFKA_TOPICS"},
				Usage:   "Kafka topics to be consumed, as a comma seperated list",
			}}

		case Postgres:
			newOptions = CommonCliOptions(PostgresHost, PostgresPort, PostgresUser, PostgresDatabase, PostgresPassword, PostgresSSL)
		case PostgresHost:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "postgres-host",
				Value:   "localhost",
				Aliases: []string{"postgres-hostname"},
				EnvVars: []string{"POSTGRES_HOST", "POSTGRES_HOSTNAME"},
				Usage:   "Postgres database host",
			}}
		case PostgresPort:
			newOptions = []cli.Flag{&cli.IntFlag{
				Name:    "postgres-port",
				Value:   5432,
				EnvVars: []string{"POSTGRES_PORT"},
				Usage:   "Postgres database port",
			}}
		case PostgresUser:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "postgres-user",
				Value:   "postgres",
				Aliases: []string{"postgres-username"},
				EnvVars: []string{"POSTGRES_USER", "POSTGRES_USERNAME"},
				Usage:   "Postgres database user",
			}}
		case PostgresDatabase:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "postgres-name",
				Value:   "postgres",
				Aliases: []string{"postgres", "database"},
				EnvVars: []string{"POSTGRES_NAME", "POSTGRES_DATABASE_NAME"},
				Usage:   "Postgres database name",
			}}
		case PostgresPassword:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "postgres-password",
				Value:   "example",
				Aliases: []string{"postgres-pass"},
				EnvVars: []string{"POSTGRES_PASSWORD", "POSTGRES_PASS"},
				Usage:   "Postgres database password",
			}}
		case PostgresSSL:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "postgres-ssl",
				Value:   "disable",
				Aliases: []string{"postgres-sslmode", "ssl-mode", "ssl"},
				EnvVars: []string{"POSTGRES_SSL", "POSTGRES_SSL_MODE", "SSL", "SSLMODE"},
				Usage:   "Postgres database ssl mode",
			}}

		case Mongo:
			newOptions = CommonCliOptions(MongoHost, MongoPort, MongoDatabase, MongoUser, MongoPassword)
		case MongoHost:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "mongodb-host",
				Value:   "mongodb",
				EnvVars: []string{"MONGODB_HOST", "MONGO_HOST"},
				Usage:   "mongodb database host",
			}}
		case MongoPort:
			newOptions = []cli.Flag{&cli.IntFlag{
				Name:    "mongodb-port",
				Value:   27017,
				EnvVars: []string{"MONGODB_PORT", "MONGO_PORT"},
				Usage:   "mongodb database port",
			}}
		case MongoDatabase:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "mongodb-database",
				Value:   "replaydata",
				Aliases: []string{"mongodb-name", "mongodb-db"},
				EnvVars: []string{"MONGODB_DATABASE_NAME", "MONGODB_NAME"},
				Usage:   "mongodb database name",
			}}
		case MongoUser:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "mongodb-user",
				Value:   "",
				EnvVars: []string{"MONGODB_USERNAME", "MONGODB_USER"},
				Usage:   "mongodb database username",
			}}
		case MongoPassword:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "mongodb-password",
				Value:   "",
				EnvVars: []string{"MONGODB_PASSWORD", "MONGO_PASS"},
				Usage:   "mongodb database password",
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
				Usage:   "Service port",
			}}
		default:
			// Do not add any
			continue
		}
		flags = append(flags, newOptions...)
	}
	return flags
}
