package movement.nodegrid;

import core.Settings;

import java.util.*;

public class NodeGridMovementModelSettings {
    private static final String NODE_GRID_NS = "NodeGridMovementModel";
    private static final String RASTER_INTERVAL = "rasterInterval";
    private static final String OSM_LEVEL_FILE = "osmLevel";
    private static final String PROJECTION_LATITUDE = "projectionLatitude";

    private final double rasterInterval;
    private final double projectionLatitude;
    private final List<String> osmLevelFiles;

    public NodeGridMovementModelSettings() {
        Settings settings = new Settings(NODE_GRID_NS);
        rasterInterval = settings.getDouble(RASTER_INTERVAL, 1);
        projectionLatitude = settings.getDouble(PROJECTION_LATITUDE, 0);
        osmLevelFiles = readPaths(settings, OSM_LEVEL_FILE);
    }

    public double getRasterInterval() {
        return rasterInterval;
    }

    public double getProjectionLatitude() {
        return projectionLatitude;
    }

    public List<String> getOsmLevelFiles() {
        return osmLevelFiles;
    }

    private static List<String> readPaths(Settings settings, String settingName) {
        List<String> includedPolygons = new ArrayList<>();
        for (int current = 0; settings.contains(settingName + current); current++) {
            includedPolygons.add(settings.getSetting(settingName + current));
        }
        return includedPolygons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeGridMovementModelSettings that = (NodeGridMovementModelSettings) o;
        return Double.compare(that.getRasterInterval(), getRasterInterval()) == 0 &&
                Double.compare(that.getProjectionLatitude(), getProjectionLatitude()) == 0 &&
                getOsmLevelFiles().equals(that.getOsmLevelFiles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRasterInterval(), getProjectionLatitude(), getOsmLevelFiles());
    }
}
