package movement.nodegrid;

import core.Coord;
import movement.map.MapNode;

import java.util.Optional;

public class PointOfInterestMapNode extends MapNode {
    private final Integer capacity;

    public PointOfInterestMapNode(Coord location, short type, Integer capacity) {
        super(location);
        this.addType(type);
        this.capacity = capacity;
    }

    public Optional<Integer> getCapacity() {
        return Optional.ofNullable(capacity);
    }
}
