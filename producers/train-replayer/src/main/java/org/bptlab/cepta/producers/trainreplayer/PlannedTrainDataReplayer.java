package org.bptlab.cepta.producers.trainreplayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.bptlab.cepta.LiveTrainData;
import org.bptlab.cepta.PlannedTrainData;
import org.bptlab.cepta.producers.PostgresReplayer;


public class PlannedTrainDataReplayer extends PostgresReplayer<Long, PlannedTrainData> {

  // TODO rename tableName
  public PlannedTrainDataReplayer(Properties props) {
    super(props);
    this.sortColumn = "pointtime";
    this.tableName = "import.timetable";
  }

  @Override
  public void produce() throws NoDatabaseConnectionException {
    if (!connected) {
      throw new NoDatabaseConnectionException("Not connected to database");
    }

    this.running = true;
    try {
      String query = buildReplayQuery();
      Statement getNextEntryStatement = connection.createStatement();
      ResultSet result = getNextEntryStatement.executeQuery(query);

      while (result.next()) {
        System.out.println(result.getString("pointtime"));
        PlannedTrainData event = getPlannedTrainData(result);
        ProducerRecord<Long, PlannedTrainData> record =
            new ProducerRecord<Long, PlannedTrainData>(topic, event);

        try {
          RecordMetadata metadata = producer.send(record).get();
          System.out.printf(
              "sent record(key=%s value=%s) " + "meta(partition=%d, offset=%d)\n",
              record.key(), (PlannedTrainData) record.value(), metadata.partition(), metadata.offset());
          producer.flush();
          Thread.sleep(this.frequency);
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }
      }
      System.out.println("There is no new Train Data left in the database. Exiting.");
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      producer.close();
    }
  }

  // TODO: change column name
  private PlannedTrainData getPlannedTrainData(ResultSet result) {
    PlannedTrainData plannedData = new PlannedTrainData();
    try {
      plannedData.setId(result.getInt("id"));
      plannedData.setTrainId(result.getInt("train_id"));
      plannedData.setLocationId(result.getInt("location_id"));
      plannedData.setPlannedTime(result.getLong("planned_time"));
      plannedData.setStatus(result.getInt("status"));
      plannedData.setFirstTrainNumber(result.getInt("first_train_number"));
      plannedData.setTrainNumberReference(result.getInt("train_number_reference"));
      plannedData.setPlannedDepartureReference(result.getLong("planned_departure_reference"));
      plannedData.setPlannedArrivalReference(result.getLong("planned_arrival_reference"));
      plannedData.setTrainOperatorId(result.getInt("train_operator_id"));
      plannedData.setTransferLocationId(result.getInt("transfer_location_id"));
      plannedData.setReportingImId(result.getInt("reporting_im_id"));
      plannedData.setNextImId(result.getInt("next_im_id"));
      plannedData.setMessageStatus(result.getInt("message_status"));
      plannedData.setMessageCreation(result.getInt("message_creation"));
      plannedData.setOriginalTrainNumber(result.getInt("original_train_number"));
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return plannedData;
  }
}
