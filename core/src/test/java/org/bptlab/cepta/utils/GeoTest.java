package org.bptlab.cepta.test.utils;

import org.bptlab.cepta.utils.IDGenerator;
import org.bptlab.cepta.utils.geo.Geo;
import org.javatuples.Quartet;
import org.junit.Assert;
import org.junit.Test;
import org.javatuples.Triplet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bptlab.cepta.models.internal.types.coordinate.CoordinateOuterClass;

public class GeoTest {

    private CoordinateOuterClass.Coordinate coord(double lat, double lon) {
        return CoordinateOuterClass.Coordinate.newBuilder().setLatitude(lat).setLongitude(lon).build();
    }

    @Test
  public void testDistance() {
        // Values based on https://map.meurisse.org/
        List<Quartet<String, CoordinateOuterClass.Coordinate, CoordinateOuterClass.Coordinate, Double>> data = List.of(
                // London <-> Paris is ~340km
                new Quartet<>("LONDON<->PARIS", coord(51.4882243263235,-0.10986328125), coord(48.8502581997215,2.3291015625), 341220D),
                // Hamburg <-> Munich is ~613km
                new Quartet<>("HAMBURG<->MUNICH",coord(53.5598889724546,9.9755859375), coord(48.1367666796927,11.57958984375), 613738D),
                // Wannsee <-> Griebnitzsee is ~4.6km
                new Quartet<>("WANNSEE<->GRIEBNITZSEE",coord(52.4212405457208,13.1797313690186), coord(52.3944061012646,13.1274604797363), 4644D)
        );

        for (Quartet<String, CoordinateOuterClass.Coordinate, CoordinateOuterClass.Coordinate, Double> sample : data) {
            Geo.Distance dist = new Geo.Distance(sample.getValue1(), sample.getValue2());
            System.out.printf("%s: %f km\n", sample.getValue0(), dist.getBeelineMeters()/1000);
            Assert.assertTrue((Math.abs(dist.getBeelineMeters() - sample.getValue3())) < 500); // Allow for 500m inaccuracy
        }
  }

}