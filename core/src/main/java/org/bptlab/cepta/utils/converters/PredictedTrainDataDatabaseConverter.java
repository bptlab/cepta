package org.bptlab.cepta.utils.converters;

import java.sql.ResultSet;
import com.github.jasync.sql.db.RowData;
import org.bptlab.cepta.PredictedTrainData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PredictedTrainDataDatabaseConverter extends DatabaseConverter<PredictedTrainData> {
  private static final Logger logger = LoggerFactory.getLogger(PredictedTrainDataDatabaseConverter.class.getName());

  public PredictedTrainData fromResult(ResultSet result) throws Exception {
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
    logger.debug(event.build().toString());
    return event.build();
  }
  public PredictedTrainData fromRowData(RowData result) {
    PredictedTrainData.Builder event = PredictedTrainData.newBuilder();
    try {
      event.setId(result.getInt("id"));
      event.setTrainId(result.getInt("train_id"));
      event.setLocationId(result.getInt("location_id"));
      convertLocalDateTime(result.getDate("predicted_time"), event::setPredictedTime);
      event.setStatus(result.getInt("status"));
      event.setFirstTrainNumber(result.getInt("first_train_number"));
      event.setTrainNumberReference(result.getInt("train_number_reference"));
      convertLocalDateTime(result.getDate("arrival_time_reference"), event::setArrivalTimeReference);
      event.setPlannedArrivalDeviation(result.getInt("planned_arrival_deviation"));
      event.setTransferLocationId(result.getInt("transfer_location_id"));
      event.setReportingImId(result.getInt("reporting_im_id"));
      event.setNextImId(result.getInt("next_im_id"));
      event.setMessageStatus(result.getInt("message_status"));
      convertLocalDateTime(result.getDate("message_creation"), event::setMessageCreation);
    } catch (Exception e) {
      logger.error("Failed to convert database entry to predicted train data event");
      throw e;
    }
    logger.debug(event.build().toString());
    return event.build();
  }
}

