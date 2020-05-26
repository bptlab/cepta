package org.bptlab.cepta.providers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass;
import org.bptlab.cepta.utils.database.Mongo;
import org.bson.Document;
import org.testcontainers.containers.GenericContainer;

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

    public static GenericContainer newMongoContainer() {
        return new GenericContainer("mongo")
                .withExposedPorts(27017)
                .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
                .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example");
    }
}
