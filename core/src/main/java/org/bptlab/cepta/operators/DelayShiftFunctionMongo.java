package org.bptlab.cepta.operators;

import com.google.protobuf.Duration;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.MongoCredential;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Sorts.*;

import org.bptlab.cepta.models.events.train.TrainDelayNotificationOuterClass;
import org.bptlab.cepta.models.internal.delay.DelayOuterClass.Delay;
import org.bptlab.cepta.models.internal.types.ids.Ids;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.Document;
import static org.bson.codecs.configuration.CodecRegistries.*;

import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.util.Collector;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.models.events.train.TrainDelayNotificationOuterClass.TrainDelayNotification;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass.*;



public class DelayShiftFunctionMongo extends
    RichAsyncFunction<LiveTrainData, Notification> {

    private MongoConfig mongoConfig = new MongoConfig();
    private transient MongoClient mongoClient;

    public DelayShiftFunctionMongo(MongoConfig mongoConfig) {
        this.mongoConfig = mongoConfig;    }

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
        MongoCollection<Document> plannedTrainDataCollection = database.getCollection("plannedtraindata");

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
//        List<Document> docs = findMultipleSubscriber.get();
//        try {
//            System.out.println(docs);
//            System.out.println(docs.get(0).get("planned_event_time"));
//            System.out.println(docs.get(0).get("planned_event_time") instanceof Timestamp);
//        } catch (IndexOutOfBoundsException e) {
//            System.out.println("Empty result");
//        }
//      TODO the actual work
        CompletableFuture<Void> queryFuture = CompletableFuture.supplyAsync(new Supplier<List<Document>>() {
            @Override
            public List<Document> get() {
                return findMultipleSubscriber.get();
            }
        }).thenAccept(result ->{


            resultFuture.complete(generateDelayEvents(dataset, Mongo.documentListToPlannedTrainDataList(result)));
        });
        queryFuture.get();
    }
    private Collection<Notification> generateDelayEvents(LiveTrainData liveTrainData,List<PlannedTrainData> plannedTrainDataList) {
        Collection<Notification> events = new ArrayList<>();
        long delay = liveTrainData.getEventTime().getSeconds() - plannedTrainDataList.get(0).getPlannedEventTime().getSeconds();
        int backwardsIterator = plannedTrainDataList.size()-1;
        while (liveTrainData.getStationId() != plannedTrainDataList.get(backwardsIterator).getStationId()){
            DelayNotification.Builder delayBuilder = DelayNotification.newBuilder();
            delayBuilder.setDelay(Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(delay).build()).setDetails("DelayShift from Station: "+liveTrainData.getStationId()).build() );
            delayBuilder.setTransportId(Ids.CeptaTransportID.newBuilder());
            delayBuilder.setStationId(Ids.CeptaStationID.newBuilder().setId(String.valueOf(liveTrainData.getStationId())).build());
            events.add(Notification.newBuilder().setDelay(delayBuilder.build()).build());
            backwardsIterator--;
            if (backwardsIterator<0) {
                break;
            }
        }

        return events;
    }
}