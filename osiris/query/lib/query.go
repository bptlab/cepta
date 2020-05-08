package lib

import (
	// "context"
	// "errors"
	// "github.com/google/uuid"
	// "go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"

	// "github.com/bptlab/cepta/models/internal/types/users"
	"github.com/bptlab/cepta/models/internal/types/ids"
	pb "github.com/bptlab/cepta/models/grpc/query"
	"github.com/bptlab/cepta/models/internal/transport"
	// "github.com/bptlab/cepta/osiris/lib/utils"
	// "github.com/golang/protobuf/proto"
	// "github.com/google/uuid"
	"github.com/romnnn/bsonpb"
	// "github.com/romnnn/flatbson"
	// log "github.com/sirupsen/logrus"
)

var defaultMarshaler = bsonpb.Marshaler{Omit: bsonpb.OmitOptions{All: true}}
var defaultUnmarshaler = bsonpb.Unmarshaler{AllowUnknownFields: true}

// QueryTransports ...
func QueryTransports(db *mongo.Collection, query *pb.QueryTransportsRequest, stream pb.Query_QueryTransportsServer) error {
	/*
	cur, err := db.Find(context.Background(), bson.D{{"user.transports", bson.D{{"$elemMatch", bson.D{{"id", transportID.GetId()}}}}}})
	if err != nil {
		return err
	}
	defer cur.Close(context.Background())
	for cur.Next(context.Background()) {
		var result users.InternalUser
		err := cur.Decode(&result)
		if err != nil {
			log.Warnf("Failed to decode user: %v", err)
			continue
		}
		if err := stream.Send(result.User); err != nil {
			log.Warn("Failed to send: %v", err)
		}
	}
	return nil
	 */
	return nil
}

func GetTransport(db *mongo.Collection, transportID *ids.CeptaTransportID) (*transport.Transport, bool, error) {
	// Create user to look for in the database
	/*
	query, err := queryUser(&users.InternalUser{User: user})
	if err != nil {
		return nil, err
	}

	// Check for the user in the database
	log.Debugf("Looking up user: %s", query)
	rawResult, err := db.FindOne(context.Background(), query).DecodeBytes()
	if err != nil {
		if err != mongo.ErrNoDocuments {
			log.Errorf("Failed to decode user %s: %v", query, err)
			return nil, err
		}
		// User not in the database
		return nil, nil
	}

	var userResult users.InternalUser
	err = defaultUnmarshaler.Unmarshal(rawResult, &userResult)
	if err != nil {
		return nil, err
	}
	return userResult.User, err
	 */
	return nil, false, nil
}
