package resolvers

import (
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	_ "github.com/jinzhu/gorm/dialects/postgres"
	log "github.com/sirupsen/logrus"
)

type QueryDB struct {
	DB *libdb.DB
}

func (qdb *QueryDB) getTransportsByID(ids []int64) error {
	// SELECT * FROM public.planned WHERE TRAIN_ID = 47298333 ORDER BY planned_time
	query := qdb.DB.DB.Model(&libdb.PlannedTrainData{})
	query = query.Where("TRAIN_ID IN (?)", ids)
	query = query.Order("PLANNED_TIME asc")
	rows, err := query.Rows()
	defer rows.Close()
	if err == nil {
		for rows.Next() {
			var plannedTrainData libdb.PlannedTrainData
			qdb.DB.DB.ScanRows(rows, &plannedTrainData)
			log.Debugf("%v", plannedTrainData)
		}
	}
	return err
}

func (qdb *QueryDB) getTransportIDsForUsers(userIds []int64) {

}
