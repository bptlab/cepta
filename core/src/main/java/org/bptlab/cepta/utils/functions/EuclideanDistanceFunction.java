package org.bptlab.cepta.utils.functions;

import com.github.davidmoten.grumpy.core.Position;
import com.google.protobuf.Duration;
import com.google.protobuf.util.Timestamps;
import org.bptlab.cepta.models.internal.correlateable_event.CorrelateableEventOuterClass.CorrelateableEvent;
import org.bptlab.cepta.models.internal.types.coordinate.CoordinateOuterClass;

import javax.vecmath.Vector3d;
import java.util.Vector;

public class EuclideanDistanceFunction implements DistanceFunction {

    private double timeWeight = 1;
    private double distanceWeight = 1;
    private double directionWeight = 1;

    private Long maxDistance = 100L;
    private Duration maxTimespan = Duration.newBuilder().setSeconds(43200).build();

    public double distanceBetween(CorrelateableEvent eventA, CorrelateableEvent eventB){
        /*
        we calculate the knn-distance between two events over the timespan,
        spacial distance and the angle the train would drive using the
        euclidean distance

        we also normalize these events according their maximum values, so that they are in [0,1].
         */

        Vector<Double> features = new Vector<>();
        features.add(this.timeWeight *
                (double) Timestamps.between(
                        eventA.getTimestamp(),
                        eventB.getTimestamp())
                        .getSeconds() / this.maxTimespan.getSeconds());
        features.add(this.distanceWeight * beelineBetween(eventA, eventB)/this.maxDistance);;

        if (eventB.getCorrelatedEvent() != null) {
            //if they are in a straight line they dont have much distance
            features.add(this.directionWeight * angleBetween(eventA, eventB, eventA.getCorrelatedEvent()) / Math.PI);
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

    public Vector3d coordinateToXYZ(CoordinateOuterClass.Coordinate coordinate){
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

    public EuclideanDistanceFunction setTimeWeight(double timeWeight) {
        this.timeWeight = timeWeight;
        return this;
    }

    public EuclideanDistanceFunction setDistanceWeight(double distanceWeight) {
        this.distanceWeight = distanceWeight;
        return this;
    }

    public EuclideanDistanceFunction setDirectionWeight(double directionWeight) {
        this.directionWeight = directionWeight;
        return this;
    }

    /**
     * @param maxDistance in km
     */
    public DistanceFunction setMaxDistance(Number maxDistance) {
        this.maxDistance = Math.max(maxDistance.longValue(), 0);
        return this;
    }

    /**
     * @param maxTimespan in seconds
     */
    public DistanceFunction setMaxTimespan(Duration maxTimespan) {
        this.maxTimespan = maxTimespan;
        return this;
    }
}
