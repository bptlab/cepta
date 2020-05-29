package org.bptlab.cepta.utils.geo;

import org.bptlab.cepta.models.internal.types.coordinate.CoordinateOuterClass;

public class Geo {

    // See https://en.wikipedia.org/wiki/Earth
    public static final int radius = 6371; // Radius of the earth in kilometers
    public static final int elevation = 797;  // mean height of land above sea level in meters

    public static class Distance {

        private double lat1, lat2, lon1, lon2, el1, el2;

        public Distance(double lat1, double lat2, double lon1,
                        double lon2, double el1, double el2) {
            super();
            this.lat1 = lat1;
            this.lat2 = lat2;
            this.lon1 = lon1;
            this.lon2 = lon2;
            this.el1 = el1;
            this.el2 = el2;
        }

        public Distance(double lat1, double lat2, double lon1, double lon2) {
            this(lat1, lat2, lon1, lon2, 0, 0);
        }

        public Distance(CoordinateOuterClass.Coordinate c1, CoordinateOuterClass.Coordinate c2) {
            this(c1.getLatitude(), c2.getLatitude(), c1.getLongitude(), c2.getLongitude(), c1.getAltitude(), c2.getAltitude());
        }

        public double getBeelineMeters() {
            // https://stackoverflow.com/a/16794680
            double latDistance = Math.toRadians(lat2 - lat1);
            double lonDistance = Math.toRadians(lon2 - lon1);
            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = Geo.radius * c * 1000; // convert to meters

            double height = el1 - el2;

            distance = Math.pow(distance, 2) + Math.pow(height, 2);

            return Math.sqrt(distance);
        }
    }
}
