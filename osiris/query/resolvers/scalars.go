package resolvers

import (
	"fmt"
	"strconv"

	log "github.com/sirupsen/logrus"
)

// "github.com/uber/jaeger-client-go"
// "github.com/golang/protobuf/proto"
// "github.com/bptlab/cepta/schemas/types/basic"
// "/schemas/types/basic"

// graphql "github.com/graph-gophers/graphql-go"

type Int64 struct {
	Value int64
}
type Int32 struct {
	Value int32
}

func (_ Int64) ImplementsGraphQLType(name string) bool {
	return name == "Int64"
}

func (_ Int32) ImplementsGraphQLType(name string) bool {
	return name == "Int32"
}

func (t *Int64) UnmarshalGraphQL(input interface{}) error {
	log.Info(input)
	switch input := input.(type) {
	case int64:
		t.Value = input
		return nil
	case *int64:
		t.Value = *input
		return nil
	case string:
		i64, err := strconv.Atoi(input)
		if err != nil {
			return err
		}
		t.Value = int64(i64)
		return nil
	case *string:
		i64, err := strconv.Atoi(*input)
		if err != nil {
			return err
		}
		t.Value = int64(i64)
		return nil
	default:
		return fmt.Errorf("wrong type %T for Int64", input)
	}
}

func (t *Int32) UnmarshalGraphQL(input interface{}) error {
	log.Info(input)
	switch input := input.(type) {
	case int32:
		t.Value = input
		return nil
	case *int32:
		t.Value = *input
		return nil
	case string:
		i64, err := strconv.Atoi(input)
		if err != nil {
			return err
		}
		t.Value = int32(i64)
		return nil
	case *string:
		i64, err := strconv.Atoi(*input)
		if err != nil {
			return err
		}
		t.Value = int32(i64)
		return nil
	default:
		return fmt.Errorf("wrong type %T for Int32", input)
	}
}
