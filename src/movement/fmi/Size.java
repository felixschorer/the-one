package movement.fmi;

public enum Size {
    SMALL(5, "small"),
    MEDIUM(6, "medium"),
    LARGE(7, "large");

    private final int type;
    private final String settingsName;

    Size(int type, String settingsName) {
        this.type = type;
        this.settingsName = settingsName;
    }

    public int getType() {
        return type;
    }

    public String getSettingsName() {
        return settingsName;
    }
}
