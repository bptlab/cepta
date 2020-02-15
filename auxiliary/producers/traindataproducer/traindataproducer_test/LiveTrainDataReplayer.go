package livetraindatareplayer_test

import (
	replayer "github.com/bptlab/cepta/auxiliary/producers/producer/replayer"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/jinzhu/gorm"
)

type LiveTrainReplayer struct {
	Parent *replayer.Replayer
}

func (this LiveTrainReplayer) getNextRow() {
}

func (this LiveTrainReplayer) completeQuery() *gorm.DB {
	query := this.Parent.DebugDatabase().Model(&libdb.LiveTrainData{})
	return query
}

// Start starts the replayer
func (this LiveTrainReplayer) Start() error {
	query := this.completeQuery()
	return this.Parent.Start(query)
}

// GetParent returns the parent replayer
func (this LiveTrainReplayer) GetParent() *replayer.Replayer {
	return this.Parent
}
