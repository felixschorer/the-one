package input;

import core.Coord;
import core.Polygon;
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

public class OSMReader {
    private static final double EQUATOR_CIRCUMFERENCE_IN_METERS = 6378137.0;

    private double referenceX;
    private double referenceY;

    public OSMReader(double referenceLongitude, double referenceLatitude) {
        referenceX = longitudeToX(referenceLongitude);
        referenceY = latitudeToY(referenceLatitude);
    }

    public List<Polygon> readPolygons(File osmFile) {
        return readPolygons(parseXmlDocument(osmFile));
    }

    public List<Polygon> readPolygons(Document doc) {
        List<Polygon> polygons = new ArrayList<>();

        Map<String, Coord> nodes = readNodes(doc);

        NodeList wayNodesList = doc.getElementsByTagName("way");
        for (Element way : toElementList(wayNodesList)) {
            if (!isArea(way)) {
                continue;
            }
            NodeList ndNodeList = way.getElementsByTagName("nd");
            List<Coord> vertices = new ArrayList<>(ndNodeList.getLength());
            for (Element nd : toElementList(ndNodeList)) {
                String ref = nd.getAttribute("ref");
                vertices.add(nodes.get(ref));
            }

            polygons.add(new Polygon(vertices.toArray(Coord[]::new)));
        }

        return polygons;
    }

    private Map<String, Coord> readNodes(Document doc) {
        Map<String, Coord> nodes = new HashMap<>();
        NodeList nodeNodeList = doc.getElementsByTagName("node");
        for (Element node : toElementList(nodeNodeList)) {
            double longitude = Double.parseDouble(node.getAttribute("lon"));
            double latitude = Double.parseDouble(node.getAttribute("lat"));
            double x = longitudeToX(longitude) - referenceX;
            double y = latitudeToY(latitude) - referenceY;
            nodes.put(node.getAttribute("id"), new Coord(x, y));
        }
        return nodes;
    }

    private boolean isArea(Element way) {
        NodeList tagNodeList = way.getElementsByTagName("tag");
        for (Element tag : toElementList(tagNodeList)) {
            if (tag.getAttribute("k").equals("area")) {
                return tag.getAttribute("v").equals("yes");
            }
        }
        return false;
    }

    public static double latitudeToY(double latitude) {
        return Math.log(Math.tan(Math.PI / 4 + Math.toRadians(latitude) / 2)) * EQUATOR_CIRCUMFERENCE_IN_METERS;
    }
    public static double longitudeToX(double longitude) {
        return Math.toRadians(longitude) * EQUATOR_CIRCUMFERENCE_IN_METERS;
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

    private static List<Element> toElementList(NodeList nodeList) {
        List<Element> elements = new ArrayList<>(nodeList.getLength());
        for (int index = 0; index < nodeList.getLength(); index++) {
            elements.add((Element) nodeList.item(index));
        }
        return elements;
    }
}
