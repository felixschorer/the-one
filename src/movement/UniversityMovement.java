package movement;

import core.Coord;
import core.Settings;
import movement.nodegrid.NodeGridMovementModel;
import movement.university.UniversityRoomBookingSettings;
import movement.university.UniversityRoomBookings;


public class UniversityMovement extends NodeGridMovementModel {
    private static UniversityRoomBookings roomBookings = null;

    public UniversityMovement(Settings settings) {
        super(settings);
        UniversityRoomBookingSettings universityRoomBookingSettings = new UniversityRoomBookingSettings();
        if (roomBookings == null || !roomBookings.getSettings().equals(universityRoomBookingSettings)) {
            roomBookings = new UniversityRoomBookings(rng, universityRoomBookingSettings, getPointsOfInterest());
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
