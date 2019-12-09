package org.bptlab.cepta.utils.converters;

import java.sql.ResultSet;
import com.github.jasync.sql.db.RowData;
import org.bptlab.cepta.PlannedTrainData;

public class PlannedTrainDataDatabaseConverter extends DatabaseConverter<PlannedTrainData> {
  public PlannedTrainData fromResult(ResultSet result) throws Exception {
    return null;
  }
  public PlannedTrainData fromRowData(RowData result) {
    PlannedTrainData.Builder event = PlannedTrainData.newBuilder();
    try {
      event.setId(result.getInt("id"));
      event.setTrainId(result.getInt("train_id"));
      event.setLocationId(result.getInt("location_id"));
      convertTimestamp(result.getLong("planned_time"), event::setPlannedTime);
      event.setStatus(result.getInt("status"));
      event.setFirstTrainNumber(result.getInt("first_train_number"));
      event.setTrainNumberReference(result.getInt("train_number_reference"));
      convertTimestamp(result.getLong("planned_departure_reference"), event::setPlannedDepartureReference);
      convertTimestamp(result.getLong("planned_arrival_reference"), event::setPlannedArrivalReference);
      event.setTrainOperatorId(result.getInt("train_operator_id"));
      event.setTransferLocationId(result.getInt("transfer_location_id"));
      event.setReportingImId(result.getInt("reporting_im_id"));
      event.setNextImId(result.getInt("next_im_id"));
      event.setMessageStatus(result.getInt("message_status"));
      convertTimestamp(result.getLong("message_creation"), event::setMessageCreation);
      event.setOriginalTrainNumber(result.getInt("original_train_number"));
    } catch (Exception e) {
      logger.error("Failed to convert database entry to planned train data event");
      throw e;
    }
    logger.info(event.build().toString());
    return event.build();
  }
}
