package extractors

import (
	"fmt"
	"time"
	"github.com/golang/protobuf/proto"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/golang/protobuf/ptypes"
	"github.com/jinzhu/gorm"
	"database/sql"
	pb "github.com/bptlab/cepta/models/grpc/replayer"
)

// DbExtractor ...
type DbExtractor interface {
	GetAll(*sql.Rows, *gorm.DB) (time.Time, proto.Message, error)
	GetInstance() interface{}
}

// Extractor ...
type Extractor interface {
	Get() (time.Time, proto.Message, error)
	StartQuery(sourceName string, IDFieldName string, query *ReplayQuery) error
	Next() bool
	Done()
	SetDebug(bool)
}

// ReplayQuery ... 
type ReplayQuery struct {
	IncludeIds *[]string
	Timerange  *pb.Timerange
	SortColumn string
	Limit      *int
	Offset     int
}

// PostgresExtractor ...
type PostgresExtractor struct {
	Extractor DbExtractor
	DB *libdb.PostgresDB
	debug bool
	rows *sql.Rows
	IDFieldName string
}

// NewPostgresExtractor ...
func NewPostgresExtractor(db *libdb.PostgresDB, extractor DbExtractor) *PostgresExtractor {
	return &PostgresExtractor{
		DB: db,
		Extractor: extractor,
	}
}

// Get ...
func (ex *PostgresExtractor) Get() (time.Time, proto.Message, error) {
	return ex.Extractor.GetAll(ex.rows, ex.DB.DB)
}

// StartQuery ...
func (ex *PostgresExtractor) StartQuery(tableName string, IDFieldName string, query *ReplayQuery) error {
	ex.IDFieldName = IDFieldName
	dbQuery := ex.makeQuery(query)
	var err error
	ex.rows, err = dbQuery.Rows()
	return err
}

// Next ...
func (ex *PostgresExtractor) Next() bool {
	return ex.rows.Next()
}

// Done ...
func (ex *PostgresExtractor) Done() {
	ex.rows.Close()
}

// SetDebug ...
func (ex *PostgresExtractor) SetDebug(debug bool) {
	ex.debug = debug
}

func (ex *PostgresExtractor) database() *gorm.DB {
	if ex.debug {
		return ex.DB.DB.Debug()
	}
	return ex.DB.DB
}

func (ex *PostgresExtractor) makeQuery(queryOptions *ReplayQuery) *gorm.DB {
	query := ex.database().Model(ex.Extractor.GetInstance())
	/* Match ERRIDs
	if queryOptions.MustMatch != nil {
		for _, condition := range *(queryOptions.MustMatch) {
			if len(condition) > 0 {
				query = query.Where(condition)
			}
		}
	}
	*/
	if queryOptions.IncludeIds != nil && ex.IDFieldName != "" {
		for _, id := range *(queryOptions.IncludeIds) {
			query = query.Where(fmt.Sprintf("%s=%s", ex.IDFieldName, id))
		}
	}

	// Match time range
	for _, constraint := range postgresTimerangeQuery(queryOptions.SortColumn, queryOptions.Timerange) {
		query = query.Where(constraint)
	}
	// Order by column (ascending order)
	query = query.Order(fmt.Sprintf("%s asc", queryOptions.SortColumn))
	// Set limit
	if queryOptions.Limit != nil {
		query = query.Limit(*(queryOptions.Limit))
	}
	// Set offset
	query = query.Offset(queryOptions.Offset)
	return query
}

func postgresTimerangeQuery(column string, timerange *pb.Timerange) []string {
	frmt := "2006-01-02 15:04:05" // Go want's this date!
	query := []string{}
	if start, err := ptypes.Timestamp(timerange.GetStart()); err != nil && start.Unix() > 0 {
		query = append(query, fmt.Sprintf("%s >= '%s'", column, start.Format(frmt)))
	}
	if end, err := ptypes.Timestamp(timerange.GetEnd()); err != nil && end.Unix() > 0 {
		query = append(query, fmt.Sprintf("%s < '%s'", column, end.Format(frmt)))
	}
	return query
}