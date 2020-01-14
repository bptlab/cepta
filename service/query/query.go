package main

import (
	"context"
	"database/sql"
	"log"
	"net/http"
	"strings"
	"time"

	graphql "github.com/graph-gophers/graphql-go"
	"github.com/graph-gophers/graphql-go/relay"
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

// TODO: Schema
// TODO: Model
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

func main() {
	schema := graphql.MustParseSchema(s, &query{})
	http.Handle("/query", &relay.Handler{Schema: schema})
	// TODO: init model
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
	
	graphiqlHandler, err := graphiql.NewGraphiqlHandler("/query")
	if err != nil {
		panic(err)
	}
	http.Handle("/", graphiqlHandler)
	
	log.Println("Server ready at 8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}
