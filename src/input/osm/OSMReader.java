package input.osm;

import core.Coord;
import core.SimError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class OSMReader {
    private static final double EQUATOR_CIRCUMFERENCE_IN_METERS = 6370997.0;

    private static final String WAY = "way";
    private static final String NODE = "node";
    private static final String AREA = "area";
    private static final String RELATION = "relation";

    private final OSMEntityFactory entityFactory;
    private final Document doc;

    private Double scaleFactor;

    private Map<String, OSMNode> cachedNodes;
    private Map<String, OSMWay> cachedWays;
    private Map<String, OSMRelation> cachedRelations;

    public OSMReader(String osmFileName, double projectionLatitude) {
        entityFactory = new OSMEntityFactory(osmFileName);
        doc = parseXmlDocument(new File(osmFileName));
        scaleFactor = Math.cos(Math.toRadians(projectionLatitude));
    }

    public Collection<OSMNode> getNodes() {
        return readNodes().values();
    }

    public Collection<OSMWay> getWays() {
        return readWays().values();
    }

    public Collection<OSMArea> getAreas() {
        return readWays().values().stream()
                .filter(way -> way.getTags().containsKey(AREA))
                .map(entityFactory::area)
                .collect(Collectors.toList());
    }

    public Collection<OSMMultiPolygon> getMultiPolygons() {
        List<OSMMultiPolygon> polygons = new ArrayList<>();
        for (OSMRelation relation : readRelations().values()) {
            if (!"multipolygon".equals(relation.getTags().get("type"))) {
                continue;
            }
            List<OSMArea> outerPolygons = new ArrayList<>();
            List<OSMArea> innerPolygons = new ArrayList<>();
            for (Map.Entry<OSMEntity, String> entityWithRole : relation.getMembers().entrySet()) {
                if (!(entityWithRole.getKey() instanceof OSMWay)) {
                    continue;
                }
                OSMWay way = (OSMWay) entityWithRole.getKey();
                String role = entityWithRole.getValue();
                if ("outer".equals(role)) {
                    outerPolygons.add(entityFactory.area(way));
                }
                if ("inner".equals(role)) {
                    innerPolygons.add(entityFactory.area(way));
                }
            }
            polygons.add(entityFactory.multiPolygon(relation.getId(), outerPolygons, innerPolygons, relation.getTags()));
        }
        return polygons;
    }

    private Map<String, OSMWay> readWays() {
        if (cachedWays == null) {
            Map<String, OSMWay> ways = new HashMap<>();
            Map<String, OSMNode> nodes = readNodes();
            NodeList wayNodesList = doc.getElementsByTagName(WAY);
            for (Element way : toElementList(wayNodesList)) {
                NodeList ndNodeList = way.getElementsByTagName("nd");
                List<OSMNode> points = new ArrayList<>(ndNodeList.getLength());
                for (Element nd : toElementList(ndNodeList)) {
                    String ref = nd.getAttribute("ref");
                    points.add(nodes.get(ref));
                }
                String id = readId(way);
                OSMWay osmWay = entityFactory.way(id, points, readTags(way));
                ways.put(id, osmWay);
            }
            cachedWays = ways;
        }
        return cachedWays;
    }

    private Map<String, OSMNode> readNodes() {
        if (cachedNodes == null) {
            Map<String, OSMNode> nodes = new HashMap<>();
            NodeList nodeNodeList = doc.getElementsByTagName(NODE);
            for (Element node : toElementList(nodeNodeList)) {
                double longitude = Double.parseDouble(node.getAttribute("lon"));
                double latitude = Double.parseDouble(node.getAttribute("lat"));
                double x = longitudeToX(longitude, scaleFactor);
                double y = latitudeToY(latitude, scaleFactor);
                String id = readId(node);
                OSMNode osmNode = entityFactory.node(id, new Coord(x, y), readTags(node));
                nodes.put(id, osmNode);
            }
            cachedNodes = nodes;
        }
        return cachedNodes;
    }

    private Map<String, OSMRelation> readRelations() {
        if (cachedRelations == null) {
            Map<String, OSMRelation> relations = new HashMap<>();
            NodeList relationNodeList = doc.getElementsByTagName(RELATION);
            for (Element relation : toElementList(relationNodeList)) {
                NodeList memberNodeList = relation.getElementsByTagName("member");
                Map<OSMEntity, String> members = new HashMap<>();
                for (Element member : toElementList(memberNodeList)) {
                    String type = member.getAttribute("type");
                    String ref = member.getAttribute("ref");
                    String role = member.getAttribute("role");
                    members.put(getRelatedEntity(type, ref), role);
                }
                String id = readId(relation);
                OSMRelation osmRelation = entityFactory.relation(id, members, readTags(relation));
                relations.put(id, osmRelation);
            }
            cachedRelations = relations;
        }
        return cachedRelations;
    }

    private OSMEntity getRelatedEntity(String type, String id) {
        Map<String, OSMNode> nodes = readNodes();
        Map<String, OSMWay> ways = readWays();
        switch (type) {
            case NODE:
                return nodes.get(id);
            case WAY:
                return ways.get(id);
            default:
                return entityFactory.entity(id, Map.of());
        }
    }

    private static String readId(Element element) {
        return element.getAttribute("id");
    }

    private static Map<String, String> readTags(Element element) {
        Map<String, String> tags = new HashMap<>();
        NodeList tagNodeList = element.getElementsByTagName("tag");
        for (Element tag : toElementList(tagNodeList)) {
            String key = tag.getAttribute("k");
            String value = tag.getAttribute("v");
            tags.put(key, value);
        }
        return tags;
    }

    private Document parseXmlDocument(File osmFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(osmFile);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new SimError(e.getMessage());
        }
    }

    private static double latitudeToY(double latitude, double scaleFactor) {
        return - Math.log(Math.tan(Math.PI / 4 + Math.toRadians(latitude) / 2)) * EQUATOR_CIRCUMFERENCE_IN_METERS * scaleFactor;
    }

    private static double longitudeToX(double longitude, double scaleFactor) {
        return Math.toRadians(longitude) * EQUATOR_CIRCUMFERENCE_IN_METERS * scaleFactor;
    }

    private static List<Element> toElementList(NodeList nodeList) {
        List<Element> elements = new ArrayList<>(nodeList.getLength());
        for (int index = 0; index < nodeList.getLength(); index++) {
            elements.add((Element) nodeList.item(index));
        }
        return elements;
    }
}
