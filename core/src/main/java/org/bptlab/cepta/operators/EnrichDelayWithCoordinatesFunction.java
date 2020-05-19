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
import org.bson.Document;

import java.util.Hashtable;
import java.util.List;

public class EnrichDelayWithCoordinatesFunction extends RichFlatMapFunction<NotificationOuterClass.Notification, NotificationOuterClass.Notification> {

    public EnrichDelayWithCoordinatesFunction(MongoConfig mongoConfig){
        this.mongoConfig = mongoConfig;
    }

    private Hashtable<Integer, CoordinateOuterClass.Coordinate> coordinateMapping =  new Hashtable<>();

    public Hashtable<Integer, CoordinateOuterClass.Coordinate> getCoordinateMapping(){ return coordinateMapping;}

    private MongoConfig mongoConfig = new MongoConfig();
    private transient MongoClient mongoClient;

    private boolean readInStationData(){
        MongoDatabase database = mongoClient.getDatabase(mongoConfig.getName());
        MongoCollection<Document> eletastations = database.getCollection("eletastations");

        SubscriberHelpers.OperationSubscriber<Document> findMultipleSubscriber = new SubscriberHelpers.OperationSubscriber<>();
        eletastations.find().subscribe(findMultipleSubscriber);

        List<Document> allStations = findMultipleSubscriber.get();

        System.out.println("HALLO HIER KOMMEN DIE STATIONS!!!!!!!!!!!");
        for (Document station: allStations){
            System.out.println(station);
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
        this.mongoClient = Mongo.getMongoClient(mongoConfig);
        readInStationData();
    }

    @Override
    public void flatMap(NotificationOuterClass.Notification notification, Collector<NotificationOuterClass.Notification> collector) throws Exception {
//        System.out.println("Trying to flatten and mappen" + notification.toString());
        System.out.println("Trying to flatten and mappen");
    }
    
    /* This Function takes an inputstream of DelayNotifications and enriches the events 
    with information about the coordinations of the dedicated station */

//    public static DataStream<NotificationOuterClass.DelayNotification> enrichDelayWithCoordinatesFunction(DataStream<NotificationOuterClass.DelayNotification> inputStream) {
//        DataStream<NotificationOuterClass.DelayNotification> outputStream;
//        return outputStream;
//    }


}