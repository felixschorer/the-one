package movement;

import core.Coord;
import core.Settings;
import movement.nodegrid.NodeGridMovementModel;
import movement.university.UniversitySettings;
import movement.university.UniversityScheduleGenerator;


public class UniversityMovement extends NodeGridMovementModel {
    private static UniversityScheduleGenerator roomBookings = null;

    public UniversityMovement(Settings settings) {
        super(settings);
        UniversitySettings universitySettings = new UniversitySettings();
        if (roomBookings == null || !roomBookings.getSettings().equals(universitySettings)) {
            roomBookings = new UniversityScheduleGenerator(rng, universitySettings, getPointsOfInterest());
        }
    }

    public UniversityMovement(NodeGridMovementModel mm) {
        super(mm);
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public boolean isActive() {
        return super.isActive();
    }

    @Override
    public double nextPathAvailable() {
        return super.nextPathAvailable();
    }

    @Override
    public Coord getInitialLocation() {
        return null;
    }

    @Override
    public MovementModel replicate() {
        return new UniversityMovement(this);
    }
}
