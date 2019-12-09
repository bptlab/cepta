package org.bptlab.cepta.producers.trainreplayer;

import java.sql.ResultSet;
import java.util.Properties;
import org.bptlab.cepta.PlannedTrainData;
import org.bptlab.cepta.producers.PostgresReplayer;
import org.bptlab.cepta.utils.converters.PlannedTrainDataDatabaseConverter;

public class PlannedTrainDataReplayer extends PostgresReplayer<Long, PlannedTrainData> {

  PlannedTrainDataReplayer(Properties props, String topicName) {
    super(props, topicName);
    setSortColumn("PLANNED_TIME");
    setTableName("public.planned");
  }

  @Override
  public PlannedTrainData convertToEvent(ResultSet result) throws Exception {
    return new PlannedTrainDataDatabaseConverter().fromResult(result);
  }
}