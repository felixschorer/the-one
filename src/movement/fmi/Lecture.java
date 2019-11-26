package movement.fmi;

import core.Coord;
import movement.map.MapNode;

import java.util.Random;

public class Lecture extends Event {
    static public int CT_INCREMENT_START_OPTIONS[] = { 0, 60 * 15, 60 * 30 };
    static public int TOTAL_DURATION_OPTIONS[] = { 60 * 120, 60 * 120 * 2 };
    static public int TOTAL_CT = 60 * 30;

    private int totalDuration; // including ct
    private int ctIncrementStart;
    private int ctIncrementEnd;

    public Lecture(MapNode location, int timestampStart, Random rng) {
        super(location, timestampStart);

        int randomCtIndex = rng.nextInt(CT_INCREMENT_START_OPTIONS.length);
        ctIncrementStart = CT_INCREMENT_START_OPTIONS[randomCtIndex];

        int randomDurationIndex = rng.nextInt(TOTAL_DURATION_OPTIONS.length);
        totalDuration = TOTAL_DURATION_OPTIONS[randomDurationIndex];
        ctIncrementEnd = TOTAL_CT - ctIncrementStart;
    }

    public int getRealStart() {
        return getTimestampStart() + getCtIncrementStart();
    }

    public int getCtIncrementStart() {
        return ctIncrementStart;
    }

    public int getTotalDuration() { return totalDuration; }

    public int getCtIncrementEnd() {
        return ctIncrementEnd;
    }

    public int getRealEnd() {
        return getTimestampStart() + getTotalDuration() - getCtIncrementEnd();
    }
}
