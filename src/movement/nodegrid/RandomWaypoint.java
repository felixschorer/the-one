package movement.nodegrid;

import core.Coord;
import core.Settings;
import movement.MovementModel;
import movement.Path;
import movement.map.MapNode;
import movement.pathfinder.PathFinder;
import movement.pathfinder.PathFinderBuilder;

import java.util.List;

public class RandomWaypoint extends NodeGridMovementModel {
    private final PathFinder pathFinder;

    private MapNode currentNode;
    private boolean isStuck = false;

    public RandomWaypoint(Settings settings) {
        super(settings);
        pathFinder = PathFinderBuilder
                .random(rng, 2)
                .levelAware(getPortals())
                .build();
    }

    public RandomWaypoint(RandomWaypoint mm) {
        super(mm);
        pathFinder = mm.pathFinder;
    }

    @Override
    public Path getPath() {
        MapNode to = pickRandomNode();
        MapNode from = currentNode;

        List<MapNode> shortestPath = pathFinder.findPath(from, to);

        if (shortestPath.size() > 0) {
            currentNode = to;
        } else if (from != to) {
            isStuck = true;
            System.out.println(String.format("Could not find path from %s to %s", from, to));
        }

        Path path = new Path();
        for (MapNode hop : shortestPath) {
            path.addWaypoint(hop.getLocation(), 1);
        }

        return path;
    }

    @Override
    public boolean isActive() {
        return !isStuck;
    }

    @Override
    public Coord getInitialLocation() {
        currentNode = pickRandomNode();
        return currentNode.getLocation().clone();
    }

    @Override
    public MovementModel replicate() {
        return new RandomWaypoint(this);
    }

    private MapNode pickRandomNode() {
        List<MapNode> nodes = getMap().getNodes();
        int chosenIndex = rng.nextInt(nodes.size());
        return nodes.get(chosenIndex);
    }
}
