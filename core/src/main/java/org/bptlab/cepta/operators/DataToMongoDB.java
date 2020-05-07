package org.bptlab.cepta.operators;

import com.google.protobuf.Message;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;

import com.mongodb.MongoCredential;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.ConnectionString;

import com.sun.swing.internal.plaf.synth.resources.synth_sv;
import org.bptlab.cepta.utils.database.mongo;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;
import org.bson.BsonReader;
import org.bson.BsonTimestamp;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.util.Collector;

import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.utils.Util;
import org.bptlab.cepta.utils.Util.ProtoKeyValues;

import org.bson.Document;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import org.javatuples.Triplet;

import static org.bson.codecs.configuration.CodecRegistries.*;


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
        this.mongoClient = mongo.getMongoClient(mongoConfig);
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

        Document document = new Document();

        ProtoKeyValues protoInfo = Util.getKeyValuesOfProtoMessage(dataset);

        for (int i = 0; i < protoInfo.getColumnNames().size(); i++){
            document.append(protoInfo.getColumnNames().get(i), protoInfo.getValues().get(i));
        }
//
        SubscriberHelpers.OperationSubscriber<InsertOneResult> insertOneSubscriber = new SubscriberHelpers.OperationSubscriber<>();
        coll.insertOne(document).subscribe(insertOneSubscriber);
        //start the subscriber -> start querying timeout defaults to 60seconds
        insertOneSubscriber.await();
        //System.out.println(insertOneSubscriber.get());
        resultFuture.complete(Collections.singleton(dataset));
    }

}
