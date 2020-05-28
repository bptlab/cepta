package org.bptlab.cepta.utils.database;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import org.javatuples.Pair;

import static org.bson.codecs.configuration.CodecRegistries.*;

public class Mongo {

    public static com.mongodb.reactivestreams.client.MongoClient getMongoClient(MongoConfig mongoConfig) {
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(getCustomCodecRegistry())
                .applyConnectionString(new ConnectionString("mongodb://" + mongoConfig.getUser() + ":" + mongoConfig.getPassword() + "@" + mongoConfig.getHost() + ":" + mongoConfig.getPort() + "/?authSource=admin"))
                .build();

        return com.mongodb.reactivestreams.client.MongoClients.create(settings);
    }

    public static MongoClient getMongoClientSync(MongoConfig mongoConfig) {
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
        try {
            builder.setId((Long) doc.get("id"));
            builder.setTrainSectionId((Long) doc.get("trainSectionId"));
            builder.setStationId((Long) doc.get("stationId"));
            builder.setPlannedEventTime((Timestamp) doc.get("plannedEventTime"));
            builder.setStatus((Long) doc.get("status"));
            builder.setFirstTrainId((Long) doc.get("firstTrainId"));
            builder.setTrainId((Long) doc.get("trainId"));
            builder.setPlannedDepartureTimeStartStation((Timestamp) doc.get("plannedDepartureTimeStartStation"));
            builder.setPlannedArrivalTimeEndStation((Timestamp) doc.get("plannedArrivalTimeEndStation"));
            builder.setRuId((Long) doc.get("ruId"));
            builder.setEndStationId((Long) doc.get("endStationId"));
            builder.setImId((Long) doc.get("imId"));
            builder.setFollowingImId((Long) doc.get("followingImId"));
            builder.setMessageStatus((Long) doc.get("messageStatus"));
            builder.setIngestionTime((Timestamp) doc.get("ingestionTime"));
            builder.setOriginalTrainId((Long) doc.get("originalTrainId"));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    public static List<PlannedTrainData> getUpcomingPlannedTrainDataStartingFromStation(long currentStationId, List<Document> documentList) {
        ArrayList<PlannedTrainData> plannedTrainDataList = new ArrayList<>();
        boolean hasReferenceStation = false;
        try {
            for (int backwardsIterator = documentList.size()-1; backwardsIterator >= 0; backwardsIterator--) {
                long plannedStationId = (long) documentList.get(backwardsIterator).get("stationId");
                if (currentStationId != plannedStationId ) {
                    plannedTrainDataList.add(documentToPlannedTrainData(documentList.get(backwardsIterator)));
                } else {
                    plannedTrainDataList.add(documentToPlannedTrainData(documentList.get(backwardsIterator)));
                    hasReferenceStation = true;
                    break;
                }
            }
        } catch ( Exception e) {
            // No element in DocumentList with stationId
            e.printStackTrace();
        }
        if (hasReferenceStation) {
            return  plannedTrainDataList;
        } else {
            return new ArrayList<PlannedTrainData>();
        }
    }

    public static class IndexContainer implements Serializable {
        private String indexAttributeNameOrCompound;
        private Integer orderIndicator;

        //orderIndicator = 0 indicates both ascending and descending indices will be created
        public IndexContainer(String indexAttributeNameOrCompound){
            this.indexAttributeNameOrCompound = indexAttributeNameOrCompound;
            this.orderIndicator = 0;
        }

        public IndexContainer(String indexAttributeNameOrCompound, Integer orderIndicator){
            this.indexAttributeNameOrCompound = indexAttributeNameOrCompound;
            this.orderIndicator = orderIndicator;
        }

        public Pair<String,Integer> get(){
            return new Pair<>(indexAttributeNameOrCompound, orderIndicator);
        }

        public String getIndexAttributeNameOrCompound() {
            return indexAttributeNameOrCompound;
        }

        public Integer getOrderIndicator() {
            return orderIndicator;
        }
    }

    public static List<IndexContainer> makeIndexContainerListFromPairs(List<Pair<String,Integer>> indicesToBeCreated){
        ArrayList<IndexContainer> indexList = new ArrayList();
        for (Pair<String,Integer> indexPair : indicesToBeCreated) {
            indexList.add(new IndexContainer(indexPair.getValue0(),indexPair.getValue1()) );
        }
        return indexList;
    }

    public static List<IndexContainer> makeIndexContainerList(List<String> indicesToBeCreated){
        ArrayList<IndexContainer> indexList = new ArrayList();
        for (String indexAttributeOrCompound : indicesToBeCreated) {
            indexList.add(new IndexContainer(indexAttributeOrCompound) );
        }
        return indexList;
    }
}

