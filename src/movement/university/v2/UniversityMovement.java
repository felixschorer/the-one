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
    private static final String MEAN_LECTURES_PER_STUDENT = "universityMeanLecturesPerStudent";
    private static final String PATH_RANDOMNESS = "universityPathRandomness";
    private static final String DISTANCE_ESTIMATIE_DISTORTION = "universityDistanceEstimateDistortion";
    private static final String TEMPORAL_SPREAD = "universityTemporalSpread";

    private static UniversityScheduleGenerator scheduleGeneratorCache = null;

    private UniversityScheduleGenerator scheduleGenerator;
    private Schedule schedule;
    private MapNode currentNode;
    private PathFinder pathFinder;
    private double numberOfLectures;
    private boolean isStuck = false;

    public UniversityMovement(Settings settings) {
        super(settings);
        UniversitySettings universitySettings = new UniversitySettings();
        if (scheduleGeneratorCache == null || !scheduleGeneratorCache.getSettings().equals(universitySettings)) {
            scheduleGeneratorCache = new UniversityScheduleGenerator(rng, universitySettings, getPointsOfInterest());
        }
        scheduleGenerator = scheduleGeneratorCache;

        numberOfLectures = settings.getDouble(MEAN_LECTURES_PER_STUDENT, 2);

        double pathRandomness = settings.getDouble(PATH_RANDOMNESS, 2);

        pathFinder = PathFinderBuilder
                .random(rng, pathRandomness)
                .discourage( NodeType.LECTURE_HALL.getType(), NodeType.EXERCISE_ROOM.getType(), NodeType.STUDY_PLACE.getType())
                .levelAware(getPortals())
                .build();
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
