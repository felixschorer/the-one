package movement.fmi;

public enum PointOfInterest {
    TRANSPORT(0, "transport"),
    LECTURE_HALL(1, "lectureHall"),
    EXERCISE_ROOM(2, "exerciseRoom"),
    STUDY_PLACE(3, "studyPlace"),
    CAFE(4, "cafe");

    private final int type;
    private final String settingsName;

    PointOfInterest(int type, String settingsName) {
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
