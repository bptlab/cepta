package org.bptlab.cepta.producers.trainreplayer;

import java.sql.ResultSet;
import java.util.Properties;
import org.bptlab.cepta.PredictedTrainData;
import org.bptlab.cepta.producers.PostgresReplayer;
import org.bptlab.cepta.utils.converters.PredictedTrainDataDatabaseConverter;

public class PredictedTrainDataReplayer extends PostgresReplayer<Long, PredictedTrainData> {

  PredictedTrainDataReplayer(Properties props, String topicName) {
    super(props, topicName);
    setSortColumn("PREDICTED_TIME");
    setTableName("public.predictions");
  }

  @Override
  public PredictedTrainData convertToEvent(ResultSet result) throws Exception {
    return new PredictedTrainDataDatabaseConverter().fromResult(result);
  }
}