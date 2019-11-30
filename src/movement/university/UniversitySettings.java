package movement.university;

import core.Settings;
import core.SimError;
import movement.fmi.Size;
import movement.fmi.PointOfInterest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UniversitySettings {
    private static final String UNIVERSITY_NS = "University";
    private static final String LECTURE_LENGTH = "lectureLength";
    private static final String FIRST_LECTURES_START = "firstLecturesStart";
    private static final String LAST_LECTURES_START = "lastLecturesStart";
    private static final String EXERCISE_ROOM_OCCUPATION_PERCENTAGE = "exerciseRoomOccupationPercentage";
    private static final String CAPACITY = "capacity";

    private static final String TIME_FORMAT = "HH:mm";

    private final int lectureLength;
    private final int firstLecturesStart;
    private final int lastLecturesStart;
    private final double exerciseRoomOccupation;
    private final Map<PointOfInterest, Map<Size, Integer>> capacities;

    public UniversitySettings() {
        Settings settings = new Settings(UNIVERSITY_NS);
        lectureLength = readMinutesToSeconds(settings, LECTURE_LENGTH, 90);
        firstLecturesStart = readTimeToSeconds(settings, FIRST_LECTURES_START, "08:30");
        lastLecturesStart = readTimeToSeconds(settings, LAST_LECTURES_START, "18:30");
        exerciseRoomOccupation = readProbability(settings, EXERCISE_ROOM_OCCUPATION_PERCENTAGE, 1);
        capacities = readCapacities(settings, CAPACITY);
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

    public double getExerciseRoomOccupation() {
        return exerciseRoomOccupation;
    }

    public Map<PointOfInterest, Map<Size, Integer>> getCapacities() {
        return capacities;
    }

    private static Map<PointOfInterest, Map<Size, Integer>> readCapacities(Settings settings, String settingsSuffix) {
        Map<PointOfInterest, Map<Size, Integer>> capacities = new HashMap<>();
        for (PointOfInterest pointOfInterest : PointOfInterest.values()) {
            capacities.put(pointOfInterest, new HashMap<>());
            for (Size size : Size.values()) {
                int capacity = settings.getInt(buildCapacitySettingsName(pointOfInterest, size, settingsSuffix), 0);
                capacities.get(pointOfInterest).put(size, capacity);
            }
        }
        return capacities;
    }

    private static int readMinutesToSeconds(Settings settings, String settingsName, int defaultValue) {
        return settings.getInt(settingsName, defaultValue) * 60;
    }

    private static int readTimeToSeconds(Settings settings, String settingsName, String defaultValue) {
        return parseTimeToSeconds(settings.getSetting(settingsName, defaultValue));
    }

    private static double readProbability(Settings settings, String settingsName, double defaultValue) {
        return Math.max(0, Math.min(1, settings.getDouble(settingsName, defaultValue)));
    }

    private static int parseTimeToSeconds(String timeString) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
            Date reference = dateFormat.parse("00:00:00");
            Date date = dateFormat.parse(timeString);
            long seconds = (date.getTime() - reference.getTime()) / 1000L;
            return (int) seconds;
        } catch (ParseException e) {
            throw new SimError(e);
        }
    }

    private static String buildCapacitySettingsName(PointOfInterest pointOfInterest, Size size, String settingsSuffix) {
        return pointOfInterest.getSettingsName()
                + capitalizeWord(size.getSettingsName())
                + capitalizeWord(settingsSuffix);
    }

    private static String capitalizeWord(String word) {
        if (word.length() < 2) {
            return word.toUpperCase();
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
}
