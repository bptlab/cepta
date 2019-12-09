package org.bptlab.cepta.producers.trainreplayer;

import java.sql.ResultSet;
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
  public LiveTrainData convertToEvent(ResultSet result) throws Exception {
    LiveTrainData.Builder event = LiveTrainData.newBuilder();
    try {
      event.setId(result.getInt("id"));
      event.setTrainId(result.getInt("train_id"));
      event.setLocationId(result.getInt("location_id"));
      convertTimestamp(result.getTimestamp("actual_time"), event::setActualTime);
      event.setStatus(result.getInt("status"));
      event.setFirstTrainNumber(result.getInt("first_train_number"));
      event.setTrainNumberReference(result.getInt("train_number_reference"));
      convertTimestamp(result.getTimestamp("arrival_time_reference"), event::setArrivalTimeReference);
      event.setPlannedArrivalDeviation(result.getInt("planned_arrival_deviation"));
      event.setTransferLocationId(result.getInt("transfer_location_id"));
      event.setReportingImId(result.getInt("reporting_im_id"));
      event.setNextImId(result.getInt("next_im_id"));
      event.setMessageStatus(result.getInt("message_status"));
      convertTimestamp(result.getTimestamp("message_creation"), event::setMessageCreation);
    } catch (Exception e) {
      logger.error("Failed to convert database entry to live train data event");
      throw e;
    }
    logger.debug(event.build().toString());
    return event.build();
  }
}
