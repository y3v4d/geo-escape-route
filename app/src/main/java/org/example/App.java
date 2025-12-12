package org.example;

import org.example.geo.FloodZoneLoader;
import org.example.geo.GeoBuilder;
import org.example.geo.RoadNetworkLoader;
import org.example.graph.GraphBuilder;
import org.example.graph.PathFinder;
import org.example.utils.FileIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mapbox.geojson.Point;

import io.javalin.Javalin;

public class App {
    private final static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        String roadsJson = FileIO.loadResource("roads.geojson");
        String floodZonesJson = FileIO.loadResource("flood_zones.geojson");

        var roadsFC = GeoBuilder.buildFromJSON(roadsJson);
        var floodZonesFC = GeoBuilder.buildFromJSON(floodZonesJson);

        var roadLines = RoadNetworkLoader.loadFromGeoJson(roadsFC);
        var floodZones = FloodZoneLoader.loadFromGeoJSON(floodZonesFC);

        var graph = GraphBuilder.buildGraph(roadLines, floodZones);

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(rule -> {
                    rule.anyHost();
                });
            });
        }).start(3001);

        app.get("/api/evac", ctx -> {
            String startParam = ctx.queryParam("start");
            String endParam = ctx.queryParam("end");
            String fullParam = ctx.queryParam("full");

            if(startParam == null || endParam == null) {
                ctx.status(400).result("Missing 'start' or 'end' query parameters.");
                return;
            }

            Point startPoint, endPoint;
            try {
                startPoint = parsePoint(startParam);
                endPoint = parsePoint(endParam);
            } catch (IllegalArgumentException e) {
                ctx.status(400).result("Invalid coordinate format. Expected 'lon,lat'");
                return;
            }

            var nearestStart = PathFinder.findNearestPoint(graph, startPoint, 0.05);
            var nearestEnd = PathFinder.findNearestPoint(graph, endPoint, 0.05);
            if(nearestStart == null || nearestEnd == null) {
                ctx.status(404).result("No nearby road points found within 50 meters.");
                return;
            }

            var path = PathFinder.findShortestPath(graph, nearestStart, nearestEnd);
            if (path == null) {
                ctx.status(404).result("No path found between the points.");
                return;
            }

            if(fullParam != null) {
                var fullFC = GeoBuilder.buildFinalVisualization(
                    roadLines,
                    floodZones,
                    path.getVertexList()
                );

                ctx.contentType("application/json").result(fullFC.toJson());
            } else {
                var pathFC = GeoBuilder.buildFromGraphPath(path);
                ctx.contentType("application/json").result(pathFC.toJson());
            }
        });

        logger.info("Server started at http://localhost:3001");
    }

    private static Point parsePoint(String coordString) {
        String[] parts = coordString.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid coordinate format. Expected 'lon,lat'");
        }

        double lon = Double.parseDouble(parts[0]);
        double lat = Double.parseDouble(parts[1]);

        return Point.fromLngLat(lon, lat);
    }
}
