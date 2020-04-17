package org.bptlab.cepta;

import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.CEP;

import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.patterns.StaysInStationPattern;
import org.junit.Assert;
import org.junit.Test;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import java.util.Iterator;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.StaysInStation;



import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;

public class PatternTests {

    @Test
    public void TestStaysInStationDirectly() throws Exception {
        DataStream<LiveTrainData> liveTrainDataStream = LiveTrainDataProvider.staysInStationDirectly();

        Iterator<LiveTrainData> iterator = DataStreamUtils.collect(liveTrainDataStream);
        while(iterator.hasNext()){
            LiveTrainData rüdiger = iterator.next();
            System.out.println(rüdiger.toString());
        }

        PatternStream<Event> patternStream = CEP.pattern(liveTrainDataStream, StaysInStationPattern.staysInStationPattern);

        DataStream<StaysInStation> generatedEvents = patternStream.select(
            (Map<String, List<LiveTrainData>> pattern) -> {
                LiveTrainData first = (LiveTrainData) pattern.get("first").get(0);

                StaysInStation detected = StaysInStation.newBuilder()
                    .setTrainSectionId(first.getTrainSectionId)
                    .setStationId(first.getStationid)
                    .setTrainId(first.getTrainId)
                    .setEventTime(first.getEventTime)
                    .build();
                return detected;
            }
        );

        int count = 0;
        Iterator<StaysInStation> detectedEventsItr = DataStreamUtils.collect(generatedEvents);
        while(detectedEventsItr.hasNext()){
            count ++;
        }


        Assert.assertTrue(count == 1);

    }
    
}