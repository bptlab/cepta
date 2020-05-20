package org.bptlab.cepta.utils.notification;


import com.google.protobuf.Duration;
import org.bptlab.cepta.models.internal.delay.DelayOuterClass;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass.Notification;
import org.bptlab.cepta.models.internal.types.coordinate.CoordinateOuterClass;
import org.bptlab.cepta.models.internal.types.ids.Ids;

public class NotificationHelper {
    public static Notification getTrainDelayNotificationFrom(String ceptaID, long delayedSeconds, String delayDetail, long stationId, CoordinateOuterClass.Coordinate coordinate){
        NotificationOuterClass.DelayNotification.Builder delayBuilder = NotificationOuterClass.DelayNotification.newBuilder();
        delayBuilder.setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(delayedSeconds).build()).setDetails(delayDetail).build() );
        delayBuilder.setTransportId(Ids.CeptaTransportID.newBuilder().setId(ceptaID).build());
        delayBuilder.setStationId(Ids.CeptaStationID.newBuilder().setId(String.valueOf(stationId)).build());
        delayBuilder.setCoordinate(coordinate);
        return Notification.newBuilder().setDelay(delayBuilder.build()).build();
    }
    public static Notification getTrainDelayNotificationFrom(Ids.CeptaTransportID ceptaID, DelayOuterClass.Delay delay, Ids.CeptaStationID stationId, CoordinateOuterClass.Coordinate coordinate){
        NotificationOuterClass.DelayNotification.Builder delayBuilder = NotificationOuterClass.DelayNotification.newBuilder();
        delayBuilder.setDelay(delay).build();
        delayBuilder.setTransportId(ceptaID).build();
        delayBuilder.setStationId(stationId).build();
        delayBuilder.setCoordinate(coordinate);
        return Notification.newBuilder().setDelay(delayBuilder.build()).build();
    }

    public static Notification getTrainDelayNotificationFrom( String ceptaID, long delayedSeconds, String delayDetail, long stationId  ){
        NotificationOuterClass.DelayNotification.Builder delayBuilder = NotificationOuterClass.DelayNotification.newBuilder();
        delayBuilder.setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(delayedSeconds).build()).setDetails(delayDetail).build() );
        delayBuilder.setTransportId(Ids.CeptaTransportID.newBuilder().setId(ceptaID).build());
        delayBuilder.setStationId(Ids.CeptaStationID.newBuilder().setId(String.valueOf(stationId)).build());
        return Notification.newBuilder().setDelay(delayBuilder.build()).build();
    }
    public static Notification getTrainDelayNotificationFrom( String ceptaID, long delayedSeconds, long stationId  ){
        NotificationOuterClass.DelayNotification.Builder delayBuilder = NotificationOuterClass.DelayNotification.newBuilder();
        delayBuilder.setDelay(DelayOuterClass.Delay.newBuilder().setDelta(Duration.newBuilder().setSeconds(delayedSeconds).build()).setDetails("").build() );
        delayBuilder.setTransportId(Ids.CeptaTransportID.newBuilder().setId(ceptaID).build());
        delayBuilder.setStationId(Ids.CeptaStationID.newBuilder().setId(String.valueOf(stationId)).build());
        return Notification.newBuilder().setDelay(delayBuilder.build()).build();
    }
}
