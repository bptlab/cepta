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
import org.javatuples.Pair;
import java.util.Comparator;
import java.util.Vector;

public class ContextualCorrelationFunction extends RichFlatMapFunction<LiveTrainDataOuterClass.LiveTrainData, CorrelateableEvent> {

    private StationToCoordinateMap stationToCoordinateMap;

    private int k = 1;

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
        System.out.println("processing " + liveTrainData.getTrainId());
        final CorrelateableEvent uncorrelatedEvent =
                CorrelateableEvent.newBuilder()
                        .setCoordinate(stationToCoordinateMap.get(liveTrainData.getStationId()))
                        .setTimestamp(liveTrainData.getEventTime())
                        .setLiveTrain(liveTrainData)
                    .build();

            Point pointLocation = pointOfEvent(uncorrelatedEvent);
        currentEvents = currentEvents.add(uncorrelatedEvent, pointOfEvent(uncorrelatedEvent));
        System.out.println("pointLocation :" + pointLocation);

        try {
            Vector<Pair<Entry<CorrelateableEvent, Geometry>, Long>> closeEvents = new Vector<>();
            currentEvents.search(pointLocation, 50000)
                    //filter out events that are on the same point as the variable
                    .filter(entry -> { return entry.geometry().distance(pointLocation) != 0;})
                    // map each incoming entry to a pair of the entry and its distance to the uncorrelated event
                    .map(entry -> new Pair<>(entry, distanceOfEvent(entry.value())))
                    .toBlocking()
                    .subscribe(closeEvents::add);

            //sort by distance
            closeEvents.sort(Comparator.comparing(Pair::getValue1));


//                CorrelateableEvent closestEvent = closeEvents.firstElement().getValue0().value();
//                System.out.println("closest Event of LiveTrain:" + liveTrainData + " is: " + closestEvent);
//                collector.collect(closestEvent);


            } catch (Exception e) {
                e.printStackTrace();
            }

            collector.collect(null);

    }

    private long distanceOfEvent(CorrelateableEvent event){
        return 0;
    }

    private Point pointOfEvent(CorrelateableEvent event){
        Coordinate coordinate = event.getCoordinate();
        return Geometries.pointGeographic(coordinate.getLongitude(), coordinate.getLatitude());
    }

    private Vector<CorrelateableEvent> correlatedEventsOf(CorrelateableEvent newEvent){
        return null;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = Math.max(k, 1);
    }
}
