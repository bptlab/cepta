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
import org.bptlab.cepta.producers.PostgresReplayer;


public class LiveTrainDataReplayer extends PostgresReplayer<Long, LiveTrainData> {

  // TODO: change table name
  LiveTrainDataReplayer(Properties props) {
    super(props);
    this.sortColumn = "pointtime";
    this.tableName = "import.running";
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
        LiveTrainData event = getLiveTrainData(result);
        ProducerRecord<Long, LiveTrainData> record =
            new ProducerRecord<Long, LiveTrainData>(topic, event);

        try {
          RecordMetadata metadata = producer.send(record).get();
          System.out.format(
              "sent record(key=%s value=%s) meta(partition=%d, offset=%d)\n",
              record.key(), (LiveTrainData) record.value(), metadata.partition(), metadata.offset());
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
  private LiveTrainData getLiveTrainData(ResultSet result) {
    LiveTrainData liveData = new LiveTrainData();
    try {
      liveData.setId(result.getInt("id"));
      liveData.setTrainId(result.getInt("trainid"));
      liveData.setLocationId(result.getInt("pointid"));
      liveData.setActualTime(result.getLong("pointtime"));
      liveData.setStatus(result.getInt("pointstatus"));
      liveData.setFirstTrainNumber(result.getInt("servicenb"));
      liveData.setTrainNumberReference(result.getInt("referencenb"));
      liveData.setArrivalTimeReference(result.getLong("referenceidtime"));
      liveData.setPlannedArrivalDeviation(result.getInt("delta"));
      liveData.setTransferLocationId(result.getInt("transferpointid"));
      liveData.setReportingImId(result.getInt("reportingim"));
      liveData.setNextImId(result.getInt("nextim"));
      liveData.setMessageStatus(result.getInt("status"));
      liveData.setMessageCreation(result.getInt("posted"));
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return liveData;
  }
}
