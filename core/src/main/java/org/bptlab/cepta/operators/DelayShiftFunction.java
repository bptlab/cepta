package org.bptlab.cepta.operators;

import com.github.jasync.sql.db.ConnectionPoolConfigurationBuilder;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.pool.ConnectionPool;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection;
import com.github.jasync.sql.db.RowData;
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder;
import java.lang.Long;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass.LiveTrainData;
import org.bptlab.cepta.models.events.train.PlannedTrainDataOuterClass.PlannedTrainData;
import org.bptlab.cepta.config.PostgresConfig;
import org.bptlab.cepta.models.internal.notifications.notification.NotificationOuterClass;
import org.bptlab.cepta.utils.converters.PlannedTrainDataDatabaseConverter;
import org.bptlab.cepta.utils.notification.NotificationHelper;

/*Depricated: Makes very basic Shift of Delays on upcoming Stations of the Same Day*/
public class DelayShiftFunction extends
    RichAsyncFunction<LiveTrainData, NotificationOuterClass.Notification> {
    /**
     * This function can be applied to a stream of LiveTrainData.
     * For every event it creates TrainDelayNotifications shifting its delay to the following stations
     * by executing queries to a database with PlannedTrainData
     */

    private PostgresConfig postgresConfig = new PostgresConfig();
    private transient ConnectionPool<PostgreSQLConnection> connection;
    private long delayThreshold = 60;

    @Deprecated
    public DelayShiftFunction(PostgresConfig postgresConfig) {
        this.postgresConfig = postgresConfig;
    }
    @Deprecated
    public DelayShiftFunction(PostgresConfig postgresConfig, long delayThreshold) {
        this.postgresConfig = postgresConfig;
        this.delayThreshold = delayThreshold;
    }

    public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
        /*
        the Configuration class must be from flink, it will give errors when jasync's Configuration is taken
        open should be called before methods like map() or join() are executed
        this must be set to transient, as flink will otherwise try to serialize it which it is not
        */
        super.open(parameters);
        ConnectionPoolConfigurationBuilder config = new ConnectionPoolConfigurationBuilder();
        config.setUsername(postgresConfig.getUser());
        config.setPassword(postgresConfig.getPassword());
        config.setHost(postgresConfig.getHost());
        config.setPort(postgresConfig.getPort());
        config.setDatabase(postgresConfig.getName());
        // Having the same maximum amount of connections as concurrent asynchronous requests seems to work
        config.setMaxActiveConnections(12);
        try{
        connection = PostgreSQLConnectionBuilder.createConnectionPool(config);
        }catch(Error e){
        e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        connection.disconnect();
    }

    @Override
    public void asyncInvoke(LiveTrainData liveEvent,
        final ResultFuture<NotificationOuterClass.Notification> resultFuture) throws Exception {

        /*
        asyncInvoke will be called for each incoming live train element
        the resultFuture is where the outgoing delay notification elements are collected to
        */
        java.sql.Timestamp liveEventTimeSql = protoTimestampToSqlTimestamp(liveEvent.getEventTime());
        String query = String
            .format("select * from public.planned where train_section_id = %d"
                + " and date(planned_event_time) = date('%s')",
                liveEvent.getTrainSectionId(), liveEventTimeSql);
        final CompletableFuture<QueryResult> future = connection.sendPreparedStatement(query);
        
        /*
        We create a new CompletableFuture which will be automatically and asynchronously computed with the value
        from the supplier.
        */
        CompletableFuture.supplyAsync(new Supplier<ArrayList<PlannedTrainData>>() {
            // this gets the matching PlannedTrainDatas and supplies them for the CompletableFuture
            @Override
            public ArrayList<PlannedTrainData> get() {
                ArrayList<PlannedTrainData> plannedTrainData = new ArrayList<PlannedTrainData>();
                try {
                    QueryResult queryResult = future.get();
                    for(RowData row : queryResult.getRows()){
                        plannedTrainData.add(new PlannedTrainDataDatabaseConverter().fromRowData(row));
                    }
                    return plannedTrainData;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }).thenAccept((ArrayList<PlannedTrainData> plannedTrainData) -> {
            /*
            After the CompletableFuture is completed, the .thenAccept call will be made with the value from the CompletableFuture.
            We use the supplied PlannedTrainDatas to build corresponding TrainDelayNotifications.
            */
            PlannedTrainData referencePlannedData = PlannedTrainData.newBuilder().build();
            ArrayList<NotificationOuterClass.Notification> delays = new ArrayList<>();
            for (PlannedTrainData planned : plannedTrainData){
                // find matching planned data
                if (planned.getStationId() == liveEvent.getStationId()){
                    referencePlannedData = planned;
                    break;
                }
            }
            //only send events if there is a reference station found
            if (referencePlannedData.getStationId() == liveEvent.getStationId()) {
                long delay = liveEvent.getEventTime().getSeconds() - referencePlannedData.getPlannedEventTime().getSeconds();
                for (PlannedTrainData planned : plannedTrainData){
                    // only consider the stations beyond the current Station (including)
                    if (compareProtoTimestamps(planned.getPlannedEventTime(), referencePlannedData.getPlannedEventTime()) >= 0){
                        //only send beyond the threshold
                        if (Math.abs(delay)>=delayThreshold){
                            delays.add(NotificationHelper.getTrainDelayNotificationFrom(
                                    String.valueOf(planned.getTrainSectionId()),
                                    delay,
                                    "DelayShift from Station: "+liveEvent.getStationId(),
                                    planned.getStationId())
                            );
                        }
                    }
                }
            }
            resultFuture.complete(delays);
        });
        
    }

    private java.sql.Timestamp protoTimestampToSqlTimestamp(com.google.protobuf.Timestamp protoTimestamp){
        long seconds = protoTimestamp.getSeconds();
        // convert to milliseconds for the sql timestamp's construction
        java.sql.Timestamp timestamp = new java.sql.Timestamp(seconds*1000);
        return timestamp;
    }

    private int compareProtoTimestamps(com.google.protobuf.Timestamp t1, com.google.protobuf.Timestamp t2){
        //  1 ; t1 > t2 : t1 is after t2
        // -1 ; t1 < t2 : t1 is before t2
        //  0 ; t1 = t2 : t1 and t2 are at the same time
        Long l = t1.getSeconds();
        return l.compareTo(t2.getSeconds());
    }
}