package main

import (
	"log"
	"bytes"
	"strings"
	"text/template"

	"github.com/jinzhu/gorm"
	_ "github.com/jinzhu/gorm/dialects/postgres"
)

type DB struct {
	DB *gorm.DB
}

type DBConfig struct {
	Host string
	Port    uint
	User    string
	Name    string
	Password    string
}

func postgresDatabase(config *DBConfig) (*DB, error) {
	options := []string{
		"host={{.Host}}",
		"port={{.Port}}",
		"user={{.User}}",
		"dbname={{.Name}}",
		"password={{.Password}}",
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
	log.Println(connectionOptions.String())
	db, err := gorm.Open("postgres", connectionOptions.String())
  	defer db.Close()

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
	return &DB{db}, nil
}