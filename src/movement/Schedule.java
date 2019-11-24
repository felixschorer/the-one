package movement;

import movement.map.MapNode;
import java.util.*;

public class Schedule {
    private int eventIndex;
    private List<Event> schedule;

    public Schedule(Set<MapNode> pointsOfInterest) {
        // init dummy data
        schedule = new ArrayList<>();
        int time = 0;
        for (MapNode node : pointsOfInterest) {
            schedule.add(new Event(node, time));
            time += 100;
        }

        eventIndex = -1;
    }

    // get next event and increment index such as when calling it next time again we always get the next
    public Event getNextEvent() {
        if (eventIndex < schedule.size() - 1) {
            eventIndex++;
        }

        return schedule.get(eventIndex);
    }
}
