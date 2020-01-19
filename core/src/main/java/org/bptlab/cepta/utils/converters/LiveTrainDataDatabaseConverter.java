package org.bptlab.cepta.utils.converters;

import com.github.jasync.sql.db.RowData;
import java.sql.ResultSet;
import org.bptlab.cepta.LiveTrainData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveTrainDataDatabaseConverter extends DatabaseConverter<LiveTrainData> {
  private static final Logger logger = LoggerFactory.getLogger(LiveTrainDataDatabaseConverter.class.getName());

  public LiveTrainData fromResult(ResultSet result) throws Exception {
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
      logger.error(e.toString());
      logger.error("Failed to convert database entry to planned train data event");
      throw e;
    }
    logger.debug(event.build().toString());
    return event.build();
  }

  public LiveTrainData fromRowData(RowData result) throws Exception {
    LiveTrainData.Builder event = LiveTrainData.newBuilder();
    try {
      event.setId(result.getInt("id"));
      event.setTrainId(result.getInt("train_id"));
      event.setLocationId(result.getInt("location_id"));
      convertLocalDateTime(result.getDate("actual_time"), event::setActualTime);
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
      logger.error(e.toString());
      logger.error("Failed to convert database entry to planned train data event");
      throw e;
    }
    logger.debug(event.build().toString());
    return event.build();
  }
}
