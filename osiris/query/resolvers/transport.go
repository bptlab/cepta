package resolvers

import (
	"context"

	queryschema "github.com/bptlab/cepta/models/gql/query"
)

type TransportResolver struct {
	db        *QueryDB
	transport *queryschema.Transport
}

func (tr *TransportResolver) Id(ctx context.Context) Int32 {
	return Int32{Value: tr.transport.Id}
}

func (tr *TransportResolver) Operator(ctx context.Context) *string {
	return &tr.transport.Operator
}

func (tr *TransportResolver) Planned(ctx context.Context) (*[]*PlannedTransportScheduleResolver, error) {
	planned := []*queryschema.PlannedTransportSchedule{
		&queryschema.PlannedTransportSchedule{Arrival: "Today"},
		&queryschema.PlannedTransportSchedule{Arrival: "Tomorrow"},
	}
	r := make([]*PlannedTransportScheduleResolver, len(planned))
	for i := range planned {
		r[i] = &PlannedTransportScheduleResolver{
			db:       tr.db,
			schedule: planned[i],
		}
	}
	return &r, nil
}

func (tr *TransportResolver) Etas(ctx context.Context) (*[]*TransportETAResolver, error) {
	eta := []*queryschema.TransportETA{
		&queryschema.TransportETA{Arrival: "Today", Reason: "Heavy Storm"},
		&queryschema.TransportETA{Arrival: "Tomorrow", Reason: "Heavy Storm"},
	}
	r := make([]*TransportETAResolver, len(eta))
	for i := range eta {
		r[i] = &TransportETAResolver{
			db:  tr.db,
			eta: eta[i],
		}
	}
	return &r, nil
}
