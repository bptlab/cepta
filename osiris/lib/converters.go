package utils

import (
  "time"
  "github.com/golang/protobuf/ptypes"
  tspb "github.com/golang/protobuf/ptypes/timestamp"
)

func ToProtoTime(t time.Time) (*tspb.Timestamp, error) {
  return ptypes.TimestampProto(t)
}

func FromProtoTime(t *tspb.Timestamp) (time.Time, error) {
  return ptypes.Timestamp(t)
}