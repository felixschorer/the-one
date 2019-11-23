package input.osm;

import java.util.List;
import java.util.Map;

public class OSMMultiPolygon extends OSMEntity {
    private final List<OSMArea> outerPolygons;
    private final List<OSMArea> innerPolygons;

    public OSMMultiPolygon(
            String fileName,
            String id,
            List<OSMArea> outerPolygons,
            List<OSMArea> innerPolygons,
            Map<String, String> tags
    ) {
        super(fileName, id, tags);
        this.outerPolygons = outerPolygons;
        this.innerPolygons = innerPolygons;
    }

    public List<OSMArea> getOuterPolygons() {
        return outerPolygons;
    }

    public List<OSMArea> getInnerPolygons() {
        return innerPolygons;
    }
}
