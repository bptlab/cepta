package org.bptlab.cepta.operators;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;
import com.google.protobuf.util.Timestamps;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.config.MongoConfig;
import org.bptlab.cepta.models.events.train.LiveTrainDataOuterClass;
import org.bptlab.cepta.models.internal.correlateable_event.CorrelateableEventOuterClass.*;
import org.bptlab.cepta.models.internal.types.coordinate.CoordinateOuterClass.*;
import org.bptlab.cepta.models.internal.types.ids.Ids;
import org.javatuples.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
//        System.out.println("processing " + liveTrainData.getTrainId());
        CorrelateableEvent uncorrelatedEvent =
                CorrelateableEvent.newBuilder()
                        .setCoordinate(stationToCoordinateMap.get(liveTrainData.getStationId()))
                        .setTimestamp(liveTrainData.getEventTime())
                        .setLiveTrain(liveTrainData)
                    .build();

            Point pointLocation = pointOfEvent(uncorrelatedEvent);
//        System.out.println("pointLocation :" + pointLocation);

        Vector<Pair<Entry<CorrelateableEvent, Geometry>, Double>> closeEvents = new Vector<>();
        currentEvents.search(pointLocation, 50, (a, b) -> {
                    Position positionA = Position.create(a.mbr().y1(),a.mbr().x1());
                    Position positionB = Position.create(b.mbr().y1(),b.mbr().x1());
                    return positionA.getDistanceToKm(positionB);
                })
                //filter out events that are on the same point as the variable
//                .filter(entry -> entry.geometry().distance(pointLocation) != 0)
                // map each incoming entry to a pair of the entry and its distance to the uncorrelated event
                .map(entry ->
                        new Pair<>(
                                entry,
                                euclideanDistance(entry.value(), uncorrelatedEvent)
                        )
                )
                .toBlocking()
                .subscribe(closeEvents::add);

        CorrelateableEvent correlatedEvent =
                uncorrelatedEvent
                    .toBuilder()
                    .setCeptaId(
                            Ids.CeptaTransportID
                                    .newBuilder()
                                    .setId("NOT YET SET")
                                    .build()
                    )
                    .build();
        if (closeEvents.size() == 0) {
            //there were no close events, so we assume that this must be a new train
            correlatedEvent =
                    correlatedEvent
                        .toBuilder()
                            .setCeptaId(
                                    Ids.CeptaTransportID
                                            .newBuilder()
                                            .setId(String.valueOf(uncorrelatedEvent.getLiveTrain().getTrainSectionId()))
                                            .build()
                            )
                        .build();
        } else {
            //sort by distance
            closeEvents.sort(Comparator.comparing(Pair::getValue1));
            //we now want to only get the first k events, so we resize the vector to k. If it was smaller than k its filled up with null
            //so we remove those in the second step
            closeEvents.setSize(k);
            closeEvents.removeIf(Objects::isNull);

            if (k == 1){
                //we only look at the nearest event, correlate to that and delete the previous one
                CorrelateableEvent closestEvent = closeEvents.firstElement().getValue0().value();
                uncorrelatedEvent.toBuilder().setCorrelatedEvent(closestEvent);

                //delete that event from our RTree
                currentEvents.delete(closestEvent, pointOfEvent(closestEvent));

            } else {
                //now we just look for the ID that is most common under those k events
                Hashtable<Ids.CeptaTransportID, Integer> countOfIDs = new Hashtable<>();
                closeEvents.forEach(entry -> {
                    Ids.CeptaTransportID currentID = entry.getValue0().value().getCeptaId();
                    //increase the value by 1, if it is not yet assigned put 1
                    countOfIDs.merge(currentID, 1, Integer::sum);
                });

                AtomicReference<Ids.CeptaTransportID> mostCommonID = new AtomicReference<>();
                AtomicInteger mostCommonIdCount = new AtomicInteger(0);
                try {
                    countOfIDs.forEach((id, count) -> {
                        if (count > mostCommonIdCount.get()) {
                            mostCommonIdCount.set(count + 1);
                            mostCommonID.set(id);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                correlatedEvent =
                        uncorrelatedEvent
                                .toBuilder()
                                .setCeptaId(mostCommonID.get())
                                .build();
            }
            }

        currentEvents = currentEvents.add(correlatedEvent, pointOfEvent(uncorrelatedEvent));
        collector.collect(correlatedEvent);
    }

    private Point pointOfEvent(CorrelateableEvent event){
        Coordinate coordinate = event.getCoordinate();
        return Geometries.pointGeographic(coordinate.getLongitude(), coordinate.getLatitude());
    }

    private double euclideanDistance(CorrelateableEvent eventA, CorrelateableEvent eventB){
        Vector<Double> features = new Vector<Double>();
        features.add((double) Timestamps.between(
                eventA.getTimestamp(),
                eventB.getTimestamp())
                .getSeconds());
        features.add(beelineBetween(eventA, eventB));;

        Double sumOfSquared = 0.0;
        for (Double feature : features) {
            sumOfSquared += Math.pow(feature, 2.0);
        }
        return Math.sqrt(sumOfSquared);
    }

    private double beelineBetween(CorrelateableEvent a, CorrelateableEvent b){
        Position positionA = Position.create(a.getCoordinate().getLatitude(),a.getCoordinate().getLongitude());
        Position positionB = Position.create(b.getCoordinate().getLatitude(),b.getCoordinate().getLongitude());
        return positionA.getDistanceToKm(positionB);
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = Math.max(k, 1);
    }
}
