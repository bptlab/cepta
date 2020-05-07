package org.bptlab.cepta.operators;

import com.google.protobuf.Message;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;

import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;

import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.streaming.api.functions.async.ResultFuture;

import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.utils.database.Util;
import org.bptlab.cepta.utils.database.Util.ProtoKeyValues;

import org.bson.Document;

import java.util.Collections;

import static org.bptlab.cepta.utils.database.Mongo.protoToBson;


public class DataToMongoDB<T extends Message> extends RichAsyncFunction<T, T> {
    private String collection_name;
    private MongoConfig mongoConfig = new MongoConfig();
    private transient MongoClient mongoClient;

    public DataToMongoDB(String collection_name, MongoConfig mongoConfig){
        this.collection_name = collection_name;
        this.mongoConfig = mongoConfig;
    }

    @Override
    public void open(org.apache.flink.configuration.Configuration parameters) throws Exception{
        super.open(parameters);
        this.mongoClient = Mongo.getMongoClient(mongoConfig);
    }

    @Override
    public void close(){
        this.mongoClient.close();
        // super.close();
    }

    @Override
    public void asyncInvoke(T dataset, ResultFuture<T> resultFuture) throws Exception {
        //http://mongodb.github.io/mongo-java-driver/4.0/driver-reactive/tutorials/connect-to-mongodb/
        //https://github.com/mongodb/mongo-java-driver/blob/eac754d2eed76fe4fa07dbc10ad3935dfc5f34c4/driver-reactive-streams/src/examples/reactivestreams/tour/QuickTour.java

        MongoDatabase database = mongoClient.getDatabase(mongoConfig.getName());
        MongoCollection<Document> coll = database.getCollection(collection_name);

        Document document = protoToBson(dataset);

        SubscriberHelpers.OperationSubscriber<InsertOneResult> insertOneSubscriber = new SubscriberHelpers.OperationSubscriber<>();
        coll.insertOne(document).subscribe(insertOneSubscriber);
        //start the subscriber -> start querying timeout defaults to 60seconds
        insertOneSubscriber.await();
        resultFuture.complete(Collections.singleton(dataset));
    }

}
