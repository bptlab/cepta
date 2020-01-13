package org.bptlab.cepta.operators;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.bptlab.cepta.LiveTrainData;
import org.bptlab.cepta.TrainDelayNotification;
import org.bptlab.cepta.WeatherData;

public class WeatherLiveTrainJoinFunction {
  public static DataStream<TrainDelayNotification> delayFromWeather(DataStream<Tuple2<WeatherData, Integer>> weather, DataStream<LiveTrainData> train){
    return weather.join(train)
        .where(new KeySelector<Tuple2<WeatherData, Integer>, Object>() {
          @Override
          public Object getKey(Tuple2<WeatherData, Integer> weatherDataIntegerTuple2)
              throws Exception {
            System.out.println(weatherDataIntegerTuple2);
            return weatherDataIntegerTuple2.f1;
          }
        }).equalTo(new KeySelector<LiveTrainData, Object>() {
          @Override
          public Object getKey(LiveTrainData liveTrainData) throws Exception {
            System.out.println(liveTrainData.getLocationId());
            return liveTrainData.getLocationId();
          }
        })//.window(SlidingEventTimeWindows.of(Time.seconds(60), Time.seconds(5)))
        .window(SlidingProcessingTimeWindows.of(Time.seconds(60), Time.seconds(10)))
        .apply(new JoinFunction<Tuple2<WeatherData, Integer>, LiveTrainData, TrainDelayNotification>() {
          @Override
          public TrainDelayNotification join(Tuple2<WeatherData, Integer> weatherDataIntegerTuple2,
              LiveTrainData liveTrainData) throws Exception {

            TrainDelayNotification adelheit = TrainDelayNotification.newBuilder().setDelay(9000)
                .setTrainId(liveTrainData.getTrainId()).setLocationId(liveTrainData.getLocationId())
                .build();
            System.out.println(adelheit);
            return adelheit;
          }
        });
  }
}
