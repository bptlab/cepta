package org.bptlab.cepta.utils.database;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.bptlab.cepta.config.MongoConfig;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;

import static org.bson.codecs.configuration.CodecRegistries.*;

public class Mongo {

    public static MongoClient getMongoClient(MongoConfig mongoConfig) {
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(getCustomCodecRegistry())
                .applyConnectionString(new ConnectionString("mongodb://" + mongoConfig.getUser() + ":" + mongoConfig.getPassword() + "@" + mongoConfig.getHost() + ":" + mongoConfig.getPort() + "/?authSource=admin"))
                .build();

        return MongoClients.create(settings);
    }

    private static CodecRegistry getCustomCodecRegistry() {
        Map<BsonType, Class<?>> replacements = new HashMap<BsonType, Class<?>>();
        replacements.put(BsonType.DATE_TIME, com.google.protobuf.Timestamp.class);
        BsonTypeClassMap protoTimestampBsonTypeClassMap = new BsonTypeClassMap(replacements);

        CodecRegistry defaultCodecRegistry = MongoClientSettings.getDefaultCodecRegistry();
        DocumentCodecProvider protoTimeCodecProvider = new DocumentCodecProvider(protoTimestampBsonTypeClassMap);

        CodecRegistry pojoCodecRegistry = fromRegistries(fromCodecs(new TimestampCodec()),
                fromProviders(protoTimeCodecProvider),
                defaultCodecRegistry);
        return pojoCodecRegistry;
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

    public static PlannedTrainData documentToPlannedTrainData(Document doc){
        PlannedTrainData.Builder builder = PlannedTrainData.newBuilder();
        builder.setId((Long)doc.get("id"));
        builder.setTrainSectionId((Long)doc.get("train_section_id"));
        builder.setStationId((Long)doc.get("station_id"));
        builder.setPlannedEventTime((Timestamp)doc.get("planned_event_time"));
        builder.setStatus((Long)doc.get("status"));
        builder.setFirstTrainId((Long)doc.get("first_train_id"));
        builder.setTrainId((Long)doc.get("train_id"));
        builder.setPlannedDepartureTimeStartStation((Timestamp)doc.get("planned_departure_time_start_station"));
        builder.setPlannedArrivalTimeEndStation((Timestamp)doc.get("planned_arrival_time_end_station"));
        builder.setRuId((Long)doc.get("ru_id"));
        builder.setEndStationId((Long)doc.get("end_station_id"));
        builder.setImId((Long)doc.get("im_id"));
        builder.setFollowingImId((Long)doc.get("following_im_id"));
        builder.setMessageStatus((Long)doc.get("message_status"));
        builder.setIngestionTime((Timestamp)doc.get("ingestion_time"));
        builder.setOriginalTrainId((Long)doc.get("original_train_id"));
        return builder.build();
    }

    public static List<PlannedTrainData> documentListToPlannedTrainDataList(List<Document> documentList) {
        List<PlannedTrainData> plannedTrainDataList = new ArrayList<>();
        for (Document doc : documentList) {
            plannedTrainDataList.add(documentToPlannedTrainData(doc));
        }
        return plannedTrainDataList;
    }
}

