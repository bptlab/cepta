package org.bptlab.cepta.providers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.events.info.LocationDataOuterClass;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass;
import org.bptlab.cepta.utils.database.Mongo;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;
import org.bptlab.cepta.utils.functions.StreamUtils;
import org.bson.Document;
import org.testcontainers.containers.GenericContainer;

import javax.xml.stream.Location;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.bptlab.cepta.utils.database.Mongo.protoToBson;

public class MongoDbProvider {
    public static MongoConfig setupMongoContainer() {
        GenericContainer mongoContainer = newMongoContainer();
        mongoContainer.start();
        String address = mongoContainer.getContainerIpAddress();
        Integer port = mongoContainer.getFirstMappedPort();
        MongoConfig mongoConfig = new MongoConfig().withHost(address).withPort(port).withPassword("example").withUser("root").withName("mongodb");

        return mongoConfig;
    }

    public static void insertToDb(MongoConfig mongoConfig, PlannedTrainDataOuterClass.PlannedTrainData dataset) throws Exception {
        MongoClient mongoClient = Mongo.getMongoClientSync(mongoConfig);
        MongoDatabase database = mongoClient.getDatabase("mongodb");
        MongoCollection<Document> plannedTrainDataCollection = database.getCollection("plannedTrainData");

        Document document = protoToBson(dataset);

        plannedTrainDataCollection.insertOne(document);
    }

    public static void insertToDb(MongoConfig mongoConfig, LocationDataOuterClass.LocationData dataset) throws Exception {
        MongoClient mongoClient = Mongo.getMongoClientSync(mongoConfig);
        MongoDatabase database = mongoClient.getDatabase("mongodb");
        MongoCollection<Document> plannedTrainDataCollection = database.getCollection("locationData");

        Document document = protoToBson(dataset);

        plannedTrainDataCollection.insertOne(document);
    }

    public static GenericContainer newMongoContainer() {
        return new GenericContainer("mongo")
                .withExposedPorts(27017)
                .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
                .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example");
    }

    public static List<Document> getDatabaseContent(MongoConfig mongoConfig, StreamExecutionEnvironment env) throws Exception{
        // if this gets flaky, use the actual output stream of the asynch operator instead of this
        // to make sure the elements are in the database
        DataStream<Integer> oneElementStream = env.fromElements(1);

        /* we do this with a stream because we collect asynchronously and
           this is the easiest way we came up with to do this */
        DataStream<List<Document>> checkStream = AsyncDataStream
                .unorderedWait(oneElementStream, new RichAsyncFunction<Integer, List<Document>>() {
                    @Override
                    public void asyncInvoke(Integer elem, ResultFuture<List<Document>> resultFuture) throws Exception {
                        com.mongodb.reactivestreams.client.MongoClient mongoClient = Mongo.getMongoClient(mongoConfig);
                        com.mongodb.reactivestreams.client.MongoDatabase database = mongoClient.getDatabase("mongodb");
                        com.mongodb.reactivestreams.client.MongoCollection<Document> plannedTrainDataCollection = database.getCollection("plannedTrainData");

                        SubscriberHelpers.OperationSubscriber<Document> findSubscriber = new SubscriberHelpers.OperationSubscriber<>();

                        plannedTrainDataCollection.find().subscribe(findSubscriber);

                        CompletableFuture<Void> queryFuture = CompletableFuture.supplyAsync(new Supplier<List<Document>>() {
                            @Override
                            public List<Document> get() {
                                // get all the database's content
                                List<Document> result = findSubscriber.get();
                                return result;
                            }
                        }).thenAccept(result ->{
                            resultFuture.complete(Collections.singletonList(result));
                            //System.out.println(result);
                        });

                    }
                },100000, TimeUnit.MILLISECONDS, 1);

        return StreamUtils.collectStreamToArrayList(checkStream).get(0);
    }

    public static ArrayList<PlannedTrainDataOuterClass.PlannedTrainData> getDatabaseContentAsData(MongoConfig mongoConfig, StreamExecutionEnvironment env) throws Exception{
        List<Document> docs = getDatabaseContent(mongoConfig, env);
        ArrayList<PlannedTrainDataOuterClass.PlannedTrainData> plannedTrainData = new ArrayList<PlannedTrainDataOuterClass.PlannedTrainData>();
        for (Document doc : docs){
            plannedTrainData.add(Mongo.documentToPlannedTrainData(doc));
        }
        return plannedTrainData;
    }
}
