package movement.fmi;

public enum PointOfInterest {
    TRANSPORT(NodeType.TRANSPORT, "transport"),
    LECTURE_HALL(NodeType.LECTURE_HALL, "lectureHall"),
    EXERCISE_ROOM(NodeType.EXERCISE_ROOM, "exerciseRoom"),
    STUDY_PLACE(NodeType.STUDY_PLACE, "studyPlace"),
    CAFE(NodeType.CAFE, "cafe");

    private final NodeType type;
    private final String settingsName;

    PointOfInterest(NodeType type, String settingsName) {
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
