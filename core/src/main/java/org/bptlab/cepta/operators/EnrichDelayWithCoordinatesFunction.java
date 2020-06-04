package org.bptlab.cepta.operators;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.models.internal.types.coordinate.CoordinateOuterClass;
import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;
import org.bson.Document;

import java.util.Hashtable;
import java.util.List;


public class EnrichDelayWithCoordinatesFunction extends RichFlatMapFunction<NotificationOuterClass.Notification, NotificationOuterClass.Notification> {
    /* This Function takes an inputstream of DelayNotifications and enriches the events
       with information about the coordinations of the dedicated station */

    private MongoConfig mongoConfig = new MongoConfig();
    private transient MongoClient mongoClient;
    private String tableName;
    private String databaseName;

    private Hashtable<String, CoordinateOuterClass.Coordinate> coordinateMapping =  new Hashtable<>();


    public EnrichDelayWithCoordinatesFunction(MongoConfig mongoConfig){
        this.mongoConfig = mongoConfig;
        this.tableName = "eletastations";
        this.databaseName = "replay";
    }

    public EnrichDelayWithCoordinatesFunction(String tableName, MongoConfig mongoConfig){
        this.mongoConfig = mongoConfig;
        this.tableName = tableName;
        this.databaseName = "replay";
    }
    
    public EnrichDelayWithCoordinatesFunction(String datebaseName, String tableName, MongoConfig mongoConfig){
        this.mongoConfig = mongoConfig;
        this.tableName = tableName;
        this.databaseName = datebaseName;
    }

    public Hashtable<String, CoordinateOuterClass.Coordinate> getMapping(){ return coordinateMapping;}

    private boolean readInStationData(){
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> stationsCollection = database.getCollection(tableName);

        SubscriberHelpers.OperationSubscriber<Document> findMultipleSubscriber = new SubscriberHelpers.OperationSubscriber<>();
        stationsCollection.find().subscribe(findMultipleSubscriber);

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
        this.mongoClient = Mongo.getMongoClient(mongoConfig);
        readInStationData();
    }

    @Override
    public void flatMap(NotificationOuterClass.Notification unenrichedNotification, Collector<NotificationOuterClass.Notification> collector) throws Exception {

        String searchForStationId = unenrichedNotification.getDelay().getStationId().getId();
        if (coordinateMapping.containsKey(searchForStationId) && unenrichedNotification.hasDelay()){
            CoordinateOuterClass.Coordinate matchingCoordinate = coordinateMapping.get(searchForStationId);

            NotificationOuterClass.DelayNotification enrichedDelayNotification = unenrichedNotification.getDelay().toBuilder()
                    .setCoordinate(matchingCoordinate)
                    .build();
            NotificationOuterClass.Notification enrichedNotification = unenrichedNotification.toBuilder()
                    .setDelay(enrichedDelayNotification)
                    .build();

            collector.collect(enrichedNotification);
        } else {
            collector.collect(unenrichedNotification);
        }
    }
}