package movement.fmi;

import movement.map.MapNode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Schedule {
    private LinkedList<Event> schedule;
    private int SWITCHING_PLACE_TIME = 60 * 15;
    private int MAX_TIME_AT_UNIVERSITY = 60 * 60 * 10;

    public Schedule(ArrayList<Lecture> fixedEvents, ArrayList<MapNode> otherAreas, Random rng) {
        LinkedList<Event> lectureSchedule = new LinkedList<>();
        LinkedList<Event> otherSchedule = new LinkedList<>();

        // fill up time with lectures/exercises
        int currentScheduleTime = 0;
        while (currentScheduleTime < MAX_TIME_AT_UNIVERSITY) {
            int currentAvailableTime = currentScheduleTime + SWITCHING_PLACE_TIME;
            ArrayList<Lecture> possibleLectures = new ArrayList<>(fixedEvents);
            possibleLectures.stream()
                    .filter(lecture -> lecture.getRealStart() >= currentAvailableTime)
                    .collect(Collectors.toCollection(ArrayList::new));
            int randomPossibleLectureIndex = rng.nextInt(possibleLectures.size());
            boolean isOtherEventPreferred = rng.nextBoolean();
            if (!isOtherEventPreferred) {
                Lecture lecture = possibleLectures.get(randomPossibleLectureIndex);
                lectureSchedule.add(lecture);
                currentScheduleTime += lecture.getRealEnd();
            }
        }

        // go somewhere else between lectures/exercises if time allows
        for (int i = 1; i < lectureSchedule.size(); i++) {
            Lecture lecture1 = (Lecture) lectureSchedule.get(i - 1);
            Lecture lecture2 = (Lecture) lectureSchedule.get(i);
            int currentAvailableTime = lecture1.getRealEnd() + SWITCHING_PLACE_TIME;

            if (lecture2.getRealStart() - currentAvailableTime > 0) {
                int collectionAreaIndex = rng.nextInt(otherAreas.size());
                Event otherEvent = new Event(otherAreas.get(collectionAreaIndex), currentAvailableTime);
                otherSchedule.add(otherEvent);
            }
        }

        schedule = Stream.of(lectureSchedule, otherSchedule)
                .flatMap(x -> x.stream())
                .collect(Collectors.toCollection(LinkedList::new));

        schedule.sort(Comparator.comparing(Event::getRealStart));
    }

    public boolean hasNextEvent() {
        return schedule.size() > 0;
    }

    public Event getNextEvent() {
        return schedule.pollFirst();
    }
}
