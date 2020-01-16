package org.bptlab.cepta.producers.trainreplayer;

import java.sql.ResultSet;
import java.util.Properties;
import org.bptlab.cepta.TrainInformationData;
import org.bptlab.cepta.producers.PostgresReplayer;
import org.bptlab.cepta.utils.converters.TrainInformationDataDatabaseConverter;

public class TrainInformationDataReplayer extends PostgresReplayer<Long, TrainInformationData> {

  TrainInformationDataReplayer(Properties props, String topicName) {
    super(props, topicName);
    setSortColumn("PLANNED_DEPARTURE");
    setTableName("public.traininfo");
  }

  @Override
  public TrainInformationData convertToEvent(ResultSet result) throws Exception {
    return new TrainInformationDataDatabaseConverter().fromResult(result);
  }
}