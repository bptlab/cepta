package org.bptlab.cepta.producers.trainreplayer;

import java.sql.ResultSet;
import java.util.Properties;
import org.bptlab.cepta.PredictedTrainData;
import org.bptlab.cepta.TrainInformationData;
import org.bptlab.cepta.producers.PostgresReplayer;

public class TrainInformationDataReplayer extends PostgresReplayer<Long, TrainInformationData> {

  TrainInformationDataReplayer(Properties props, String topicName) {
    super(props, topicName);
    setSortColumn("PLANNED_DEPARTURE");
    setTableName("public.traininfo");
  }

  @Override
  public TrainInformationData convertToEvent(ResultSet result) throws Exception {
    TrainInformationData.Builder event = TrainInformationData.newBuilder();
    try {
      event.setId(result.getInt("id"));
      event.setTrainNumberReference(result.getInt("train_number_reference"));
      convertTimestamp(result.getTimestamp("planned_departure_reference"), event::setPlannedDepartureReference);
      convertTimestamp(result.getTimestamp("planned_arrival_reference"), event::setPlannedArrivalReference);;
      event.setStartLocationId(result.getInt("start_location_id"));
      convertTimestamp(result.getTimestamp("planned_departure"), event::setPlannedDeparture);
      event.setPlannedDepartureDeviation(result.getInt("planned_departure_deviation"));
      event.setEndLocationId(result.getInt("end_location_id"));
      convertTimestamp(result.getTimestamp("planned_arrival"), event::setPlannedArrival);
      event.setPlannedArrivalDeviation(result.getInt("planned_arrival_deviation"));
      event.setEuroRailRunId(result.getInt("euro_rail_run_id"));
      convertTimestamp(result.getTimestamp("train_production_date"), event::setTrainProductionDate);
      event.setFirstTrainNumber(result.getInt("first_train_number"));
      event.setOriginalTrainNumber(result.getInt("original_train_number"));
      event.setImId(result.getInt("im_id"));
    } catch (Exception e) {
      logger.error("Failed to convert database entry to train information event");
      throw e;
    }
    logger.info(event.build().toString());
    return event.build();
  }
}