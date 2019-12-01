package movement.university.v2;

import core.Coord;
import core.Settings;
import movement.MovementModel;
import movement.Path;
import movement.university.NodeType;
import movement.map.MapNode;
import movement.nodegrid.NodeGridMovementModel;
import movement.pathfinder.*;

import java.util.List;

public class UniversityMovement extends NodeGridMovementModel {
    private static final String UNIVERSITY_NROF_LECTURES = "universityNrofLectures";

    private static UniversityScheduleGenerator scheduleGeneratorCache = null;

    private UniversityScheduleGenerator scheduleGenerator;
    private Schedule schedule;
    private MapNode currentNode;
    private PathFinder pathFinder;
    private int numberOfLectures;
    private boolean isStuck = false;

    public UniversityMovement(Settings settings) {
        super(settings);
        UniversitySettings universitySettings = new UniversitySettings();
        if (scheduleGeneratorCache == null || !scheduleGeneratorCache.getSettings().equals(universitySettings)) {
            scheduleGeneratorCache = new UniversityScheduleGenerator(rng, universitySettings, getPointsOfInterest());
        }
        scheduleGenerator = scheduleGeneratorCache;

        numberOfLectures = settings.getInt(UNIVERSITY_NROF_LECTURES, 2);

        Heuristic heuristic = new RandomizedDistanceHeuristic(rng::nextGaussian, 2);
        Heuristic levelAwareHeuristic = new LevelAwareHeuristic(heuristic, getPortals());
        Heuristic discouragingHeuristic = new DiscouragingHeuristic(levelAwareHeuristic,
                NodeType.LECTURE_HALL.getType(), NodeType.EXERCISE_ROOM.getType(),
                NodeType.CAFE.getType(), NodeType.STUDY_PLACE.getType());
        pathFinder = new AStarPathFinder(discouragingHeuristic);
    }

    public UniversityMovement(UniversityMovement mm) {
        super(mm);
        pathFinder = mm.pathFinder;
        schedule = mm.scheduleGenerator.generateSchedule(mm.numberOfLectures).orElseThrow();
    }

    @Override
    public Path getPath() {
        MapNode to = schedule.nextMapNode();
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
    public double nextPathAvailable() {
        return schedule.nextMapNodeAvailable();
    }

    @Override
    public Coord getInitialLocation() {
        currentNode = schedule.getInitialMapNode();
        return currentNode.getLocation().clone();
    }

    @Override
    public MovementModel replicate() {
        return new UniversityMovement(this);
    }
}
