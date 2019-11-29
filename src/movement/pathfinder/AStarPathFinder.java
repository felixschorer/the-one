package movement.pathfinder;

import movement.map.MapNode;

import java.util.*;

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
