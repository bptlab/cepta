package extractors

import (
	"fmt"
	"reflect"
	"time"
	"context"
	"github.com/golang/protobuf/proto"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"github.com/romnnn/bsonpb"
)

// MongoExtractor ...
type MongoExtractor struct {
	Proto proto.Message
	DB *libdb.MongoDB
	debug bool
	cur *mongo.Cursor
	unmarshaler bsonpb.Unmarshaler
}

// Get ...
func (ex *MongoExtractor) Get() (time.Time, proto.Message, error) {
	// Get raw bson
	var result bson.D
	err := ex.cur.Decode(&result)
	if err != nil {
		return time.Time{}, nil, err
	}

	// Marshal to bson bytes first
	resultBytes, mErr := bson.Marshal(result)
	if mErr != nil {
		return time.Time{}, nil, fmt.Errorf("marshaling bson to bytes failed: %v", mErr)
	}

	// Now unmarshal to proto message
	target := reflect.New(reflect.TypeOf(ex.Proto).Elem()).Interface().(proto.Message)
	umErr := ex.unmarshaler.Unmarshal(resultBytes, target)
	if umErr != nil {
		return time.Time{}, nil, fmt.Errorf("unmarshaling failed: %v", umErr)
	}

	return time.Time{}, target, nil
}

// StartQuery ...
func (ex *MongoExtractor) StartQuery(collectionName string, query *ReplayQuery) error {
	ex.unmarshaler = bsonpb.Unmarshaler{AllowUnknownFields: true}
	collection := ex.DB.DB.Collection(collectionName)
	aggregation := ex.buildAggregation(query)
	var err error
	ex.cur, err = collection.Aggregate(context.Background(), aggregation)
	return err
}

// Next ...
func (ex *MongoExtractor) Next() bool {
	return ex.cur.Next(context.Background())
}

// Done ...
func (ex *MongoExtractor) Done() {
	ex.cur.Close(context.Background())
}

// SetDebug ...
func (ex *MongoExtractor) SetDebug(debug bool) {
	ex.debug = debug
}

// NewMongoExtractor ...
func NewMongoExtractor(db *libdb.MongoDB, proto proto.Message) *MongoExtractor {
	return &MongoExtractor{
		DB: db,
		Proto: proto,
	}
}

func (ex *MongoExtractor) buildAggregation(queryOptions *ReplayQuery) bson.A {
	mustMatch := bson.D{}
	/* Match ERRIDs
	// TODO
	if queryOptions.MustMatch != nil {
		for _, condition := range *(queryOptions.MustMatch) {
			if len(condition) > 0 {
				query = query.Where(condition)
			}
		}
	}
	*/
	/* Match time range
	// TODO
	for _, constraint := range buildQuery(queryOptions.SortColumn, queryOptions.Timerange) {
		query = query.Where(constraint)
	}
	*/

	aggregation := bson.A{
		bson.D{{"$match", mustMatch}},
		bson.D{{"$sort", bson.D{{queryOptions.SortColumn, 1}}}}, // Order by column (ascending order)
		bson.D{{"$skip", queryOptions.Offset}}, // Set offset
	}

	// Set limit
	if queryOptions.Limit != nil {
		aggregation = append(aggregation, bson.D{{"$limit", *(queryOptions.Limit)}})
	}

	return aggregation
}
