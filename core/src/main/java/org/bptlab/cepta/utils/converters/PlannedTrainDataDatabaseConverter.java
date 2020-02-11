package org.bptlab.cepta.utils.converters;

import java.sql.ResultSet;
import com.github.jasync.sql.db.RowData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataProtos.PlannedTrainData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlannedTrainDataDatabaseConverter extends DatabaseConverter<PlannedTrainData> {
  private static final Logger logger = LoggerFactory.getLogger(PlannedTrainDataDatabaseConverter.class.getName());

  public PlannedTrainData fromResult(ResultSet result) throws Exception {
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
      logger.error(e.toString());
      e.printStackTrace();
      logger.error("Failed to convert database entry to planned train data event");
      throw e;
    }
    logger.debug(event.build().toString());
    return event.build();
  }

  public PlannedTrainData fromRowData(RowData result) {
    PlannedTrainData.Builder event = PlannedTrainData.newBuilder();
    logger.debug(result.toString());
    try {
      if (result.getInt("id") != null) event.setId(result.getInt("id"));
      if (result.getInt("train_id") != null) event.setTrainId(result.getInt("train_id"));
      if (result.getInt("location_id") != null) event.setLocationId(result.getInt("location_id"));
      if (result.getDate("planned_time") != null) convertLocalDateTime(result.getDate("planned_time"), event::setPlannedTime);
      if (result.getInt("status") != null) event.setStatus(result.getInt("status"));
      if (result.getInt("first_train_number") != null) event.setTrainId(result.getInt("first_train_number"));
      if (result.getInt("train_number_reference") != null) event.setTrainNumberReference(result.getInt("train_number_reference"));
      if (result.getDate("planned_departure_reference") != null) convertLocalDateTime(result.getDate("planned_departure_reference"), event::setPlannedDepartureReference);
      if (result.getDate("planned_arrival_reference") != null) convertLocalDateTime(result.getDate("planned_arrival_reference"), event::setPlannedArrivalReference);
      if (result.getInt("train_operator_id") != null) event.setTrainOperatorId(result.getInt("train_operator_id"));
      if (result.getInt("transfer_location_id") != null) event.setTransferLocationId(result.getInt("transfer_location_id"));
      if (result.getInt("reporting_im_id") != null) event.setReportingImId(result.getInt("reporting_im_id"));
      if (result.getInt("next_im_id") != null) event.setNextImId(result.getInt("next_im_id"));
      if (result.getInt("message_status") != null) event.setMessageStatus(result.getInt("message_status"));
      if (result.<org.joda.time.LocalDate>getAs("message_creation") != null) convertLocalDate(result.<org.joda.time.LocalDate>getAs("message_creation"), event::setMessageCreation);
      if (result.getInt("original_train_number") != null) event.setOriginalTrainNumber(result.getInt("original_train_number"));
    } catch (Exception e) {
      logger.error(e.toString());
      e.printStackTrace();
      logger.error("Failed to convert database entry to planned train data event");
      throw e;
    }
    logger.debug(event.build().toString());
    return event.build();
  }
}
