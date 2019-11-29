package movement.pathfinder;

import movement.map.MapNode;

import java.util.List;

public interface PathFinder {
    List<MapNode> findPath(MapNode from, MapNode to);
}
