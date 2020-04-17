package extractors

import (
	"time"

	"database/sql"

	pb "github.com/bptlab/cepta/models/grpc/replayer"
	"github.com/jinzhu/gorm"
)

// DbExtractor ...
type DbExtractor interface {
	GetAll(*sql.Rows, *gorm.DB) (*pb.ReplayedEvent, error)
	GetInstance() interface{}
}

// Extractor ...
type Extractor interface {
	Get() (time.Time, *pb.ReplayedEvent, error)
	StartQuery(sourceName string, queryOptions *pb.SourceQueryOptions) error
	Next() bool
	Done()
	SetDebug(bool)
}
