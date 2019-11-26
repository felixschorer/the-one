package movement.nodegrid;

import core.BoundingBox;
import core.Coord;
import movement.map.MapNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NodeGridLevel {
    private Map<Coord, MapNode> nodes;
    private final Map<String, MapNode> portals;
    private final Set<MapNode> pointsOfInterest;

    public NodeGridLevel(Map<Coord, MapNode> nodes, Map<String, MapNode> portals, Set<MapNode> pointsOfInterest) {
        this.nodes = nodes;
        this.portals = portals;
        this.pointsOfInterest = pointsOfInterest;
    }

    public void setDisplayOffset(double x, double y) {
        Collection<MapNode> nodesToOffset = nodes.values();
        for (MapNode node : nodesToOffset) {
            node.getLocation().setDisplayOffset(x, y);
        }
        rehashNodes(nodesToOffset);
    }

    public void translate(double dx, double dy) {
        Collection<MapNode> nodesToTranslate = nodes.values();
        for (MapNode node : nodesToTranslate) {
            node.getLocation().translate(dx, dy);
        }
        rehashNodes(nodesToTranslate);
    }

    public BoundingBox getBoundingBox() {
        Coord[] points = nodes.keySet().toArray(new Coord[0]);
        return BoundingBox.fromPoints(points);
    }

    public Map<Coord, MapNode> getNodes() {
        return nodes;
    }

    public Map<String, MapNode> getPortals() {
        return portals;
    }

    public Set<MapNode> getPointsOfInterest() {
        return pointsOfInterest;
    }

    private void rehashNodes(Collection<MapNode> allNodes) {
        nodes = new HashMap<>();
        for (MapNode node : allNodes) {
            nodes.put(node.getLocation(), node);
        }
    }
}
