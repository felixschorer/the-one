package input.osm;

import java.util.Map;
import java.util.Objects;

public class OSMEntity {
    private final String fileName;
    private final String id;
    private final Map<String, String> tags;

    public OSMEntity(String fileName, String id, Map<String, String> tags) {
        this.fileName = fileName;
        this.id = id;
        this.tags = tags;
    }

    public String getFileName() {
        return fileName;
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OSMEntity osmEntity = (OSMEntity) o;
        return getFileName().equals(osmEntity.getFileName()) &&
                getId().equals(osmEntity.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileName(), getId());
    }
}
