package movement.fmi;

public enum NodeType {
    LECTURE_HALL(1), COLLECTION_AREA(2);

    private final int type;

    NodeType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
