package movement.pathfinding;

import movement.map.MapNode;

import java.util.List;
import java.util.Map;

public class LevelAwareHeuristic implements Heuristic {
    private final Map<Integer, List<MapNode>> portals;
    private final Heuristic heuristic;

    public LevelAwareHeuristic(Map<Integer, List<MapNode>> portals, Heuristic heuristic) {
        this.portals = portals;
        this.heuristic = heuristic;
    }

    @Override
    public double compute(MapNode from, MapNode to) {
        if (from.getLocation().getLayer() == to.getLocation().getLayer()) {
            return heuristic.compute(from, to);
        }

        List<MapNode> portals = this.portals.get(from.getLocation().getLayer());
        double lowestCost = Double.POSITIVE_INFINITY;
        for (MapNode portal : portals) {
            double cost = heuristic.compute(from, portal) + heuristic.compute(portal, to);
            if (cost < lowestCost) {
                lowestCost = cost;
            }
        }

        return lowestCost;
    }
}
