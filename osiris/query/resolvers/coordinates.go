package resolvers

import (
	"context"

	queryschema "github.com/bptlab/cepta/models/gql/query"
)

type CoordinatesResolver struct {
	db     *QueryDB
	coords *queryschema.Coordinates
}

func (cr *CoordinatesResolver) Lat(ctx context.Context) *float64 {
	return &cr.coords.Lat
}
func (cr *CoordinatesResolver) Lon(ctx context.Context) *float64 {
	return &cr.coords.Lon
}
