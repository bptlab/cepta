package extractors

import (
	"context"
	"fmt"
	"reflect"
	"time"

	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/golang/protobuf/proto"
	"github.com/romnnn/bsonpb"
	"github.com/golang/protobuf/ptypes"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
)

// MongoExtractor ...
type MongoExtractor struct {
	Proto       proto.Message
	DB          *libdb.MongoDB
	debug       bool
	cur         *mongo.Cursor
	unmarshaler bsonpb.Unmarshaler
	IDFieldName string
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

// GetReplayedEvent ...
func (ex *MongoExtractor) GetReplayedEvent() (*pb.ReplayedEvent, error) {
  var replayEvent pb.ReplayedEvent
  replayTime, event, err := ex.Get()
  if err != nil {
    return nil, err
  }
  if protoReplayTime, err := ptypes.TimestampProto(replayTime); err != nil {
     replayEvent.ReplayTimestamp = protoReplayTime
  }
  fmt.Printf("%v", event)
  // TODO: Cast to correct type here
  // replayEvent.Event = reflect.ValueOf(event).Convert(reflect.TypeOf(ex.Proto).Elem())
  // replayEvent.Event = event.(reflect.New(reflect.TypeOf(ex.Proto))
  return &replayEvent, nil
}

// StartQuery ...
func (ex *MongoExtractor) StartQuery(collectionName string, IDFieldName string, query *ReplayQuery) error {
	ex.IDFieldName = IDFieldName
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
		DB:    db,
		Proto: proto,
	}
}

func (ex *MongoExtractor) buildAggregation(queryOptions *ReplayQuery) bson.A {
	mustMatch := bson.D{}
	// Match ERRIDs
	if queryOptions.IncludeIds != nil && ex.IDFieldName != "" {
		ids := bson.A{}
		for _, id := range *(queryOptions.IncludeIds) {
			ids = append(ids, id)
		}
		mustMatch = append(mustMatch, bson.E{ex.IDFieldName, bson.E{"$in", ids}})
	}

	// Match time range
	if constraints := mongoTimerangeQuery(queryOptions.SortColumn, queryOptions.Timerange); len(constraints) > 0 {
		mustMatch = append(mustMatch, bson.E{queryOptions.SortColumn, constraints})
	}

	aggregation := bson.A{
		bson.D{{"$match", mustMatch}},
		bson.D{{"$sort", bson.D{{queryOptions.SortColumn, 1}}}}, // Order by column (ascending order)
		bson.D{{"$skip", queryOptions.Offset}},                  // Set offset
	}

	// Set limit
	if queryOptions.Limit != nil {
		aggregation = append(aggregation, bson.D{{"$limit", *(queryOptions.Limit)}})
	}

	return aggregation
}

func mongoTimerangeQuery(column string, timerange *pb.Timerange) bson.D {
	var constraints bson.D
	if start, err := ptypes.Timestamp(timerange.GetStart()); err != nil && start.Unix() > 0 {
		constraints = append(constraints, bson.E{"$gte", primitive.NewDateTimeFromTime(start)})
	}
	if end, err := ptypes.Timestamp(timerange.GetEnd()); err != nil && end.Unix() > 0 {
		constraints = append(constraints, bson.E{"$lt", primitive.NewDateTimeFromTime(end)})
	}
	return constraints
}
