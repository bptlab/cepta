package org.bptlab.cepta.producers.trainreplayer;

import java.sql.ResultSet;
import java.util.Properties;
import org.bptlab.cepta.LiveTrainData;
import org.bptlab.cepta.producers.PostgresReplayer;
import org.bptlab.cepta.utils.converters.LiveTrainDataDatabaseConverter;

public class LiveTrainDataReplayer extends PostgresReplayer<Long, LiveTrainData> {

  LiveTrainDataReplayer(Properties props, String topicName) {
    super(props, topicName);
    setSortColumn("ACTUAL_TIME");
    setTableName("public.live");
  }

  @Override
  public LiveTrainData convertToEvent(ResultSet result) throws Exception {
    return new LiveTrainDataDatabaseConverter().fromResult(result);
  }
}
