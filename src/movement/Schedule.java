package movement;

import core.Coord;
import movement.map.MapNode;
import java.util.*;

public class Schedule {
    private int eventIndex;
    private List<Event> schedule;

    public Schedule() {
        // init dummy data
        Event event1 = new Event(new MapNode(new Coord(150, 250)), 0);
        Event event2 = new Event(new MapNode(new Coord(10, 10)), 100);
        eventIndex = -1;
        schedule = Arrays.asList(event1, event2);
    }

    public List<Event> getSchedule() {
        return schedule;
    }

    // get next event and increment index such as when calling it next time again we always get the next
    public Event getNextEvent() {
        if (eventIndex < schedule.size() - 1) {
            eventIndex++;
        }

        return schedule.get(eventIndex);
    }
}
