package movement;

import core.Coord;
import core.Settings;
import movement.fmi.Event;
import movement.fmi.NodeType;
import movement.map.MapNode;
import movement.map.SimMap;
import movement.nodegrid.NodeGridSettings;
import movement.nodegrid.OSM2NodeGrid;
import movement.pathfinding.AStarPathFinder;
import movement.pathfinding.Heuristic;
import movement.pathfinding.PathFinder;
import movement.pathfinding.RandomizedDistanceHeuristic;
import movement.fmi.Lecture;
import movement.fmi.Schedule;

import java.util.*;
import java.util.stream.Collectors;

public class FMIMovement extends MovementModel implements RenderableMovement {
    private static OSM2NodeGrid osm2NodeGridCache = null;

    private Set<MapNode> pointsOfInterest;

    private SimMap nodeGrid;

    private PathFinder pathFinder;

    private MapNode currentNode;

    private Event nextEvent;

    private Schedule schedule;

    private ArrayList<Lecture> lectures;

    public FMIMovement(Settings settings) {
        super(settings);

        // cache map in case of multiple host groups
        NodeGridSettings nodeGridSettings = new NodeGridSettings();
        if (osm2NodeGridCache == null || !nodeGridSettings.equals(osm2NodeGridCache.getSettings())) {
            osm2NodeGridCache = new OSM2NodeGrid(nodeGridSettings);
        }

        nodeGrid = osm2NodeGridCache.getSimMap();
        pointsOfInterest = osm2NodeGridCache.getPointsOfInterest();

        Heuristic heuristic = new RandomizedDistanceHeuristic(rng::nextGaussian, 2);
        pathFinder = new AStarPathFinder(heuristic);

        lectures = generateLectures();
    }

    private ArrayList<Lecture> generateLectures() {
        lectures = new ArrayList<>();
        ArrayList<MapNode> lectureHalls = pointsOfInterest.stream()
                .filter(poi -> poi.isType(NodeType.LECTURE_HALL.getType()))
                .collect(Collectors.toCollection(ArrayList::new));
        int offset = 300;
        int startTimesByRoom[] = Arrays.stream(new int[lectureHalls.size()]).map(start -> offset).toArray();

        for (int i = 0; i < lectureHalls.size(); i++) {
            // only add lecture if room has not been occupied for 10h already
            while (startTimesByRoom[i] < 60 * 60 * 10) {
                Lecture lecture = new Lecture(lectureHalls.get(i), startTimesByRoom[i], rng);
                lectures.add(lecture);
                startTimesByRoom[i] += lecture.getTotalDuration();
            }
        }

        return lectures;
    }

    public FMIMovement(FMIMovement other) {
        super(other);
        nodeGrid = other.nodeGrid;
        pointsOfInterest = other.pointsOfInterest;
        pathFinder = other.pathFinder;
        ArrayList<MapNode> collectionAreas = pointsOfInterest.stream()
                .filter(poi -> poi.isType(NodeType.COLLECTION_AREA.getType()))
                .collect(Collectors.toCollection(ArrayList::new));
        schedule = new Schedule(other.lectures, collectionAreas, rng);
    }

    @Override
    public SimMap getMap() {
        return nodeGrid;
    }

    @Override
    public Path getPath() {
        MapNode from = currentNode;
        MapNode to = nextEvent.getLocation();
        currentNode = to;

        List<MapNode> shortestPath = pathFinder.findPath(from, to);

        Path path = new Path();
        for (MapNode hop : shortestPath) {
            path.addWaypoint(hop.getLocation(), 1);
        }

        if (schedule.hasNextEvent()) {
            nextEvent = schedule.getNextEvent();
        }

        return path;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public double nextPathAvailable() {
        double estimatedTime = estimateTravelTime(currentNode, nextEvent.getLocation(), 1);
        return nextEvent.getRealStart() - estimatedTime;
    }

    @Override
    public Coord getInitialLocation() {
        nextEvent = schedule.getNextEvent();
        currentNode = pickRandomNode(nodeGrid.getNodes());
        return currentNode.getLocation().clone();
    }

    @Override
    public MovementModel replicate() {
        return new FMIMovement(this);
    }

    private double estimateTravelTime(MapNode from, MapNode to, double averageSpeed) {
        List<MapNode> path = new AStarPathFinder().findPath(from, to);
        double distance = 0;
        MapNode current = from;
        for (MapNode next : path) {
            distance += current.getLocation().distance(next.getLocation());
            current = next;
        }
        return distance / averageSpeed;
    }

    private MapNode pickRandomNode(List<MapNode> graphNodes) {
        int chosenIndex = rng.nextInt(graphNodes.size());
        return graphNodes.get(chosenIndex);
    }
}