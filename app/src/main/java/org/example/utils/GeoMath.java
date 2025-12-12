package org.example.utils;

import java.util.List;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

/**
 * Utility class for geographical calculations
 */
public class GeoMath {
    /**
     * Calculate the Haversine distance between two points
     * @param p0 The first point
     * @param p1 The second point
     * @return The distance between the two points in kilometers
     */
    public static double haversine(Point p0, Point p1) {
        return haversine(p0.latitude(), p0.longitude(), p1.latitude(), p1.longitude());
    }

    /**
     * Calculate the Haversine distance between two latitude/longitude pairs
     * @param lat0 The latitude of the first point
     * @param lon0 The longitude of the first point
     * @param lat1 The latitude of the second point
     * @param lon1 The longitude of the second point
     * @return The distance between the two points in kilometers
     */
    public static double haversine(double lat0, double lon0, double lat1, double lon1) {
        final double R = 6371; // Radius of the Earth in kilometers

        double latDistance = Math.toRadians(lat1 - lat0);
        double lonDistance = Math.toRadians(lon1 - lon0);

        lat0 = Math.toRadians(lat0);
        lat1 = Math.toRadians(lat1);

        double a = 
            Math.pow(Math.sin(latDistance / 2), 2) +
            Math.pow(Math.sin(lonDistance / 2), 2) *
            Math.cos(lat0) *
            Math.cos(lat1);

        double c = 2 * Math.asin(Math.sqrt(a));

        return R * c;
    }

    /**
     * Check if a line between two points crosses any flood zones
     * @param p0 The starting point of the line
     * @param p1 The ending point of the line
     * @param floodZones The list of flood zone polygons
     * @return True if the line crosses any flood zone, false otherwise
     */
    public static boolean isLineCrossingFloodZone(
        Point p0, Point p1,
        List<Polygon> floodZones
    ) {
        for(Polygon zone : floodZones) {
            if(doesLineIntersectPolygon(p0, p1, zone)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a point is inside any flood zone
     * @param point The point to check
     * @param floodZones The list of flood zone polygons
     * @return True if the point is inside any flood zone, false otherwise
     */
    public static boolean isPointInFloodZone(
        Point point,
        List<Polygon> floodZones
    ) {
        for(Polygon zone : floodZones) {
            if(isPointInPolygon(point, zone)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if two line segments intersect
     * @param p0 The first point of the first line segment
     * @param p1 The second point of the first line segment
     * @param q0 The first point of the second line segment
     * @param q1 The second point of the second line segment
     * @return True if the line segments intersect, false otherwise
     */
    private static boolean doLinesIntersect(
        Point p0, Point p1,
        Point q0, Point q1
    ) {
        double s1_x = p1.longitude() - p0.longitude();
        double s1_y = p1.latitude() - p0.latitude();
        double s2_x = q1.longitude() - q0.longitude();
        double s2_y = q1.latitude() - q0.latitude();

        double s, t;
        double denom = (-s2_x * s1_y + s1_x * s2_y);
        if (denom == 0) {
            return false; // Collinear or parallel
        }

        s = (-s1_y * (p0.longitude() - q0.longitude()) + s1_x * (p0.latitude() - q0.latitude())) / denom;
        t = ( s2_x * (p0.latitude() - q0.latitude()) - s2_y * (p0.longitude() - q0.longitude())) / denom;

        return (s >= 0 && s <= 1 && t >= 0 && t <= 1);
    }

    /**
     * Check if a line segment intersects a polygon
     * @param p0 The first point of the line segment
     * @param p1 The second point of the line segment
     * @param polygon The polygon to check against
     * @return True if the line segment intersects the polygon, false otherwise
     */
    private static boolean doesLineIntersectPolygon(
        Point p0, Point p1,
        Polygon polygon
    ) {
        List<List<Point>> coordinates = polygon.coordinates();
        if (coordinates.isEmpty()) {
            return false;
        }

        List<Point> ring = coordinates.get(0);
        int n = ring.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point q0 = ring.get(j);
            Point q1 = ring.get(i);

            if (doLinesIntersect(p0, p1,  q0, q1)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a point is inside a polygon
     * @param point The point to check
     * @param polygon The polygon to check against
     * @return True if the point is inside the polygon, false otherwise
     */
    private static boolean isPointInPolygon(Point point, Polygon polygon) {
        List<List<Point>> coordinates = polygon.coordinates();
        if (coordinates.isEmpty()) {
            return false;
        }

        List<Point> ring = coordinates.get(0);
        boolean inside = false;
        int n = ring.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = ring.get(i).longitude();
            double yi = ring.get(i).latitude();
            double xj = ring.get(j).longitude();
            double yj = ring.get(j).latitude();

            boolean intersect = 
                ((yi > point.latitude()) != (yj > point.latitude())) &&
                (point.longitude() < (xj - xi) * (point.latitude() - yi) / (yj - yi) + xi);
            
            if (intersect) {
                inside = !inside;
            }
        }

        return inside;
    }
}
