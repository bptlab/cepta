package org.bptlab.cepta.operators;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.models.internal.types.coordinate.CoordinateOuterClass;
import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;
import org.bptlab.cepta.utils.notification.NotificationHelper;
import org.bson.Document;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;


public class EnrichDelayWithCoordinatesFunction extends RichFlatMapFunction<NotificationOuterClass.Notification, NotificationOuterClass.Notification> {
    /* This Function takes an inputstream of DelayNotifications and enriches the events
       with information about the coordinations of the dedicated station */

    public EnrichDelayWithCoordinatesFunction(MongoConfig mongoConfig){
        this.mongoConfig = mongoConfig;
        this.tableName = "eletastations"/*"location"*/;
    }

    public EnrichDelayWithCoordinatesFunction(String tableName, MongoConfig mongoConfig){
        this.mongoConfig = mongoConfig;
        this.tableName = tableName;
    }

    private Hashtable<String, CoordinateOuterClass.Coordinate> coordinateMapping =  new Hashtable<>();

    public Hashtable<String, CoordinateOuterClass.Coordinate> getMapping(){ return coordinateMapping;}

    private MongoConfig mongoConfig = new MongoConfig();
    private transient MongoClient mongoClient;
    private String tableName;

    private boolean readInStationData(){
        MongoDatabase database = mongoClient.getDatabase("replay"/*mongoConfig.getName()*/);
        MongoCollection<Document> eletastations = database.getCollection(tableName);

        SubscriberHelpers.OperationSubscriber<Document> findMultipleSubscriber = new SubscriberHelpers.OperationSubscriber<>();
        eletastations.find().subscribe(findMultipleSubscriber);

        List<Document> allStations = findMultipleSubscriber.get();

        for (Document station: allStations){
            CoordinateOuterClass.Coordinate coordinate =
                    CoordinateOuterClass.Coordinate.newBuilder()
                            .setLongitude((double) station.get("longitude"))
                            .setLatitude((double) station.get("latitude"))
                            .build();
            Long key = (Long) station.get("stationId");
            coordinateMapping.put(String.valueOf(key), coordinate);
        }
        return true;
    }


    /**
     * First we read in the station data into our local map variable.
     * @param parameters
     * @throws Exception
     */
    public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
        super.open(parameters);
        System.out.println("open!!!!!!!!!");
        //TODO Remove Port after Demo!
        this.mongoClient = Mongo.getMongoClient(mongoConfig/*.withPort(27017)*/);
        readInStationData();
    }

    @Override
    public void flatMap(NotificationOuterClass.Notification notification, Collector<NotificationOuterClass.Notification> collector) throws Exception {
        String searchForStationId = notification.getDelay().getStationId().getId();
        CoordinateOuterClass.Coordinate matchingCoordinate = coordinateMapping.get(searchForStationId);
        NotificationOuterClass.DelayNotification delayNotification = notification.getDelay();
        //TODO make this less ugly please!
        NotificationOuterClass.Notification new_notification = NotificationHelper.getTrainDelayNotificationFrom(
                delayNotification.getTransportId(),
                delayNotification.getDelay(),
                delayNotification.getStationId(),
                matchingCoordinate);
        collector.collect(new_notification);
    }


}