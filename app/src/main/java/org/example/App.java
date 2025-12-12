package org.example;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.example.geo.FloodZoneLoader;
import org.example.geo.GeoBuilder;
import org.example.geo.RoadNetworkLoader;
import org.example.graph.GraphBuilder;
import org.example.graph.PathFinder;
import org.example.utils.FileIO;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

public class App {
    public static void main(String[] args) throws Exception {
        double startLon = 21.7643873;
        double startLat = 49.6833371;
        double endLon = 21.7602742;
        double endLat = 49.6853010;

        Point startPoint = Point.fromLngLat(startLon, startLat);
        Point endPoint = Point.fromLngLat(endLon, endLat);

        String roadsJson = FileIO.loadResource("roads.geojson");
        String floodZonesJson = FileIO.loadResource("flood_zones.geojson");

        var roadsFC = GeoBuilder.buildFromJSON(roadsJson);
        var floodZonesFC = GeoBuilder.buildFromJSON(floodZonesJson);

        var roadLines = RoadNetworkLoader.loadFromGeoJson(roadsFC);
        var floodZones = FloodZoneLoader.loadFromGeoJSON(floodZonesFC);

        var graph = GraphBuilder.buildGraph(roadLines, floodZones);

        var nearestStart = PathFinder.findNearestPoint(graph, startPoint, 0.05);
        var nearestEnd = PathFinder.findNearestPoint(graph, endPoint, 0.05);

        var path = PathFinder.findShortestPath(graph, nearestStart, nearestEnd);
        if (path != null) {
            System.out.println("Shortest path distance: " + path.getWeight() + " km");
            System.out.println("Shortest path: " + path.getVertexList());

            var finalVisualization = GeoBuilder.buildFinalVisualization(
                roadLines,
                floodZones,
                path.getVertexList()
            );

            String url = generateGeojsonIOURL(finalVisualization);
            openWithBrowser(url);
        } else {
            System.out.println("No path found between the points.");
        }
    }

    private static String generateGeojsonIOURL(FeatureCollection featureCollection) {
        String encodedGeojson = URLEncoder.encode(featureCollection.toJson(), StandardCharsets.UTF_8);
        String url = "http://geojson.io/#data=data:application/json," + encodedGeojson;

        return url;
    }

    private static void openWithBrowser(String url) throws Exception {
        if (java.awt.Desktop.isDesktopSupported()) {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            desktop.browse(new java.net.URI(url));
        } else {
            System.out.println("Desktop is not supported.");
        }
    }
}
