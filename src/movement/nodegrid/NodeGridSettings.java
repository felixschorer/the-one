package movement.nodegrid;

import core.Settings;

import java.util.Objects;

public class NodeGridSettings {
    private static final String NODE_GRID_NS = "NodeGrid";
    private static final String RASTER_INTERVAL = "rasterInterval";
    private static final String OSM_FILE = "osmFile";

    private final double rasterInterval;
    private final String osmFile;

    public NodeGridSettings() {
        Settings settings = new Settings(NODE_GRID_NS);
        rasterInterval = settings.getDouble(RASTER_INTERVAL, 1);
        osmFile = settings.getSetting(OSM_FILE);
    }

    public double getRasterInterval() {
        return rasterInterval;
    }

    public String getOsmFile() {
        return osmFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeGridSettings that = (NodeGridSettings) o;
        return Double.compare(that.getRasterInterval(), getRasterInterval()) == 0 &&
                getOsmFile().equals(that.getOsmFile());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRasterInterval(), getOsmFile());
    }
}
