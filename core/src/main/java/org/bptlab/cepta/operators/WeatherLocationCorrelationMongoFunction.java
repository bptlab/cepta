package org.bptlab.cepta.operators;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.events.weather.WeatherDataOuterClass.WeatherData;
import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.mongodb.client.model.Filters.*;

public class WeatherLocationCorrelationMongoFunction extends
    RichAsyncFunction<WeatherData, Tuple2<WeatherData, Long> > {

  private MongoConfig mongoConfig = new MongoConfig();
  private transient MongoClient mongoClient;
  private String tableName;

  public WeatherLocationCorrelationMongoFunction(String tableName,MongoConfig mongoConfig) {
    this.mongoConfig = mongoConfig;
    this.tableName = tableName;
  }

  public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
    /*
      the Configuration class must be from flink, it will give errors when jasync's Configuration is taken
      open should be called before methods like map() or join() are executed
      this must be set to transient, as flink will otherwise try to serialize it which it is not
     */
    super.open(parameters);
    this.mongoClient = Mongo.getMongoClient(mongoConfig);
  }

  @Override
  public void close() throws Exception {
    this.mongoClient.close();
  }

  @Override
  public void asyncInvoke(WeatherData weatherEvent,
    final ResultFuture<Tuple2<WeatherData, Long>> resultFuture) throws Exception {
    /*
        asyncInvoke will be called for each incoming element
        the resultFuture is where the outgoing element(s) will be
        */
    MongoDatabase database = mongoClient.getDatabase(mongoConfig.getName());
    MongoCollection<Document> locationDataCollection = database.getCollection(tableName);

    //The new AsyncMongo Driver now uses Reactive Streams,
    // so we need Subscribers to get the Query executed and Results received.
    // For further details consider the following links:
    //http://mongodb.github.io/mongo-java-driver/4.0/driver-reactive/tutorials/connect-to-mongodb/
    //https://github.com/mongodb/mongo-java-driver/blob/eac754d2eed76fe4fa07dbc10ad3935dfc5f34c4/driver-reactive-streams/src/examples/reactivestreams/tour/QuickTour.java
    //https://github.com/mongodb/mongo-java-driver/blob/eac754d2eed76fe4fa07dbc10ad3935dfc5f34c4/driver-reactive-streams/src/examples/reactivestreams/helpers/SubscriberHelpers.java#L53
    //https://github.com/reactive-streams/reactive-streams-jvm/tree/v1.0.3#2-subscriber-code
    SubscriberHelpers.OperationSubscriber<Document> findMultipleSubscriber = new SubscriberHelpers.OperationSubscriber<>();
    locationDataCollection.find(
            and(
                    /* 0.02 is about 2 kilometers */
                    gte("latitude", weatherEvent.getLatitude() - 0.02)
                    , lte("latitude", weatherEvent.getLatitude() + 0.02)
                    , gte("longitude", weatherEvent.getLongitude() - 0.02)
                    , lte("longitude", weatherEvent.getLongitude() + 0.02)
            )
    ).subscribe(findMultipleSubscriber);

    CompletableFuture<Void> queryFuture = CompletableFuture.supplyAsync(new Supplier<List<Document>>() {
      @Override
      public List<Document> get() {
        return findMultipleSubscriber.get();
      }
    }).thenAccept(result -> {
      //only generate Events if there are matching Stations in the DB
      if (!result.isEmpty()) {
        resultFuture.complete(generateWeatherStationEvents(weatherEvent,result));
      } else {
        // Empty Collection will result into no generation of output events
        resultFuture.complete(new ArrayList<>());
      }
    });
    queryFuture.get();
  }

  private Collection<Tuple2<WeatherData, Long>> generateWeatherStationEvents(WeatherData weatherData, List<Document> matchingStations) {
    Collection<Tuple2<WeatherData, Long>> events = new ArrayList<>();
      for ( Document matchedStation : matchingStations) {
        try {
          events.add(new Tuple2<>(weatherData,matchedStation.getLong("stationId")));
        } catch ( Exception e) {
          e.printStackTrace();
        }
      }
    return events;
  }
}

