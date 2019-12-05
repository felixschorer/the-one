package movement.university.v1;

import movement.map.MapNode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Schedule {
    private LinkedList<Event> schedule;
    private int SWITCHING_PLACE_TIME = 60 * 15;
    private int MAX_TIME_AT_UNIVERSITY = 60 * 60 * 10;

    public Schedule(ArrayList<Lecture> fixedEvents, ArrayList<MapNode> outsideBuildingPois, ArrayList<MapNode> otherPois, Random rng) {
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
                int collectionAreaIndex = rng.nextInt(otherPois.size());
                Event otherEvent = new Event(otherPois.get(collectionAreaIndex), currentAvailableTime);
                otherSchedule.add(otherEvent);
            }
        }

        schedule = Stream.of(lectureSchedule, otherSchedule)
                .flatMap(x -> x.stream())
                .collect(Collectors.toCollection(LinkedList::new));

        schedule.sort(Comparator.comparing(Event::getRealStart));

        // leave the building in the end of the university day
        if (schedule.size() > 0) {
            Event lastEvent = schedule.get(schedule.size() - 1);
            int currentAvailableTime = lastEvent instanceof Lecture ? ((Lecture) lastEvent).getRealEnd() : MAX_TIME_AT_UNIVERSITY;
            int collectionAreaIndex = rng.nextInt(outsideBuildingPois.size());
            Event otherEvent = new Event(outsideBuildingPois.get(collectionAreaIndex), currentAvailableTime);
            schedule.add(otherEvent);
        }
    }

    public boolean hasNextEvent() {
        return schedule.size() > 0;
    }

    public Event getNextEvent() {
        return schedule.pollFirst();
    }
}
