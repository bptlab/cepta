package org.bptlab.cepta.providers;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.models.internal.types.ids.Ids;
import org.bptlab.cepta.models.internal.delay.DelayOuterClass;
import com.google.protobuf.Duration;


public class TrainDelayNotificationDataProvider {

        public static NotificationOuterClass.Notification getDefaultLTrainDelayNotificationDataEvent() {
            return NotificationOuterClass.Notification.newBuilder().setDelay(NotificationOuterClass.DelayNotification.newBuilder()
                    .setCeptaId(Ids.CeptaTransportID.newBuilder().setId("1").build())
                    .setStationId(Ids.CeptaStationID.newBuilder().setId("1").build())
                    .setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(1).build()).build())
                    .build()
            ).build();
        }

      
        public static DataStream<NotificationOuterClass.Notification> TrainDelayNotificationDataStream(){
          StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
          env.setParallelism(1);

            NotificationOuterClass.Notification ele1 = trainDelayNotificationWithLocationIdWithTrainIdWithDelay("1","1",10L);
            NotificationOuterClass.Notification ele2 = trainDelayNotificationWithLocationIdWithTrainIdWithDelay("2","2",5L);
            NotificationOuterClass.Notification ele3 = trainDelayNotificationWithLocationIdWithTrainIdWithDelay("1","2",15L);
            NotificationOuterClass.Notification ele4 = trainDelayNotificationWithLocationIdWithTrainIdWithDelay("2","1",8L);
      
          DataStream<NotificationOuterClass.Notification> trainDelayNotificationStream = env.fromElements(ele1, ele2, ele3, ele4);
      
          return trainDelayNotificationStream;
        }
      

        private static NotificationOuterClass.Notification trainDelayNotificationWithLocationIdWithTrainIdWithDelay(String locationId, String trainId, Long delay){
            return NotificationOuterClass.Notification.newBuilder().setDelay(NotificationOuterClass.DelayNotification.newBuilder()
                    .setCeptaId(Ids.CeptaTransportID.newBuilder().setId(trainId).build())
                    .setStationId(Ids.CeptaStationID.newBuilder().setId(locationId).build())
                    .setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(delay).build()).build())
                    .build()
            ).build();
        }

        private static NotificationOuterClass.Notification trainDelayNotificationWithLocationIdWithTrainId(String locationId, String trainId){
            return NotificationOuterClass.Notification.newBuilder().setDelay(NotificationOuterClass.DelayNotification.newBuilder()
                    .setCeptaId(Ids.CeptaTransportID.newBuilder().setId(trainId).build())
                    .setStationId(Ids.CeptaStationID.newBuilder().setId(locationId).build())
                    .setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(1).build()).build())
                    .build()
            ).build();
        }

        private static NotificationOuterClass.Notification trainDelayNotificationWithLocationId(String locationId){
            return NotificationOuterClass.Notification.newBuilder().setDelay(NotificationOuterClass.DelayNotification.newBuilder()
                    .setCeptaId(Ids.CeptaTransportID.newBuilder().setId("1").build())
                    .setStationId(Ids.CeptaStationID.newBuilder().setId(locationId).build())
                    .setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(1).build()).build())
                    .build()
            ).build();
        }

        private static NotificationOuterClass.Notification trainDelayNotificationWithTrainId(String trainId){
            return NotificationOuterClass.Notification.newBuilder().setDelay(NotificationOuterClass.DelayNotification.newBuilder()
                    .setCeptaId(Ids.CeptaTransportID.newBuilder().setId(trainId).build())
                    .setStationId(Ids.CeptaStationID.newBuilder().setId("1").build())
                    .setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(1).build()).build())
                    .build()
            ).build();
        }

        
        private static NotificationOuterClass.Notification trainDelayNotificationWithDelay(Long delay){
            return NotificationOuterClass.Notification.newBuilder().setDelay(NotificationOuterClass.DelayNotification.newBuilder()
                    .setCeptaId(Ids.CeptaTransportID.newBuilder().setId("1").build())
                    .setStationId(Ids.CeptaStationID.newBuilder().setId("1").build())
                    .setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(delay).build()).build())
                    .build()
            ).build();
        }


}