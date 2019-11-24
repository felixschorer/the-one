package input.osm;

import core.Coord;
import core.Polygon;

import java.util.List;
import java.util.Map;

public class OSMArea extends OSMWay {
    public OSMArea(String fileName, String id, List<OSMNode> nodes, Map<String, String> tags) {
        super(fileName, id, nodes, tags);
    }

    public Polygon getPolygon() {
        Coord[] vertices = getNodes().stream().map(OSMNode::getLocation).toArray(Coord[]::new);
        return new Polygon(vertices);
    }
}
