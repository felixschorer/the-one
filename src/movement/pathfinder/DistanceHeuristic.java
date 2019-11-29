package movement.pathfinder;

import movement.map.MapNode;

public class DistanceHeuristic implements Heuristic {
    @Override
    public double compute(MapNode from, MapNode to) {
        return from.getLocation().distance(to.getLocation());
    }
}
