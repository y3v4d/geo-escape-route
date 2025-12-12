package org.example.geo;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.MultiPolygon;
import com.mapbox.geojson.Polygon;

/**
 * Utility class for loading flood zones
 */
public class FloodZoneLoader {
    /**
     * Load flood zones from a GeoJSON FeatureCollection. Handles both Polygon and MultiPolygon geometries.
     * @param fc the FeatureCollection
     * @return list of flood zone polygons
     */
    public static List<Polygon> loadFromGeoJSON(FeatureCollection fc) {
        List<Polygon> zones = new ArrayList<>();

        for(var feature : fc.features()) {
            var geometry = feature.geometry();

            if(geometry instanceof Polygon polygon) {
                zones.add(polygon);
            } else if(geometry instanceof MultiPolygon multiPolygon) {
                zones.addAll(multiPolygon.polygons());
            }
        }

        return zones;
    }
}
