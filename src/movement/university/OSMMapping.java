package movement.university;

public enum OSMMapping {
    LECTURE_HALL(1),
    EXERCISE_ROOM(2),
    STUDY_PLACE(3),
    COLLECTION_AREA(4),
    SMALL(5),
    MEDIUM(6),
    LARGE(7);

    private final int type;

    OSMMapping(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
