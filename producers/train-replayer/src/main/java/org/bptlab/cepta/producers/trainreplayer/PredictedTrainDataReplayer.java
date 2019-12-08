package org.bptlab.cepta.producers.trainreplayer;

import java.sql.ResultSet;
import java.util.Properties;
import org.bptlab.cepta.PlannedTrainData;
import org.bptlab.cepta.PredictedTrainData;
import org.bptlab.cepta.producers.PostgresReplayer;

public class PredictedTrainDataReplayer extends PostgresReplayer<Long, PredictedTrainData> {

  PredictedTrainDataReplayer(Properties props, String topicName) {
    super(props, topicName);
    setSortColumn("PREDICTED_TIME");
    setTableName("public.predictions");
  }

  @Override
  public PredictedTrainData convertToEvent(ResultSet result) throws Exception {
    PredictedTrainData.Builder event = PredictedTrainData.newBuilder();
    try {
      event.setId(result.getInt("id"));
      event.setTrainId(result.getInt("train_id"));
      event.setLocationId(result.getInt("location_id"));
      convertTimestamp(result.getTimestamp("predicted_time"), event::setPredictedTime);
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
      logger.error("Failed to convert database entry to predicted train data event");
      throw e;
    }
    logger.info(event.build().toString());
    return event.build();
  }
}