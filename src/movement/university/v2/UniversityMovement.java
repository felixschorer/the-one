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
    private static UniversityScheduleGenerator scheduleGeneratorCache = null;

    private final UniversityGroupSettings groupSettings;
    private final UniversityScheduleGenerator scheduleGenerator;
    private final Schedule schedule;
    private final PathFinder pathFinder;

    private MapNode currentNode;
    private boolean isStuck = false;

    public UniversityMovement(Settings settings) {
        super(settings);

        // cache scheduleGenerator in case of multiple host groups
        UniversitySettings universitySettings = new UniversitySettings();
        if (scheduleGeneratorCache == null || !scheduleGeneratorCache.getSettings().equals(universitySettings)) {
            scheduleGeneratorCache = new UniversityScheduleGenerator(rng, universitySettings, getPointsOfInterest());
        }
        scheduleGenerator = scheduleGeneratorCache;


        groupSettings = new UniversityGroupSettings(settings);
        schedule = scheduleGenerator.generateSchedule(groupSettings.getMeanLecturesPerStudent()).orElseThrow();

        pathFinder = PathFinderBuilder
                .random(rng, groupSettings.getPathRandomness())
                .discourage( NodeType.LECTURE_HALL.getType(), NodeType.EXERCISE_ROOM.getType(), NodeType.STUDY_PLACE.getType())
                .levelAware(getPortals())
                .build();
    }

    public UniversityMovement(UniversityMovement mm) {
        super(mm);
        pathFinder = mm.pathFinder;
        scheduleGenerator = mm.scheduleGenerator;
        groupSettings = mm.groupSettings;
        schedule = mm.scheduleGenerator.generateSchedule(groupSettings.getMeanLecturesPerStudent()).orElseThrow();
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
