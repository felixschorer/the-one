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
    private ArrayList<MapNode> outsideBuildingPois;
    private ArrayList<MapNode> otherPois;

    private boolean isStuck = false;

    public UniversityMovement(Settings settings) {
        super(settings);

        pathFinder = PathFinderBuilder
                .random(rng, 2)
                .discourage( NodeType.LECTURE_HALL.getType(), NodeType.EXERCISE_ROOM.getType(), NodeType.STUDY_PLACE.getType())
                .levelAware(getPortals())
                .build();

        fixedEvents = generateFixedEvents();
        outsideBuildingPois = getCollectionAreas();
        otherPois = getOtherPois();
    }

    private ArrayList<Lecture> generateFixedEvents() {
        ArrayList<MapNode> pois = filterPointsOfInterest(NodeType.LECTURE_HALL, NodeType.EXERCISE_ROOM);

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

    private ArrayList<MapNode> getCollectionAreas() {
        return filterPointsOfInterest(NodeType.COLLECTION_AREA);
    }

    private ArrayList<MapNode> getOtherPois() {
        return filterPointsOfInterest(NodeType.COLLECTION_AREA, NodeType.STUDY_PLACE);
    }

    private ArrayList<MapNode> filterPointsOfInterest(NodeType... types) {
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
        fixedEvents = other.fixedEvents;
        outsideBuildingPois = other.outsideBuildingPois;
        otherPois = other.otherPois;

        schedule = new Schedule(fixedEvents, outsideBuildingPois, otherPois, rng);
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

        int randomLocationIndex = rng.nextInt(outsideBuildingPois.size());
        currentNode = outsideBuildingPois.get(randomLocationIndex);
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
}
