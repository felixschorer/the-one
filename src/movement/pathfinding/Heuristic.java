package movement.pathfinding;

import movement.map.MapNode;

/**
 * Heuristic for choosing the next node.
 * The following must be true: 0 <= returned cost <= actual cost.
 * The actual cost is the distance between the two points.
 * If the returned cost is smaller than the actual cost, the path might not be optimal.
 */
@FunctionalInterface
public interface Heuristic {
    double compute(MapNode form, MapNode to);
}
