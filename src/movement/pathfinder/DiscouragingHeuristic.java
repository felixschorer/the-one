package movement.pathfinder;

import movement.map.MapNode;

public class DiscouragingHeuristic implements Heuristic {
    private final Heuristic heuristic;
    private final int[] notOkMapNodes;

    public DiscouragingHeuristic(Heuristic heuristic, int... notOkMapNodes) {
        this.heuristic = heuristic;
        this.notOkMapNodes = notOkMapNodes;
    }

    @Override
    public double compute(MapNode from, MapNode to) {
        if (from != to && from.isType(notOkMapNodes)) {
            return 10000 + heuristic.compute(from, to);
        }
        return heuristic.compute(from, to);
    }
}
