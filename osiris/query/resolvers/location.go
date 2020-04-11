package resolvers

import (
	"context"

	queryschema "github.com/bptlab/cepta/models/gql/query"
)

type LocationResolver struct {
	db       *QueryDB
	location *queryschema.Location
}

func (lr *LocationResolver) Country(ctx context.Context) *string {
	return &lr.location.Country
}

func (lr *LocationResolver) Name(ctx context.Context) *string {
	return &lr.location.Name
}

func (lr *LocationResolver) Position(ctx context.Context) (*CoordinatesResolver, error) {
	return &CoordinatesResolver{
		db:     lr.db,
		coords: &queryschema.Coordinates{Lat: 12.453, Lon: 134.42},
	}, nil
}
