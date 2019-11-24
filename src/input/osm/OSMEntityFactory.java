package input.osm;

import core.Coord;

import java.util.List;
import java.util.Map;

public class OSMEntityFactory {
    private final String fileName;

    public OSMEntityFactory(String fileName) {
        this.fileName = fileName;
    }

    public OSMEntity entity(String id, Map<String, String> tags) {
        return new OSMEntity(fileName, id, tags);
    }

    public OSMNode node(String id, Coord location, Map<String, String> tags) {
        return new OSMNode(fileName, id, location, tags);
    }

    public OSMWay way(String id, List<OSMNode> nodes, Map<String, String> tags) {
        return new OSMWay(fileName, id, nodes, tags);
    }

    public OSMArea area(OSMWay way) {
        return new OSMArea(way.getFileName(), way.getId(), way.getNodes(), way.getTags());
    }

    public OSMArea area(String id, List<OSMNode> nodes, Map<String, String> tag) {
        return new OSMArea(fileName, id, nodes, tag);
    }

    public OSMMultiPolygon multiPolygon(String id, List<OSMArea> outerPolygons,
                                        List<OSMArea> innerPolygons, Map<String, String> tags) {
        return new OSMMultiPolygon(fileName, id, outerPolygons, innerPolygons, tags);
    }

    public OSMRelation relation(String id, Map<OSMEntity, String> members, Map<String, String> tags) {
        return new OSMRelation(fileName, id, members, tags);
    }
}
