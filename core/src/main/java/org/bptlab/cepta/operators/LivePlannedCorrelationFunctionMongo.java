package org.bptlab.cepta.operators;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;

import org.apache.flink.api.java.tuple.Tuple2;
import org.bson.Document;

import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.streaming.api.functions.async.ResultFuture;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;


/* This Function calls a MongoDB to get all future Planned Stations
    and sends Notifications with the Delay between the Current stations plannedArrivalTime
     and the live event Time, for all upcoming stations. */
public class LivePlannedCorrelationFunctionMongo extends
        RichAsyncFunction<LiveTrainData, Tuple2<LiveTrainData,PlannedTrainData>> {

  private MongoConfig mongoConfig = new MongoConfig();
  private transient MongoClient mongoClient;
  private long delayThreshold = 60;

  public LivePlannedCorrelationFunctionMongo(MongoConfig mongoConfig) {
    this.mongoConfig = mongoConfig;    }

  public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
    super.open(parameters);
    this.mongoClient = Mongo.getMongoClient(mongoConfig);
  }

  @Override
  public void close() {
    this.mongoClient.close();
  }

  @Override
  public void asyncInvoke(LiveTrainData dataset,
                          final ResultFuture<Tuple2<LiveTrainData,PlannedTrainData>> resultFuture) throws Exception {
        /*
        asyncInvoke will be called for each incoming element
        the resultFuture is where the outgoing element(s) will be
        */
    MongoDatabase database = mongoClient.getDatabase(mongoConfig.getName());
    MongoCollection<Document> plannedTrainDataCollection = database.getCollection("plannedTrainData");

    //https://github.com/mongodb/mongo-java-driver/blob/eac754d2eed76fe4fa07dbc10ad3935dfc5f34c4/driver-reactive-streams/src/examples/reactivestreams/helpers/SubscriberHelpers.java#L53
    //https://github.com/reactive-streams/reactive-streams-jvm/tree/v1.0.3#2-subscriber-code
    SubscriberHelpers.OperationSubscriber<Document> findMultipleSubscriber = new SubscriberHelpers.OperationSubscriber<>();
    plannedTrainDataCollection.find(
            and(
                    eq("trainSectionId",dataset.getTrainSectionId())
                    ,eq("stationId",dataset.getStationId())
                    ,eq("endStationId",dataset.getEndStationId())
                    ,eq("plannedArrivalTimeEndStation",dataset.getPlannedArrivalTimeEndStation())
            )
    ).sort( descending("ingestionTime")).subscribe(findMultipleSubscriber);


    CompletableFuture<Void> queryFuture = CompletableFuture.supplyAsync(new Supplier<List<Document>>() {
      @Override
      public List<Document> get() {
        return findMultipleSubscriber.get();
      }
    }).thenAccept(result ->{
      //only generate Notifications if there is Data about the Train in the DB
      if (!result.isEmpty()) {
        resultFuture.complete( Collections.singleton( new Tuple2<LiveTrainData,PlannedTrainData>(dataset, Mongo.documentToPlannedTrainData(result.get(0)))));
      } else {
        resultFuture.complete( Collections.singleton( new Tuple2<LiveTrainData,PlannedTrainData>(dataset, PlannedTrainData.newBuilder().build())) );
      }
    });
    queryFuture.get();
  }
}