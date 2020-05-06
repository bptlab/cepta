package org.bptlab.cepta.operators;

import com.google.protobuf.Message;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;

import com.mongodb.MongoCredential;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.ConnectionString;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

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

import java.util.Arrays;
import java.util.Collections;
import org.javatuples.Triplet;


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
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .applyConnectionString(new ConnectionString("mongodb://"+mongoConfig.getUser()+":"+mongoConfig.getPassword()+"@"+mongoConfig.getHost()+":"+mongoConfig.getPort()+"/?authSource=admin"))
                .build();
        this.mongoClient = MongoClients.create(settings);
        
        //this.mongoClient = MongoClients.create("mongodb://"+mongoConfig.getUser()+":"+mongoConfig.getPassword()+"@"+mongoConfig.getHost()+":"+mongoConfig.getPort()+"/?authSource=admin");
    }

    @Override
    public void close(){
        this.mongoClient.close();
        // super.close();
    }

    @Override
    public void asyncInvoke(T dataset, ResultFuture<T> resultFuture) throws Exception {
        //http://mongodb.github.io/mongo-java-driver/4.0/driver-reactive/tutorials/connect-to-mongodb/
//        MongoCredential credential = MongoCredential.createCredential(mongoConfig.getUser(), /*THE DB in which this user is defined*/"admin", mongoConfig.getPassword().toCharArray());
//        MongoClientSettings settings = MongoClientSettings.builder()
//                .credential(credential)
//                .applyToSslSettings(builder -> builder.enabled(true))
//                .applyToClusterSettings(builder ->
//                        builder.hosts(Arrays.asList(new ServerAddress(mongoConfig.getHost(), mongoConfig.getPort()))))
//                .build();
//        MongoClient mongoClient = MongoClients.create(settings);
        //"mongodb://user1:pwd1@host1:port/?authSource=db1&ssl=true"
//        MongoClient mongoClient = MongoClients.create("mongodb://"+mongoConfig.getUser()+"@"+mongoConfig.getHost()+":"+mongoConfig.getPort()+"/?authSource=admin");
        // MongoClient mongoClient = MongoClients.create("mongodb://"+mongoConfig.getUser()+":"+mongoConfig.getPassword()+"@"+mongoConfig.getHost()+":"+mongoConfig.getPort()+"/?authSource=admin");

        MongoDatabase database = mongoClient.getDatabase(mongoConfig.getName());
        MongoCollection<T> coll = database.getCollection(collection_name, dataset.getClass());
/* 
        //Document document = new Document();
        ProtoKeyValues protoInfo = Util.getKeyValuesOfProtoMessage(dataset);
        for (int i = 0; i < protoInfo.getColumnNames().size(); i++){
            document.append(protoInfo.getColumnNames().get(i), protoInfo.getValues().get(i));
        } */
        //https://github.com/mongodb/mongo-java-driver/blob/eac754d2eed76fe4fa07dbc10ad3935dfc5f34c4/driver-reactive-streams/src/examples/reactivestreams/helpers/SubscriberHelpers.java#L53
        //https://github.com/reactive-streams/reactive-streams-jvm/tree/v1.0.3#2-subscriber-code
        Subscriber subscriber = new Subscriber() {
            @Override
            public void onSubscribe(Subscription subscription) {
                //Number of elements the subscriber want to get from the publisher
                subscription.request(Integer.MAX_VALUE);
            }

            @Override
            public void onNext(Object o) {
                System.out.println(o.toString());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Mongo Operation Failed");
                System.out.println(throwable);
            }

            @Override
            public void onComplete() {
                System.out.println("Mongo Operation Successful");
                //mongoClient.close();
            }
        };
        coll.insertOne(dataset).subscribe(subscriber);
        resultFuture.complete(Collections.singleton(dataset));
    }

}
