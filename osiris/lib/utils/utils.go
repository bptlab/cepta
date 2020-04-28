package utils

import (
	"context"
	"reflect"
	"regexp"

	"github.com/imdario/mergo"
	"github.com/mitchellh/mapstructure"
	"google.golang.org/grpc/metadata"
)

// MaxInt64 ...
func MaxInt64(a int64, b int64) int64 {
	if a > b {
		return a
	}
	return b
}

// AbsInt64 ...
func AbsInt64(x int64) int64 {
	if x < 0 {
		return -x
	}
	return x
}

func IsValidEmail(s string) bool {
	re := regexp.MustCompile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$")
	return re.MatchString(s)
}

// IsNilInterface ...
func IsNilInterface(v interface{}) bool {
	return v == nil || (reflect.ValueOf(v).Kind() == reflect.Ptr && reflect.ValueOf(v).IsNil())
}

// InPlusMinusRange ...
func InPlusMinusRange(center int64, fluctuation int64, value int64) bool {
	if value <= center+fluctuation && value >= center-fluctuation {
		return true
	}
	return false
}

// Contains ...
func Contains(s []string, e string) bool {
	for _, a := range s {
		if a == e {
			return true
		}
	}
	return false
}

// Unique creates a copy from a slice with all duplicates removed
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

// GetUserToken ...
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

type wkt interface {
	XXX_WellKnownType() string
}

type optionalWKTTransformer struct{}

func (t optionalWKTTransformer) Transformer(typ reflect.Type) func(dst, src reflect.Value) error {
	// See https://github.com/imdario/mergo/issues/131#issuecomment-589844203
	if wkt, ok := reflect.New(typ).Elem().Interface().(wkt); ok {
		switch wkt.XXX_WellKnownType() {
		case "DoubleValue", "FloatValue", "Int64Value", "UInt64Value",
			"Int32Value", "UInt32Value", "BoolValue", "StringValue", "BytesValue":
			return func(dst, src reflect.Value) error {
				if dst.CanSet() && !src.IsNil() {
					dst.Set(src)
				}
				return nil
			}
		default:
			return nil
		}
	}
	return nil
}

// MergeWithOverride ...
func MergeWithOverride(dest, merge interface{}) error {
	return mergo.Merge(dest, merge, mergo.WithOverride, mergo.WithTransformers(optionalWKTTransformer{}))
}
