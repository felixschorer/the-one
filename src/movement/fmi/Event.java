package movement.fmi;

import movement.map.MapNode;

public class Event {
    private MapNode location;
    private int timestampStart;
    private int timestampEnd;

    public Event(MapNode location, int timestampStart, int timestampEnd) {
        this.location = location;
        this.timestampStart = timestampStart;
        this.timestampEnd = timestampEnd;
    }

    public MapNode getLocation() {
        return location;
    }

    public int getTimestampStart() {
        return timestampStart;
    }

    public int getTimestampEnd() {
        return timestampEnd;
    }
}
