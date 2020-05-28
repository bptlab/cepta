package org.bptlab.cepta.utils.converters;

import java.sql.ResultSet;
import java.time.*;
import com.github.jasync.sql.db.RowData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlannedTrainDataDatabaseConverter extends DatabaseConverter<PlannedTrainData> {
  private static final Logger logger = LoggerFactory.getLogger(PlannedTrainDataDatabaseConverter.class.getName());

  public PlannedTrainData fromResult(ResultSet result) throws Exception {
    PlannedTrainData.Builder event = PlannedTrainData.newBuilder();
    try {
      event.setId(result.getLong("id"));
      event.setTrainSectionId(result.getLong("train_section_id"));
      event.setStationId(result.getLong("station_id"));
      event.setPlannedEventTime(sqlTimestampToPrototimestamp(result.getTimestamp("planned_event_time")));
      event.setStatus(result.getLong("status"));
      event.setFirstTrainId(result.getLong("first_train_id"));
      event.setTrainId(result.getLong("train_id"));
      event.setPlannedDepartureTimeStartStation(sqlTimestampToPrototimestamp(result.getTimestamp("planned_departure_time_start_station")));
      event.setPlannedArrivalTimeEndStation(sqlTimestampToPrototimestamp(result.getTimestamp("planned_arrival_time_end_station")));
      event.setRuId(result.getLong("ru_id"));
      event.setEndStationId(result.getLong("end_station_id"));
      event.setImId(result.getLong("im_id"));
      event.setFollowingImId(result.getLong("following_im_id"));
      event.setMessageStatus(result.getLong("message_status"));
      event.setIngestionTime(sqlTimestampToPrototimestamp(result.getTimestamp("ingestion_time")));
      event.setOriginalTrainId(result.getLong("original_train_id"));
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
      if (result.getLong("id") != null) event.setId(result.getLong("id"));
      if (result.getLong("train_section_id") != null) event.setTrainSectionId(result.getLong("train_section_id"));
      if (result.getLong("station_id") != null) event.setStationId(result.getLong("station_id"));
      if (result.getDate("planned_event_time") != null) event.setPlannedEventTime(localDateTimeToPrototimestamp(result.getDate("planned_event_time")));
      if (result.getLong("status") != null) event.setStatus(result.getLong("status"));
      if (result.getLong("first_train_id") != null) event.setFirstTrainId(result.getLong("first_train_id"));
      if (result.getLong("train_id") != null) event.setTrainId(result.getLong("train_id"));
      if (result.getDate("planned_departure_time_start_station") != null) event.setPlannedDepartureTimeStartStation(localDateTimeToPrototimestamp(result.getDate("planned_departure_time_start_station")));
      if (result.getDate("planned_arrival_time_end_station") != null) event.setPlannedArrivalTimeEndStation(localDateTimeToPrototimestamp(result.getDate("planned_arrival_time_end_station")));
      if (result.getLong("ru_id") != null) event.setRuId(result.getLong("ru_id"));
      if (result.getLong("end_station_id") != null) event.setEndStationId(result.getLong("end_station_id"));
      if (result.getLong("im_id") != null) event.setImId(result.getLong("im_id"));
      if (result.getLong("following_im_id") != null) event.setFollowingImId(result.getLong("following_im_id"));
      if (result.getLong("message_status") != null) event.setMessageStatus(result.getLong("message_status"));
      if (result.getDate("ingestion_time") != null) event.setIngestionTime(localDateTimeToPrototimestamp(result.getDate("ingestion_time")));
      if (result.getLong("original_train_id") != null) event.setOriginalTrainId(result.getLong("original_train_id"));
    } catch (Exception e) {
      logger.error(e.toString());
      e.printStackTrace();
      logger.error("Failed to convert database entry to planned train data event");
      throw e;
    }
    logger.debug(event.build().toString());
    return event.build();
  }

  private com.google.protobuf.Timestamp sqlTimestampToPrototimestamp(java.sql.Timestamp sqlTimestamp){
    Instant instant = sqlTimestamp.toInstant();
    com.google.protobuf.Timestamp timestamp = com.google.protobuf.Timestamp.newBuilder().setSeconds(instant.getEpochSecond())
         .setNanos( instant.getNano() ).build();
    return timestamp;
  }

  
  private com.google.protobuf.Timestamp localDateTimeToPrototimestamp(org.joda.time.LocalDateTime dateTime){
    Instant instant = dateTime.toDate().toInstant();
    com.google.protobuf.Timestamp timestamp = com.google.protobuf.Timestamp.newBuilder().setSeconds(instant.getEpochSecond())
    .setNanos(instant.getNano()).build();
    return timestamp;
  }
  
}
