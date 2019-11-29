package movement.nodegrid;

import core.Coord;
import movement.map.MapNode;

public class OSMMapNode extends MapNode {
    private final String id;

    public OSMMapNode(String id, Coord location, int... types) {
        super(location, types);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return super.toString() + String.format("|OSM %s|", id);
    }
}
