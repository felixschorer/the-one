package movement.university;

public enum NodeType {
    COLLECTION_AREA(OSMMapping.COLLECTION_AREA, "collectionArea"),
    LECTURE_HALL(OSMMapping.LECTURE_HALL, "lectureHall"),
    EXERCISE_ROOM(OSMMapping.EXERCISE_ROOM, "exerciseRoom"),
    STUDY_PLACE(OSMMapping.STUDY_PLACE, "studyPlace");

    private final OSMMapping type;
    private final String settingsName;

    NodeType(OSMMapping type, String settingsName) {
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
