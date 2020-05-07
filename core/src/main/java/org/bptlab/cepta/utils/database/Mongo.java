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
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.bson.codecs.configuration.CodecRegistries.*;

public class Mongo {

    public static MongoClient getMongoClient(MongoConfig mongoConfig) {
        Map<BsonType, Class<?>> replacements = new HashMap<BsonType, Class<?>>();
        replacements.put(BsonType.DATE_TIME, com.google.protobuf.Timestamp.class);
        BsonTypeClassMap bsonTypeClassMap = new BsonTypeClassMap(replacements);

        CodecRegistry defaultCodecRegistry = MongoClientSettings.getDefaultCodecRegistry();
        TimestampCodecProvider timestampCodecProvider = new TimestampCodecProvider(bsonTypeClassMap);
        Codec<Timestamp> timestampCodec = new TimestampCodec();

        CodecRegistry pojoCodecRegistry = fromRegistries(fromCodecs(timestampCodec),
            fromProviders(timestampCodecProvider),
            defaultCodecRegistry);

        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .applyConnectionString(new ConnectionString("mongodb://" + mongoConfig.getUser() + ":" + mongoConfig.getPassword() + "@" + mongoConfig.getHost() + ":" + mongoConfig.getPort() + "/?authSource=admin"))
                .build();

        return MongoClients.create(settings);
    }

    // http://mongodb.github.io/mongo-java-driver/4.0/bson/codecs/
    public static class TimestampCodecProvider implements CodecProvider {
        private final BsonTypeClassMap bsonTypeClassMap;

        public TimestampCodecProvider(final BsonTypeClassMap bsonTypeClassMap) {
            this.bsonTypeClassMap = bsonTypeClassMap;
        }

        @Override
        public <T> Codec<T> get(final Class<T> aClass, final CodecRegistry codecRegistry) {
            if (aClass == Document.class) {
//                Map<BsonType, Class<?>> replacements = new HashMap<BsonType, Class<?>>();
//                replacements.put(BsonType.DATE_TIME, com.google.protobuf.Timestamp.class);
//                BsonTypeClassMap bsonTypeClassMap = new BsonTypeClassMap(replacements);
//                CodecRegistry timestampCodecRegistry = CodecRegistries.fromCodecs(new Mongo.TimestampCodec());
                return (Codec<T>) new DocumentCodec(codecRegistry,bsonTypeClassMap);
            }

            return null;
        }
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
    
    //https://github.com/mongodb/mongo-java-driver/blob/eac754d2eed76fe4fa07dbc10ad3935dfc5f34c4/driver-reactive-streams/src/examples/reactivestreams/helpers/SubscriberHelpers.java#L53
    //https://github.com/reactive-streams/reactive-streams-jvm/tree/v1.0.3#2-subscriber-code
    //https://github.com/mongodb/mongo-java-driver/blob/master/driver-reactive-streams/src/examples/reactivestreams/documentation/DocumentationSamples.java
    public static SubscriberHelpers.OperationSubscriber getSubscriber() {
        return new SubscriberHelpers.OperationSubscriber();
    }

    public static Document protoToBson(Message dataset ) {
        Util.ProtoKeyValues protoInfo = new Util.ProtoKeyValues();
        try {
            protoInfo = Util.getKeyValuesOfProtoMessage(dataset);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            System.out.println("Failed to convert Message to Bson: "+e.getLocalizedMessage());
        }
        Document document = new Document();
        for (int i = 0; i < protoInfo.getColumnNames().size(); i++){
            document.append(protoInfo.getColumnNames().get(i), protoInfo.getValues().get(i));
        }
        return document;
    }


}

