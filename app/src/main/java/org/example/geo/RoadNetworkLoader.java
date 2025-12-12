package org.example.geo;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;

/**
 * Utility class for loading road networks
 */
public class RoadNetworkLoader {
    /**
     * Load road lines from a GeoJSON FeatureCollection. Handles LineString geometries.
     * @param fc the FeatureCollection
     * @return the list of road LineStrings
     */
    public static List<LineString> loadFromGeoJson(FeatureCollection fc) {
        List<LineString> roadLines = new ArrayList<>();

        for(var feature : fc.features()) {
            var geometry = feature.geometry();

            if(geometry instanceof LineString lineString) {
                roadLines.add(lineString);
            }
        }

        return roadLines;
    }
}
