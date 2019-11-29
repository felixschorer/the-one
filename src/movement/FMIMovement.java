package movement;

import core.Coord;
import core.Settings;
import movement.fmi.Event;
import movement.fmi.NodeType;
import movement.map.MapNode;
import movement.pathfinder.*;
import movement.fmi.Lecture;
import movement.fmi.Schedule;

import java.util.*;
import java.util.stream.Collectors;

public class FMIMovement extends NodeGridBasedMovement {
    private PathFinder pathFinder;

    private MapNode currentNode;

    private Event nextEvent;

    private Schedule schedule;

    private ArrayList<Lecture> fixedEvents;
    private ArrayList<MapNode> otherAreas;

    public FMIMovement(Settings settings) {
        super(settings);

        Heuristic heuristic = new RandomizedDistanceHeuristic(rng::nextGaussian, 2);
        Heuristic levelAwareHeuristic = new LevelAwareHeuristic(heuristic, getPortals());
        Heuristic discouragingHeuristic = new DiscouragingHeuristic(levelAwareHeuristic,
                NodeType.LECTURE_HALL.getType(), NodeType.EXERCISE_ROOM.getType(),
                NodeType.CAFE.getType(), NodeType.STUDY_PLACE.getType());
        pathFinder = new AStarPathFinder(discouragingHeuristic);

        fixedEvents = generateFixedEvents();
        otherAreas = getOtherAreas();
    }

    private ArrayList<MapNode> getOtherAreas() {
        return getPointsOfInterest().stream()
                .filter(poi -> {
                    boolean isCollectionArea = poi.isType(NodeType.COLLECTION_AREA.getType());
                    boolean isStudyPlace = poi.isType(NodeType.STUDY_PLACE.getType());
                    boolean isCafe = poi.isType(NodeType.CAFE.getType());

                    return isCollectionArea || isStudyPlace || isCafe;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<Lecture> generateFixedEvents() {
        fixedEvents = new ArrayList<>();
        ArrayList<MapNode> fixedEvents = getPointsOfInterest().stream()
                .filter(poi -> {
                    boolean isLectureHall = poi.isType(NodeType.LECTURE_HALL.getType());
                    boolean isExerciseRoom = poi.isType(NodeType.EXERCISE_ROOM.getType());

                    return isLectureHall || isExerciseRoom;
                })
                .collect(Collectors.toCollection(ArrayList::new));
        int offset = 300;
        int[] startTimesByRoom = Arrays.stream(new int[fixedEvents.size()]).map(start -> offset).toArray();

        for (int i = 0; i < fixedEvents.size(); i++) {
            // only add lecture if room has not been occupied for 10h already
            while (startTimesByRoom[i] < 60 * 60 * 10) {
                Lecture lecture = new Lecture(fixedEvents.get(i), startTimesByRoom[i], rng);
                this.fixedEvents.add(lecture);
                startTimesByRoom[i] += lecture.getTotalDuration();
            }
        }

        return this.fixedEvents;
    }

    public FMIMovement(FMIMovement other) {
        super(other);
        pathFinder = other.pathFinder;
        otherAreas = other.otherAreas;

        schedule = new Schedule(other.fixedEvents, otherAreas, rng);
    }

    @Override
    public Path getPath() {
        MapNode from = currentNode;
        MapNode to = nextEvent.getLocation();

        List<MapNode> shortestPath = pathFinder.findPath(from, to);

        if (shortestPath.size() > 0) {
            currentNode = to;
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
        currentNode = pickRandomNode(getMap().getNodes());
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
