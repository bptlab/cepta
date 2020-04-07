package org.bptlab.cepta.operators;

import org.apache.flink.api.common.functions.RichJoinFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.bptlab.cepta.models.events.weather.WeatherDataProtos.WeatherData;
import org.bptlab.cepta.models.events.train.TrainDelayNotificationProtos.TrainDelayNotification;
import org.bptlab.cepta.models.events.train.LiveTrainDataProtos.LiveTrainData;

public class WeatherLiveTrainJoinFunction {
  public static DataStream<TrainDelayNotification> delayFromWeather(DataStream<Tuple2<WeatherData, Integer>> weather, DataStream<LiveTrainData> train){
    return weather.join(train)
        .where(new KeySelector<Tuple2<WeatherData, Integer>, Integer>() {
          @Override
          public Integer getKey(Tuple2<WeatherData, Integer> weatherDataIntegerTuple2) throws Exception {
            return weatherDataIntegerTuple2.f1;
          }
        }).equalTo(new KeySelector<LiveTrainData, Integer>() {
          @Override
          public Integer getKey(LiveTrainData liveTrainData) throws Exception {
            return (int) liveTrainData.getEletaStationId();
          }
        })
        .window(SlidingEventTimeWindows.of(Time.seconds(60), Time.seconds(60)))
        .apply(new RichJoinFunction<Tuple2<WeatherData, Integer>, LiveTrainData, TrainDelayNotification>() {
          @Override
          public TrainDelayNotification join(Tuple2<WeatherData, Integer> weatherDataIntegerTuple2,
              LiveTrainData liveTrainData) throws Exception {
            return TrainDelayNotification.newBuilder()
                .setDelay(delayFromWeather(weatherDataIntegerTuple2.f0))
                .setTrainId(liveTrainData.getTrainId())
                .setLocationId(liveTrainData.getEletaStationId())
                .build();
          }
        });
  }

  private static int delayFromWeather(WeatherData weather){
    String eventClass = weather.getEventClass().toString();
    int delay;
    switch (eventClass){
      case "Clear_night": delay = 0; break;
      case "rain": delay = 10; break;
      default: delay = 0;
    }
    return delay;
  }
}
