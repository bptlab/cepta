package org.bptlab.cepta.operators;

import com.google.protobuf.Message;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;

import com.mongodb.MongoCredential;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.config.MongoConfig;
import org.bson.Document;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Arrays;


public class DataToMongoDB<T extends Message> implements FlatMapFunction<T,T> {

    private String collection_name;
    private MongoConfig mongoConfig = new MongoConfig();

    public DataToMongoDB(String collection_name, MongoConfig mongoConfig){
        this.collection_name = collection_name;
        this.mongoConfig = mongoConfig;
    }

    @Override
    public void flatMap(T dataset, Collector<T> collector) throws Exception {
        //http://mongodb.github.io/mongo-java-driver/4.0/driver-reactive/tutorials/connect-to-mongodb/
//        MongoCredential credential = MongoCredential.createCredential(mongoConfig.getUser(), /*THE DB in which this user is defined*/"admin", mongoConfig.getPassword().toCharArray());
//        MongoClientSettings settings = MongoClientSettings.builder()
//                .credential(credential)
//                .applyToSslSettings(builder -> builder.enabled(true))
//                .applyToClusterSettings(builder ->
//                        builder.hosts(Arrays.asList(new ServerAddress(mongoConfig.getHost(), mongoConfig.getPort()))))
//                .build();
//        MongoClient mongoClient = MongoClients.create(settings);
        MongoClient mongoClient = MongoClients.create();

        MongoDatabase database = mongoClient.getDatabase(mongoConfig.getName());
        MongoCollection<Document> coll = database.getCollection(collection_name);

        Document document = new Document("name","Karl");
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
            }

            @Override
            public void onComplete() {
                System.out.println("Mongo Operation Successful");
                mongoClient.close();
            }
        };
        coll.insertOne(document).subscribe(subscriber);
    }

}
