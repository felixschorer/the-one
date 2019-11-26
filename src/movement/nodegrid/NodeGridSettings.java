package movement.nodegrid;

import core.Settings;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NodeGridSettings {
    private static final String NODE_GRID_NS = "NodeGrid";
    private static final String RASTER_INTERVAL = "rasterInterval";
    private static final String OSM_LEVEL_FILE = "osmLevel";

    private final double rasterInterval;
    private final Set<String> osmLevelFiles;

    public NodeGridSettings() {
        Settings settings = new Settings(NODE_GRID_NS);
        rasterInterval = settings.getDouble(RASTER_INTERVAL, 1);
        osmLevelFiles = readPaths(settings, OSM_LEVEL_FILE);
    }

    public double getRasterInterval() {
        return rasterInterval;
    }

    public Set<String> getOsmLevelFiles() {
        return osmLevelFiles;
    }

    private static Set<String> readPaths(Settings settings, String settingName) {
        Set<String> includedPolygons = new HashSet<>();
        for (int current = 1; settings.contains(settingName + current); current++) {
            includedPolygons.add(settings.getSetting(settingName + current));
        }
        return includedPolygons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeGridSettings that = (NodeGridSettings) o;
        return Double.compare(that.getRasterInterval(), getRasterInterval()) == 0 &&
                getOsmLevelFiles().equals(that.getOsmLevelFiles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRasterInterval(), getOsmLevelFiles());
    }
}
