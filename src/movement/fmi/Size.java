package movement.fmi;

public enum Size {
    SMALL(NodeType.SMALL, "small"),
    MEDIUM(NodeType.MEDIUM, "medium"),
    LARGE(NodeType.LARGE, "large");

    private final NodeType type;
    private final String settingsName;

    Size(NodeType type, String settingsName) {
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
