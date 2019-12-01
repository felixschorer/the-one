package movement.university.v2;

import movement.map.MapNode;
import movement.university.NodeType;
import movement.university.Size;

import java.util.Map;
import java.util.function.Function;

public class PointOfInterest {
    private final NodeType nodeType;
    private final Size size;
    private final MapNode mapNode;

    public PointOfInterest(MapNode mapNode) {
        NodeType nodeType = null;
        for (NodeType nodeTypeToCheck : NodeType.values()) {
            if (mapNode.isType(nodeTypeToCheck.getType())) {
                nodeType = nodeTypeToCheck;
                break;
            }
        }
        Size size = null;
        for (Size sizeToCheck : Size.values()) {
            if (mapNode.isType(sizeToCheck.getType())) {
                size = sizeToCheck;
                break;
            }
        }

        this.nodeType = nodeType;
        this.size = size;
        this.mapNode = mapNode;
    }

    public <T> T getProperty(Map<NodeType, Map<Size, T>> propertyMap) {
        if (propertyMap.containsKey(nodeType) && propertyMap.get(nodeType).containsKey(size)) {
            return propertyMap.get(nodeType).get(size);
        }
        return null;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public Size getSize() {
        return size;
    }

    public MapNode getMapNode() {
        return mapNode;
    }

    public static <T> Function<PointOfInterest, T> property(Map<NodeType, Map<Size, T>> propertyMap) {
        return pointOfInterest -> pointOfInterest.getProperty(propertyMap);
    }
}
