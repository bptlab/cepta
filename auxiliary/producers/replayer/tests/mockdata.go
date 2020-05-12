package main

import (
	"math/rand"
	"time"

	livetrainpb "github.com/bptlab/cepta/models/events/livetraindataevent"
	plannedtrainpb "github.com/bptlab/cepta/models/events/plannedtraindataevent"
	"github.com/golang/protobuf/proto"
)

func ascendingLiveTrainEvents(n int64, interval int) []proto.Message {
	var events []proto.Message
	for i := int64(1); i <= n; i++ {
		events = append(events, &livetrainpb.LiveTrainData{TrainId: i, EventTime: toProtoTime(time.Now().Add(time.Duration(i*int64(interval)) * time.Second))})
	}
	rand.Seed(time.Now().UnixNano())
	rand.Shuffle(len(events), func(i, j int) { events[i], events[j] = events[j], events[i] })
	return events
}

func ascendingPlannedTrainEvents(n int64, interval int) []proto.Message {
	var events []proto.Message
	for i := int64(1); i <= n; i++ {
		events = append(events, &plannedtrainpb.PlannedTrainData{TrainId: i, IngestionTime: toProtoTime(time.Now().Add(time.Duration(i*int64(interval)) * time.Second))})
	}
	rand.Seed(time.Now().UnixNano())
	rand.Shuffle(len(events), func(i, j int) { events[i], events[j] = events[j], events[i] })
	return events
}
