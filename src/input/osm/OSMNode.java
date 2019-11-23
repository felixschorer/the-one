package input.osm;

import core.Coord;

import java.util.Map;

public class OSMNode extends OSMEntity {
    private final Coord location;

    public OSMNode(String fileName, String id, Coord location, Map<String, String> tags) {
        super(fileName, id, tags);
        this.location = location;
    }

    public Coord getLocation() {
        return location;
    }
}
