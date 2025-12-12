package org.example.geo;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.GraphPath;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

/**
 * Utility class for building GeoJSON features
 */
public class GeoBuilder {
    /**
     * Build a FeatureCollection from a GeoJSON string
     * @param geoJson the GeoJSON string
     * @return the FeatureCollection
     */
    public static FeatureCollection buildFromJSON(String geoJson) {
        return FeatureCollection.fromJson(geoJson);
    }

    /**
     * Build a FeatureCollection from a graph path
     * @param path the graph path
     * @return the FeatureCollection representing the path
     */
    public static FeatureCollection buildFromGraphPath(
        GraphPath<Point, ?> path
    ) {
        LineString lineString = LineString.fromLngLats(path.getVertexList());
        FeatureCollection fc = FeatureCollection.fromFeature(Feature.fromGeometry(lineString));

        return fc;
    }

    /**
     * Build a final visualization FeatureCollection including roads, flood zones, and the escape path
     * @param roads the road lines
     * @param floodZones the flood zone polygons
     * @param path the escape path points
     * @return the FeatureCollection representing the final visualization
     */
    public static FeatureCollection buildFinalVisualization(
        List<LineString> roads,
        List<Polygon> floodZones,
        List<Point> path
    ) {
        List<Feature> features = new ArrayList<>();

        // Add road lines
        for (LineString road : roads) {
            Feature roadFeature = Feature.fromGeometry(road);
            roadFeature.addStringProperty("type", "road");
            roadFeature.addStringProperty("stroke", "#3887be");
            roadFeature.addNumberProperty("stroke-width", 2);
            features.add(roadFeature);
        }

        // Add flood zones
        for (Polygon floodZone : floodZones) {
            Feature floodFeature = Feature.fromGeometry(floodZone);
            floodFeature.addStringProperty("type", "flood_zone");
            floodFeature.addStringProperty("fill", "#e55e5e");
            floodFeature.addNumberProperty("fill-opacity", 0.5);
            floodFeature.addStringProperty("stroke", "#e55e5e");
            floodFeature.addNumberProperty("stroke-width", 1);
            features.add(floodFeature);
        }

        // Add path as a line
        if (path.size() > 1) {
            LineString pathLine = LineString.fromLngLats(path);
            Feature pathFeature = Feature.fromGeometry(pathLine);
            pathFeature.addStringProperty("type", "escape_path");
            pathFeature.addStringProperty("stroke", "#00ff00");
            pathFeature.addNumberProperty("stroke-width", 4);
            features.add(pathFeature);
        }

        // Add starting and ending points
        if (!path.isEmpty()) {
            Feature startPointFeature = Feature.fromGeometry(path.get(0));
            startPointFeature.addStringProperty("type", "start_point");
            startPointFeature.addStringProperty("marker-color", "#00ff00");
            features.add(startPointFeature);

            Feature endPointFeature = Feature.fromGeometry(path.get(path.size() - 1));
            endPointFeature.addStringProperty("type", "end_point");
            endPointFeature.addStringProperty("marker-color", "#ff0000");
            features.add(endPointFeature);
        }

        FeatureCollection collection = FeatureCollection.fromFeatures(features);
        return collection;
    }
}
