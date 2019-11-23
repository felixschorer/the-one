package movement.pathfinding;

import movement.map.MapNode;

public class DistanceHeuristic implements Heuristic {
    @Override
    public double compute(MapNode mapNode, MapNode mapNode2) {
        return mapNode.getLocation().distance(mapNode2.getLocation());
    }
}
