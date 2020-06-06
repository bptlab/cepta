package org.bptlab.cepta.operators;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass;
import org.bptlab.cepta.models.internal.correlateable_event.CorrelateableEventOuterClass.*;
import org.bptlab.cepta.models.internal.types.coordinate.CoordinateOuterClass.*;
import org.bptlab.cepta.models.internal.types.ids.Ids;

import java.util.List;
import java.util.Vector;

public class ContextualCorrelationFunction extends RichFlatMapFunction<LiveTrainDataOuterClass.LiveTrainData, CorrelateableEvent> {

    private StationToCoordinateMap stationToCoordinateMap;

    private static RTree<CorrelateableEvent, Geometry> currentEvents = RTree.create();

    public ContextualCorrelationFunction(String datebaseName, String tableName, MongoConfig mongoConfig) {
        this.stationToCoordinateMap = new StationToCoordinateMap(datebaseName, tableName, mongoConfig);
    }

    public StationToCoordinateMap getMapping(){ return stationToCoordinateMap;}

    /**
     * This is the method that gets called for every incoming to-be-correlated Event
     * @param liveTrainData
     * @param collector
     * @throws Exception
     */
    @Override
    public void flatMap(LiveTrainDataOuterClass.LiveTrainData liveTrainData, Collector<CorrelateableEvent> collector) throws Exception {
        CorrelateableEvent uncorrelatedEvent =
                CorrelateableEvent.newBuilder()
                        .setCoordinate(stationToCoordinateMap.get(liveTrainData.getStationId()))
                        .setTimestamp(liveTrainData.getEventTime())
                        .setLiveTrain(liveTrainData)
                    .build();
        uncorrelatedEvent = uncorrelatedEvent.toBuilder()
                    .setCeptaId(
                            Ids.CeptaTransportID.newBuilder()
                                    .setId(String.valueOf(uncorrelatedEvent.getLiveTrain().getTrainSectionId()))
                                    .build())
                    .build();

            Point pointLocation = pointOfEvent(uncorrelatedEvent);

            List<Entry<CorrelateableEvent, Geometry>> closeEvents = currentEvents.search(pointLocation, 5).toList().toBlocking().single();

            currentEvents = currentEvents.add(uncorrelatedEvent, pointOfEvent(uncorrelatedEvent));
            collector.collect(uncorrelatedEvent);

    }

    private Point pointOfEvent(CorrelateableEvent event){
        Coordinate coordinate = event.getCoordinate();
        return Geometries.pointGeographic(coordinate.getLongitude(), coordinate.getLatitude());
    }

    private Vector<CorrelateableEvent> correlatedEventsOf(CorrelateableEvent newEvent){
        return null;
    }
}
