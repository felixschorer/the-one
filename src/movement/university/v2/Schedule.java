package movement.university.v2;

import movement.map.MapNode;

import java.util.LinkedList;
import java.util.List;

public class Schedule {
    private final MapNode initialMapNode;
    private final LinkedList<MovementTrigger> triggers;

    public Schedule(MapNode initialMapNode, List<MovementTrigger> triggers) {
        this.initialMapNode = initialMapNode;
        this.triggers = new LinkedList<>(triggers);
    }

    public MapNode getInitialMapNode() {
        return initialMapNode;
    }

    public MapNode nextMapNode() {
        return triggers.remove().getNode();
    }

    public int nextMapNodeAvailable() {
        if (triggers.size() == 0) {
            return Integer.MAX_VALUE;
        }
        return triggers.get(0).getStartingTime();
    }
}
