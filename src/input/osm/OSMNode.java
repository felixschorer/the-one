package input.osm;

import core.Coord;

import java.util.Map;

public class OSMNode extends OSMEntity {
    private final Coord location;

    public OSMNode(String id, Coord location, Map<String, String> tags) {
        super(id, tags);
        this.location = location;
    }

    public Coord getLocation() {
        return location;
    }
}
