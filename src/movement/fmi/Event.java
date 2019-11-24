package movement.fmi;

import movement.map.MapNode;

import java.util.Random;

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
