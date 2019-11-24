package movement.nodegrid;

import core.Coord;
import core.Polygon;
import input.osm.*;
import movement.map.MapNode;
import movement.map.SimMap;

import java.util.*;

public class OSM2NodeGrid {
    private static final String ATTACHMENT_POINT = "one:attachment_point";

    private static final String EDGE = "one:edge";
    private static final String BIDIRECTIONAL = "bidirectional";

    private static final String POINT_OF_INTEREST = "one:point_of_interest";

    private final NodeGridSettings settings;

    private Set<MapNode> pointsOfInterest;
    private SimMap simMap;

    public OSM2NodeGrid(NodeGridSettings settings) {
        this.settings = settings;
        load();
    }

    public Set<MapNode> getPointsOfInterest() {
        return pointsOfInterest;
    }

    public SimMap getSimMap() {
        return simMap;
    }

    public NodeGridSettings getSettings() {
        return settings;
    }

    private void load() {
        NodeGridBuilder builder = new NodeGridBuilder(settings.getRasterInterval());
        Set<MapNode> pointsOfInterest = new HashSet<>();
        Map<Coord, MapNode> nodes = new HashMap<>();

        OSMReader reader = new OSMReader(settings.getOsmFile(), settings.getReferenceLong(), settings.getReferenceLat());

        loadIncludedAreas(reader).forEach(builder::add);
        loadExcludedAreas(reader).forEach(builder::subtract);

        for (MapNode node : loadPointsOfInterest(reader)) {
            nodes.put(node.getLocation(), node);
            pointsOfInterest.add(node);
        }

        insertAttachmentNodes(builder, nodes, reader);
        insertEdges(nodes, reader);

        nodes.putAll(builder.build());
        this.simMap = new SimMap(nodes);
        this.pointsOfInterest = pointsOfInterest;
    }

    private void insertEdges(Map<Coord, MapNode> nodes, OSMReader reader) {
        for (OSMWay way : reader.getWays()) {
            Map<String, String> tags = way.getTags();
            if (!tags.containsKey(EDGE)) {
                continue;
            }

            List<OSMNode> wayPoints = way.getNodes();

            // add missing nodes
            for (OSMNode node : wayPoints) {
                if (!nodes.containsKey(node.getLocation())) {
                    nodes.put(node.getLocation(), new MapNode(node.getLocation()));
                }
            }

            // connect nodes
            for (int index = 0; index < wayPoints.size() - 1; index++) {
                MapNode current = nodes.get(wayPoints.get(index).getLocation());
                MapNode next = nodes.get(wayPoints.get(index + 1).getLocation());
                current.addNeighbor(next);
                if (BIDIRECTIONAL.equals(way.getTags().get(EDGE))) {
                    next.addNeighbor(current);
                }
            }
        }
    }

    private void insertAttachmentNodes(NodeGridBuilder builder, Map<Coord, MapNode> nodes, OSMReader reader) {
        Map<Coord, Integer> attachments = loadAttachments(reader);
        for (Coord location : attachments.keySet()) {

            // add missing node
            if (!nodes.containsKey(location)) {
                nodes.put(location, new MapNode(location));
            }

            MapNode node = nodes.get(location);
            int attachmentPoints = attachments.get(location);
            builder.attachNodeByClosestNodes(node, attachmentPoints);
        }
    }

    private List<Polygon> loadIncludedAreas(OSMReader reader) {
        List<Polygon> polygons = new ArrayList<>();
        for (OSMArea area : reader.getAreas()) {
            polygons.add(area.getPolygon());
        }
        for (OSMMultiPolygon multiPolygon : reader.getMultiPolygons()) {
            for (OSMArea area : multiPolygon.getOuterPolygons()) {
                polygons.add(area.getPolygon());
            }
        }
        return polygons;
    }

    private List<Polygon> loadExcludedAreas(OSMReader reader) {
        List<Polygon> polygons = new ArrayList<>();
        for (OSMMultiPolygon polygon : reader.getMultiPolygons()) {
            for (OSMArea area : polygon.getInnerPolygons()) {
                polygons.add(area.getPolygon());
            }
        }
        return polygons;
    }

    private List<MapNode> loadPointsOfInterest(OSMReader reader) {
        List<MapNode> nodes = new ArrayList<>();
        for (OSMNode node : reader.getNodes()) {
            Map<String, String> tags = node.getTags();
            if (!tags.containsKey(POINT_OF_INTEREST)) {
                continue;
            }

            int[] types = parseTypes(tags.get(POINT_OF_INTEREST));
            nodes.add(new MapNode(node.getLocation(), types));
        }
        return nodes;
    }

    private Map<Coord, Integer> loadAttachments(OSMReader reader) {
        Map<Coord, Integer> attachmentCoordinates = new HashMap<>();
        for (OSMNode node : reader.getNodes()) {
            Map<String, String> tags = node.getTags();
            if (!tags.containsKey(ATTACHMENT_POINT)) {
                continue;
            }

            int attachmentPoints = Integer.parseInt(tags.get(ATTACHMENT_POINT));
            double rasterInterval = settings.getRasterInterval();
            int scaledAttachmentPoints = Math.max(1, (int) Math.round(attachmentPoints / rasterInterval));
            attachmentCoordinates.put(node.getLocation(), scaledAttachmentPoints);
        }
        return attachmentCoordinates;
    }

    private static int[] parseTypes(String commaSeparatedTypes) {
        String[] values = commaSeparatedTypes.split("\\s*,\\s*");
        int[] types = new int[values.length];
        for (int index = 0; index < values.length; index++) {
            types[index] = Short.parseShort(values[index]);
        }
        return types;
    }
}
