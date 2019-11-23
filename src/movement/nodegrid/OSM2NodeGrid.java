package movement.nodegrid;

import core.Coord;
import input.osm.*;
import movement.map.MapNode;
import movement.map.SimMap;

import java.util.*;

public class OSM2NodeGrid {
    private static final String DOOR = "door";
    private static final String FOOT_WAY = "footway";
    private static final String USAGE = "usage";
    private static final String LECTURE_HALL = "lecture_hall";

    private Set<MapNode> pointsOfInterest;
    private SimMap simMap;

    public OSM2NodeGrid(NodeGridSettings settings) {
        load(settings);
    }

    public Set<MapNode> getPointsOfInterest() {
        return pointsOfInterest;
    }

    public SimMap getSimMap() {
        return simMap;
    }

    // TODO: split this method into smaller chucks
    private void load(NodeGridSettings settings) {
        NodeGridBuilder builder = new NodeGridBuilder(settings.getRasterInterval());
        Set<MapNode> pointsOfInterest = new HashSet<>();
        Map<Coord, MapNode> mapNodes = new HashMap<>();

        for (String path : settings.getOsmFiles()) {
            OSMReader reader = new OSMReader(path, settings.getReferenceLong(), settings.getReferenceLat());

            // add simple polygons
            for (OSMArea area : reader.getAreas()) {
                builder.add(area.getPolygon());
            }

            // add multi polygons
            for (OSMMultiPolygon polygon : reader.getMultiPolygons()) {
                for (OSMArea area : polygon.getOuterPolygons()) {
                    builder.add(area.getPolygon());
                }
                for (OSMArea area : polygon.getInnerPolygons()) {
                    builder.subtract(area.getPolygon());
                }
            }

            // add functional nodes
            for (OSMNode osmNode : reader.getNodes()) {
                if (mapNodes.containsKey(osmNode.getLocation())) {
                    continue;
                }
                MapNode node = new MapNode(osmNode.getLocation());

                if (osmNode.getTags().containsKey(DOOR)) {
                    mapNodes.put(osmNode.getLocation(), node);
                    builder.attachNodeByClosestNodes(node, 3);
                }

                if (LECTURE_HALL.equals(osmNode.getTags().get(USAGE))) {
                    mapNodes.put(osmNode.getLocation(), node);
                    pointsOfInterest.add(node);
                }
            }

            // add paths
            for (OSMWay way : reader.getWays()) {
                if (way.getTags().containsKey(FOOT_WAY)) {
                    List<OSMNode> waypoints = way.getNodes();

                    // add missing intermediary waypoints
                    for (OSMNode osmNode : waypoints) {
                        if (!mapNodes.containsKey(osmNode.getLocation())) {
                            MapNode node = new MapNode(osmNode.getLocation());
                            mapNodes.put(osmNode.getLocation(), node);
                        }
                    }

                    // connect waypoints
                    for (int index = 0; index < waypoints.size() - 1; index++) {
                        MapNode current = mapNodes.get(waypoints.get(index).getLocation());
                        MapNode next = mapNodes.get(waypoints.get(index + 1).getLocation());
                        current.addNeighbor(next);
                        next.addNeighbor(current);
                    }
                }
            }
        }

        mapNodes.putAll(builder.build());
        this.simMap = new SimMap(mapNodes);
        this.pointsOfInterest = pointsOfInterest;
    }
}
