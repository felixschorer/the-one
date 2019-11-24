package movement.fmi;

import java.util.*;
import java.util.stream.Collectors;

public class Schedule {
    private LinkedList<Event> schedule;

    public Schedule(Map<Integer, List<Event>> eventsByTimeslot, Random rng) {
        // pick random events
        schedule = eventsByTimeslot.keySet().stream()
                .filter(timeslot -> rng.nextBoolean())
                .sorted()
                .map(eventsByTimeslot::get)
                .map(events -> events.get(rng.nextInt(events.size())))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public boolean hasNextEvent() {
        return schedule.size() > 0;
    }

    public Event getNextEvent() {
        return schedule.pollFirst();
    }
}
