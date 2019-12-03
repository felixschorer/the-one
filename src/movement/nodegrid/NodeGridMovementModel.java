package movement.nodegrid;

import core.Settings;
import movement.MovementModel;
import movement.map.HasMap;
import movement.map.MapNode;
import movement.map.SimMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class NodeGridMovementModel extends MovementModel implements HasMap {
    private static OSM2NodeGrid osm2NodeGridCache = null;

    private SimMap simMap;
    private Set<MapNode> pointsOfInterest;
    private Map<Integer, List<MapNode>> portals;

    public NodeGridMovementModel(Settings settings) {
        super(settings);

        // cache map in case of multiple host groups
        NodeGridMovementModelSettings nodeGridMovementModelSettings = new NodeGridMovementModelSettings();
        if (osm2NodeGridCache == null || !nodeGridMovementModelSettings.equals(osm2NodeGridCache.getSettings())) {
            osm2NodeGridCache = new OSM2NodeGrid(nodeGridMovementModelSettings);
        }

        simMap = osm2NodeGridCache.getSimMap();
        pointsOfInterest = osm2NodeGridCache.getPointsOfInterest();
        portals = osm2NodeGridCache.getPortals();
    }

    public NodeGridMovementModel(NodeGridMovementModel mm) {
        super(mm);
        simMap = mm.simMap;
        pointsOfInterest = mm.pointsOfInterest;
        portals = mm.portals;
    }

    @Override
    public SimMap getMap() {
        return simMap;
    }

    public Set<MapNode> getPointsOfInterest() {
        return pointsOfInterest;
    }

    public Map<Integer, List<MapNode>> getPortals() {
        return portals;
    }
}
