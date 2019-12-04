package movement.university.v2;

import core.Settings;

public class UniversityGroupSettings {
    private static final String MEAN_LECTURES_PER_STUDENT = "universityMeanLecturesPerStudent";
    private static final String PATH_RANDOMNESS = "universityPathRandomness";
    private static final String DISTANCE_ESTIMATIE_DISTORTION = "universityDistanceEstimateDistortion";
    private static final String TEMPORAL_SPREAD = "universityTemporalSpread";

    private final double meanLecturesPerStudent;
    private final double pathRandomness;
    private final double distanceEstimateDistortion;
    private final double temporalSpread;

    public UniversityGroupSettings(Settings settings) {
        meanLecturesPerStudent = settings.getDouble(MEAN_LECTURES_PER_STUDENT, 2);
        pathRandomness = settings.getDouble(PATH_RANDOMNESS, 2);
        distanceEstimateDistortion = settings.getDouble(DISTANCE_ESTIMATIE_DISTORTION, 1);
        temporalSpread = settings.getDouble(TEMPORAL_SPREAD, 120);
    }

    public double getMeanLecturesPerStudent() {
        return meanLecturesPerStudent;
    }

    public double getPathRandomness() {
        return pathRandomness;
    }

    public double getDistanceEstimateDistortion() {
        return distanceEstimateDistortion;
    }

    public double getTemporalSpread() {
        return temporalSpread;
    }
}
