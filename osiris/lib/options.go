package cli

import (
	libutils "github.com/bptlab/cepta/osiris/lib/utils"
	"github.com/romnnn/flags4urfavecli/flags"
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

	// RabbitMQ options
	RabbitMQ
	RabbitMQHost
	RabbitMQPort
	RabbitMQExchangeName
	RabbitMQQueueName
	RabbitMQExchangeRoutingKey
	RabbitMQQueueLength

	// Service options
	ServicePort
	ServiceLogLevel

	// Connection Tolerance
	ServiceConnectionTolerance
	ConnectionTimeoutSec
	MaxRetries
	RetryIntervalSec
)

// ConnectionTolerance ...
type ConnectionTolerance struct {
	ConnectionTimeoutSec int
	MaxRetries           int
	RetryIntervalSec     int
}

func (ct ConnectionTolerance) ParseCli(ctx *cli.Context) ConnectionTolerance {
	t := ConnectionTolerance{
		ConnectionTimeoutSec: ctx.Int("connection-timeout-sec"),
		MaxRetries:           ctx.Int("connection-max-retries"),
		RetryIntervalSec:     ctx.Int("connection-retry-interval-sec"),
	}
	if t.ConnectionTimeoutSec > 0 {
		t.MaxRetries = 1
		t.RetryIntervalSec = t.ConnectionTimeoutSec
	}
	return t
}

func (ct ConnectionTolerance) Timeout() int {
	if ct.ConnectionTimeoutSec > 0 {
		return ct.ConnectionTimeoutSec
	}
	return ct.MaxRetries * ct.RetryIntervalSec
}

func CommonCliOptions(options ...int) []cli.Flag {
	commonFlags := []cli.Flag{}
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

		case RabbitMQ:
			newOptions = CommonCliOptions(RabbitMQHost)
		case RabbitMQHost:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "rabbitmq-host",
				Value:   "localhost",
				EnvVars: []string{"RABBITMQ_HOST"},
				Usage:   "RabbitMQ Host to connect to",
			}}
		case RabbitMQPort:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "rabbitmq-port",
				Value:   "5672",
				EnvVars: []string{"RABBITMQ_PORT"},
				Usage:   "RabbitMQ Port to connect to",
			}}
    case RabbitMQExchangeName:
      newOptions = []cli.Flag{&cli.StringFlag{
        Name:    "rabbitmq-exchange-name",
        Value:   "notification_exchange",
        Aliases: []string{"exchange-name"},
        EnvVars: []string{"RABBITMQ_EXCHANGE_NAME"},
        Usage:   "RabbitMQ exchange name to connect to",
      }}
    case RabbitMQQueueName:
      newOptions = []cli.Flag{&cli.StringFlag{
        Name:    "rabbitmq-queue-name",
        Value:   "user_notification",
        Aliases: []string{"queue-name"},
        EnvVars: []string{"RABBITMQ_QUEUE_NAME"},
        Usage:   "RabbitMQ queue name to connect to",
      }}
    case RabbitMQExchangeRoutingKey:
      newOptions = []cli.Flag{&cli.StringFlag{
        Name:    "rabbitmq-exchange-routing-key",
        Value:   "0",
        Aliases: []string{"exchange-routing-key"},
        EnvVars: []string{"RABBITMQ_ROUTING_KEY"},
        Usage:   "RabbitMQ exchange routing key to connect to the queue belonging to the user",
      }}
    case RabbitMQQueueLength:
      newOptions = []cli.Flag{&cli.IntFlag{
        Name:    "rabbitmq-queue-length",
        Value:   100,
        Aliases: []string{"queue-length"},
        EnvVars: []string{"RABBITMQ_QUEUE_LENGTH"},
        Usage:   "RabbitMQ to set the correct queue length",
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
				Value:   "localhost",
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
				Value:   "replay",
				Aliases: []string{"mongodb-name", "mongodb-db"},
				EnvVars: []string{"MONGODB_DATABASE_NAME", "MONGODB_NAME"},
				Usage:   "mongodb database name",
			}}
		case MongoUser:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "mongodb-user",
				Value:   "root",
				EnvVars: []string{"MONGODB_USERNAME", "MONGODB_USER"},
				Usage:   "mongodb database username",
			}}
		case MongoPassword:
			newOptions = []cli.Flag{&cli.StringFlag{
				Name:    "mongodb-password",
				Value:   "example",
				EnvVars: []string{"MONGODB_PASSWORD", "MONGO_PASS"},
				Usage:   "mongodb database password",
			}}

		case ServiceLogLevel:
			newOptions = []cli.Flag{&flags.LogLevelFlag}
		case ServicePort:
			newOptions = []cli.Flag{&cli.IntFlag{
				Name:    "port",
				Value:   80,
				Aliases: []string{"p"},
				EnvVars: []string{"PORT"},
				Usage:   "Service port",
			}}

		case ServiceConnectionTolerance:
			newOptions = CommonCliOptions(ConnectionTimeoutSec, MaxRetries, RetryIntervalSec)
		case ConnectionTimeoutSec:
			newOptions = []cli.Flag{&cli.IntFlag{
				Name:    "connection-timeout-sec",
				Value:   0, // Default is max retries and retry interval
				Aliases: []string{"timeout-sec"},
				EnvVars: []string{"TIMEOUT_SEC", "CONNECTION_TIMEOUT_SEC"},
				Usage:   "Timeout for connections during startup",
			}}
		case MaxRetries:
			newOptions = []cli.Flag{&cli.IntFlag{
				Name:    "connection-max-retries",
				Value:   12,
				Aliases: []string{"max-retries"},
				EnvVars: []string{"MAX_RETRIES", "CONNECTION_MAX_RETRIES"},
				Usage:   "Max number of retries when connections fail during startup",
			}}
		case RetryIntervalSec:
			newOptions = []cli.Flag{&cli.IntFlag{
				Name:    "connection-retry-interval-sec",
				Value:   10,
				Aliases: []string{"retry-interval-sec"},
				EnvVars: []string{"RETRY_INTERVAL_SEC", "CONNECTION_RETRY_INTERVAL_SEC"},
				Usage:   "Number of seconds between connection attempts",
			}}
		default:
			// Do not add any
			continue
		}
		commonFlags = append(commonFlags, newOptions...)
	}
	return commonFlags
}
