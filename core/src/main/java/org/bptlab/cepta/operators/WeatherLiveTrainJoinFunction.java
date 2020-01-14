package org.bptlab.cepta.operators;

import org.apache.flink.api.common.functions.RichJoinFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.bptlab.cepta.LiveTrainData;
import org.bptlab.cepta.TrainDelayNotification;
import org.bptlab.cepta.WeatherData;

public class WeatherLiveTrainJoinFunction {
  public static DataStream<TrainDelayNotification> delayFromWeather(DataStream<Tuple2<WeatherData, Integer>> weather, DataStream<LiveTrainData> train){
    return weather.join(train)
        .where(new KeySelector<Tuple2<WeatherData, Integer>, Integer>() {
          @Override
          public Integer getKey(Tuple2<WeatherData, Integer> weatherDataIntegerTuple2)
              throws Exception {
            return weatherDataIntegerTuple2.f1;
          }
        }).equalTo(new KeySelector<LiveTrainData, Integer>() {
          @Override
          public Integer getKey(LiveTrainData liveTrainData) throws Exception {
            return liveTrainData.getLocationId();
          }
        })
        .window(SlidingEventTimeWindows.of(Time.seconds(60), Time.seconds(2)))
        .apply(new RichJoinFunction<Tuple2<WeatherData, Integer>, LiveTrainData, TrainDelayNotification>() {
          @Override
          public TrainDelayNotification join(Tuple2<WeatherData, Integer> weatherDataIntegerTuple2,
              LiveTrainData liveTrainData) throws Exception {
            TrainDelayNotification adelheit = TrainDelayNotification.newBuilder().setDelay(9000)
                .setTrainId(liveTrainData.getTrainId()).setLocationId(liveTrainData.getLocationId())
                .build();
            return adelheit;
          }
        });
  }
}
