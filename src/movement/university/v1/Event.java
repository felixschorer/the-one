package movement.university.v1;

import movement.map.MapNode;

public class Event {
    private MapNode location;
    private int timestampStart;

    public Event(MapNode location, int timestampStart) {
        this.location = location;
        this.timestampStart = timestampStart;
    }

    public MapNode getLocation() {
        return location;
    }

    public int getTimestampStart() {
        return timestampStart;
    }

    public int getRealStart() {
        return getTimestampStart();
    }
}
