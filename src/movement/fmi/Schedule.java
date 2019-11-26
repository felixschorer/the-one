package movement.fmi;

import movement.map.MapNode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Schedule {
    private LinkedList<Event> schedule;
    private int SWITCHING_PLACE_TIME = 60 * 15;

    public Schedule(ArrayList<Lecture> lectures, ArrayList<MapNode> collectionAreas, Random rng) {
        lectures.sort(Comparator.comparing(Lecture::getRealStart));

        LinkedList<Event> lectureSchedule = new LinkedList<>();
        LinkedList<Event> otherSchedule = new LinkedList<>();

        // fill up time with lectures
        int currentScheduleTime = 0;
        for (Lecture lecture : lectures) {
            int currentAvailableTime = currentScheduleTime + SWITCHING_PLACE_TIME;
            boolean isLectureTimeDoable = currentAvailableTime <= lecture.getRealStart();
            boolean isOtherEventPreferred = rng.nextBoolean();
            if (isLectureTimeDoable && !isOtherEventPreferred) {
                lectureSchedule.add(lecture);
                currentScheduleTime += lecture.getRealEnd();
            }
        }

        // go somewhere else between lectures if time allows
        for (int i = 1; i < lectureSchedule.size(); i++) {
            Lecture lecture1 = (Lecture) lectureSchedule.get(i - 1);
            Lecture lecture2 = (Lecture) lectureSchedule.get(i);
            int currentAvailableTime = lecture1.getRealEnd() + SWITCHING_PLACE_TIME;

            if (lecture2.getRealStart() - currentAvailableTime > 0) {
                int collectionAreaIndex = rng.nextInt(collectionAreas.size());
                Event otherEvent = new Event(collectionAreas.get(collectionAreaIndex), currentAvailableTime);
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
