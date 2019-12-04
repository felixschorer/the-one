package report;

import core.Coord;
import core.DTNHost;
import core.MovementListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

// DONE: distance that each host is going average/variance over all hosts, number of nodes not moving at all
// TODO: number of floor changes

public class BasicMetricsReport extends Report implements MovementListener {
    private HashMap<String, HostEntry> valuesByHost = new HashMap<String, HostEntry>();

    public BasicMetricsReport() {
        init();
    }

    public void done() {
        write("# Scenario " + getScenarioName());

        reportAggregations();

        write("# host name, distance: ");

        for (String hostName : valuesByHost.keySet()) {
            HostEntry hostEntry = valuesByHost.get(hostName);
            report(hostName, hostEntry.getDistance());
        }

        super.done();
    }

    /**
     * Writes a report line
     * @param hostName name of host
     * @param distance distance of host over all points of interest
     */
    private void report(String hostName, double distance) {
        write(hostName + " " + distance);
    }

    private void reportAggregations() {
        ArrayList distances = valuesByHost.values().stream()
                .map(host -> host.getDistance())
                .filter(distance -> distance != 0)
                .collect(Collectors.toCollection(ArrayList::new));

        int numberOfNodesNotMoving = valuesByHost.values().stream()
                .filter(host -> host.getDistance() == 0)
                .collect(Collectors.toCollection(ArrayList::new))
                .size();

        double averageDistance = Double.valueOf(getAverage(distances));
        double varianceDistance = Double.valueOf(getVariance(distances));
        double standardDeviationDistance = Math.sqrt(varianceDistance);

        write("# average distance of moving hosts: " + averageDistance);
        write("# standard deviation of distances of moving hosts: " + standardDeviationDistance);
        write("# hosts that are not moving: " + numberOfNodesNotMoving + "/" + valuesByHost.size());
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
