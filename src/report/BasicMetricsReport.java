package report;

import core.Coord;
import core.DTNHost;
import core.MovementListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class BasicMetricsReport extends Report implements MovementListener {

    private HashMap<String, HostEntry> valuesByHost = new HashMap<String, HostEntry>();

    public BasicMetricsReport() {
        init();
    }

    public void done() {
        write("# Scenario " + getScenarioName());

        reportDistanceAggregations();
        reportVisitedLayersAggregation();

        write("# host name, distance, visited layers: ");

        for (String hostName : valuesByHost.keySet()) {
            HostEntry hostEntry = valuesByHost.get(hostName);
            report(hostName, hostEntry.getDistance(), hostEntry.getVisitedLayers());
        }

        super.done();
    }

    /**
     * Writes a report line
     * @param hostName name of host
     * @param distance distance of hosts over all points of interest
     * @param layers array of layers that the host visited
     */
    private void report(String hostName, double distance, ArrayList layers) {
        write(hostName + " " + distance + " " + layers);
    }

    private void reportDistanceAggregations() {
        ArrayList distances = valuesByHost.values().stream()
                .map(host -> host.getDistance())
                .filter(distance -> distance != 0)
                .collect(Collectors.toCollection(ArrayList::new));

        int numberOfNodesNotMoving = valuesByHost.values().stream()
                .filter(host -> host.getDistance() == 0)
                .collect(Collectors.toCollection(ArrayList::new))
                .size();

        double average = getAverage(distances) != "NaN" ? Double.valueOf(getAverage(distances)) : 0.0;
        double variance = getVariance(distances) != "NaN" ? Double.valueOf(getVariance(distances)) : 0.0;
        double standardDeviation = Math.sqrt(variance);

        write("# average distance of moving hosts: " + average);
        write("# standard deviation of distances of moving hosts: " + standardDeviation);
        write("# hosts that are not moving: " + numberOfNodesNotMoving + "/" + valuesByHost.size());
    }

    private void reportVisitedLayersAggregation() {
        ArrayList numberOfLayerChanges = valuesByHost.values().stream()
                .filter(host -> host.getDistance() != 0)
                .map(host -> Double.valueOf(host.getVisitedLayers().size() - 1))
                .collect(Collectors.toCollection(ArrayList::new));

        double average = getAverage(numberOfLayerChanges) != "NaN" ? Double.valueOf(getAverage(numberOfLayerChanges)) : 0.0;

        write("# average number of layer changes of moving hosts: " + average);
    }

    @Override
    public void newDestination(DTNHost host, Coord destination, double speed) {
        String name = host.toString();
        HostEntry hostEntry = valuesByHost.get(name);

        Coord oldLocation = hostEntry.getLocation();
        double oldDistance = hostEntry.getDistance();
        double additionalDistance = destination.distance(oldLocation);
        double newDistance = oldDistance + additionalDistance;
        int layer = destination.getLayer();

        hostEntry.setDistance(newDistance);
        hostEntry.setLocation(destination);
        hostEntry.pushNewToLayers(layer);
    }

    @Override
    public void initialLocation(DTNHost host, Coord location) {
        HostEntry hostEntry = new HostEntry(host.toString(), 0, location, location.getLayer());
        valuesByHost.put(host.toString(), hostEntry);
    }

    private class HostEntry {
        private String name;
        private double distance;
        private Coord location;
        private ArrayList<Integer> visitedLayers;

        public HostEntry(String name, int distance, Coord location, int layer) {
            this.name = name;
            this.distance = distance;
            this.location = location;
            this.visitedLayers = new ArrayList<>();
            this.visitedLayers.add(layer);
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

        public ArrayList getVisitedLayers() {
            return visitedLayers;
        }

        // add layer to layers array if it's not the last in the list
        public void pushNewToLayers(int layer) {
            if (visitedLayers.get(visitedLayers.size() - 1) != layer) {
                this.visitedLayers.add(layer);
            }
        }
    }
}
