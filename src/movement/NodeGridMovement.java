package movement;

import core.Coord;
import core.Settings;
import movement.map.MapNode;
import movement.map.SimMap;
import movement.pathfinding.AStarPathFinder;
import movement.nodegrid.NodeGridBuilder;
import movement.nodegrid.Polygon;
import movement.pathfinding.Heuristic;
import movement.pathfinding.PathFinder;
import movement.pathfinding.RandomizedDistanceHeuristic;
import java.util.List;

public class NodeGridMovement extends MovementModel implements RenderableMovement {
    private static final String RASTER_INTERVAL = "ngmRasterInterval";

    private SimMap nodeGrid;

    private PathFinder pathFinder;

    private MapNode currentNode;

    private List<MapNode> locations;

    private Schedule schedule = new Schedule();

    public NodeGridMovement(Settings settings) {
        super(settings);
        double rasterInterval = settings.getDouble(RASTER_INTERVAL);
        Polygon outerBound = new Polygon(
                new Coord(0, 0),
                new Coord(0, 40),
                new Coord(160, 40),
                new Coord(160, 0)
        );
        outerBound.translate(20, 30);

        NodeGridBuilder builder = new NodeGridBuilder(rasterInterval).add(outerBound);

        schedule
                .getSchedule()
                .forEach(event -> builder.attachNodeByClosestNodes(event.getLocation(), 1));

        nodeGrid = builder.build();

        Heuristic heuristic = new RandomizedDistanceHeuristic(rng::nextGaussian, 2);
        pathFinder = new AStarPathFinder(heuristic);
    }

    public NodeGridMovement(NodeGridMovement other) {
        super(other);
        nodeGrid = other.nodeGrid;
        pathFinder = other.pathFinder;
    }

    @Override
    public SimMap getMap() {
        return nodeGrid;
    }

    @Override
    public Path getPath() {
        MapNode from = currentNode;
        MapNode to = schedule.getNextEvent().getLocation();
//        System.out.println("next destination:" + to);
        currentNode = to;

        List<MapNode> shortestPath = pathFinder.findPath(from, to);

        Path path = new Path();
        for (MapNode hop : shortestPath) {
            path.addWaypoint(hop.getLocation(), 1);
        }

        return path;
    }

    @Override
    public Coord getInitialLocation() {
        currentNode = pickRandomNode(nodeGrid.getNodes());
        return currentNode.getLocation().clone();
    }

    @Override
    public MovementModel replicate() {
        return new NodeGridMovement(this);
    }

    private MapNode pickRandomNode(List<MapNode> graphNodes) {
        int chosenIndex = rng.nextInt(graphNodes.size());
        return graphNodes.get(chosenIndex);
    }
}
