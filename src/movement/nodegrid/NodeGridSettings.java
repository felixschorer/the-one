package movement.nodegrid;

import core.Settings;

public class NodeGridSettings {
    private static final String NODE_GRID_NS = "NodeGrid";
    private static final String RASTER_INTERVAL = "rasterInterval";
    private static final String REFERENCE_LONG = "referenceLong";
    private static final String REFERENCE_LAT = "referenceLat";
    private static final String OSM_FILE = "osmFile";

    private final double rasterInterval;
    private final double referenceLong;
    private final double referenceLat;
    private final String osmFile;

    public NodeGridSettings() {
        Settings settings = new Settings(NODE_GRID_NS);
        rasterInterval = settings.getDouble(RASTER_INTERVAL, 1);
        referenceLong = settings.getDouble(REFERENCE_LONG, 0);
        referenceLat = settings.getDouble(REFERENCE_LAT, 0);
        osmFile = settings.getSetting(OSM_FILE);
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

    public String getOsmFile() {
        return osmFile;
    }
}
