package movement;

import core.Coord;
import core.Settings;
import movement.map.MapNode;
import movement.map.SimMap;
import movement.nodegrid.NodeGridSettings;
import movement.nodegrid.OSM2NodeGrid;
import movement.nodegrid.PointOfInterestMapNode;
import movement.pathfinding.AStarPathFinder;
import movement.pathfinding.Heuristic;
import movement.pathfinding.PathFinder;
import movement.pathfinding.RandomizedDistanceHeuristic;

import java.util.*;

public class NodeGridMovement extends MovementModel implements RenderableMovement {
    private static OSM2NodeGrid osm2NodeGridCache = null;

    private static NodeGridSettings nodeGridSettingsCache = null;

    private Set<PointOfInterestMapNode> pointsOfInterest;

    private SimMap nodeGrid;

    private PathFinder pathFinder;

    private MapNode currentNode;

    public NodeGridMovement(Settings settings) {
        super(settings);

        // cache map in case of multiple host groups
        NodeGridSettings nodeGridSettings = new NodeGridSettings();
        if (!nodeGridSettings.equals(nodeGridSettingsCache)) {
            nodeGridSettingsCache = nodeGridSettings;
            osm2NodeGridCache = new OSM2NodeGrid(nodeGridSettings);
        }

        nodeGrid = osm2NodeGridCache.getSimMap();
        pointsOfInterest = osm2NodeGridCache.getPointsOfInterest();

        Heuristic heuristic = new RandomizedDistanceHeuristic(rng::nextGaussian, 2);
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
        MapNode to = pickRandomNode(nodeGrid.getNodes());
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
