package org.bptlab.cepta.providers;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.models.internal.types.ids.Ids;
import org.bptlab.cepta.models.internal.delay.DelayOuterClass;
import com.google.protobuf.Duration;
import org.bptlab.cepta.utils.notification.NotificationHelper;


public class TrainDelayNotificationDataProvider {

        public static NotificationOuterClass.Notification getDefaultLTrainDelayNotificationDataEvent() {
            return NotificationOuterClass.Notification.newBuilder().setDelay(NotificationOuterClass.DelayNotification.newBuilder()
                    .setTransportId(Ids.CeptaTransportID.newBuilder().setId("1").build())
                    .setStationId(Ids.CeptaStationID.newBuilder().setId("1").build())
                    .setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(1).build()).build())
                    .build()
            ).build();
        }
      
        public static DataStream<NotificationOuterClass.DelayNotification> TrainDelayNotificationDataStream(){
          StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
          env.setParallelism(1);

            NotificationOuterClass.DelayNotification ele1 = trainDelayNotificationWithLocationIdWithTrainIdWithDelay("1","1",10L);
            NotificationOuterClass.DelayNotification ele2 = trainDelayNotificationWithLocationIdWithTrainIdWithDelay("2","2",5L);
            NotificationOuterClass.DelayNotification ele3 = trainDelayNotificationWithLocationIdWithTrainIdWithDelay("1","2",15L);
            NotificationOuterClass.DelayNotification ele4 = trainDelayNotificationWithLocationIdWithTrainIdWithDelay("2","1",8L);
      
          DataStream<NotificationOuterClass.DelayNotification> trainDelayNotificationStream = env.fromElements(ele1, ele2, ele3, ele4);
      
          return trainDelayNotificationStream;
        }

        //TODO make own Provider
        public static DataStream<NotificationOuterClass.Notification> NotificationDataStream(){
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.setParallelism(1);

            NotificationOuterClass.Notification ele1 = NotificationHelper.getTrainDelayNotificationFrom("1",10L,1);
            NotificationOuterClass.Notification ele2 = NotificationHelper.getTrainDelayNotificationFrom("2",5L,2);
            NotificationOuterClass.Notification ele3 = NotificationHelper.getTrainDelayNotificationFrom("2",15L,1);
            NotificationOuterClass.Notification ele4 = NotificationHelper.getTrainDelayNotificationFrom("1",8L,2);

            DataStream<NotificationOuterClass.Notification> notificationStream = env.fromElements(ele1, ele2, ele3, ele4);

            return notificationStream;
        }


        private static NotificationOuterClass.DelayNotification trainDelayNotificationWithLocationIdWithTrainIdWithDelay(String locationId, String trainId, Long delay){
            return NotificationOuterClass.DelayNotification.newBuilder()
                    .setTransportId(Ids.CeptaTransportID.newBuilder().setId(trainId).build())
                    .setStationId(Ids.CeptaStationID.newBuilder().setId(locationId).build())
                    .setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(delay).build()).build())
                    .build();
        }

        private static NotificationOuterClass.DelayNotification trainDelayNotificationWithLocationIdWithTrainId(String locationId, String trainId){
            return NotificationOuterClass.DelayNotification.newBuilder()
                    .setTransportId(Ids.CeptaTransportID.newBuilder().setId(trainId).build())
                    .setStationId(Ids.CeptaStationID.newBuilder().setId(locationId).build())
                    .setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(1).build()).build())
                    .build();
        }

        private static NotificationOuterClass.DelayNotification trainDelayNotificationWithLocationId(String locationId){
            return NotificationOuterClass.DelayNotification.newBuilder()
                    .setTransportId(Ids.CeptaTransportID.newBuilder().setId("1").build())
                    .setStationId(Ids.CeptaStationID.newBuilder().setId(locationId).build())
                    .setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(1).build()).build())
                    .build();
        }

        private static NotificationOuterClass.DelayNotification trainDelayNotificationWithTrainId(String trainId){
            return NotificationOuterClass.DelayNotification.newBuilder()
                    .setTransportId(Ids.CeptaTransportID.newBuilder().setId(trainId).build())
                    .setStationId(Ids.CeptaStationID.newBuilder().setId("1").build())
                    .setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(1).build()).build())
                    .build();
        }

        
        private static NotificationOuterClass.DelayNotification trainDelayNotificationWithDelay(Long delay){
            return NotificationOuterClass.DelayNotification.newBuilder()
                    .setTransportId(Ids.CeptaTransportID.newBuilder().setId("1").build())
                    .setStationId(Ids.CeptaStationID.newBuilder().setId("1").build())
                    .setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(delay).build()).build())
                    .build();
        }
}