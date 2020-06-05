package org.bptlab.cepta.operators;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.internal.types.coordinate.CoordinateOuterClass;
import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;
import org.bson.Document;

import java.util.Hashtable;
import java.util.List;

public class StationToCoordinateMap extends Hashtable<Long, CoordinateOuterClass.Coordinate> {
    private MongoConfig mongoConfig = new MongoConfig();
    private transient MongoClient mongoClient;
    private String tableName;
    private String databaseName;

    public StationToCoordinateMap(MongoConfig mongoConfig){
        this.mongoConfig = mongoConfig;
        this.tableName = "eletastations";
        this.databaseName = "replay";
        mongoClient = Mongo.getMongoClient(mongoConfig);
        readInStationData();
    }

    public StationToCoordinateMap(String tableName, MongoConfig mongoConfig){
        this.mongoConfig = mongoConfig;
        this.tableName = tableName;
        this.databaseName = "replay";
        mongoClient = Mongo.getMongoClient(mongoConfig);
        readInStationData();
    }

    public StationToCoordinateMap(String datebaseName, String tableName, MongoConfig mongoConfig){
        this.mongoConfig = mongoConfig;
        this.tableName = tableName;
        this.databaseName = datebaseName;
        mongoClient = Mongo.getMongoClient(mongoConfig);
        readInStationData();
    }

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
            this.put(key, coordinate);
        }
        return true;
    }



}
