package movement.university;

public enum Size {
    SMALL(OSMMapping.SMALL, "small"),
    MEDIUM(OSMMapping.MEDIUM, "medium"),
    LARGE(OSMMapping.LARGE, "large");

    private final OSMMapping type;
    private final String settingsName;

    Size(OSMMapping type, String settingsName) {
        this.type = type;
        this.settingsName = settingsName;
    }

    public int getType() {
        return type.getType();
    }

    public String getSettingsName() {
        return settingsName;
    }
}
