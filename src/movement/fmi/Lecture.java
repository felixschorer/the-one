package movement.fmi;

import movement.map.MapNode;

import java.util.Random;

public class Lecture extends Event {
    static public int CT_INCREMENT_START[] = { 0, 60 * 15, 60 * 30 };
    static public int TOTAL_DURATION = 60 * 120;
    static public int TOTAL_CT = 60 * 30;

    private int ctIncrementStart;
    private int ctIncrementEnd;

    public Lecture(MapNode location, int timestampStart, Random rng) {
        super(location, timestampStart);

        int randomCtIndex = rng.nextInt(CT_INCREMENT_START.length);
        ctIncrementStart = CT_INCREMENT_START[randomCtIndex];
        ctIncrementEnd = TOTAL_CT - ctIncrementStart;
    }

    public int getRealStart() {
        return getTimestampStart() + getCtIncrementStart();
    }

    public int getCtIncrementStart() {
        return ctIncrementStart;
    }

    public int getCtIncrementEnd() {
        return ctIncrementEnd;
    }
}
