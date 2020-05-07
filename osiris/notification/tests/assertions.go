package main

import (
	notificationpb "github.com/bptlab/cepta/models/internal/notifications/notification"
	"github.com/bptlab/cepta/models/internal/types/users"
	"github.com/davecgh/go-spew/spew"
	"github.com/golang/protobuf/proto"
	"testing"
)

func compareNotifications(t *testing.T, observed map[*users.UserID]*notificationpb.AccumulatedNotifications, expected map[string][]*notificationpb.Notification) {
	if len(observed) != len(expected) {
		t.Fatalf("Expected %d users to have received notifications but got %d", len(expected), len(observed))
	}
	for ou, on := range observed {
		en, ok := expected[ou.GetId()]
		if !ok {
			t.Errorf("Observed notifications for user %s but did not expect any", ou.GetId())
			continue
		}
		ean := &notificationpb.AccumulatedNotifications{Notifications: en, Total: int32(len(en))}
		if !proto.Equal(on, ean) {
			t.Errorf("Observed notifications %s for user %s but expected %s", spew.Sdump(on), ou.GetId(), spew.Sdump(ean))
		}
	}
}