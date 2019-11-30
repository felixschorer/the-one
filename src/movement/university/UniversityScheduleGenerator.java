package movement.university;

import movement.fmi.PointOfInterest;
import movement.fmi.Size;
import movement.map.MapNode;

import java.util.*;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UniversityScheduleGenerator {
    private static final PointOfInterest[] relevantTypes = new PointOfInterest[] {
            PointOfInterest.EXERCISE_ROOM,
            PointOfInterest.LECTURE_HALL
    };

    private final Random rng;
    private final UniversitySettings settings;
    private final List<List<Lecture>> lectureBuckets;

    public UniversityScheduleGenerator(Random rng, UniversitySettings settings, Set<MapNode> pointsOfInterest) {
        this.rng = rng;
        this.settings = settings;
        this.lectureBuckets = generateLectures(pointsOfInterest);
    }

    public UniversitySettings getSettings() {
        return settings;
    }

    public List<MovementTrigger> generateSchedule(double numberOfLectures) {
        return null;
    }

    private List<Lecture> pickLectures(double numberOfLectures) {
        // convert expected number of lectures into probability of binomial distribution
        double probability = numberOfLectures / lectureBuckets.size();

        List<Lecture> pickedLectures = new ArrayList<>();
        for (List<Lecture> lectureBucket : lectureBuckets) {
            if (rng.nextDouble() < probability) {
                int totalSize = lectureBucket.stream().map(Lecture::getSize).reduce(0, Integer::sum);
                double accumulatedProbabilityMass = 0;
                double randomValue = rng.nextDouble();
                for (Lecture lecture : lectureBucket) {
                    double probabilityMass = lecture.getSize() / (double) totalSize;
                    double newAccumulatedProbabilityMass = accumulatedProbabilityMass + probabilityMass;
                    if (accumulatedProbabilityMass <= randomValue && randomValue < newAccumulatedProbabilityMass) {
                        pickedLectures.add(lecture);
                        break;
                    }
                    accumulatedProbabilityMass = newAccumulatedProbabilityMass;
                }
            }
        }

        return pickedLectures;
    }

    private List<List<Lecture>> generateLectures(Set<MapNode> pointsOfInterest) {
        Map<Integer, List<Lecture>> lectureBuckets = new LinkedHashMap<>();
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
                            int bucketNumber = (offsetSeconds - settings.getFirstLecturesStart()) / settings.getLectureLength();
                            if (!lectureBuckets.containsKey(bucketNumber)) {
                                lectureBuckets.put(bucketNumber, new ArrayList<>());
                            }
                            List<Lecture> bucket = lectureBuckets.get(bucketNumber);
                            int actualSize = settings.getCapacities().get(type).get(size);
                            bucket.add(new Lecture(offsetSeconds, settings.getLectureLength(), actualSize, node));
                            offsetSeconds += settings.getLectureLength();
                        } else {
                            offsetSeconds += minimumInterval;
                        }
                    }
                }

            }
        }
        return lectureBuckets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private Set<MapNode> filterPointsOfInterest(Set<MapNode> pointsOfInterest, PointOfInterest type, Size size) {
        return pointsOfInterest.stream()
                .filter(node -> node.isType(type.getType()) && node.isType(size.getType()))
                .collect(Collectors.toSet());
    }

    public static class Schedule {
        private final MapNode initialMapNode;
        private final LinkedList<MovementTrigger> triggers;

        public Schedule(MapNode initialMapNode, List<MovementTrigger> triggers) {
            this.initialMapNode = initialMapNode;
            this.triggers = new LinkedList<>(triggers);
        }

        public MapNode getInitialMapNode() {
            return initialMapNode;
        }

        public MapNode nextMapNode() {
            return triggers.remove().getNode();
        }

        public int nextMapNodeAvailable() {
            if (triggers.size() == 0) {
                return Integer.MAX_VALUE;
            }
            return triggers.get(0).getStartingTime();
        }
    }

    public static class MovementTrigger {
        private final int startingTime;
        private final MapNode mapNode;

        public MovementTrigger(int startingTime, MapNode mapNode) {
            this.startingTime = startingTime;
            this.mapNode = mapNode;
        }

        public int getStartingTime() {
            return startingTime;
        }

        public MapNode getNode() {
            return mapNode;
        }
    }

    public static class Lecture {
        private final int startingTime;
        private final int duration;
        private final int size;
        private final MapNode room;

        public Lecture(int startingTime, int duration, int size, MapNode room) {
            this.startingTime = startingTime;
            this.duration = duration;
            this.size = size;
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

        public int getSize() {
            return size;
        }

        public MapNode getRoom() {
            return room;
        }
    }
}
