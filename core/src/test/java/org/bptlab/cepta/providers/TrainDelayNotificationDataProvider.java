package org.bptlab.cepta.providers;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bptlab.cepta.models.events.train.TrainDelayNotificationProtos.TrainDelayNotification;


public class TrainDelayNotificationDataProvider {

        public static TrainDelayNotification getDefaultLTrainDelayNotificationDataEvent() {
        TrainDelayNotification.Builder builder = TrainDelayNotification.newBuilder();
          builder.setTrainId(1);
          builder.setLocationId(1);
          builder.setDelay(1);
          return builder.build();
        }

      
        public static DataStream<TrainDelayNotification> TrainDelayNotificationDataStream(){
          StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
          env.setParallelism(1);
      
          TrainDelayNotification ele1 = trainDelayNotificationWithLocationIdWithTrainIdWithDelay(1,1,Double.valueOf(10));
          TrainDelayNotification ele2 = trainDelayNotificationWithLocationIdWithTrainIdWithDelay(2,2,Double.valueOf(5));
          TrainDelayNotification ele3 = trainDelayNotificationWithLocationIdWithTrainIdWithDelay(1,2,Double.valueOf(15));
          TrainDelayNotification ele4 = trainDelayNotificationWithLocationIdWithTrainIdWithDelay(2,1,Double.valueOf(8));
      
          DataStream<TrainDelayNotification> trainDelayNotificationStream = env.fromElements(ele1, ele2, ele3, ele4);
      
          return trainDelayNotificationStream;
        }
      

        private static TrainDelayNotification trainDelayNotificationWithLocationIdWithTrainIdWithDelay(int locationId, int trainId, Double delay){
          return TrainDelayNotificationDataProvider.getDefaultLTrainDelayNotificationDataEvent().toBuilder()
              .setLocationId(locationId).setTrainId(trainId).setDelay(delay).build();
        }

        private static TrainDelayNotification trainDelayNotificationWithLocationIdWithTrainId(int locationId, int trainId){
          return TrainDelayNotificationDataProvider.getDefaultLTrainDelayNotificationDataEvent().toBuilder()
              .setLocationId(locationId).setTrainId(trainId).build();
        }

        private static TrainDelayNotification trainDelayNotificationWithLocationId(int locationId){
          return TrainDelayNotificationDataProvider.getDefaultLTrainDelayNotificationDataEvent().toBuilder()
              .setLocationId(locationId).build();
        }

        private static TrainDelayNotification trainDelayNotificationWithTrainId(int trainId){
          return TrainDelayNotificationDataProvider.getDefaultLTrainDelayNotificationDataEvent().toBuilder()
              .setTrainId(trainId).build();
        }

        
        private static TrainDelayNotification trainDelayNotificationWithDelay(Double delay){
          return TrainDelayNotificationDataProvider.getDefaultLTrainDelayNotificationDataEvent().toBuilder()
              .setDelay(delay).build();
        }


}