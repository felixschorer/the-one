package movement;

import core.Coord;
import core.Settings;
import input.osm.OSMArea;
import input.osm.OSMMultiPolygon;
import input.osm.OSMReader;
import movement.map.MapNode;
import movement.map.SimMap;
import movement.nodegrid.NodeGridSettings;
import movement.pathfinding.AStarPathFinder;
import movement.nodegrid.NodeGridBuilder;
import movement.pathfinding.Heuristic;
import movement.pathfinding.PathFinder;
import movement.pathfinding.RandomizedDistanceHeuristic;

import java.io.File;
import java.util.List;

public class NodeGridMovement extends MovementModel implements RenderableMovement {
    private static NodeGridSettings cachedMapSettings = null;

    private static SimMap cachedMap = null;

    private SimMap nodeGrid;

    private PathFinder pathFinder;

    private MapNode currentNode;

    public NodeGridMovement(Settings settings) {
        super(settings);
        nodeGrid = readMap();
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

    public static SimMap readMap() {
        NodeGridSettings settings = new NodeGridSettings();
        if (settings.equals(cachedMapSettings) && cachedMap != null) {
            return cachedMap;
        }

        NodeGridBuilder builder = new NodeGridBuilder(settings.getRasterInterval());

        for (String path : settings.getOsmFiles()) {
            OSMReader reader = new OSMReader(new File(path), settings.getReferenceLong(), settings.getReferenceLat());

            for (OSMArea area : reader.getAreas()) {
                builder.add(area.getPolygon());
            }

            for (OSMMultiPolygon polygon : reader.getMultiPolygons()) {
                for (OSMArea area : polygon.getOuterPolygons()) {
                    builder.add(area.getPolygon());
                }
                for (OSMArea area : polygon.getInnerPolygons()) {
                    builder.subtract(area.getPolygon());
                }
            }
        }

        cachedMap = builder.build();
        cachedMapSettings = settings;
        return cachedMap;
    }
}
