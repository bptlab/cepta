package org.bptlab.cepta.utils.database;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.bptlab.cepta.utils.database.mongohelper.SubscriberHelpers;
import org.bptlab.cepta.config.MongoConfig;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class mongo {

    public static MongoClient getMongoClient(MongoConfig mongoConfig) {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromCodecs(new mongo.TimestampCodec())
        );
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .applyConnectionString(new ConnectionString("mongodb://" + mongoConfig.getUser() + ":" + mongoConfig.getPassword() + "@" + mongoConfig.getHost() + ":" + mongoConfig.getPort() + "/?authSource=admin"))
                .build();

        return MongoClients.create(settings);
    }

    public static class TimestampCodec implements Codec<Timestamp> {
        @Override
        public void encode(final BsonWriter writer, final com.google.protobuf.Timestamp ts, final EncoderContext encoderContext) {
            ZonedDateTime dateTime = Instant
                    .ofEpochSecond(ts.getSeconds(), ts.getNanos())
                    .atZone(ZoneId.of("Europe/Berlin"));
            writer.writeDateTime(dateTime.toInstant().toEpochMilli());
        }

        @Override
        public com.google.protobuf.Timestamp decode(final BsonReader reader, final DecoderContext decoderContext) {
            long milliseconds = reader.readDateTime();
            Instant instant = Instant.ofEpochMilli(milliseconds);
            return com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build();
        }

        @Override
        public Class<com.google.protobuf.Timestamp> getEncoderClass() {
            return com.google.protobuf.Timestamp.class;
        }
    }


//    public abstract static class Subscriber
    //https://github.com/mongodb/mongo-java-driver/blob/eac754d2eed76fe4fa07dbc10ad3935dfc5f34c4/driver-reactive-streams/src/examples/reactivestreams/helpers/SubscriberHelpers.java#L53
    //https://github.com/reactive-streams/reactive-streams-jvm/tree/v1.0.3#2-subscriber-code
    //https://github.com/mongodb/mongo-java-driver/blob/master/driver-reactive-streams/src/examples/reactivestreams/documentation/DocumentationSamples.java
    public static SubscriberHelpers.OperationSubscriber getSubscriber() {
        return new SubscriberHelpers.OperationSubscriber();
//        return new Subscriber() {
//            @Override
//            public void onSubscribe(Subscription subscription) {
//                //Number of elements the subscriber want to get from the publisher
//                subscription.request(Integer.MAX_VALUE);
//            }
//
//            @Override
//            public void onNext(Object o) {
//                System.out.println(o.toString());
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                System.out.println("Mongo Operation Failed");
//            }
//
//            @Override
//            public void onComplete() {
//                System.out.println("Mongo Operation Successful");
//                //mongoClient.close();
//            }
//        };
    }
}
