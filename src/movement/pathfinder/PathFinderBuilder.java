package movement.pathfinder;

import movement.map.MapNode;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PathFinderBuilder {
    private Heuristic base;
    private Map<Class<?>, Function<Heuristic, Heuristic>> singletonModifiers = new HashMap<>();
    private List<Function<Heuristic, Heuristic>> modifiers = new ArrayList<>();

    private PathFinderBuilder(Heuristic base) {
        this.base = base;
    }

    public PathFinderBuilder discourage(int... nodeTypes) {
        modifiers.add(heuristic -> new DiscouragingHeuristic(heuristic, nodeTypes));
        return this;
    }

    public PathFinderBuilder levelAware(Map<Integer, List<MapNode>> portals) {
        singletonModifiers.put(LevelAwareHeuristic.class, heuristic -> new LevelAwareHeuristic(heuristic, portals));
        return this;
    }

    public PathFinder build() {
        List<Function<Heuristic, Heuristic>> allModifiers = Stream.concat(
                singletonModifiers.values().stream(),
                modifiers.stream()
        ).collect(Collectors.toList());

        Heuristic heuristic = base;
        for (Function<Heuristic, Heuristic> modifier : allModifiers) {
            heuristic = modifier.apply(heuristic);
        }

        return new AStarPathFinder(heuristic);
    }

    public static PathFinderBuilder random(Random rng, double variance) {
        // random provided by folded normal distribution
        double normalVariance = Math.sqrt(variance * variance / (1 -  2 / Math.PI));
        Supplier<Double> randomSupplier = () -> Math.abs(rng.nextGaussian() * normalVariance);
        return new PathFinderBuilder(new RandomizedDistanceHeuristic(randomSupplier));
    }

    public static PathFinderBuilder shortestDistance() {
        return new PathFinderBuilder(new DistanceHeuristic());
    }
}
