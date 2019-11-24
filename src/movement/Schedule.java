package movement;

import movement.map.MapNode;
import java.util.*;

public class Schedule {
    private LinkedList<Event> schedule;

    public Schedule(Set<MapNode> pointsOfInterest) {
        schedule = new LinkedList<>();
        int time = 1000;
        for (MapNode node : pointsOfInterest) {
            schedule.add(new Event(node, time));
            time += 1000;
        }
    }

    public boolean hasNextEvent() {
        return schedule.size() > 0;
    }

    public Event getNextEvent() {
        return schedule.pollFirst();
    }
}
