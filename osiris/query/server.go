package main

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"os"
	"path"

	"path/filepath"

	// "github.com/uber/jaeger-client-go"
	// "github.com/golang/protobuf/proto"
	// "github.com/bptlab/cepta/schemas/types/basic"
	// "/schemas/types/basic"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/bptlab/cepta/osiris/query/resolvers"
	"github.com/friendsofgo/graphiql"
	graphql "github.com/graph-gophers/graphql-go"
	"github.com/graph-gophers/graphql-go/relay"
	log "github.com/sirupsen/logrus"
	"github.com/urfave/cli/v2"
)

/*
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
*/

func loadSchema(path string) (string, error) {
	b, err := ioutil.ReadFile(path)
	if err != nil {
		return "", err
	}
	return string(b), nil
}

func strPtr(str string) *string {
	return &str
}

func serve(ctx *cli.Context) error {

	/*
		if !ctx.Bool("ginger-crouton") {
			return cli.Exit("Ginger croutons are not in the soup", 86)
		}
	*/

	dir, err := filepath.Abs(filepath.Dir(os.Args[0]))
	if err != nil {
		log.Fatal(err)
	}
	schemaPath := path.Join(dir, "query.runfiles", "__main__", ctx.String("schema"))

	s, err := loadSchema(schemaPath)
	if err != nil {
		panic(err)
	}

	config := libdb.PostgresDBConfig{}.ParseCli(ctx)
	db, err := libdb.PostgresDatabase(&config)
	if err != nil {
		log.Fatalf("failed to initialize database: %v", err)
	}

	schema := graphql.MustParseSchema(s, &resolvers.Resolver{DB: &resolvers.QueryDB{DB: db}}, graphql.UseStringDescriptions())
	http.Handle("/query", &relay.Handler{Schema: schema})

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
		Name:  "CEPTA Query service",
		Usage: "Provides a GraphQL interface for querying transportation data",
		Flags: append(libdb.PostgresDatabaseCliOptions, []cli.Flag{
			&cli.BoolFlag{
				Name:    "debug",
				Value:   false,
				Aliases: []string{"d"},
				EnvVars: []string{"DEBUG", "ENABLE_DEBUG"},
				Usage:   "Start a graphiql webserver for testing and debugging queries",
			},
			&cli.IntFlag{
				Name:    "port",
				Value:   80,
				Aliases: []string{"p"},
				EnvVars: []string{"PORT"},
				Usage:   "GraphQL server port",
			},
			&cli.StringFlag{
				Name:    "schema",
				Value:   "models/gql/query_gql_proto/models/gql/query.pb.graphqls",
				Aliases: []string{"gql-schema", "graphql-schema"},
				EnvVars: []string{"SCHEMA"},
				Usage:   "Path to the GraphQL service schema",
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
