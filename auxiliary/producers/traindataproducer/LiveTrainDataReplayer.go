package livetraindatareplayer

import (
	replayer "github.com/bptlab/cepta/auxiliary/producers/producer/replayer"
	libdb "github.com/bptlab/cepta/osiris/lib/db"
)

type LiveTrainReplayer struct {
	replayer.Replayer
}

func (r LiveTrainReplayer) getNextRow() {

}

func (r LiveTrainReplayer) completeQuery() {
	query := r.Replayer.DebugDatabase(&r).Model(&libdb.LiveTrainData{})
	query = r.Replayer.enrichQuery(query)
}
