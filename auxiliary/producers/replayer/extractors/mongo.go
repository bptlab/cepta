package extractors

import (
	"context"
	"fmt"
	"reflect"
	"strconv"
	"strings"
	"time"

	eventpb "github.com/bptlab/cepta/models/events/event"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/sirupsen/logrus"
	log "github.com/sirupsen/logrus"

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
	logger      *log.Logger
}

// NewMongoExtractor ...
func NewMongoExtractor(db *libdb.MongoDB, wrapperFunc WrapperFunc, proto proto.Message, config MongoExtractorConfig) *MongoExtractor {
	return &MongoExtractor{
		DB:          db,
		Config:      config,
		WrapperFunc: wrapperFunc,
		Proto:       proto,
		logger:      log.New(),
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

	// Extract time from bson
	if sortFieldValue, ok := result.Map()[ex.Config.SortFieldName]; ok {
		if dtt, ok := sortFieldValue.(primitive.DateTime); ok {
			replayTime = dtt.Time()
		}
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
func (ex *MongoExtractor) StartQuery(ctx context.Context, collectionName string, queryOptions *pb.SourceReplay) error {
	ex.unmarshaler = bsonpb.Unmarshaler{AllowUnknownFields: true}
	collection := ex.DB.DB.Collection(collectionName)
	aggregation := ex.buildAggregation(queryOptions)
	ex.logger.Debug(aggregation)
	var err error
	allowDisk := true
	ex.cur, err = collection.Aggregate(ctx, aggregation, &options.AggregateOptions{AllowDiskUse: &allowDisk})
	return err
}

// Next ...
func (ex *MongoExtractor) Next() bool {
	return ex.cur.Next(context.Background())
}

// Done ...
func (ex *MongoExtractor) Done() {
	if ex.cur != nil {
		ex.cur.Close(context.Background())
	}
}

// SetLogLevel ...
func (ex *MongoExtractor) SetLogLevel(level logrus.Level) {
	ex.logger.SetLevel(level)
}

func (ex *MongoExtractor) getIDFieldType() reflect.Type {
	protoFieldName := strings.Title(strings.TrimSpace(ex.Config.IDFieldName))
	t := reflect.TypeOf(ex.Proto).Elem()
	f, ok := t.FieldByName(protoFieldName)
	if !ok {
		ex.logger.Errorf("Could not get ID field \"%s\" of proto %v. Falling back to string", protoFieldName, t)
		return reflect.TypeOf("")
	}
	return f.Type
}

func (ex *MongoExtractor) buildAggregation(queryOptions *pb.SourceReplay) bson.A {
	mustMatch := bson.A{}

	// Match ERRIDs
	if len(queryOptions.GetIds()) > 0 && ex.Config.IDFieldName != "" {
		// Get target type
		tT := ex.getIDFieldType()
		ids := bson.A{}
		for _, id := range queryOptions.GetIds() {
			idV := reflect.ValueOf(id)

			switch tT.Kind() {
			case reflect.Int, reflect.Int32, reflect.Int64:
				if idV.Type().ConvertibleTo(tT) {
					ids = append(ids, idV.Convert(tT).Int())
				} else if idV.Kind() == reflect.String {
					converted, err := strconv.Atoi(id)
					if err != nil {
						ex.logger.Error("Failed to convert ID value %v to required int: %v", id, err)
					} else {
						ids = append(ids, converted)
					}
				} else {
					ex.logger.Error("Skipping bad ID %v of type %v (wanted int)", idV, idV.Type())
				}
			case reflect.String:
				if idV.Kind() == reflect.String {
					ids = append(ids, idV.String())
				} else if idV.Kind() == reflect.Int || idV.Kind() == reflect.Int32 || idV.Kind() == reflect.Int64 {
					ids = append(ids, strconv.Itoa(int(idV.Int())))
				} else {
					ex.logger.Error("Skipping bad ID %v of type %v (wanted string)", idV, idV.Type())
				}
			default:
				// Not implemented: Slice, Map, Invalid, Bool
				ex.logger.Error("Unsupported ID type %v for Value %v", idV.Type(), idV)
			}
		}
		mustMatch = append(mustMatch, bson.D{{strings.TrimSpace(ex.Config.IDFieldName), bson.D{{"$in", ids}}}})
	}

	// Match time range
	if constraints := mongoTimerangeQuery(ex.Config.SortFieldName, queryOptions.GetOptions().GetTimerange()); len(constraints) > 0 {
		mustMatch = append(mustMatch, bson.D{{ex.Config.SortFieldName, constraints}})
	}

	aggregation := bson.A{
		bson.D{{"$sort", bson.D{{ex.Config.SortFieldName, 1}}}}, // Order by column (ascending order)
		bson.D{{"$skip", queryOptions.GetOptions().GetOffset()}},          // Set offset
	}

	if len(mustMatch) > 0 {
		aggregation = append(aggregation, bson.D{{"$match", bson.D{{"$and", mustMatch}}}})
	}

	// Set limit
	if queryOptions.GetOptions().GetLimit() > 0 {
		aggregation = append(aggregation, bson.D{{"$limit", queryOptions.GetOptions().GetLimit()}})
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
