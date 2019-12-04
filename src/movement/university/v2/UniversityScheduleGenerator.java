package movement.university.v2;

import movement.university.NodeType;
import movement.map.MapNode;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UniversityScheduleGenerator {
    private static final int MINIMUM_STAY_TIME = 5 * 60;
    private static final int MINIMUM_TIME_BETWEEN_BOOKINGS = 15 * 60;

    private final Random rng;
    private final UniversitySettings settings;
    private final List<List<Lecture>> lectureBuckets;
    private final List<PointOfInterest> rooms;
    private final List<PointOfInterest> transports;
    private final List<PointOfInterest> activities;

    public UniversityScheduleGenerator(Random rng, UniversitySettings settings, Set<MapNode> nodes) {
        this.rng = rng;
        this.settings = settings;
        List<PointOfInterest> pointOfInterests = nodes.stream()
                .map(PointOfInterest::new)
                .collect(Collectors.toList());
        rooms = filterNodeTypes(pointOfInterests, NodeType.EXERCISE_ROOM, NodeType.LECTURE_HALL);
        transports = filterNodeTypes(pointOfInterests, NodeType.COLLECTION_AREA);
        activities = filterNodeTypes(pointOfInterests, NodeType.STUDY_PLACE, NodeType.COLLECTION_AREA);

        this.lectureBuckets = generateLectureBuckets();
    }

    public UniversitySettings getSettings() {
        return settings;
    }

    public Schedule generateSchedule(UniversityGroupSettings groupSettings) {
        MapNode initialMapNode = pickWeightedItems(transports, PointOfInterest.property(settings.getCapacities())).getMapNode();
        List<Lecture> lectures = pickLectures(groupSettings.getMeanLecturesPerStudent());
        List<MovementTrigger> triggers = new ArrayList<>();
        Lecture previousLecture = null;
        for (Lecture lecture : lectures) {
            MapNode currentNode = previousLecture == null ? initialMapNode : previousLecture.getRoom();
            int startWalkingAt = lecture.getStartingTime() - estimateTravelTime(currentNode, lecture.getRoom(), groupSettings.getDistanceEstimateDistortion())
                    + (int) nextTemporalOffset(groupSettings.getTemporalSpread());
            int currentTime = previousLecture == null ? startWalkingAt : previousLecture.getEndTime();
            // come form transport
            if (previousLecture == null) {
                triggers.add(new MovementTrigger(startWalkingAt, lecture.getRoom()));
            }
            // already at university
            else {
                // lecture overlap
                if (currentTime > startWalkingAt) {
                   if (currentTime - startWalkingAt > settings.getLectureLength() / 2) {
                       // missing over 50% of the lecture, skip lecture
                       continue;
                   } else {
                       // come in late
                       triggers.add(new MovementTrigger(currentTime + (int) nextTemporalOffset(groupSettings.getTemporalSpread()), lecture.getRoom()));
                   }
                }
                // free time to fill
                else {
                    while (true) {
                        int spareTime = lecture.getStartingTime() - currentTime;
                        List<PointOfInterest> possibleActivities = new ArrayList<>();
                        for (PointOfInterest activity : activities) {
                            int travelTimeTo = estimateTravelTime(currentNode, activity.getMapNode(), groupSettings.getDistanceEstimateDistortion());
                            int travelTimeFrom = estimateTravelTime(activity.getMapNode(), lecture.getRoom(), groupSettings.getDistanceEstimateDistortion());
                            int timeSpent = travelTimeFrom + travelTimeTo + MINIMUM_STAY_TIME;
                            if (timeSpent <= spareTime) {
                                possibleActivities.add(activity);
                            }
                        }
                        if (possibleActivities.size() == 0) {
                            break;
                        }
                       PointOfInterest pickedActivity = pickWeightedItems(possibleActivities, PointOfInterest.property(settings.getCapacities()));
                        currentNode = pickedActivity.getMapNode();
                        triggers.add(new MovementTrigger(currentTime + (int) nextTemporalOffset(groupSettings.getTemporalSpread()), currentNode));
                        currentTime += MINIMUM_STAY_TIME;
                        startWalkingAt = lecture.getStartingTime() - estimateTravelTime(currentNode, lecture.getRoom(), groupSettings.getDistanceEstimateDistortion());

                        // convert seconds into probability of geometric distribution
                        int stayTime = pickedActivity.getProperty(settings.getStayTimes());
                        double expectedValue = Math.max(1, stayTime / (double) MINIMUM_STAY_TIME);
                        // probability for leaving in a given slot
                        double probability = 1 / expectedValue;
                        while (rng.nextDouble() >= probability && currentTime < startWalkingAt) {
                            currentTime += MINIMUM_STAY_TIME;
                        }
                    }
                    triggers.add(new MovementTrigger(currentTime + (int) nextTemporalOffset(groupSettings.getTemporalSpread()), lecture.getRoom()));
                }
            }
            previousLecture = lecture;
        }
        // go back to transport
        if (previousLecture != null) {
            triggers.add(new MovementTrigger(previousLecture.getEndTime() + (int) nextTemporalOffset(groupSettings.getTemporalSpread()), initialMapNode));
        }
        return new Schedule(initialMapNode, triggers);
    }

    private double nextTemporalOffset(double variance) {
        return rng.nextGaussian() * variance;
    }

    private int estimateTravelTime(MapNode from, MapNode to, double distortionFactor) {
        double distance = from.getLocation().distance(to.getLocation());
        double distortion = Math.sqrt(distance * distance / 2) * 2 - distance;
        double distortedDistance = distance + distortion * distortionFactor;
        double speed = 1.0;  // meters per second
        return  (int) (speed * distortedDistance);
    }

    private List<Lecture> pickLectures(double numberOfLectures) {
        // convert expected number of lectures into probability of binomial distribution
        double probability = numberOfLectures / lectureBuckets.size();

        List<Lecture> pickedLectures = new ArrayList<>();
        for (List<Lecture> lectureBucket : lectureBuckets) {
            if (rng.nextDouble() < probability) {
                pickedLectures.add(pickWeightedItems(lectureBucket, Lecture::getSize));
            }
        }

        return pickedLectures;
    }

    private List<List<Lecture>> generateLectureBuckets() {
        Map<Integer, List<Lecture>> lectureBuckets = new HashMap<>();
        for (PointOfInterest room : rooms) {
            int timeBetweenBookings = room.getProperty(settings.getTimeBetweenBookings());
            int capacity = room.getProperty(settings.getCapacities());

            // convert seconds between booking into probability of geometric distribution
            double expectedValue = timeBetweenBookings / (double) MINIMUM_TIME_BETWEEN_BOOKINGS + 1;
            // probability for having a lecture in the a given slot
            double probability = 1 / expectedValue;

            int offsetSeconds = settings.getFirstLecturesStart();
            while (offsetSeconds <= settings.getLastLecturesStart()) {
                boolean hasLectureInSlot = rng.nextDouble() < probability;
                if (hasLectureInSlot) {
                    int bucketNumber = (offsetSeconds - settings.getFirstLecturesStart()) / settings.getLectureLength();
                    if (!lectureBuckets.containsKey(bucketNumber)) {
                        lectureBuckets.put(bucketNumber, new ArrayList<>());
                    }
                    List<Lecture> bucket = lectureBuckets.get(bucketNumber);
                    bucket.add(new Lecture(offsetSeconds, settings.getLectureLength(), capacity, room.getMapNode()));
                    offsetSeconds += settings.getLectureLength();
                } else {
                    offsetSeconds += MINIMUM_TIME_BETWEEN_BOOKINGS;
                }
            }
        }
        return lectureBuckets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private <T> T pickWeightedItems(Collection<T> items, Function<T, Integer> weightGetter) {
        double totalWeight = items.stream().map(weightGetter).reduce(0, Integer::sum);
        double accumulatedProbabilityMass = 0;
        double randomValue = rng.nextDouble();
        for (T item : items) {
            double probabilityMass = weightGetter.apply(item) / totalWeight;
            double newAccumulatedProbabilityMass = accumulatedProbabilityMass + probabilityMass;
            if (accumulatedProbabilityMass <= randomValue && randomValue < newAccumulatedProbabilityMass) {
                return item;
            }
            accumulatedProbabilityMass = newAccumulatedProbabilityMass;
        }
        return null;
    }

    private List<PointOfInterest> filterNodeTypes(List<PointOfInterest> pointOfInterests, NodeType... nodeTypes) {
        return pointOfInterests.stream()
                .filter(pointOfInterest -> {
                    for (NodeType nodeType : nodeTypes) {
                        if (pointOfInterest.getNodeType() == nodeType) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

}
