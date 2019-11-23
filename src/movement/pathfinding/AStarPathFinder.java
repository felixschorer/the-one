package movement.pathfinding;

import movement.map.MapNode;

import java.util.*;
import java.util.function.Supplier;

public class AStarPathFinder implements PathFinder {
    private int[] okMapNodes;

    private Heuristic heuristic;

    public AStarPathFinder(int... okMapNodes) {
        this(new DistanceHeuristic(), okMapNodes);
    }

    public AStarPathFinder(Heuristic heuristic, int... okMapNodes) {
        this.heuristic = heuristic;
        this.okMapNodes = okMapNodes;
    }

    @Override
    public List<MapNode> findPath(MapNode from, MapNode to) {
        assert from.isType(okMapNodes) && to.isType(okMapNodes);

        PriorityQueue<NodeWithCost> openList = new PriorityQueue<>();
        Set<MapNode> closedList = new HashSet<>();
        Map<MapNode, MapNode> predecessor = new HashMap<>();
        Map<MapNode, Double> currentCost = new HashMap<>();

        currentCost.put(from, 0.0);
        openList.add(new NodeWithCost(heuristic.compute(from, to), from));

        do {
            MapNode currentNode = openList.remove().getNode();
            if (currentNode.equals(to)) {
                return predecessorsToPath(to, predecessor);
            }
            closedList.add(currentNode);

            for (MapNode successor : currentNode.getNeighbors()) {
                if (!successor.isType(okMapNodes) || closedList.contains(successor)) {
                    continue;
                }
                double cost = currentNode.getLocation().distance(successor.getLocation());
                double tentativeCost = cost + currentCost.getOrDefault(currentNode, Double.POSITIVE_INFINITY);
                double estimatedCost = tentativeCost + heuristic.compute(successor, to);
                NodeWithCost successorWithCost = new NodeWithCost(estimatedCost, successor);

                if (tentativeCost >= currentCost.getOrDefault(successor, Double.POSITIVE_INFINITY)
                        && openList.contains(successorWithCost)) {
                    continue;
                }
                openList.add(successorWithCost);
                currentCost.put(successor, tentativeCost);
                predecessor.put(successor, currentNode);
            }
        } while (!openList.isEmpty());

        return Collections.emptyList();
    }

    private static List<MapNode> predecessorsToPath(MapNode to, Map<MapNode, MapNode> predecessor) {
        List<MapNode> path = new LinkedList<>();
        while (predecessor.containsKey(to)) {
            path.add(0, to);
            to = predecessor.get(to);
        }
        return path;
    }

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

    public static class DistanceHeuristic implements Heuristic {
        @Override
        public double compute(MapNode mapNode, MapNode mapNode2) {
            return mapNode.getLocation().distance(mapNode2.getLocation());
        }
    }

    public static class RandomizedDistanceHeuristic implements Heuristic {
        private final double magnitude;
        private final Supplier<Double> randomSupplier;

        public RandomizedDistanceHeuristic(Supplier<Double> randomSupplier) {
            this(randomSupplier, 1);
        }

        public RandomizedDistanceHeuristic(Supplier<Double> randomSupplier, double magnitude) {
            this.magnitude = magnitude;
            this.randomSupplier = randomSupplier;
        }

        @Override
        public double compute(MapNode mapNode, MapNode mapNode2) {
            double costReduction = Math.abs(magnitude * randomSupplier.get());
            return Math.max(0, mapNode.getLocation().distance(mapNode2.getLocation()) - costReduction);
        }
    }

    private static class NodeWithCost implements Comparable<NodeWithCost> {
        private final double estimatedCost;
        private final MapNode node;

        public NodeWithCost(double estimatedCost, MapNode node) {
            this.estimatedCost = estimatedCost;
            this.node = node;
        }

        public double getEstimatedCost() {
            return estimatedCost;
        }

        public MapNode getNode() {
            return node;
        }

        @Override
        public int compareTo(NodeWithCost other) {
            return Double.compare(getEstimatedCost(), other.getEstimatedCost());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NodeWithCost that = (NodeWithCost) o;
            return getNode().equals(that.getNode());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getNode());
        }
    }
}
