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
    private static final String INCLUDED_POLYGON = "includedPolygon";
    private static final String EXCLUDED_POLYGON = "excludedPolygon";

    private final double rasterInterval;
    private final double referenceLong;
    private final double referenceLat;
    private final Set<String> includedPolygons;
    private final Set<String> excludedPolygons;

    public NodeGridSettings() {
        Settings settings = new Settings(NODE_GRID_MOVEMENT_NS);
        rasterInterval = settings.getDouble(RASTER_INTERVAL, 1);
        referenceLong = settings.getDouble(REFERENCE_LONG, 0);
        referenceLat = settings.getDouble(REFERENCE_LAT, 0);
        includedPolygons = readPaths(settings, INCLUDED_POLYGON);
        excludedPolygons = readPaths(settings, EXCLUDED_POLYGON);
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

    public Set<String> getIncludedPolygons() {
        return includedPolygons;
    }

    public Set<String> getExcludedPolygons() {
        return excludedPolygons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeGridSettings that = (NodeGridSettings) o;
        return Double.compare(that.getRasterInterval(), getRasterInterval()) == 0 &&
                Objects.equals(getIncludedPolygons(), that.getIncludedPolygons()) &&
                Objects.equals(getExcludedPolygons(), that.getExcludedPolygons());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRasterInterval(), getIncludedPolygons(), getExcludedPolygons());
    }

    private static Set<String> readPaths(Settings settings, String settingName) {
        Set<String> includedPolygons = new HashSet<>();
        for (int current = 1; settings.contains(settingName + current); current++) {
            includedPolygons.add(settings.getSetting(settingName + current));
        }
        return includedPolygons;
    }
}
