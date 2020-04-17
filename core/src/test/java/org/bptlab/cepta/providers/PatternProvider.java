package org.bptlab.cepta.providers;

public class PatternProvider {

    public static LiveTrainData getDefaultLiveTrainDataEvent() {
        long millis = System.currentTimeMillis();
        Timestamp timestamp = Timestamp.newBuilder().setSeconds(millis / 1000)
             .setNanos((int) ((millis % 1000) * 1000000)).build();
        LiveTrainData.Builder builder = LiveTrainData.newBuilder();
        builder.setId(1);
        builder.setTrainSectionId(1);
        builder.setStationId(1);
        builder.setEventTime(timestamp);
        builder.setStatus(1);
        builder.setFirstTrainId(1);
        builder.setTrainId(1);
        builder.setPlannedArrivalTimeEndStation(timestamp);
        builder.setDelay(1);
        builder.setEndStationId(1);
        builder.setImId(1);
        builder.setFollowingImId(1);
        builder.setMessageStatus(1);
        builder.setIngestionTime(timestamp);
        return builder.build();
      }

      

}