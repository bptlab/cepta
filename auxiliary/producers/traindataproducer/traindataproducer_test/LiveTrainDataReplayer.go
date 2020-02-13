package livetraindatareplayer_test

import (
	replayer "github.com/bptlab/cepta/auxiliary/producers/producer/replayer"

	pb "github.com/bptlab/cepta/models/grpc/replayer"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
	"github.com/jinzhu/gorm"
	"github.com/sirupsen/logrus"
)

type LiveTrainReplayer struct {
	Parent *replayer.Replayer
}

func (this LiveTrainReplayer) getNextRow() {
}

func (this LiveTrainReplayer) completeQuery(log *logrus.Logger) *gorm.DB {
	query := this.Parent.DebugDatabase(log).Model(&libdb.LiveTrainData{})
	return query
}

// Start starts the replayer
func (this LiveTrainReplayer) Start(log *logrus.Logger) error {
	query := this.completeQuery(log)
	return this.Parent.Start(log, query)
}

// GetParent returns the parent replayer
func (this LiveTrainReplayer) GetParent() *replayer.Replayer {
	return this.Parent
}

func (this LiveTrainReplayer) SetParent(newReplayer *replayer.Replayer) {
	this.Parent = newReplayer
}

func (this LiveTrainReplayer) SetCtrl(ctrl chan pb.InternalControlMessageType) {
	this.Parent.Ctrl = ctrl
}
