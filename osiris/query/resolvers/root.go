package resolvers

import (
	"context"

	queryschema "github.com/bptlab/cepta/models/gql/query"
)

type Resolver struct {
	DB *QueryDB
}

func (r *Resolver) QueryGetUser(ctx context.Context, args struct{ In *queryschema.UserID }) (*UserResolver, error) {
	userId := args.In.Uid
	user := queryschema.User{
		Id:   userId,
		Name: "Uwe",
	}
	/*, err := queryschema.User{}, nil // r.getTransportFromDatabase(ctx, *args.In.Tid)
	if err != nil {
		return nil, errors.Wrap(err, "GetUser")
	}
	*/

	s := UserResolver{
		db:   r.DB,
		user: user,
	}

	return &s, nil
}
