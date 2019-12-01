package movement.university.v2;

import movement.map.MapNode;

public class Lecture {
    private final int startingTime;
    private final int duration;
    private final int size;
    private final MapNode room;

    public Lecture(int startingTime, int duration, int size, MapNode room) {
        this.startingTime = startingTime;
        this.duration = duration;
        this.size = size;
        this.room = room;
    }

    public int getStartingTime() {
        return startingTime;
    }

    public int getDuration() {
        return duration;
    }

    public int getEndTime() {
        return startingTime + duration;
    }

    public int getSize() {
        return size;
    }

    public MapNode getRoom() {
        return room;
    }
}
