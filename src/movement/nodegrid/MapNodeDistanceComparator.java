package movement.nodegrid;

import core.Coord;
import movement.map.MapNode;

import java.util.Comparator;

public class MapNodeDistanceComparator implements Comparator<MapNode> {
    private Coord referencePoint;

    public MapNodeDistanceComparator(Coord referencePoint) {
        this.referencePoint = referencePoint;
    }

    @Override
    public int compare(MapNode a, MapNode b) {
        double distanceToA = referencePoint.distance(a.getLocation());
        double distanceToB = referencePoint.distance(b.getLocation());
        return Double.compare(distanceToA, distanceToB);
    }
}
