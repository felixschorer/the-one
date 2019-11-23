package movement;

import movement.map.MapNode;

public class Event {
    MapNode location;
    int timestamp;

    Event(MapNode location, int timestamp) {
        this.location = location;
        this.timestamp = timestamp;
    }
}
