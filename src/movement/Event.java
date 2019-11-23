package movement;

import movement.map.MapNode;

public class Event {
    private MapNode location;
    private int timestamp;

    Event(MapNode location, int timestamp) {
        this.location = location;
        this.timestamp = timestamp;
    }

    public MapNode getLocation() {
        return location;
    }

    public int getTimestamp() {
        return timestamp;
    }
}
