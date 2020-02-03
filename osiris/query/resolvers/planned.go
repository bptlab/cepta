package resolvers

import (
	"context"

	queryschema "github.com/bptlab/cepta/models/gql/query"
)

type PlannedTransportScheduleResolver struct {
	db       *QueryDB
	schedule *queryschema.PlannedTransportSchedule
}

func (planr *PlannedTransportScheduleResolver) Arrival(ctx context.Context) *string {
	return &planr.schedule.Arrival
}

func (planr *PlannedTransportScheduleResolver) Departure(ctx context.Context) *string {
	return &planr.schedule.Departure
}

func (planr *PlannedTransportScheduleResolver) Location(ctx context.Context) (*LocationResolver, error) {
	return &LocationResolver{
		db:       planr.db,
		location: &queryschema.Location{Country: "DE", Name: "UBahn"},
	}, nil
}
