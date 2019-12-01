package movement.university.v2;

import core.Settings;
import core.SimError;
import movement.university.Size;
import movement.university.NodeType;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class UniversitySettings {
    private static final String UNIVERSITY_NS = "University";
    private static final String LECTURE_LENGTH = "lectureLength";
    private static final String FIRST_LECTURES_START = "firstLecturesStart";
    private static final String LAST_LECTURES_START = "lastLecturesStart";
    private static final String TRAVEL_TIME_ESTIMATION_MAGIC_FACTOR = "travelTimeEstimationMagicFactor";
    private static final String TIME_BETWEEN_BOOKINGS = "timeBetweenBookings";
    private static final String STAY_TIME = "stayTime";
    private static final String CAPACITY = "capacity";

    private static final String TIME_FORMAT = "HH:mm";

    private final int lectureLength;
    private final int firstLecturesStart;
    private final int lastLecturesStart;
    private final double travelTimeEstimationMagicFactor;
    private final Map<NodeType, Map<Size, Integer>> timeBetweenBookings;
    private final Map<NodeType, Map<Size, Integer>> stayTimes;
    private final Map<NodeType, Map<Size, Integer>> capacities;

    public UniversitySettings() {
        Settings settings = new Settings(UNIVERSITY_NS);
        lectureLength = settings.getInt(LECTURE_LENGTH, 90) * 60;
        firstLecturesStart = readTimeToSeconds(settings, FIRST_LECTURES_START, "08:30");
        lastLecturesStart = readTimeToSeconds(settings, LAST_LECTURES_START, "18:30");
        travelTimeEstimationMagicFactor = settings.getDouble(TRAVEL_TIME_ESTIMATION_MAGIC_FACTOR, 1.0);
        timeBetweenBookings = readTimeIntervals(settings, TIME_BETWEEN_BOOKINGS, 180);
        stayTimes = readTimeIntervals(settings, STAY_TIME, 30);
        capacities = readCapacities(settings);
    }

    public int getLectureLength() {
        return lectureLength;
    }

    public int getFirstLecturesStart() {
        return firstLecturesStart;
    }

    public int getLastLecturesStart() {
        return lastLecturesStart;
    }

    public double getTravelTimeEstimationMagicFactor() {
        return travelTimeEstimationMagicFactor;
    }

    public Map<NodeType, Map<Size, Integer>> getTimeBetweenBookings() {
        return timeBetweenBookings;
    }

    public Map<NodeType, Map<Size, Integer>> getStayTimes() {
        return stayTimes;
    }

    public Map<NodeType, Map<Size, Integer>> getCapacities() {
        return capacities;
    }

    private static Map<NodeType, Map<Size, Integer>> readTimeIntervals(
            Settings settings, String settingsSuffix, int defaultValue) {
        Map<NodeType, Map<Size, Integer>> timeIntervals = new HashMap<>();
        for (NodeType nodeType : NodeType.values()) {
            timeIntervals.put(nodeType, new HashMap<>());
            for (Size size : Size.values()) {
                String settingsName = buildPOISettingsName(nodeType, size, settingsSuffix);
                int timeInterval = settings.getInt(settingsName, defaultValue) * 60;
                timeIntervals.get(nodeType).put(size, timeInterval);
            }
        }
        return timeIntervals;
    }

    private static Map<NodeType, Map<Size, Integer>> readCapacities(Settings settings) {
        Map<NodeType, Map<Size, Integer>> capacities = new HashMap<>();
        for (NodeType nodeType : NodeType.values()) {
            capacities.put(nodeType, new HashMap<>());
            for (Size size : Size.values()) {
                String settingsName = buildPOISettingsName(nodeType, size, CAPACITY);
                int capacity = settings.getInt(settingsName, 0);
                capacities.get(nodeType).put(size, capacity);
            }
        }
        return capacities;
    }

    private static int readTimeToSeconds(Settings settings, String settingsName, String defaultValue) {
        return parseTimeToSeconds(settings.getSetting(settingsName, defaultValue));
    }

    private static int parseTimeToSeconds(String timeString) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
            Date reference = dateFormat.parse("00:00:00");
            Date date = dateFormat.parse(timeString);
            long minutes = (date.getTime() - reference.getTime()) / 1000L;
            return (int) minutes;
        } catch (ParseException e) {
            throw new SimError(e);
        }
    }

    private static String buildPOISettingsName(NodeType type, Size size, String settingsSuffix) {
        return String.format("%s_%s_%s", type.getSettingsName(), size.getSettingsName(), settingsSuffix);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniversitySettings that = (UniversitySettings) o;
        return getLectureLength() == that.getLectureLength() &&
                getFirstLecturesStart() == that.getFirstLecturesStart() &&
                getLastLecturesStart() == that.getLastLecturesStart() &&
                Double.compare(that.travelTimeEstimationMagicFactor, travelTimeEstimationMagicFactor) == 0 &&
                getTimeBetweenBookings().equals(that.getTimeBetweenBookings()) &&
                getStayTimes().equals(that.getStayTimes()) &&
                getCapacities().equals(that.getCapacities());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLectureLength(), getFirstLecturesStart(), getLastLecturesStart(),
                travelTimeEstimationMagicFactor, getTimeBetweenBookings(), getStayTimes(), getCapacities());
    }
}
