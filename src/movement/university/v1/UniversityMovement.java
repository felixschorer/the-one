package movement.university.v1;

import core.Coord;
import core.Settings;
import movement.MovementModel;
import movement.Path;
import movement.university.NodeType;
import movement.map.MapNode;
import movement.nodegrid.NodeGridMovementModel;
import movement.pathfinder.*;

import java.util.*;
import java.util.stream.Collectors;

public class UniversityMovement extends NodeGridMovementModel {
    private PathFinder pathFinder;

    private MapNode currentNode;

    private Event nextEvent;

    private Schedule schedule;

    private ArrayList<Lecture> fixedEvents;
    private ArrayList<MapNode> otherPois;

    private boolean isStuck = false;

    public UniversityMovement(Settings settings) {
        super(settings);

        Heuristic heuristic = new RandomizedDistanceHeuristic(rng::nextGaussian, 2);
        Heuristic levelAwareHeuristic = new LevelAwareHeuristic(heuristic, getPortals());
        Heuristic discouragingHeuristic = new DiscouragingHeuristic(levelAwareHeuristic,
                NodeType.LECTURE_HALL.getType(), NodeType.EXERCISE_ROOM.getType(),
                NodeType.CAFE.getType(), NodeType.STUDY_PLACE.getType());
        pathFinder = new AStarPathFinder(discouragingHeuristic);

        fixedEvents = generateFixedEvents();
        otherPois = getOtherPois();
    }

    private ArrayList<MapNode> getOtherPois() {
        ArrayList<NodeType> areaTypes = new ArrayList<>(
                Arrays.asList(NodeType.TRANSPORT, NodeType.STUDY_PLACE, NodeType.CAFE));
        return filterPointsOfInterest(areaTypes);
    }

    private ArrayList<Lecture> generateFixedEvents() {
        ArrayList<NodeType> areaTypes = new ArrayList<>(
                Arrays.asList(NodeType.LECTURE_HALL, NodeType.EXERCISE_ROOM));
        ArrayList<MapNode> pois = filterPointsOfInterest(areaTypes);

        fixedEvents = new ArrayList<>();
        int offset = 300;
        int[] startTimesByRoom = Arrays.stream(new int[pois.size()]).map(start -> offset).toArray();

        for (int i = 0; i < pois.size(); i++) {
            // only add lecture if room has not been occupied for 10h already
            while (startTimesByRoom[i] < 60 * 60 * 10) {
                Lecture lecture = new Lecture(pois.get(i), startTimesByRoom[i], rng);
                this.fixedEvents.add(lecture);
                startTimesByRoom[i] += lecture.getTotalDuration();
            }
        }

        return this.fixedEvents;
    }

    private ArrayList<MapNode> filterPointsOfInterest(ArrayList<NodeType> types) {
        return getPointsOfInterest().stream()
                .filter(poi -> {
                    for (NodeType type : types) {
                        if (poi.isType(type.getType())) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public UniversityMovement(UniversityMovement other) {
        super(other);
        pathFinder = other.pathFinder;
        otherPois = other.otherPois;
        fixedEvents = other.fixedEvents;

        schedule = new Schedule(fixedEvents, otherPois, rng);
    }

    @Override
    public Path getPath() {
        MapNode from = currentNode;
        MapNode to = nextEvent.getLocation();

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

        if (schedule.hasNextEvent()) {
            nextEvent = schedule.getNextEvent();
        }

        return path;
    }

    @Override
    public boolean isActive() {
        return !isStuck;
    }

    @Override
    public double nextPathAvailable() {
        double estimatedTime = estimateTravelTime(currentNode, nextEvent.getLocation(), 1);
        return nextEvent.getRealStart() - estimatedTime;
    }

    @Override
    public Coord getInitialLocation() {
        nextEvent = schedule.getNextEvent();

        ArrayList<NodeType> areaTypes = new ArrayList<>(
                Arrays.asList(NodeType.TRANSPORT));
        ArrayList<MapNode> collectionAreas = filterPointsOfInterest(areaTypes);
        int randomLocationIndex = rng.nextInt(collectionAreas.size());
//        currentNode = pickRandomNode(getMap().getNodes());
        currentNode = collectionAreas.get(randomLocationIndex);
        return currentNode.getLocation().clone();
    }

    @Override
    public MovementModel replicate() {
        return new UniversityMovement(this);
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
