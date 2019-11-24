package movement.fmi;

import movement.map.MapNode;

import java.util.*;
import java.util.stream.Collectors;

public class Schedule {
    private LinkedList<Event> schedule;

    public Schedule(Map<Integer, List<Event>> eventsByTimeslot, ArrayList<MapNode> collectionAreas, Random rng) {
        // pick random events
        schedule = eventsByTimeslot.keySet().stream()
                .sorted()
                .map(eventsByTimeslot::get)
                .map(events -> {
                    int locationIndex = rng.nextInt(events.size());
                    return events.get(locationIndex);
                })
                .map(event -> {
                    if (rng.nextBoolean()) {
                        return event;
                    }

                    int collectionAreaIndex = rng.nextInt(collectionAreas.size());
                    return new Event(collectionAreas.get(collectionAreaIndex), event.getTimestampStart());
                })
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public boolean hasNextEvent() {
        return schedule.size() > 0;
    }

    public Event getNextEvent() {
        return schedule.pollFirst();
    }
}
