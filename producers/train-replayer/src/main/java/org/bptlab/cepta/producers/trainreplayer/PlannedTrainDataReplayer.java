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
      plannedData.setTrainId(result.getInt("trainid"));
      plannedData.setLocationId(result.getInt("pointid"));
      plannedData.setPlannedTime(result.getLong("pointtime"));
      plannedData.setStatus(result.getInt("pointstatus"));
      plannedData.setFirstTrainNumber(result.getInt("servicenb"));
      plannedData.setTrainNumberReference(result.getInt("referencenb"));
      plannedData.setPlannedDepartureReference(result.getLong("referencedeparture"));
      plannedData.setPlannedArrivalReference(result.getLong("referenceidtime"));
      plannedData.setTrainOperatorId(result.getInt("ruid"));
      plannedData.setTransferLocationId(result.getInt("transferpointid"));
      plannedData.setReportingImId(result.getInt("reportingim"));
      plannedData.setNextImId(result.getInt("nextim"));
      plannedData.setMessageStatus(result.getInt("status"));
      plannedData.setMessageCreation(result.getInt("posted"));
      plannedData.setOriginalTrainNumber(result.getInt("referencenboriginal"));
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return plannedData;
  }
}
