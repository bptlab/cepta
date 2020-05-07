package lib

import (
	"context"

	"github.com/bptlab/cepta/models/internal/types/users"
	"github.com/romnnn/bsonpb"
	"github.com/romnnn/flatbson"
	log "github.com/sirupsen/logrus"
	"go.mongodb.org/mongo-driver/mongo"
)

// FindUser queries the user database by email
func FindUser(db *mongo.Collection, email string) (*users.InternalUser, error) {
	// Create user to look for in the database
	marshaler := bsonpb.Marshaler{Omit: bsonpb.OmitOptions{All: true}}
	userBson, err := marshaler.Marshal(&users.InternalUser{User: &users.User{Email: email}})
	if err != nil {
		log.Errorf("Failed to marshal with error: %v", err)
		return nil, err
	}

	// Check for the user in the database
	flatUserBson, err := flatbson.Flattened(userBson, ".")
	if err != nil {
		return nil, err
	}
	log.Debugf("Looking up user: %s", flatUserBson)
	rawResult, err := db.FindOne(context.Background(), flatUserBson).DecodeBytes()
	if err != nil {
		if err != mongo.ErrNoDocuments {
			log.Debugf("Failed to decode user %s (%s): %v", userBson, flatUserBson, err)
		}
		// User invalid or not in the database
		return nil, err
	}

	unmarshaler := bsonpb.Unmarshaler{AllowUnknownFields: true}
	var userResult users.InternalUser
	err = unmarshaler.Unmarshal(rawResult, &userResult)
	if err != nil {
		return nil, err
	}

	log.Debugf("Found user: %s", userResult)
	return &userResult, err
}
