package org.example.graph;

import java.util.List;

import org.example.utils.GeoMath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

/**
 * Utility class for building a graph from road networks and flood zones
 */
public class GraphBuilder {
    /**
     * Build a weighted graph from road lines and flood zones
     * @param roads The list of road line strings
     * @param floodZones The list of flood zone polygons
     * @return A weighted graph representing the road network excluding flood zones
     */
    public static SimpleWeightedGraph<Point, DefaultWeightedEdge> buildGraph(
        List<LineString> roads,
        List<Polygon> floodZones
    ) {
        var graph = new SimpleWeightedGraph<Point, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        for(LineString road : roads) {
            Point prevPoint = null;

            for(Point point : road.coordinates()) {
                if(GeoMath.isPointInFloodZone(point, floodZones)) {
                    prevPoint = null;
                    continue;
                }

                graph.addVertex(point);

                if(prevPoint != null) {
                    if(GeoMath.isLineCrossingFloodZone(prevPoint, point, floodZones)) {
                        prevPoint = point;
                        continue;
                    }

                    DefaultWeightedEdge edge = graph.addEdge(prevPoint, point);
                    double distance = GeoMath.haversine(prevPoint, point);

                    graph.setEdgeWeight(edge, distance);
                }

                prevPoint = point;
            }
        }

        return graph;
    }
}
