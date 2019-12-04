package movement.pathfinder;

import movement.map.MapNode;

import java.util.function.Supplier;

public class RandomizedDistanceHeuristic implements Heuristic {
    /**
     * Random value must be positive!
     */
    private final Supplier<Double> randomSupplier;

    public RandomizedDistanceHeuristic(Supplier<Double> randomSupplier) {
        this.randomSupplier = randomSupplier;
    }

    @Override
    public double compute(MapNode from, MapNode to) {
        double costReduction = Math.abs(randomSupplier.get());
        return Math.max(0, from.getLocation().distance(to.getLocation()) - costReduction);
    }
}
