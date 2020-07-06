package org.bptlab.cepta.operators;

import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;
import com.google.protobuf.Duration;
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.internal.correlateable_event.CorrelateableEventOuterClass.*;
import org.bptlab.cepta.models.internal.types.coordinate.CoordinateOuterClass.*;
import org.bptlab.cepta.models.internal.types.ids.Ids;
import org.bptlab.cepta.utils.functions.DistanceFunction;
import org.bptlab.cepta.utils.functions.EuclideanDistanceFunction;
import org.javatuples.Pair;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ContextualCorrelationFunction extends RichMapFunction<CorrelateableEvent, CorrelateableEvent> {

    private int k = 1;
    private Long maxDistance = 100L;
    private Duration maxTimespan = Duration.newBuilder().setSeconds(43200).build();
    public static RTree<CorrelateableEvent, Geometry> currentEvents = RTree.create();

    private DistanceFunction distanceFunction = new EuclideanDistanceFunction();

    /**
     * This is the method that gets called for every incoming to-be-correlated Event
     * @param event
     * @throws Exception
     */
    @Override
    public CorrelateableEvent map(CorrelateableEvent event) throws Exception {
        Point pointLocation = pointOfEvent(event);

        Vector<Pair<Entry<CorrelateableEvent, Geometry>, Double>> closeEvents = new Vector<>();
        CorrelateableEvent incomingEvent = event;
        currentEvents.search(pointLocation, maxDistance, (a, b) -> {
                    /*
                    calculate the distance between two events by converting the minimum bounding
                    boxes into a point each, and calculate their distance
                     */
                    Position positionA = Position.create(a.mbr().y1(),a.mbr().x1());
                    Position positionB = Position.create(b.mbr().y1(),b.mbr().x1());
                    return positionA.getDistanceToKm(positionB);
                })
                /*
                only pass events that fit inside the timewindow specified by maxTimespan.
                that window being the time between an event and the incoming event
                 */
                .filter(entry -> Durations.compare(
                        maxTimespan,
                        Timestamps.between(
                                entry.value().getTimestamp(),
                                incomingEvent.getTimestamp()))
                        >= 0)
                // map each incoming entry to a pair of the entry and its knn-distance to the uncorrelated event
                .map(entry ->
                        new Pair<>(
                                entry,
                                distanceFunction.distanceBetween(entry.value(), incomingEvent)
                        )
                )
                .toBlocking()
                .subscribe(closeEvents::add);

        if (closeEvents.size() == 0) {
            //there were no close events, so we assume that this must be a new train
//            System.out.println("No close events found, creating new ID");
            event =
                    event
                        .toBuilder()
                            .setCeptaId(
                                    Ids.CeptaTransportID
                                            .newBuilder()
                                            .setId(event.getLiveTrain().getTrainSectionId() + "@" + event.getTimestamp())
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
                //we only look at the nearest event, correlate to that and delete the previous one from our running trains
                CorrelateableEvent closestEvent = closeEvents.firstElement().getValue0().value();
                event = event.toBuilder().setCorrelatedEvent(closestEvent).build();

                //delete that event from our RTree
                currentEvents = currentEvents.delete(closestEvent, pointOfEvent(closestEvent));
                event =
                        event
                                .toBuilder()
                                .setCeptaId(closestEvent.getCeptaId())
                                .build();
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

                event =
                        event
                                .toBuilder()
                                .setCeptaId(mostCommonID.get())
                                .build();
            }
            }

        currentEvents = currentEvents.add(event, pointOfEvent(event));
        return event;
    }

    private Point pointOfEvent(CorrelateableEvent event){
        Coordinate coordinate = event.getCoordinate();
        return Geometries.pointGeographic(coordinate.getLongitude(), coordinate.getLatitude());
    }

    public void clearData(){
        currentEvents = RTree.create();
    }

    public int getK() {
        return k;
    }

    public ContextualCorrelationFunction setK(int k) {
        this.k = Math.max(k, 1);
        return this;
    }

    public ContextualCorrelationFunction setDistanceFunction(DistanceFunction distanceFunction){
        this.distanceFunction = distanceFunction;
        return this;
    }

    /**
     * @param maxDistance in km
     */
    public ContextualCorrelationFunction setMaxDistance(Long maxDistance) {
        this.maxDistance = Math.max(maxDistance, 0);
        this.distanceFunction.setMaxDistance(maxDistance);
        return this;
    }

    /**
     * @param maxTimespan in seconds
     */
    public ContextualCorrelationFunction setMaxTimespan(Duration maxTimespan) {
        this.maxTimespan = maxTimespan;
        this.distanceFunction.setMaxTimespan(maxTimespan);
        return this;
    }

    public Duration getMaxTimespan(){
        return this.maxTimespan;
    }
}
