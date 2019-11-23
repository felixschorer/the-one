package input.osm;

import java.util.Map;

public class OSMRelation extends OSMEntity {
    private final Map<OSMEntity, String> members;

    public OSMRelation(String id, Map<OSMEntity, String> members, Map<String, String> tags) {
        super(id, tags);
        this.members = members;
    }

    public Map<OSMEntity, String> getMembers() {
        return members;
    }
}
