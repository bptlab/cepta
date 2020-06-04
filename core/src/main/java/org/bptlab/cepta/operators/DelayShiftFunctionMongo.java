package org.bptlab.cepta.operators;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;

import org.bptlab.cepta.utils.notification.NotificationHelper;
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
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass.*;


/* This Function calls a MongoDB to get all future Planned Stations
    and sends Notifications with the Delay between the Current stations plannedArrivalTime
     and the live event Time, for all upcoming stations. */
public class DelayShiftFunctionMongo extends
    RichAsyncFunction<LiveTrainData, Notification> {

    private MongoConfig mongoConfig = new MongoConfig();
    private transient MongoClient mongoClient;
    private long delayThreshold = 60;

    public DelayShiftFunctionMongo(MongoConfig mongoConfig) {
        this.mongoConfig = mongoConfig;    }

    public DelayShiftFunctionMongo(MongoConfig mongoConfig, long delayThreshold) {
        this.mongoConfig = mongoConfig;
        this.delayThreshold = delayThreshold;
    }


    public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
        super.open(parameters);
        this.mongoClient = Mongo.getMongoClient(mongoConfig);
    }

    @Override
    public void close() throws Exception {
        this.mongoClient.close();
    }

    @Override
    public void asyncInvoke(LiveTrainData dataset,
        final ResultFuture<Notification> resultFuture) throws Exception {
        /*
        asyncInvoke will be called for each incoming element
        the resultFuture is where the outgoing element(s) will be
        */
        MongoDatabase database = mongoClient.getDatabase(mongoConfig.getName());
        MongoCollection<Document> plannedTrainDataCollection = database.getCollection("plannedTrainData");

        //The new AsyncMongo Driver now uses Reactive Streams,
        // so we need Subscribers to get the Query executed and Results received.
        // For further details consider the following links:
        //http://mongodb.github.io/mongo-java-driver/4.0/driver-reactive/tutorials/connect-to-mongodb/
        //https://github.com/mongodb/mongo-java-driver/blob/eac754d2eed76fe4fa07dbc10ad3935dfc5f34c4/driver-reactive-streams/src/examples/reactivestreams/tour/QuickTour.java
        //https://github.com/mongodb/mongo-java-driver/blob/eac754d2eed76fe4fa07dbc10ad3935dfc5f34c4/driver-reactive-streams/src/examples/reactivestreams/helpers/SubscriberHelpers.java#L53
        //https://github.com/reactive-streams/reactive-streams-jvm/tree/v1.0.3#2-subscriber-code

        SubscriberHelpers.OperationSubscriber<Document> findMultipleSubscriber = new SubscriberHelpers.OperationSubscriber<>();
        plannedTrainDataCollection.find(
                and(
                        eq("trainSectionId",dataset.getTrainSectionId()),
                        eq("endStationId",dataset.getEndStationId()),
                        eq("plannedArrivalTimeEndStation",dataset.getPlannedArrivalTimeEndStation())
                )
        ).sort(ascending("plannedEventTime")).subscribe(findMultipleSubscriber);

        CompletableFuture<Void> queryFuture = CompletableFuture.supplyAsync(new Supplier<List<Document>>() {
            @Override
            public List<Document> get() {
                return findMultipleSubscriber.get();
            }
        }).thenAccept(result ->{
            //only generate Notifications if there is Data about the Train in the DB
            if (!result.isEmpty()) {
                resultFuture.complete(generateDelayEvents(dataset, Mongo.getUpcomingPlannedTrainDataStartingFromStation(dataset.getStationId(), result)));
            } else {
                resultFuture.complete(new ArrayList<Notification>());
            }
        });
        queryFuture.get();
    }

    private Collection<Notification> generateDelayEvents(LiveTrainData liveTrainData, List<PlannedTrainData> plannedTrainDataList) {
        Collection<Notification> events = new ArrayList<>();
        /*plannedTrainDataList can be empty if the current Station could not be found in the result
        -> No delay will be calculated
        -> No Notifications will be send*/
        try {
            long delay = liveTrainData.getEventTime().getSeconds() - plannedTrainDataList.get(plannedTrainDataList.size()-1).getPlannedEventTime().getSeconds();
            if (Math.abs(delay)>=delayThreshold){
                for ( PlannedTrainData plannedTrainDataTrain : plannedTrainDataList) {
                    events.add(NotificationHelper.getTrainDelayNotificationFrom(
                            String.valueOf(liveTrainData.getTrainSectionId()),
                            delay,
                            "DelayShift from Station: "+liveTrainData.getStationId(),
                            plannedTrainDataTrain.getStationId()));
                }
            }
        } catch ( IndexOutOfBoundsException e) {
            //no Current or Future Station based PlannedTrainData returned from DB
        }
        return events;
    }
}
