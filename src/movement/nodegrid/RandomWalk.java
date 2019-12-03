package movement.nodegrid;

import core.Coord;
import core.Settings;
import movement.MovementModel;
import movement.Path;
import movement.map.MapNode;

import java.util.List;

public class RandomWalk extends NodeGridMovementModel {
    private MapNode currentNode;
    private boolean isStuck = false;

    public RandomWalk(Settings settings) {
        super(settings);
    }

    public RandomWalk(RandomWalk mm) {
        super(mm);
    }

    @Override
    public Path getPath() {
        MapNode to = pickRandomNode(currentNode.getNeighbors());
        MapNode from = currentNode;
        currentNode = to;

        Path path = new Path();
        path.addWaypoint(from.getLocation(), 1);
        path.addWaypoint(to.getLocation(), 1);

        return path;
    }

    @Override
    public boolean isActive() {
        return !isStuck;
    }

    @Override
    public Coord getInitialLocation() {
        currentNode = pickRandomNode(getMap().getNodes());
        return currentNode.getLocation().clone();
    }

    @Override
    public MovementModel replicate() {
        return new RandomWalk(this);
    }

    private MapNode pickRandomNode(List<MapNode> nodes) {
        int chosenIndex = rng.nextInt(nodes.size());
        return nodes.get(chosenIndex);
    }
}
