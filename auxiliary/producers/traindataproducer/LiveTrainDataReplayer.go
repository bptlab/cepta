package livetraindatareplayer

import (
	replayer "github.com/bptlab/cepta/auxiliary/producers/producer/replayer"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/jinzhu/gorm"
	"github.com/sirupsen/logrus"
)

type LiveTrainReplayer struct {
	Parent replayer.Replayer
}

func (this LiveTrainReplayer) getNextRow() {

}

func (this LiveTrainReplayer) completeQuery() *gorm.DB {
	query := this.Parent.DebugDatabase().Model(&libdb.LiveTrainData{})
	//query = this.parent.enrichQuery(query)
	return query
}

// this is the fanciest decorator you will ever see
func (this LiveTrainReplayer) Start(log *logrus.Logger) error {
	query := this.completeQuery()
	return this.Parent.Start(log, query)
}
