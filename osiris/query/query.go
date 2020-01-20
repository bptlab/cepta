package main

import (
	"context"
	"database/sql"
	"net/http"
	"strings"
	"io/ioutil"
	"time"
	"fmt"
	"log"
	"os"
    // "path/filepath"

	// "github.com/uber/jaeger-client-go"
	// "github.com/golang/protobuf/proto"
	// "github.com/bptlab/cepta/schemas/types/basic"
	// "/schemas/types/basic"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/friendsofgo/graphiql"
	graphql "github.com/graph-gophers/graphql-go"
	"github.com/graph-gophers/graphql-go/relay"
	"github.com/urfave/cli/v2"
)

var (
	ctx context.Context
	db  *sql.DB
)

func ExampleDB_QueryRowContext() {
	id := 123
	var username string
	var created time.Time
	err := db.QueryRowContext(ctx, "SELECT username, created_at FROM users WHERE id=?", id).Scan(&username, &created)
	switch {
	case err == sql.ErrNoRows:
		log.Printf("no user with id %d\n", id)
	case err != nil:
		log.Fatalf("query error: %v\n", err)
	default:
		log.Printf("username is %q, account created on %s\n", username, created)
	}
}

func loadSchema(path string) (string, error) {
	b, err := ioutil.ReadFile(path)
	if err != nil {
		return "", err
	}
	return string(b), nil
}


// TODO: Schema
const Schema = `
type Vegetable {
    name: String!
    price: Int!
    image: String
}
type Query {
    vegetable(name: String!): Vegetable
}
schema {
    query: Query
}
`

// TODO: Model
type Vegetable struct {
    name  string
    price int
    image *string
}
var vegetables map[string]Vegetable
// Utils
func strPtr(str string) *string {
    return &str
}

func (q *query) Vegetable(ctx context.Context, args struct{ Name string }) *VegetableResolver {
	v, ok := vegetables[strings.ToLower(args.Name)]
	if ok {
		return &VegetableResolver{v: &v}
	}
	return nil
}

type query struct{}

// TODO: Resolver
type VegetableResolver struct {
	v *Vegetable
}

func (r *VegetableResolver) Name() string   { return r.v.name }
func (r *VegetableResolver) Price() int32   { return int32(r.v.price) }
func (r *VegetableResolver) Image() *string { return r.v.image }

func serve(ctx *cli.Context) error {

	/*
	if !ctx.Bool("ginger-crouton") {
		return cli.Exit("Ginger croutons are not in the soup", 86)
	}
	*/

	s, err := loadSchema("test.pb.graphqls")
	if err != nil {
		panic(err)
	}
	schema := graphql.MustParseSchema(s, &query{})
	http.Handle("/query", &relay.Handler{Schema: schema})
	
	// init model
	vegetables = map[string]Vegetable{
		"tomato": Vegetable{name: "Tomato", price: 100, image: strPtr("https://picsum.photos/id/152/100/100")},
		"potato": Vegetable{name: "Potato", price: 50, image: strPtr("https://picsum.photos/id/159/100/100")},
		"corn": Vegetable{name: "Corn", price: 200},
	}
	
	if ctx.Bool("debug") {
		graphiqlHandler, err := graphiql.NewGraphiqlHandler("/query")
		if err != nil {
			panic(err)
		}
		http.Handle("/", graphiqlHandler)
		log.Println("Setup graphiql handler")
	}

	port := fmt.Sprintf(":%d", ctx.Int("port"))
	log.Printf("Server ready at %s", port)
	log.Fatal(http.ListenAndServe(port, nil))
	
	return nil
}

func main() {
	app := &cli.App{
		Name: "CEPTA Query service",
		Usage: "Provides a GraphQL interface for querying transportation data",
		Flags: append(libdb.DatabaseCliOptions, []cli.Flag{
			&cli.BoolFlag{
				Name: "debug",
				Value: false,
				Aliases: []string{"d"},
				EnvVars: []string{"DEBUG", "ENABLE_DEBUG"},
				Usage: "Start a graphiql webserver for testing and debugging queries",
			},
			&cli.IntFlag{
				Name: "port",
				Value: 80,
				Aliases: []string{"p"},
				EnvVars: []string{"PORT"},
				Usage: "GraphQL server port",
			},
		  }...),
		Action: func(ctx *cli.Context) error {
			ret := serve(ctx)
			return ret
		},
	}

	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}