package org.bptlab.cepta;

import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.CEP;

import org.bptlab.cepta.providers.LiveTrainDataProvider;
import org.bptlab.cepta.patterns.StaysInStationPattern;
import org.junit.Assert;
import org.junit.Test;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;
import org.bptlab.cepta.models.events.correlatedEvents.StaysInStationEventProtos.StaysInStationEvent;
import java.util.*;

import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;

public class PatternTests {

    @Test
    public void TestStaysInStationDirectly() throws Exception {
        System.out.println("HALOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");

        DataStream<LiveTrainData> liveTrainDataStream = LiveTrainDataProvider.staysInStationDirectly();

        Iterator<LiveTrainData> iterator = DataStreamUtils.collect(liveTrainDataStream);
        while(iterator.hasNext()){
            LiveTrainData rüdiger = iterator.next();
            System.out.println(rüdiger.toString());
        }

        PatternStream<LiveTrainData> patternStream = CEP.pattern(liveTrainDataStream, StaysInStationPattern.staysInStationPattern);

        DataStream<StaysInStationEvent> generatedEvents = patternStream.select(
            (Map<String, LiveTrainData> pattern) -> {
                System.out.println("Ich bin hier :)");
                LiveTrainData first = (LiveTrainData) pattern.get("arrivesInStation");

                
                StaysInStationEvent detected = StaysInStationEvent.newBuilder()
                    .setTrainSectionId(first.getTrainSectionId())
                    .setStationId(first.getStationId())
                    .setTrainId(first.getTrainId())
                    .setEventTime(first.getEventTime())
                    .build();
                return detected;
            }
        );

        int count = 0;
        System.out.println("Count = " + count);
        Iterator<StaysInStationEvent> detectedEventsItr = DataStreamUtils.collect(generatedEvents);
        while(detectedEventsItr.hasNext()){
            count ++;
            System.out.println("Increasing, Count = " + count);
        }

        System.out.println("Count = " + count);
        System.out.println("HALOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
        Assert.assertTrue(count==1);

    }
    
}