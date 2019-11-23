package movement.pathfinding;

import movement.map.MapNode;

import java.util.function.Supplier;

public class RandomizedDistanceHeuristic implements Heuristic {
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
