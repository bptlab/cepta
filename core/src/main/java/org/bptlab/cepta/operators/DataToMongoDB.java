package org.bptlab.cepta.operators;

import com.google.protobuf.Message;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;

import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;

import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.streaming.api.functions.async.ResultFuture;

import org.bptlab.cepta.config.MongoConfig;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bptlab.cepta.utils.database.Mongo.protoToBson;

/* This Operator pushes the received Events into a MongoDB and
    passes the received event as soon as the DB acknowledges the Upload
    ProtoTimestamps will be en/decoded by a custom en/decoder*/

public class DataToMongoDB<T extends Message> extends RichAsyncFunction<T, T> {
    private String collection_name;
    private MongoConfig mongoConfig = new MongoConfig();
    private transient MongoClient mongoClient;
    private final Logger log = LoggerFactory.getLogger(DataToMongoDB.class);
    private ArrayList<Mongo.IndexContainer> indices = new ArrayList<>();

    public DataToMongoDB(String collection_name, MongoConfig mongoConfig){
        this.collection_name = collection_name;
        this.mongoConfig = mongoConfig;
    }

    public DataToMongoDB(String collection_name, List<Mongo.IndexContainer> createIndexFor, MongoConfig mongoConfig){
        this.collection_name = collection_name;
        this.mongoConfig = mongoConfig;
        this.indices.addAll(createIndexFor);
    }

    @Override
    public void open(org.apache.flink.configuration.Configuration parameters) throws Exception{
        super.open(parameters);
        this.mongoClient = Mongo.getMongoClient(mongoConfig);
        try{
            if (!indices.isEmpty()) {
                createIndices();
            }
        } catch (NullPointerException e) {
            //no indexing provided
        }

        log.info("Mongo Connection established");
    }

    @Override
    public void close(){
        this.mongoClient.close();
        log.info("Mongo Connection closed");
        // super.close();
    }

    @Override
    public void asyncInvoke(T dataset, ResultFuture<T> resultFuture) throws Exception {
         /*
        asyncInvoke will be called for each incoming element
        the resultFuture is where the outgoing element(s) will be
        */
        MongoDatabase database = mongoClient.getDatabase(mongoConfig.getName());
        MongoCollection<Document> collection = database.getCollection(collection_name);
        Document document = protoToBson(dataset);

        //The new AsyncMongo Driver now uses Reactive Streams,
        // so we need Subscribers to get the Query executed and Results received.
        // For further details consider the following links:
        //http://mongodb.github.io/mongo-java-driver/4.0/driver-reactive/tutorials/connect-to-mongodb/
        //https://github.com/mongodb/mongo-java-driver/blob/eac754d2eed76fe4fa07dbc10ad3935dfc5f34c4/driver-reactive-streams/src/examples/reactivestreams/tour/QuickTour.java
        //https://github.com/mongodb/mongo-java-driver/blob/eac754d2eed76fe4fa07dbc10ad3935dfc5f34c4/driver-reactive-streams/src/examples/reactivestreams/helpers/SubscriberHelpers.java#L53
        //https://github.com/reactive-streams/reactive-streams-jvm/tree/v1.0.3#2-subscriber-code

        SubscriberHelpers.OperationSubscriber<InsertOneResult> insertOneSubscriber = new SubscriberHelpers.OperationSubscriber<>();
        collection.insertOne(document).subscribe(insertOneSubscriber);
        //start the subscriber -> start querying timeout defaults to 60seconds

        CompletableFuture<Boolean> queryFuture = CompletableFuture.supplyAsync(new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                List<InsertOneResult> result = insertOneSubscriber.get();
                Boolean acknowledged = result.get(0).wasAcknowledged();
                return acknowledged;
            }
        });
        CompletableFuture<Boolean> ackFuture = queryFuture.thenApply(acknowledged ->{
            resultFuture.complete(Collections.singleton(dataset));
            return acknowledged;
        });
        if (ackFuture.get()){
            // System.out.println("Success");
        } else {
            log.error("Failure insertion of {} was not acknowledged!",document);
        }
    }

    private void createIndices(){
        MongoDatabase database = mongoClient.getDatabase(mongoConfig.getName());
        MongoCollection<Document> collection = database.getCollection(collection_name);
        SubscriberHelpers.OperationSubscriber<String> indexSubscriber = new SubscriberHelpers.OperationSubscriber<>();
        for (Mongo.IndexContainer index : indices) {
            switch (index.getOrderIndicator()){
                default:
                    collection.createIndex(Indexes.ascending(index.getIndexAttributeNameOrCompound())).subscribe(indexSubscriber);
                    collection.createIndex(Indexes.descending(index.getIndexAttributeNameOrCompound())).subscribe(indexSubscriber);
                    break;
                case 1:
                    collection.createIndex(Indexes.ascending(index.getIndexAttributeNameOrCompound())).subscribe(indexSubscriber);
                    break;
                case -1:
                    collection.createIndex(Indexes.descending(index.getIndexAttributeNameOrCompound())).subscribe(indexSubscriber);
                    break;
            }
        }
        //TODO acknowledge?
        indexSubscriber.get();
    }
}
