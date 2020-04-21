package lib

import (
	"context"
	"errors"

	"github.com/bptlab/cepta/models/types/users"
	"github.com/bptlab/cepta/osiris/lib/utils"
	"github.com/golang/protobuf/proto"
	"github.com/google/uuid"
	"github.com/romnnn/bsonpb"
	"github.com/romnnn/flatbson"
	log "github.com/sirupsen/logrus"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
)

var defaultMarshaler = bsonpb.Marshaler{Omit: bsonpb.OmitOptions{All: true}}
var defaultUnmarshaler = bsonpb.Unmarshaler{AllowUnknownFields: true}

func queryUser(p proto.Message) (bson.D, error) {
	var query bson.D
	b, err := defaultMarshaler.Marshal(p)
	if err != nil {
		return query, err
	}
	if err := flatbson.Flatten(b, &query, "."); err != nil {
		return query, err
	}
	return query, nil
}

// HasAdminUser checks if at least one admin user exists in the database
func HasAdminUser(db *mongo.Collection, excluded []*users.UserID) (bool, error) {
	// TODO: Might actually introduce priviledge levels
	cur, err := db.Find(context.Background(), bson.D{})
	if err != nil {
		return false, err
	}

	var excludedUserIDs []string
	for _, u := range excluded {
		excludedUserIDs = append(excludedUserIDs, u.Id)
	}

	defer cur.Close(context.Background())
	for cur.Next(context.Background()) {
		var bsonUser bson.D
		if err := cur.Decode(&bsonUser); err == nil {
			var user users.InternalUser
			if err = defaultUnmarshaler.UnmarshalBSON(bsonUser, &user); err == nil {
				if user.User.Id != nil && !utils.Contains(excludedUserIDs, user.User.Id.Id) {
					// Found valid user
					return true, nil
				}
			}
		}
	}
	return false, cur.Err()
}

// GetUserByEmail queries the user database by email
func GetUserByEmail(db *mongo.Collection, email string) (*users.User, error) {
	return getUser(db, &users.User{Email: email})
}

// GetUserByID queries the user database by email
func GetUserByID(db *mongo.Collection, userID *users.UserID) (*users.User, error) {
	return getUser(db, &users.User{Id: userID})
}

func getUser(db *mongo.Collection, user *users.User) (*users.User, error) {
	// Create user to look for in the database
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
}

// UpdateUser updates selected properties for a user id
// If the user does not yet exist, an error is returned
func UpdateUser(db *mongo.Collection, userID *users.UserID, update *users.InternalUser) error {
	marshaler := bsonpb.Marshaler{Omit: bsonpb.OmitOptions{All: true}}
	newUserBson, mErr := marshaler.Marshal(update)
	if mErr != nil {
		return mErr
	}
	targetUserBson, mErr := marshaler.Marshal(&users.InternalUser{User: &users.User{Id: userID}})
	if mErr != nil {
		return mErr
	}
	// Update the user
	_, err := db.UpdateOne(context.Background(), targetUserBson, newUserBson)
	return err
}

// AddUser adds a new user to the database
func AddUser(db *mongo.Collection, user *users.InternalUser) (*users.User, error) {
	if user.User == nil {
		return nil, errors.New("User is empty")
	}

	// TODO: Check email is not in use

	// Generate a new user ID
	userID, idErr := uuid.NewRandom()
	if idErr != nil {
		return nil, errors.New("Failed to generate a user ID. Please try again")
	}
	user.User.Id = &users.UserID{Id: userID.String()}
	userBson, mErr := defaultMarshaler.Marshal(user)
	if mErr != nil {
		return nil, mErr
	}

	// Add the user
	log.Debugf("Adding user: %v", userBson)
	_, err := db.InsertOne(context.Background(), userBson)
	return user.User, err
}

// RemoveUser deletes a user from the database
func RemoveUser(db *mongo.Collection, userID *users.UserID) error {
	query, qErr := queryUser(&users.InternalUser{User: &users.User{Id: userID}})
	if qErr != nil {
		return qErr
	}

	// Remove the user
	log.Debugf("Removing user: %v", query)
	_, err := db.DeleteOne(context.Background(), query)
	return err
}
