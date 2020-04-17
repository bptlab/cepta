package extractors

import (
	"context"
	"fmt"
	"time"

	eventpb "github.com/bptlab/cepta/models/events/event"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libdb "github.com/bptlab/cepta/osiris/lib/db"

	"github.com/golang/protobuf/proto"
	"github.com/golang/protobuf/ptypes"
	"github.com/romnnn/bsonpb"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

// WrapperFunc ...
type WrapperFunc = func(event proto.Message) *eventpb.Event

// MongoExtractorConfig ...
type MongoExtractorConfig struct {
	IDFieldName   string
	SortFieldName string
}

// MongoExtractor ...
type MongoExtractor struct {
	DB          *libdb.MongoDB
	Proto       proto.Message
	WrapperFunc WrapperFunc
	Config      MongoExtractorConfig
	debug       bool
	cur         *mongo.Cursor
	unmarshaler bsonpb.Unmarshaler
}

// NewMongoExtractor ...
func NewMongoExtractor(db *libdb.MongoDB, wrapperFunc WrapperFunc, proto proto.Message, config MongoExtractorConfig) *MongoExtractor {
	return &MongoExtractor{
		DB:          db,
		Config:      config,
		WrapperFunc: wrapperFunc,
		Proto:       proto,
	}
}

// Get ...
func (ex *MongoExtractor) Get() (time.Time, *pb.ReplayedEvent, error) {
	// Get raw bson
	var result bson.D
	var replayTime time.Time
	err := ex.cur.Decode(&result)
	if err != nil {
		return replayTime, nil, err
	}

	// Marshal to bson bytes first
	resultBytes, mErr := bson.Marshal(result)
	if mErr != nil {
		return replayTime, nil, fmt.Errorf("marshaling bson to bytes failed: %v", mErr)
	}

	// Now unmarshal to proto message
	var replayedEvent pb.ReplayedEvent
	event := proto.Clone(ex.Proto)
	if err := ex.unmarshaler.Unmarshal(resultBytes, event); err != nil {
		return replayTime, nil, fmt.Errorf("unmarshaling failed: %v", err)
	}
	replayedEvent.Event = ex.WrapperFunc(event)
	return replayTime, &replayedEvent, nil
}

// StartQuery ...
func (ex *MongoExtractor) StartQuery(collectionName string, queryOptions *pb.SourceQueryOptions) error {
	ex.unmarshaler = bsonpb.Unmarshaler{AllowUnknownFields: true}
	collection := ex.DB.DB.Collection(collectionName)
	aggregation := ex.buildAggregation(queryOptions)
	var err error
	allowDisk := true
	ex.cur, err = collection.Aggregate(context.Background(), aggregation, &options.AggregateOptions{AllowDiskUse: &allowDisk})
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

func (ex *MongoExtractor) buildAggregation(queryOptions *pb.SourceQueryOptions) bson.A {
	mustMatch := bson.D{}
	// Match ERRIDs
	if len(queryOptions.Ids) > 0 && ex.Config.IDFieldName != "" {
		ids := bson.A{}
		for _, id := range queryOptions.Ids {
			ids = append(ids, id)
		}
		mustMatch = append(mustMatch, bson.E{ex.Config.IDFieldName, bson.E{"$in", ids}})
	}

	// Match time range
	if constraints := mongoTimerangeQuery(ex.Config.SortFieldName, queryOptions.Options.Timerange); len(constraints) > 0 {
		mustMatch = append(mustMatch, bson.E{ex.Config.SortFieldName, constraints})
	}

	aggregation := bson.A{
		bson.D{{"$match", mustMatch}},
		bson.D{{"$sort", bson.D{{ex.Config.SortFieldName, 1}}}}, // Order by column (ascending order)
		bson.D{{"$skip", queryOptions.Options.Offset}},          // Set offset
	}

	// Set limit
	if queryOptions.Options.Limit > 0 {
		aggregation = append(aggregation, bson.D{{"$limit", queryOptions.Options.Limit}})
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
