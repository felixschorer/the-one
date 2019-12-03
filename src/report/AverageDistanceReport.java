package report;

import core.Coord;
import core.DTNHost;
import core.MovementListener;

import java.util.HashMap;

public class AverageDistanceReport extends Report implements MovementListener {
    /** Syntax of the report lines */
    public static final String SYNTAX =
            "host name, distance, number of points of interest";
    private HashMap<String, HostEntry> valuesByHost = new HashMap<String, HostEntry>();

    public AverageDistanceReport() {
        init();
    }

    @Override
    protected void init() {
        super.init();
        printHeader();
    }

    public void done() {
        for (String host : valuesByHost.keySet()) {
            HostEntry hostEntry = valuesByHost.get(host);
            report(host, hostEntry.getDistance());
        }

        super.done();
    }

    private void printHeader() {
        write("# Scenario " + getScenarioName());
        write("# " + SYNTAX);
    }

    /**
     * Writes a report line
     * @param hostName name of host
     * @param distance distance of host over all points of interest
     */
    private void report(String hostName, double distance) {
        write(hostName + " " + " " + distance);
    }

    @Override
    public void newDestination(DTNHost host, Coord destination, double speed) {
        String name = host.toString();
        HostEntry hostEntry = valuesByHost.get(name);

        Coord oldLocation = hostEntry.getLocation();
        double oldDistance = hostEntry.getDistance();
        double additionalDistance = destination.distance(oldLocation);
        double newDistance = oldDistance + additionalDistance;

        hostEntry.setDistance(newDistance);
        hostEntry.setLocation(destination);
    }

    @Override
    public void initialLocation(DTNHost host, Coord location) {
        HostEntry hostEntry = new HostEntry(host.toString(), 0, location);
        valuesByHost.put(host.toString(), hostEntry);
    }

    private class HostEntry {
        private String name;
        private double distance;
        private Coord location;

        public HostEntry(String name, int distance, Coord location) {
            this.name = name;
            this.distance = distance;
            this.location = location;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public String getName() {
            return name;
        }

        public Coord getLocation() {
            return location;
        }

        public void setLocation(Coord location) {
            this.location = location;
        }
    }
}
