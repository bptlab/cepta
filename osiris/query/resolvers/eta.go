package resolvers

import (
	"context"

	queryschema "github.com/bptlab/cepta/models/gql/query"
)

type TransportETAResolver struct {
	db  *QueryDB
	eta *queryschema.TransportETA
}

func (etar *TransportETAResolver) Arrival(ctx context.Context) *string {
	return &etar.eta.Arrival
}

func (etar *TransportETAResolver) Departure(ctx context.Context) *string {
	return &etar.eta.Departure
}

func (etar *TransportETAResolver) Reason(ctx context.Context) *string {
	return &etar.eta.Reason
}

func (etar *TransportETAResolver) Location(ctx context.Context) (*LocationResolver, error) {
	return &LocationResolver{
		db:       etar.db,
		location: &queryschema.Location{Country: "DE", Name: "UBahn"},
	}, nil
}
