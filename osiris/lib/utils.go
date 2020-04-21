package utils

import (
	"context"

	"github.com/mitchellh/mapstructure"
	"google.golang.org/grpc/metadata"
)

func MaxInt64(a int64, b int64) int64 {
	if a > b {
		return a
	}
	return b
}

func Contains(s []string, e string) bool {
	for _, a := range s {
		if a == e {
			return true
		}
	}
	return false
}

func Unique(slice []int) []int {
	keys := make(map[int]bool)
	list := []int{}
	for _, entry := range slice {
		if _, value := keys[entry]; !value {
			keys[entry] = true
			list = append(list, entry)
		}
	}
	return list
}

type userTokenMetadata struct {
	cookies struct {
		userToken string `json:"user-token"`
	}
}

func GetUserToken(ctx context.Context) string {
	if md, ok := metadata.FromIncomingContext(ctx); ok {
		var encoded userTokenMetadata
		if err := mapstructure.Decode(md, &encoded); err != nil {
			return ""
		}
		return encoded.cookies.userToken
	}
	return ""
}
