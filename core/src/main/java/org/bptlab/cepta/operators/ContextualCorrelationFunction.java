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
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.bptlab.cepta.models.internal.correlateable_event.CorrelateableEventOuterClass.*;
import org.bptlab.cepta.models.internal.types.coordinate.CoordinateOuterClass.*;
import org.bptlab.cepta.models.internal.types.ids.Ids;
import org.javatuples.Pair;

import javax.vecmath.Vector3d;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ContextualCorrelationFunction extends RichFlatMapFunction<CorrelateableEvent, CorrelateableEvent> {

    private int k = 1;
    private Long maxDistance = 100L;
    private Duration maxTimespan = Duration.newBuilder().setSeconds(7200).build();
    private static RTree<CorrelateableEvent, Geometry> currentEvents = RTree.create();

    /**
     * This is the method that gets called for every incoming to-be-correlated Event
     * @param event
     * @param collector
     * @throws Exception
     */
    @Override
    public void flatMap(CorrelateableEvent event, Collector<CorrelateableEvent> collector) throws Exception {
//        System.out.println("processing " + liveTrainData.getTrainId());

            Point pointLocation = pointOfEvent(event);
        System.out.println("pointLocation :" + pointLocation);

        Vector<Pair<Entry<CorrelateableEvent, Geometry>, Double>> closeEvents = new Vector<>();
        CorrelateableEvent finalEvent = event;
        currentEvents.search(pointLocation, maxDistance, (a, b) -> {
                    Position positionA = Position.create(a.mbr().y1(),a.mbr().x1());
                    Position positionB = Position.create(b.mbr().y1(),b.mbr().x1());
                    return positionA.getDistanceToKm(positionB);
                })
                // filter out events that are not within the timewindow specified by maxTimespan
                .filter(entry -> Durations.compare(
                        maxTimespan,
                        Timestamps.between(
                                entry.value().getTimestamp(),
                                finalEvent.getTimestamp()))
                        >= 0)
                // map each incoming entry to a pair of the entry and its knn-distance to the uncorrelated event
                .map(entry ->
                        new Pair<>(
                                entry,
                                euclideanDistance(entry.value(), finalEvent)
                        )
                )
                .toBlocking()
                .subscribe(closeEvents::add);

        if (closeEvents.size() == 0) {
            //there were no close events, so we assume that this must be a new train
            System.out.println("No close events found, creating new ID");
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
                //we only look at the nearest event, correlate to that and delete the previous one
                CorrelateableEvent closestEvent = closeEvents.firstElement().getValue0().value();
                event.toBuilder().setCorrelatedEvent(closestEvent);

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
        collector.collect(event);
    }

    private Point pointOfEvent(CorrelateableEvent event){
        Coordinate coordinate = event.getCoordinate();
        return Geometries.pointGeographic(coordinate.getLongitude(), coordinate.getLatitude());
    }

    private double euclideanDistance(Collection<Number> features){
        Double sumOfSquared = 0.0;
        for (Number feature : features) {
            sumOfSquared += Math.pow(feature.doubleValue(), 2.0);
        }
        return Math.sqrt(sumOfSquared);
    }

    private double euclideanDistance(CorrelateableEvent eventA, CorrelateableEvent eventB){
        /*
        we calculate the knn-distance between two events over the timespan,
        spacial distance and the angle the train would drive

        we also normalize these events according their maximum values, so that they are in [0,1].
         */

        Vector<Double> features = new Vector<Double>();
        features.add((double) Timestamps.between(
                eventA.getTimestamp(),
                eventB.getTimestamp())
                .getSeconds() / this.maxTimespan.getSeconds());
        features.add(beelineBetween(eventA, eventB)/this.maxDistance);;

        if (eventB.getCorrelatedEvent() != null) {
            //if they are in a straight line they dont have much distance
            features.add(angleBetween(eventA, eventB, eventA.getCorrelatedEvent()) / Math.PI);
        }
        Double sumOfSquared = 0.0;
        for (Double feature : features) {
            sumOfSquared += Math.pow(feature, 2.0);
        }
        return Math.sqrt(sumOfSquared);
    }

    public double angleBetween (CorrelateableEvent a, CorrelateableEvent b, CorrelateableEvent c){
        /*
        the approach is to map the coordinates to (x,y,z) coordinates on a unit sphere
        then we use the https://en.wikipedia.org/wiki/Law_of_cosines to determine the angle
         */
        Vector3d vecA = coordinateToXYZ(a.getCoordinate());
        Vector3d vecB = coordinateToXYZ(b.getCoordinate());
        Vector3d vecC = coordinateToXYZ(c.getCoordinate());

        Vector3d vecBA = new Vector3d(vecA.x - vecB.x, vecA.y - vecB.y, vecA.z - vecB.z);
        Vector3d vecBC = new Vector3d(vecC.x - vecB.x, vecC.y - vecB.y, vecC.z - vecB.z);

        return  vecBA.angle(vecBC);
    }

    public Vector3d coordinateToXYZ(Coordinate coordinate){
        /*
        taken after https://stackoverflow.com/questions/1185408/converting-from-longitude-latitude-to-cartesian-coordinates

        z axis is between north and south pole
        x axis is between (0,0) and the center
        y axis is between (0,90) and the center
         */

        double latitudeInRad = Math.PI * coordinate.getLatitude() / 180;
        double longitudeInRad = Math.PI * coordinate.getLongitude() / 180;

        double xPos = Math.cos(latitudeInRad) * Math.cos(longitudeInRad);
        double yPos = Math.cos(latitudeInRad) * Math.sin(longitudeInRad);
        double zPos = Math.sin(latitudeInRad);

        return new Vector3d(xPos, yPos, zPos);
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

    /**
     * @param maxDistance in km
     */
    public void setMaxDistance(Long maxDistance) {
        this.maxDistance = Math.max(maxDistance, 0);
    }

    /**
     * @param maxTimespan in seconds
     */
    public void setMaxTimespan(Duration maxTimespan) {
        this.maxTimespan = maxTimespan;
    }

    public Duration getMaxTimespan(){
        return this.maxTimespan;
    }
}
