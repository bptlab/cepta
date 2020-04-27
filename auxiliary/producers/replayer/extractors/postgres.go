package extractors

import (
	"context"
	"fmt"
	"time"

	"database/sql"

	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/golang/protobuf/ptypes"
	"github.com/jinzhu/gorm"
	"github.com/sirupsen/logrus"
	log "github.com/sirupsen/logrus"
)

// PostgresExtractorConfig ...
type PostgresExtractorConfig struct {
	IDColumnName   string
	SortColumnName string
}

// PostgresExtractor ...
type PostgresExtractor struct {
	Extractor DbExtractor
	DB        *libdb.PostgresDB
	Config    PostgresExtractorConfig
	debug     bool
	rows      *sql.Rows
	logger    *log.Logger
}

// NewPostgresExtractor ...
func NewPostgresExtractor(db *libdb.PostgresDB, extractor DbExtractor) *PostgresExtractor {
	return &PostgresExtractor{
		DB:        db,
		Extractor: extractor,
		logger:    log.New(),
	}
}

// Get ...
func (ex *PostgresExtractor) Get() (time.Time, *pb.ReplayedEvent, error) {
	// TODO: Implement: return ex.Extractor.GetAll(ex.rows, ex.DB.DB)
	return time.Time{}, nil, nil
}

// StartQuery ...
func (ex *PostgresExtractor) StartQuery(ctx context.Context, tableName string, queryOptions *pb.SourceReplay) error {
	dbQuery := ex.makeQuery(queryOptions)
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

// SetLogLevel ...
func (ex *PostgresExtractor) SetLogLevel(level logrus.Level) {
	ex.logger.SetLevel(level)
}

func (ex *PostgresExtractor) database() *gorm.DB {
	if ex.debug {
		return ex.DB.DB.Debug()
	}
	return ex.DB.DB
}

func (ex *PostgresExtractor) makeQuery(queryOptions *pb.SourceReplay) *gorm.DB {
	query := ex.database().Model(ex.Extractor.GetInstance())

	// Macth ERRIDs
	if len(queryOptions.Ids) > 0 && ex.Config.IDColumnName != "" {
		for _, id := range queryOptions.Ids {
			query = query.Where(fmt.Sprintf("%s=%s", ex.Config.IDColumnName, id))
		}
	}

	// Match time range
	for _, constraint := range postgresTimerangeQuery(ex.Config.SortColumnName, queryOptions.Options.Timerange) {
		query = query.Where(constraint)
	}
	// Order by column (ascending order)
	query = query.Order(fmt.Sprintf("%s asc", ex.Config.SortColumnName))
	// Set limit
	if queryOptions.Options.Limit != 0 {
		query = query.Limit(queryOptions.Options.Limit)
	}
	// Set offset
	query = query.Offset(queryOptions.Options.Offset)
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
