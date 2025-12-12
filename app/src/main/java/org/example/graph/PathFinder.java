package org.example.graph;

import org.example.utils.GeoMath;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.mapbox.geojson.Point;

/**
 * Utility class for finding paths in a graph
 */
public class PathFinder {
    /**
     * Find the nearest point in the graph to the target point within the specified maximum distance
     * @param graph graph containing points as vertices
     * @param target the target point
     * @param maxDistanceKm maximum distance in kilometers
     * @return the nearest point in the graph within the maximum distance, or null if none found
     */
    public static Point findNearestPoint(
        Graph<Point, ?> graph,
        Point target,
        double maxDistanceKm
    ) {
        Point nearestPoint = null;
        double nearestDistance = Double.MAX_VALUE;

        for(Point vertex : graph.vertexSet()) {
            double distance = GeoMath.haversine(vertex, target);

            if(distance < nearestDistance && distance <= maxDistanceKm) {
                nearestDistance = distance;
                nearestPoint = vertex;
            }
        }

        return nearestPoint;
    }

    /**
     * Find the shortest path between two points in the graph
     * @param graph the graph
     * @param start the starting point
     * @param end the ending point
     * @return the shortest path between the two points, or null if no path exists
     */
    public static GraphPath<Point, DefaultWeightedEdge> findShortestPath(
        Graph<Point, DefaultWeightedEdge> graph,
        Point start,
        Point end
    ) {
        var dijkstraAlg = new DijkstraShortestPath<>(graph);

        return dijkstraAlg.getPath(start, end);
    }
}
