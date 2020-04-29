package main

import (
	"context"
	"fmt"
	"testing"

	"github.com/Shopify/sarama"
	topics "github.com/bptlab/cepta/models/constants/topic"
	usermgmtpb "github.com/bptlab/cepta/models/grpc/usermgmt"
	"github.com/bptlab/cepta/models/types/transports"
	"github.com/bptlab/cepta/models/types/users"
	kafkaproducer "github.com/bptlab/cepta/osiris/lib/kafka/producer"
	"github.com/golang/protobuf/proto"
	log "github.com/sirupsen/logrus"
)

func (test *Test) produceEventsToKafka(t *testing.T, topic topics.Topic, events []proto.Message) {
	producer, err := kafkaproducer.Create(context.Background(), test.kafkapConfig)
	if err != nil {
		t.Fatal("Cannot produce events: ", err)
	}
	defer producer.Close()
	for _, event := range events {
		eventBytes, err := proto.Marshal(event)
		if err != nil {
			t.Error("Failed to marshal proto: ", err)
			continue
		}
		log.Debug("Sending ", event)
		producer.Send(topic.String(), topic.String(), sarama.ByteEncoder(eventBytes))
	}
}

func (test *Test) addUsersForTransport(t *testing.T, transportID *transports.TransportID, userIDs []*users.UserID) {
	for _, userID := range userIDs {
		newUserReq := &usermgmtpb.AddUserRequest{User: &users.InternalUser{
			User:     &users.User{Email: fmt.Sprintf("%s@cepta.org", userID.GetId()), Id: userID},
			Password: "hard-to-guess",
		}}
		_, err := test.usermgmtClient.AddUser(context.Background(), newUserReq)
		if err != nil {
			t.Errorf("Failed to add new user: %v: %v", newUserReq.GetUser(), err)
		}
	}
}
