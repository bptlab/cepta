package extractors

import (
	"context"
	"time"

	"database/sql"

	pb "github.com/bptlab/cepta/models/grpc/replayer"
	"github.com/jinzhu/gorm"
	"github.com/sirupsen/logrus"
)

// DbExtractor ...
type DbExtractor interface {
	GetAll(*sql.Rows, *gorm.DB) (*pb.ReplayedEvent, error)
	GetInstance() interface{}
}

// Extractor ...
type Extractor interface {
	Get() (time.Time, *pb.ReplayedEvent, error)
	StartQuery(ctx context.Context, sourceName string, options *pb.SourceReplay) error
	Next() bool
	Done()
	SetLogLevel(logrus.Level)
}
