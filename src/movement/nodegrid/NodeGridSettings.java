package movement.nodegrid;

import core.Settings;

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
}
