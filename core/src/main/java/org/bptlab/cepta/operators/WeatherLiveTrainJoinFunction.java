package org.bptlab.cepta.operators;

import com.google.protobuf.Duration;
import org.apache.flink.api.common.functions.RichJoinFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.bptlab.cepta.models.events.weather.WeatherDataOuterClass.WeatherData;
import org.bptlab.cepta.models.internal.delay.DelayOuterClass;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.internal.types.ids.Ids;
import org.bptlab.cepta.utils.notification.NotificationHelper;

public class WeatherLiveTrainJoinFunction {
  public static DataStream<NotificationOuterClass.Notification> delayFromWeather(DataStream<Tuple2<WeatherData, Integer>> weather, DataStream<LiveTrainData> train){
    return weather.join(train)
        .where(new KeySelector<Tuple2<WeatherData, Integer>, Integer>() {
          @Override
          public Integer getKey(Tuple2<WeatherData, Integer> weatherDataIntegerTuple2) throws Exception {
            return weatherDataIntegerTuple2.f1;
          }
        }).equalTo(new KeySelector<LiveTrainData, Integer>() {
          @Override
          public Integer getKey(LiveTrainData liveTrainData) throws Exception {
            return (int) liveTrainData.getStationId();
          }
        })
        .window(SlidingEventTimeWindows.of(Time.seconds(60), Time.seconds(60)))
        .apply(new RichJoinFunction<Tuple2<WeatherData, Integer>, LiveTrainData, NotificationOuterClass.Notification>() {
          @Override
          public NotificationOuterClass.Notification join(Tuple2<WeatherData, Integer> weatherDataIntegerTuple2,
              LiveTrainData liveTrainData) throws Exception {
            return NotificationHelper.getTrainDelayNotificationFrom(
                    String.valueOf(liveTrainData.getTrainSectionId()),
                    delayFromWeather(weatherDataIntegerTuple2.f0),
                    "Delay caused by weather: "+weatherDataIntegerTuple2.f0.getEventClass().toString(),
                    liveTrainData.getStationId());
          }
        });
  }

  private static Long delayFromWeather(WeatherData weather){
    String eventClass = weather.getEventClass().toString();
    Long delayedSeconds;
    switch (eventClass){
      case "Clear_night": delayedSeconds = 0L; break;
      case "rain": delayedSeconds = 600L; break;
      default: delayedSeconds = 0L;
    }
    return delayedSeconds;
  }
}
