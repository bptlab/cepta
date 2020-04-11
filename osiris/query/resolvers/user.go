package resolvers

import (
	"context"

	queryschema "github.com/bptlab/cepta/models/gql/query"
)

type UserResolver struct {
	db   *QueryDB
	user queryschema.User
}

type UserIdResolver struct {
	db     *QueryDB
	userId *queryschema.UserID
}

func (ur *UserResolver) Id(ctx context.Context) Int32 {
	return Int32{Value: ur.user.Id}
}

func (ur *UserResolver) Name(ctx context.Context) *string {
	return &ur.user.Name
}

func (ur *UserResolver) Transports(ctx context.Context) (*[]*TransportResolver, error) {
	transports := []*queryschema.Transport{
		&queryschema.Transport{Operator: "DB"},
		&queryschema.Transport{Operator: "Hapag"},
	}
	r := make([]*TransportResolver, len(transports))
	for i := range transports {
		r[i] = &TransportResolver{
			db:        ur.db,
			transport: transports[i],
		}
	}
	return &r, nil
}

func (uidr *UserIdResolver) Uid(ctx context.Context) int32 {
	return uidr.userId.Uid
}
