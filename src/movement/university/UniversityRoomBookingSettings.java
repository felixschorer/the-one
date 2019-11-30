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
import java.util.Objects;

public class UniversityRoomBookingSettings {
    private static final String UNIVERSITY_NS = "UniversityRoomBooking";
    private static final String LECTURE_LENGTH = "lectureLength";
    private static final String FIRST_LECTURES_START = "firstLecturesStart";
    private static final String LAST_LECTURES_START = "lastLecturesStart";
    private static final String TIME_BETWEEN_BOOKINGS = "timeBetweenBookings";

    private static final String TIME_FORMAT = "HH:mm";

    private final int lectureLength;
    private final int firstLecturesStart;
    private final int lastLecturesStart;
    private final Map<PointOfInterest, Map<Size, Integer>> timeBetweenBookings;

    public UniversityRoomBookingSettings() {
        Settings settings = new Settings(UNIVERSITY_NS);
        lectureLength = settings.getInt(LECTURE_LENGTH, 90) * 60;
        firstLecturesStart = readTimeToSeconds(settings, FIRST_LECTURES_START, "08:30");
        lastLecturesStart = readTimeToSeconds(settings, LAST_LECTURES_START, "18:30");
        timeBetweenBookings = readTimeBetweenBookings(settings);
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

    public Map<PointOfInterest, Map<Size, Integer>> getTimeBetweenBookings() {
        return timeBetweenBookings;
    }

    private static Map<PointOfInterest, Map<Size, Integer>> readTimeBetweenBookings(Settings settings) {
        Map<PointOfInterest, Map<Size, Integer>> timeBetweenBookings = new HashMap<>();
        for (PointOfInterest pointOfInterest : PointOfInterest.values()) {
            timeBetweenBookings.put(pointOfInterest, new HashMap<>());
            for (Size size : Size.values()) {
                String settingsName = buildPOISettingsName(pointOfInterest, size, TIME_BETWEEN_BOOKINGS);
                int timeBetweenBooking = settings.getInt(settingsName, 180) * 60;
                timeBetweenBookings.get(pointOfInterest).put(size, timeBetweenBooking);
            }
        }
        return timeBetweenBookings;
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

    private static String buildPOISettingsName(PointOfInterest type, Size size, String settingsSuffix) {
        return String.format("%s_%s_%s", type.getSettingsName(), size.getSettingsName(), settingsSuffix);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniversityRoomBookingSettings that = (UniversityRoomBookingSettings) o;
        return getLectureLength() == that.getLectureLength() &&
                getFirstLecturesStart() == that.getFirstLecturesStart() &&
                getLastLecturesStart() == that.getLastLecturesStart() &&
                getTimeBetweenBookings().equals(that.getTimeBetweenBookings());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLectureLength(), getFirstLecturesStart(), getLastLecturesStart(), getTimeBetweenBookings());
    }
}
