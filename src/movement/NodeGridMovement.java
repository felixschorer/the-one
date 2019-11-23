package movement;

import core.Coord;
import core.Settings;
import movement.map.MapNode;
import movement.map.SimMap;
import movement.pathfinding.AStarPathFinder;
import movement.nodegrid.NodeGridBuilder;
import movement.nodegrid.Polygon;

import java.util.Arrays;
import java.util.List;

public class NodeGridMovement extends MovementModel implements RenderableMovement {
    private static final String RASTER_INTERVAL = "ngmRasterInterval";

    private SimMap nodeGrid;

    private AStarPathFinder pathFinder;

    private MapNode currentNode;

    private List<MapNode> pointsOfInterest;

    public NodeGridMovement(Settings settings) {
        super(settings);
        double rasterInterval = settings.getDouble(RASTER_INTERVAL);
        Polygon outerBound = new Polygon(
                new Coord(0, 0),
                new Coord(0, 100),
                new Coord(100, 100),
                new Coord(100, 0)
        );
        outerBound.translate(50, 50);

        MapNode pointOfInterest1 = new MapNode(new Coord(0, 0));
        MapNode pointOfInterest2 = new MapNode(new Coord(200, 200));
        pointsOfInterest = Arrays.asList(pointOfInterest1, pointOfInterest2);

        nodeGrid = new NodeGridBuilder(rasterInterval)
                .add(outerBound)
                .attachNodeByClosestNodes(pointOfInterest1, 1)
                .attachNodeByClosestNodes(pointOfInterest2, 1)
                .build();

        AStarPathFinder.Heuristic heuristic = new AStarPathFinder.RandomizedDistanceHeuristic(rng::nextDouble, 5);
        pathFinder = new AStarPathFinder(heuristic);
    }

    public NodeGridMovement(NodeGridMovement other) {
        super(other);
        nodeGrid = other.nodeGrid;
        pointsOfInterest = other.pointsOfInterest;
        pathFinder = other.pathFinder;
    }

    @Override
    public SimMap getMap() {
        return nodeGrid;
    }

    @Override
    public Path getPath() {
        MapNode from = currentNode;
        MapNode to = pickRandomNode(pointsOfInterest);
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
