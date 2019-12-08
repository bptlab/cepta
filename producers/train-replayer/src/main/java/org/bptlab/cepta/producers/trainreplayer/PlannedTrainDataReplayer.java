package org.bptlab.cepta.producers.trainreplayer;

import java.sql.ResultSet;
import java.util.Properties;
import org.bptlab.cepta.PlannedTrainData;
import org.bptlab.cepta.producers.PostgresReplayer;

public class PlannedTrainDataReplayer extends PostgresReplayer<Long, PlannedTrainData> {

  PlannedTrainDataReplayer(Properties props, String topicName) {
    super(props, topicName);
    setSortColumn("PLANNED_TIME");
    setTableName("public.planned");
  }

  @Override
  public PlannedTrainData convertToEvent(ResultSet result) throws Exception {
    PlannedTrainData.Builder event = PlannedTrainData.newBuilder();
    try {
      event.setId(result.getInt("id"));
      event.setTrainId(result.getInt("train_id"));
      event.setLocationId(result.getInt("location_id"));
      convertTimestamp(result.getTimestamp("planned_time"), event::setPlannedTime);
      event.setStatus(result.getInt("status"));
      event.setFirstTrainNumber(result.getInt("first_train_number"));
      event.setTrainNumberReference(result.getInt("train_number_reference"));
      convertTimestamp(result.getTimestamp("planned_departure_reference"), event::setPlannedDepartureReference);
      convertTimestamp(result.getTimestamp("planned_arrival_reference"), event::setPlannedArrivalReference);
      event.setTrainOperatorId(result.getInt("train_operator_id"));
      event.setTransferLocationId(result.getInt("transfer_location_id"));
      event.setReportingImId(result.getInt("reporting_im_id"));
      event.setNextImId(result.getInt("next_im_id"));
      event.setMessageStatus(result.getInt("message_status"));
      convertTimestamp(result.getTimestamp("message_creation"), event::setMessageCreation);
      event.setOriginalTrainNumber(result.getInt("original_train_number"));
    } catch (Exception e) {
      logger.error("Failed to convert database entry to planned train data event");
      throw e;
    }
    logger.info(event.build().toString());
    return event.build();
  }
}