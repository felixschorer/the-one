package report;

import core.Coord;
import core.DTNHost;
import core.MovementListener;

public class AverageDistanceReport extends Report implements MovementListener {
    /** Syntax of the report lines */
    public static final String SYNTAX =
            "number of visited pois, distance";
    private int numberOfVisitedPois;
    private int distance;
    private Coord currentLocation;

    public AverageDistanceReport() {
        init();
    }

    @Override
    protected void init() {
        super.init();
        this.numberOfVisitedPois = 0;
        this.distance = 0;
        printHeader();
    }

    public void done() {
        report(numberOfVisitedPois, distance);
        super.done();
    }

    private void printHeader() {
        write("# Scenario " + getScenarioName());
        write("# " + SYNTAX);
    }

    /**
     * Writes a report line
     * @param numberOfVisitedPois number of visited points of interest
     * @param distance distance of host over all points of interest
     */
    private void report(int numberOfVisitedPois, int distance) {
        write(format(numberOfVisitedPois) + " " + format(distance));
    }

    @Override
    public void newDestination(DTNHost host, Coord destination, double speed) {
        int distance = this.distance;
        Coord currentWaypoint = currentLocation;
        for (Coord waypoint : host.getPath().getCoords()) {
            distance += waypoint.distance(currentWaypoint);
            currentWaypoint = waypoint;
        }
        this.distance = distance;
        currentLocation = destination;
        numberOfVisitedPois++;
    }

    @Override
    public void initialLocation(DTNHost host, Coord location) {
        currentLocation = location;
    }
}
