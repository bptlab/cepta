package org.bptlab.cepta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.mongodb.client.result.InsertOneResult;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import org.apache.flink.test.streaming.runtime.util.*;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.operators.*;
import org.bptlab.cepta.providers.PlannedTrainDataProvider;
import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;
import org.bptlab.cepta.utils.functions.StreamUtils;

import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.junit.*;
import org.testcontainers.containers.GenericContainer;

import java.util.function.Supplier;

import static junit.framework.TestCase.*;
import static org.bptlab.cepta.utils.database.Mongo.protoToBson;

public class DelayShiftFunctionMongoTests {

   @Test
   public void testRightAmount() throws Exception {
      StreamExecutionEnvironment env = setupEnv();
      MongoConfig mongoConfig = setupMongoContainer();

      TestListResultSink<PlannedTrainData> sink = new TestListResultSink<>();
      PlannedTrainData train = PlannedTrainDataProvider.getDefaultPlannedTrainDataEvent();
      DataStream<PlannedTrainData> inputStream = env.fromElements(train);
      
      DataStream<PlannedTrainData> resultStream = AsyncDataStream
          .unorderedWait(inputStream, new DelayShiftFunctionMongo(mongoConfig),
              100000, TimeUnit.MILLISECONDS, 1);

      resultStream.addSink(new CollectSink());
      //checkStream.addSink(new CheckSink());
      env.execute();

      List<Document> databaseContent = getDatabaseContent(mongoConfig, env);
      assertEquals(1, databaseContent.size());
   }

   @Test
   public void testDelayNotificationGeneration() throws Exception {

   }

   @Test
   public void testDateConsideration() throws Exception {

   }

   private StreamExecutionEnvironment setupEnv(){
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(1);

      CollectSink.values.clear();
      return env;
  }

  private MongoConfig setupMongoContainer(){
      GenericContainer mongoContainer = newMongoContainer();
      mongoContainer.start();
      String address = mongoContainer.getContainerIpAddress();
      Integer port = mongoContainer.getFirstMappedPort();
      MongoConfig mongoConfig = new MongoConfig().withHost(address).withPort(port).withPassword("example").withUser("root").withName("mongodb");

      return mongoConfig;
  }   
  
  // create a testing sink that collects PlannedTrainData values
  private static class CollectSink implements SinkFunction<PlannedTrainData> {

      // must be static
      public static final List<PlannedTrainData> values = new ArrayList<>();

      @Override
      public synchronized void invoke(PlannedTrainData value) throws Exception {
          values.add(value);
      }
  }

  // create a testing sink that collects List<Document> values
  private static class CheckSink implements SinkFunction<List<Document>> {

      // must be static
      public static final List<List<Document>> values = new ArrayList<>();

      @Override
      public synchronized void invoke(List<Document> value) throws Exception {
          values.add(value);
      }
  }

  public List<Document> insertToDb(, PlannedTrainData dataset) throws Exception{
      MongoClient mongoClient = Mongo.getMongoClientSync(mongoConfig);
      MongoDatabase database = mongoClient.getDatabase("mongodb");
      MongoCollection<Document> plannedTrainDataCollection = database.getCollection("plannedTrainData");

      // SubscriberHelpers.OperationSubscriber<Document> findSubscriber = new SubscriberHelpers.OperationSubscriber<>();

      Document document = protoToBson(dataset);

      SubscriberHelpers.OperationSubscriber<InsertOneResult> insertOneSubscriber = new SubscriberHelpers.OperationSubscriber<>();
      coll.insertOne(document).subscribe(insertOneSubscriber);
      return StreamUtils.collectStreamToArrayList(checkStream).get(0); 
  }

  public ArrayList<PlannedTrainData> getDatabaseContentAsData(MongoConfig mongoConfig, StreamExecutionEnvironment env) throws Exception{
      List<Document> docs = getDatabaseContent(mongoConfig, env);
      ArrayList<PlannedTrainData> plannedTrainData = new ArrayList<PlannedTrainData>();
      for (Document doc : docs){
          plannedTrainData.add(Mongo.documentToPlannedTrainData(doc));
      }
      return plannedTrainData;
  }

  public GenericContainer newMongoContainer(){
      return new GenericContainer("mongo")
          .withExposedPorts(27017)
          .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
          .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example");
  }

}
