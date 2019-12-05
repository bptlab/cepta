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
import org.bptlab.cepta.producers.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LiveTrainDataReplayer extends PostgresReplayer<Long, LiveTrainData> {

  private Logger logger = LoggerFactory.getLogger(Producer.class.getName());

  // TODO: change table name
  LiveTrainDataReplayer(Properties props) {
    super(props);
    this.sortColumn = "pointtime";
    this.tableName = "public.running";
  }

  @Override
  public void produce() throws NoDatabaseConnectionException {
    logger.info("Starting to produce");
    if (!connected) {
      logger.error("Not connected to database");
      throw new NoDatabaseConnectionException("Not connected to database");
    }
    this.running = true;
    try {
      String query = buildReplayQuery();
      Statement getNextEntryStatement = connection.createStatement();
      ResultSet result = getNextEntryStatement.executeQuery(query);

      try {
        while (!result.next()) {
          result = getNextEntryStatement.executeQuery(query);
          logger.info("Database query yielded no results, waiting.");
          Thread.sleep(10000);
        }
      } catch (InterruptedException exception) {
        logger.error("Interrupted while waiting for database results");
      }

      while (result.next()) {
        try {
          logger.debug(result.getString("pointtime"));
          LiveTrainData event = getLiveTrainData(result);
          ProducerRecord<Long, LiveTrainData> record =
              new ProducerRecord<Long, LiveTrainData>(topic, event);
          RecordMetadata metadata = producer.send(record).get();
          logger.debug(
              String.format("sent record(key=%s value=%s) meta(partition=%d, offset=%d)\n",
              record.key(), (LiveTrainData) record.value(), metadata.partition(), metadata.offset()));
          producer.flush();
          Thread.sleep(this.frequency);
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }
      }
      logger.info("There is no more live train data left in the database. Exiting.");
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
      liveData.setTrainId(result.getInt("train_id"));
      liveData.setLocationId(result.getInt("location_id"));
      liveData.setActualTime(result.getLong("predicted_time"));
      liveData.setStatus(result.getInt("status"));
      liveData.setFirstTrainNumber(result.getInt("first_train_number"));
      liveData.setTrainNumberReference(result.getInt("train_number_reference"));
      liveData.setArrivalTimeReference(result.getLong("arrival_time_reference"));
      liveData.setPlannedArrivalDeviation(result.getInt("planned_arrival_deviation"));
      liveData.setTransferLocationId(result.getInt("transfer_location_id"));
      liveData.setReportingImId(result.getInt("reporting_im_id"));
      liveData.setNextImId(result.getInt("next_im_id"));
      liveData.setMessageStatus(result.getInt("message_status"));
      liveData.setMessageCreation(result.getInt("message_creation"));
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return liveData;
  }
}
