package movement.nodegrid;

import core.Settings;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NodeGridSettings {
    private static final String NODE_GRID_MOVEMENT_NS = "NodeGridMovement";
    private static final String RASTER_INTERVAL = "rasterInterval";
    private static final String REFERENCE_LONG = "referenceLong";
    private static final String REFERENCE_LAT = "referenceLat";
    private static final String OSM_FILE = "osmFile";

    private final double rasterInterval;
    private final double referenceLong;
    private final double referenceLat;
    private final Set<String> osmFiles;

    public NodeGridSettings() {
        Settings settings = new Settings(NODE_GRID_MOVEMENT_NS);
        rasterInterval = settings.getDouble(RASTER_INTERVAL, 1);
        referenceLong = settings.getDouble(REFERENCE_LONG, 0);
        referenceLat = settings.getDouble(REFERENCE_LAT, 0);
        osmFiles = readPaths(settings, OSM_FILE);
    }

    public double getRasterInterval() {
        return rasterInterval;
    }

    public double getReferenceLong() {
        return referenceLong;
    }

    public double getReferenceLat() {
        return referenceLat;
    }

    public Set<String> getOsmFiles() {
        return osmFiles;
    }

    private static Set<String> readPaths(Settings settings, String settingName) {
        Set<String> includedPolygons = new HashSet<>();
        for (int current = 1; settings.contains(settingName + current); current++) {
            includedPolygons.add(settings.getSetting(settingName + current));
        }
        return includedPolygons;
    }
}
