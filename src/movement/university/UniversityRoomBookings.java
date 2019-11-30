package movement.university;

import movement.fmi.PointOfInterest;
import movement.fmi.Size;
import movement.map.MapNode;

import java.util.*;
import java.util.stream.Collectors;

public class UniversityRoomBookings {
    private static final PointOfInterest[] relevantTypes = new PointOfInterest[] {
            PointOfInterest.EXERCISE_ROOM,
            PointOfInterest.LECTURE_HALL
    };

    private final UniversityRoomBookingSettings settings;
    private final List<RoomBooking> roomBookings;

    public UniversityRoomBookings(Random rng, UniversityRoomBookingSettings settings, Set<MapNode> pointsOfInterest) {
        this.settings = settings;
        this.roomBookings = generateBookings(rng, pointsOfInterest);
    }

    public UniversityRoomBookingSettings getSettings() {
        return settings;
    }

    public List<RoomBooking> getRoomBookings() {
        return roomBookings;
    }

    private List<RoomBooking> generateBookings(Random rng, Set<MapNode> pointsOfInterest) {
        List<RoomBooking> roomBookings = new ArrayList<>();
        for (PointOfInterest type : relevantTypes) {
            for (Size size : Size.values()) {
                int timeBetweenBookings = settings.getTimeBetweenBookings().get(type).get(size);

                // lecture can only start at intervals of 15 minutes
                // convert minutes between booking into probability of geometric distribution
                double minimumInterval = 15.0 * 60.0;
                double expectedValue = timeBetweenBookings / minimumInterval;
                double probability = 1 / expectedValue;

                Set<MapNode> filteredNodes = filterPointsOfInterest(pointsOfInterest, type, size);

                for (MapNode node : filteredNodes) {
                    int offsetSeconds = settings.getFirstLecturesStart();
                    while (offsetSeconds <= settings.getLastLecturesStart()) {
                        boolean hasLectureInSlot = rng.nextDouble() < probability;
                        if (hasLectureInSlot) {
                            roomBookings.add(new RoomBooking(offsetSeconds, settings.getLectureLength(), node));
                            offsetSeconds += settings.getLectureLength();
                        } else {
                            offsetSeconds += minimumInterval;
                        }
                    }
                }

            }
        }
        return roomBookings;
    }

    private Set<MapNode> filterPointsOfInterest(Set<MapNode> pointsOfInterest, PointOfInterest type, Size size) {
        return pointsOfInterest.stream()
                .filter(node -> node.isType(type.getType()) && node.isType(size.getType()))
                .collect(Collectors.toSet());
    }

    public static class RoomBooking {
        private final int startingTime;
        private final int duration;
        private final MapNode room;

        public RoomBooking(int startingTime, int duration, MapNode room) {
            this.startingTime = startingTime;
            this.duration = duration;
            this.room = room;
        }

        public int getStartingTime() {
            return startingTime;
        }

        public int getDuration() {
            return duration;
        }

        private int getEndTime() {
            return startingTime + duration;
        }

        public MapNode getRoom() {
            return room;
        }
    }
}
