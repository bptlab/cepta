package org.bptlab.cepta.producers.trainreplayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.bptlab.cepta.LiveTrainData;
import org.bptlab.cepta.producers.PostgresReplayer;

public class LiveTrainDataReplayer extends PostgresReplayer<Long, LiveTrainData> {

  LiveTrainDataReplayer(Properties props, String topicName) {
    super(props, topicName);
    setSortColumn("ACTUAL_TIME");
    setTableName("public.live");
  }

  @Override
  public LiveTrainData convertToEvent(ResultSet result) {
    LiveTrainData.Builder event = LiveTrainData.newBuilder();
    try {
      event.setId(result.getInt("id"));
      event.setTrainId(result.getInt("train_id"));
      event.setLocationId(result.getInt("location_id"));
      event.setActualTime(result.getLong("predicted_time"));
      event.setStatus(result.getInt("status"));
      event.setFirstTrainNumber(result.getInt("first_train_number"));
      event.setTrainNumberReference(result.getInt("train_number_reference"));
      event.setArrivalTimeReference(result.getLong("arrival_time_reference"));
      event.setPlannedArrivalDeviation(result.getInt("planned_arrival_deviation"));
      event.setTransferLocationId(result.getInt("transfer_location_id"));
      event.setReportingImId(result.getInt("reporting_im_id"));
      event.setNextImId(result.getInt("next_im_id"));
      event.setMessageStatus(result.getInt("message_status"));
      event.setMessageCreation(result.getInt("message_creation"));
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return event.build();
  }
}
