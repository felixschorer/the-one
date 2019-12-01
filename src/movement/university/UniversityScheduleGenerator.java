package movement.university;

import movement.fmi.NodeType;
import movement.fmi.Size;
import movement.map.MapNode;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UniversityScheduleGenerator {
    private static final NodeType[] relevantTypes = new NodeType[] {
            NodeType.EXERCISE_ROOM,
            NodeType.LECTURE_HALL
    };

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
        transports = filterNodeTypes(pointOfInterests, NodeType.TRANSPORT);
        activities = filterNodeTypes(pointOfInterests, NodeType.CAFE, NodeType.STUDY_PLACE);

        this.lectureBuckets = generateLectureBuckets();
    }

    public UniversitySettings getSettings() {
        return settings;
    }

    public Optional<Schedule> generateSchedule(double numberOfLectures) {
        return pickWeightedItems(transports, PointOfInterest.property(settings.getCapacities()))
                .map(PointOfInterest::getMapNode)
                .map(mapNode -> planDay(mapNode, pickLectures(numberOfLectures)));
    }

    private Schedule planDay(MapNode initialMapNode, List<Lecture> lectures) {
        List<MovementTrigger> triggers = new ArrayList<>();
        Lecture previousLecture = null;
        for (Lecture lecture : lectures) {
            MapNode currentNode = previousLecture == null ? initialMapNode : previousLecture.getRoom();
            int startWalkingAt = lecture.getStartingTime() - estimateTravelTime(currentNode, lecture.getRoom());
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
                       triggers.add(new MovementTrigger(currentTime, lecture.getRoom()));
                   }
                }
                // free time to fill
                else {
                    final int minimumStayingTime = 5 * 60;
                    while (true) {
                        int spareTime = lecture.getStartingTime() - currentTime;
                        List<PointOfInterest> possibleActivities = new ArrayList<>();
                        for (PointOfInterest activity : activities) {
                            int travelTimeTo = estimateTravelTime(currentNode, activity.getMapNode());
                            int travelTimeFrom = estimateTravelTime(activity.getMapNode(), lecture.getRoom());
                            int timeSpent = travelTimeFrom + travelTimeTo + minimumStayingTime;
                            if (timeSpent <= spareTime) {
                                possibleActivities.add(activity);
                            }
                        }
                        if (possibleActivities.size() == 0) {
                            break;
                        }
                        Optional<PointOfInterest> pickedActivity;
                        do {
                            pickedActivity = pickWeightedItems(possibleActivities, PointOfInterest.property(settings.getCapacities()));
                        } while (pickedActivity.isEmpty());
                        currentNode = pickedActivity.get().getMapNode();
                        triggers.add(new MovementTrigger(currentTime, currentNode));
                        currentTime += minimumStayingTime;
                        startWalkingAt = lecture.getStartingTime() - estimateTravelTime(currentNode, lecture.getRoom());

                        // convert seconds into probability of geometric distribution
                        int stayTime = pickedActivity.get().getProperty(settings.getStayTimes());
                        double expectedValue = Math.min(1, stayTime / (double) minimumStayingTime);
                        // probability for leaving in a given slot
                        double probability = 1 / expectedValue;
                        while (rng.nextDouble() >= probability && currentTime < startWalkingAt) {
                            currentTime += minimumStayingTime;
                        }
                    }
                    triggers.add(new MovementTrigger(currentTime, lecture.getRoom()));
                }
            }
            previousLecture = lecture;
        }
        // go back to transport
        if (previousLecture != null) {
            triggers.add(new MovementTrigger(previousLecture.getEndTime(), initialMapNode));
        }
        return new Schedule(initialMapNode, triggers);
    }

    private int estimateTravelTime(MapNode from, MapNode to) {
        double distance = from.getLocation().distance(to.getLocation());
        double speed = 1.0;  // meters per second
        return  (int) (speed * distance * settings.getTravelTimeEstimationMagicFactor());
    }

    private List<Lecture> pickLectures(double numberOfLectures) {
        // convert expected number of lectures into probability of binomial distribution
        double probability = numberOfLectures / lectureBuckets.size();

        List<Lecture> pickedLectures = new ArrayList<>();
        for (List<Lecture> lectureBucket : lectureBuckets) {
            if (rng.nextDouble() < probability) {
                pickWeightedItems(lectureBucket, Lecture::getSize).ifPresent(pickedLectures::add);
            }
        }

        return pickedLectures;
    }

    private List<List<Lecture>> generateLectureBuckets() {
        Map<Integer, List<Lecture>> lectureBuckets = new HashMap<>();
        for (PointOfInterest room : rooms) {
            int timeBetweenBookings = room.getProperty(settings.getTimeBetweenBookings());
            int capacity = room.getProperty(settings.getCapacities());

            // lecture can only start at intervals of 15 minutes
            // convert seconds between booking into probability of geometric distribution
            double minimumInterval = 15.0 * 60.0;
            double expectedValue = timeBetweenBookings / minimumInterval + 1;
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
                    offsetSeconds += minimumInterval;
                }
            }
        }
        return lectureBuckets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private <T> Optional<T> pickWeightedItems(Collection<T> items, Function<T, Integer> weightGetter) {
        double totalWeight = items.stream().map(weightGetter).reduce(0, Integer::sum);
        double accumulatedProbabilityMass = 0;
        double randomValue = rng.nextDouble();
        for (T item : items) {
            double probabilityMass = weightGetter.apply(item) / totalWeight;
            double newAccumulatedProbabilityMass = accumulatedProbabilityMass + probabilityMass;
            if (accumulatedProbabilityMass <= randomValue && randomValue < newAccumulatedProbabilityMass) {
                return Optional.of(item);
            }
            accumulatedProbabilityMass = newAccumulatedProbabilityMass;
        }
        return Optional.empty();
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

    public static class PointOfInterest {
        private final NodeType nodeType;
        private final Size size;
        private final MapNode mapNode;

        public PointOfInterest(MapNode mapNode) {
            NodeType nodeType = null;
            for (NodeType nodeTypeToCheck : NodeType.values()) {
                if (mapNode.isType(nodeTypeToCheck.getType())) {
                    nodeType = nodeTypeToCheck;
                    break;
                }
            }
            Size size = null;
            for (Size sizeToCheck : Size.values()) {
                if (mapNode.isType(sizeToCheck.getType())) {
                    size = sizeToCheck;
                    break;
                }
            }

            this.nodeType = nodeType;
            this.size = size;
            this.mapNode = mapNode;
        }

        public <T> T getProperty(Map<NodeType, Map<Size, T>> propertyMap) {
            if (propertyMap.containsKey(nodeType) && propertyMap.get(nodeType).containsKey(size)) {
                return propertyMap.get(nodeType).get(size);
            }
            return null;
        }

        public NodeType getNodeType() {
            return nodeType;
        }

        public Size getSize() {
            return size;
        }

        public MapNode getMapNode() {
            return mapNode;
        }

        public static <T> Function<PointOfInterest, T> property(Map<NodeType, Map<Size, T>> propertyMap) {
            return pointOfInterest -> pointOfInterest.getProperty(propertyMap);
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
