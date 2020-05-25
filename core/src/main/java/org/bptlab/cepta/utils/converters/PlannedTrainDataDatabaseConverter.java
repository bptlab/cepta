package org.bptlab.cepta.utils.converters;

import java.sql.ResultSet;
import java.time.*;
import com.github.jasync.sql.db.RowData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.protobuf.Timestamp;

public class PlannedTrainDataDatabaseConverter extends DatabaseConverter<PlannedTrainData> {
  private static final Logger logger = LoggerFactory.getLogger(PlannedTrainDataDatabaseConverter.class.getName());

  public PlannedTrainData fromResult(ResultSet result) throws Exception {
    PlannedTrainData.Builder event = PlannedTrainData.newBuilder();
    try {
      event.setId(result.getInt("id"));
      event.setTrainSectionId(result.getInt("train_section_id"));
      event.setStationId(result.getInt("station_id"));
      event.setPlannedEventTime(sqlTimestampToPrototimestamp(result.getTimestamp("planned_event_time")));
      event.setStatus(result.getInt("status"));
      event.setFirstTrainId(result.getInt("first_train_id"));
      event.setTrainId(result.getInt("train_id"));
      event.setPlannedDepartureTimeStartStation(sqlTimestampToPrototimestamp(result.getTimestamp("planned_departure_time_start_station")));
      event.setPlannedArrivalTimeEndStation(sqlTimestampToPrototimestamp(result.getTimestamp("planned_arrival_time_end_station")));
      event.setRuId(result.getInt("ru_id"));
      event.setEndStationId(result.getInt("end_station_id"));
      event.setImId(result.getInt("im_id"));
      event.setFollowingImId(result.getInt("following_im_id"));
      event.setMessageStatus(result.getInt("message_status"));
      event.setIngestionTime(sqlTimestampToPrototimestamp(result.getTimestamp("ingestion_time")));
      event.setOriginalTrainId(result.getInt("original_train_id"));
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
      if (result.getInt("train_section_id") != null) event.setTrainSectionId(result.getInt("train_section_id"));
      if (result.getInt("station_id") != null) event.setStationId(result.getInt("station_id"));
      if (result.getDate("planned_event_time") != null) event.setPlannedEventTime(localDateTimeToPrototimestamp(result.getDate("planned_event_time")));
      if (result.getInt("status") != null) event.setStatus(result.getInt("status"));
      if (result.getInt("first_train_id") != null) event.setFirstTrainId(result.getInt("first_train_id"));
      if (result.getInt("train_id") != null) event.setTrainId(result.getInt("train_id"));
      if (result.getDate("planned_departure_time_start_station") != null) event.setPlannedDepartureTimeStartStation(localDateTimeToPrototimestamp(result.getDate("planned_departure_time_start_station")));
      if (result.getDate("planned_arrival_time_end_station") != null) event.setPlannedArrivalTimeEndStation(localDateTimeToPrototimestamp(result.getDate("planned_arrival_time_end_station")));
      if (result.getInt("ru_id") != null) event.setRuId(result.getInt("ru_id"));
      if (result.getInt("end_station_id") != null) event.setEndStationId(result.getInt("end_station_id"));
      if (result.getInt("im_id") != null) event.setImId(result.getInt("im_id"));
      if (result.getInt("following_im_id") != null) event.setFollowingImId(result.getInt("following_im_id"));
      if (result.getInt("message_status") != null) event.setMessageStatus(result.getInt("message_status"));
      if (result.getDate("ingestion_time") != null) event.setIngestionTime(localDateTimeToPrototimestamp(result.getDate("ingestion_time")));
      if (result.getInt("original_train_id") != null) event.setOriginalTrainId(result.getInt("original_train_id"));
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
