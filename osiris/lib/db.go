package db

import (
	"bytes"
	"strings"
	"text/template"

	"github.com/urfave/cli/v2"
	"github.com/jinzhu/gorm"
	_ "github.com/jinzhu/gorm/dialects/postgres"
)

type DB struct {
	DB *gorm.DB
}

type DBConfig struct {
	Host 		string
	Port    	uint
	User    	string
	Name    	string
	Password    string
	SSLMode		string
}

func (config DBConfig) ParseCli(ctx *cli.Context) DBConfig {
	return DBConfig{
		Host: ctx.String("db-host"),
		Port: uint(ctx.Int("db-port")),
		User: ctx.String("db-user"),
		Name: ctx.String("db-name"),
		Password: ctx.String("db-password"),
		SSLMode: ctx.String("db-ssl"),
	}
}

var DatabaseCliOptions = []cli.Flag{
	&cli.StringFlag{
		Name: "db-host",
		Value: "localhost",
		Aliases: []string{"db-hostname"},
		EnvVars: []string{"DB_HOST", "DB_HOSTNAME"},
		Usage: "Postgres database host",
	},
	&cli.IntFlag{
		Name: "db-port",
		Value: 5432,
		EnvVars: []string{"DB_PORT"},
		Usage: "Postgres database port",
	},
	&cli.StringFlag{
		Name: "db-user",
		Value: "postgres",
		Aliases: []string{"db-username"},
		EnvVars: []string{"DB_USER", "DB_USERNAME"},
		Usage: "Postgres database user",
	},
	&cli.StringFlag{
		Name: "db-name",
		Value: "postgres",
		Aliases: []string{"db", "database"},
		EnvVars: []string{"DB_NAME", "DB_DATABASE_NAME"},
		Usage: "Postgres database name",
	},
	&cli.StringFlag{
		Name: "db-password",
		Value: "example",
		Aliases: []string{"db-pass"},
		EnvVars: []string{"DB_PASSWORD", "DB_PASS"},
		Usage: "Postgres database password",
	},
	&cli.StringFlag{
		Name: "db-ssl",
		Value: "disable",
		Aliases: []string{"db-sslmode", "ssl-mode", "ssl"},
		EnvVars: []string{"DB_SSL", "DB_SSL_MODE", "SSL", "SSLMODE"},
		Usage: "Postgres database ssl mode",
	},
}



func PostgresDatabase(config *DBConfig) (*DB, error) {
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

	/* drop tables and all data, and recreate them fresh for this run
	db.DropTableIfExists(&User{}, &Pet{}, &Tag{})
	db.AutoMigrate(&User{}, &Pet{}, &Tag{})

	// put all the users into the db
	for _, u := range users {
		if err := db.Create(&u).Error; err != nil {
			return nil, err
		}
	}

	var tg = []Tag{}
	for _, t := range tags {
		if err := db.Create(&t).Error; err != nil {
			return nil, err
		}

		tg = append(tg, t)
	}

	// put all the pets into the db
	for _, p := range pets {
		p.Tags = tg[:rand.Intn(5)]
		if err := db.Create(&p).Error; err != nil {
			return nil, err
		}
	}
	*/
	return &DB{db}, err
}