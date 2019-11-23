package input.osm;

import java.util.List;
import java.util.Map;

public class OSMWay extends OSMEntity {
    private final List<OSMNode> nodes;

    public OSMWay(String id, List<OSMNode> nodes, Map<String, String> tags) {
        super(id, tags);
        this.nodes = nodes;
    }

    public List<OSMNode> getNodes() {
        return nodes;
    }
}
