package movement.university.v2;

import movement.map.MapNode;

public class MovementTrigger {
    private final int startingTime;
    private final MapNode mapNode;

    public MovementTrigger(int startingTime, MapNode mapNode) {
        this.startingTime = startingTime;
        this.mapNode = mapNode;
    }

    public int getStartingTime() {
        return startingTime;
    }

    public MapNode getNode() {
        return mapNode;
    }
}
