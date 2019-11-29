package movement;

import core.Settings;
import movement.map.MapNode;
import movement.map.SimMap;
import movement.nodegrid.NodeGridSettings;
import movement.nodegrid.OSM2NodeGrid;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class NodeGridBasedMovement extends MovementModel implements RenderableMovement {
    private static OSM2NodeGrid osm2NodeGridCache = null;

    private SimMap simMap;
    private Set<MapNode> pointsOfInterest;
    private Map<Integer, List<MapNode>> portals;

    public NodeGridBasedMovement(Settings settings) {
        super(settings);

        // cache map in case of multiple host groups
        NodeGridSettings nodeGridSettings = new NodeGridSettings();
        if (osm2NodeGridCache == null || !nodeGridSettings.equals(osm2NodeGridCache.getSettings())) {
            osm2NodeGridCache = new OSM2NodeGrid(nodeGridSettings);
        }

        simMap = osm2NodeGridCache.getSimMap();
        pointsOfInterest = osm2NodeGridCache.getPointsOfInterest();
        portals = osm2NodeGridCache.getPortals();
    }

    public NodeGridBasedMovement(NodeGridBasedMovement mm) {
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
