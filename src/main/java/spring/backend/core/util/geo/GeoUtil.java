package spring.backend.core.util.geo;

public class GeoUtil {
    private static final double EARTH_RADIUS = 6371;

    public static double calculateDistanceBetweenTwoCoordinate(double savedLat, double savedLon, double newLat, double newLon) {
        double deltaLatDiff = Math.toRadians(Math.abs(newLat - savedLat));
        double deltaLonDiff = Math.toRadians(Math.abs(newLon - savedLon));

        double sinDeltaLatDiff = Math.sin(deltaLatDiff / 2);
        double sinDeltaLonDiff = Math.sin(deltaLonDiff / 2);

        double squareRoot = Math.sqrt(
                sinDeltaLatDiff * sinDeltaLatDiff
                        + (Math.cos(Math.toRadians(savedLat))
                        * Math.cos(Math.toRadians(newLat))
                        * sinDeltaLonDiff * sinDeltaLonDiff)
        );

        return 2 * EARTH_RADIUS * Math.asin(squareRoot);
    }
}
