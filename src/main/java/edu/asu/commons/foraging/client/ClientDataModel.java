package edu.asu.commons.foraging.client;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import edu.asu.commons.foraging.event.ClientPositionUpdateEvent;
import edu.asu.commons.foraging.event.ExplicitCollectionModeRequest;
import edu.asu.commons.foraging.event.RealTimeSanctionRequest;
import edu.asu.commons.foraging.event.SinglePlayerClientUpdateEvent;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.Direction;
import edu.asu.commons.foraging.model.ForagingDataModel;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.foraging.rules.Strategy;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.util.Duration;

/**
 * 
 * This class should only provide game state relevant to a particular client.
 * 
 * @author Allen Lee
 * @version $Revision$
 */

public class ClientDataModel extends ForagingDataModel {

    private static final long serialVersionUID = -3424256672940188027L;

    // private GroupDataModel groupDataModel;

    private final List<Identifier> allClientIdentifiers = new ArrayList<Identifier>();

    private ClientData clientData;

    // these are the subjects whom we have sanctioned
    private Map<Identifier, Duration> sanctioned = new HashMap<Identifier, Duration>();

    // these are the subjects that have sanctioned us.
    private Map<Identifier, Duration> sanctioners = new HashMap<Identifier, Duration>();

    private List<Strategy> selectedStrategies = new ArrayList<>();
    private ForagingClient client;

    private volatile boolean explicitCollectionMode = false;

    private Map<Identifier, Point> clientPositions = new HashMap<>();

    private Map<Identifier, Integer> clientTokens;

    // Clients need to know the zones of all other clients in the group. This
    // maps client ID's to zone numbers.
    private Map<Identifier, Integer> clientZones;

    private Map<Point, Resource> resourceDistribution = new HashMap<>();

    public ClientDataModel(ForagingClient client) {
        super(client.getEventChannel());
        this.client = client;
    }

    public void toggleExplicitCollectionMode() {
        explicitCollectionMode = !explicitCollectionMode;
        client.transmit(new ExplicitCollectionModeRequest(client.getId(), explicitCollectionMode));
    }

    public boolean isSanctioningAllowed() {
        return getRoundConfiguration().isSanctioningEnabled();
    }

    public boolean isHarvestingAllowed() {
        return getClientData().isHarvestingAllowed();
    }

    public boolean isMonitor() {
        return getClientData().getForagingRole().isMonitor();
    }

    /*
     * public Map<Identifier, Duration> getSanctioned() { return sanctioned; }
     */
    public void sanction(Identifier source, Identifier target) {
        Duration duration = Duration.create(getRoundConfiguration().getSanctionFlashDuration());
        sanctioners.put(source, duration);
        sanctioned.put(target, duration);
    }

    /**
     * Returns a Set containing the positions of food pellets.
     */
    public Set<Point> getResourcePositions() {
        synchronized (resourceDistribution) {
            return Collections.unmodifiableSet(resourceDistribution.keySet());
        }
    }

    public Map<Point, Resource> getResourceDistribution() {
        return resourceDistribution;
    }

    public Point getClientPosition(Identifier sanctionee) {
        return clientPositions.get(sanctionee);
    }

    public Map<Identifier, Point> getClientPositions() {
        return clientPositions;
    }

    public ClientData getClientData() {
        return clientData;
    }

    public Identifier getId() {
        return client.getId();
    }

    public void clear() {
        allClientIdentifiers.clear();
        sanctioned.clear();
        sanctioners.clear();
        // FIXME: replace
    }

    public void initialize(GroupDataModel groupDataModel) {
        clear();
        Map<Identifier, ClientData> clientDataMap = groupDataModel.getClientDataMap();
        Identifier[] ids = new Identifier[clientDataMap.size()];
        clientZones = new HashMap<Identifier, Integer>();
        // ensure that the allClientIdentifiers natural ordering is by assigned number.
        for (Map.Entry<Identifier, ClientData> entry : clientDataMap.entrySet()) {
            Identifier id = entry.getKey();
            ClientData data = entry.getValue();
            int index = data.getAssignedNumber() - 1;
            ids[index] = id;
            // clientAssignedNumbers.put(id, data.getAssignedNumber());
            clientZones.put(id, data.getZone());
        }
        allClientIdentifiers.addAll(Arrays.asList(ids));
        setGroupDataModel(groupDataModel);
    }

    public Identifier getClientId(int assignedNumber) {
        // the actual assigned number is off-by-one
        int index = assignedNumber - 1;
        // bounds check
        if (index < allClientIdentifiers.size() && index >= 0) {
            return allClientIdentifiers.get(index);
        }
        return null;
    }

    public void setGroupDataModel(GroupDataModel groupDataModel) {
        if (groupDataModel == null) {
            return;
        }
        boolean singlePlayer = getRoundConfiguration().isSinglePlayer();
        resourceDistribution = groupDataModel.getResourceDistribution();
        if (clientData == null || !singlePlayer) {
            clientData = groupDataModel.getClientData(getId());
        }
        if (singlePlayer) {
            clientTokens = groupDataModel.getClientTokens();
            clientPositions = groupDataModel.getClientPositions();
            clientPositions.put(getId(), clientData.getPosition());
            clientData.setCurrentTokens(clientTokens.get(getId()));
        } else {
            update(groupDataModel.getClientTokens(), groupDataModel.getClientPositions(), clientData.getLatestSanctions(), null, null);
        }
    }

    public List<Identifier> getAllClientIdentifiers() {
        return allClientIdentifiers;
    }

    @Deprecated
    public List<ClientData> getOtherClients() {
        throw new UnsupportedOperationException("This is now deprecated.");
    }

    public int getAssignedNumber(Identifier id) {
        return allClientIdentifiers.indexOf(id) + 1;
    }

    /**
     * Updates client positions, current tokens, etc.
     */
    public void update(ClientPositionUpdateEvent event) {
        update(event.getClientTokens(), event.getClientPositions(), event.getLatestSanctions(), event.getAddedTokens(), event.getRemovedTokens());
        Identifier id = getId();
        clientData.setPosition(clientPositions.get(id));
        clientData.setCurrentTokens(clientTokens.get(id));
    }

    public void update(SinglePlayerClientUpdateEvent event) {
        clientTokens = event.getClientTokens();
        // dirty hack to keep client position authoritative
        event.getClientPositions().put(getId(), clientData.getPoint());
        clientPositions = event.getClientPositions();
        synchronized (resourceDistribution) {
            for (Point p : event.getRemovedResources()) {
                resourceDistribution.remove(p);
            }
            for (Resource r : event.getAddedResources()) {
                resourceDistribution.put(r.getPosition(), r);
            }
        }
    }

    public void update(Map<Identifier, Integer> clientTokens,
            Map<Identifier, Point> currentPositions,
            Queue<RealTimeSanctionRequest> latestSanctions,
            Resource[] addedResources,
            Resource[] removedResources) {
        this.clientTokens = clientTokens;
        this.clientPositions = currentPositions;
        handleRealTimeSanctions(latestSanctions);
        synchronized (resourceDistribution) {
            if (removedResources != null) {
                for (Resource resource : removedResources) {
                    resourceDistribution.remove(resource.getPosition());
                }
            }
            if (addedResources != null) {
                for (Resource resource : addedResources) {
                    resourceDistribution.put(resource.getPosition(), resource);
                }
            }
        }
    }

    private synchronized void handleRealTimeSanctions(Queue<RealTimeSanctionRequest> latestSanctions) {
        for (RealTimeSanctionRequest sanctionRequest : latestSanctions) {
            System.err.println("Processing real time sanction: from " + sanctionRequest.getSource() + " to " + sanctionRequest.getTarget());
            sanction(sanctionRequest.getSource(), sanctionRequest.getTarget());
        }
    }

    public Point getCurrentPosition() {
        return clientData.getPosition();
    }

    public int getCurrentTokens() {
        return clientData.getCurrentTokens();
    }

    public int getCurrentTokens(Identifier id) {
        return clientTokens.get(id);
    }

    // public Map<Identifier, ClientData> getClientDataMap() {
    // return groupDataModel.getClientDataMap();
    // }

    public synchronized boolean isBeingSanctioned(Identifier id) {
        return checkSanctionStatus(sanctioned, id);
    }

    public int getTotalTokens() {
        return clientData.getTotalTokens();
    }

    public double getCurrentIncome() {
        if (getRoundConfiguration().isPracticeRound()) {
            return 0.0;
        }
        return getIncome(getCurrentTokens());
    }

    public double getTotalIncome() {
        return getClientData().getTotalIncome();
    }

    public double getIncome(int tokens) {
        return tokens * getRoundConfiguration().getDollarsPerToken();
    }

    private synchronized boolean checkSanctionStatus(Map<Identifier, Duration> durations, Identifier id) {
        Duration duration = durations.get(id);
        if (duration == null) {
            return false;
        } else if (duration.hasExpired()) {
            durations.remove(id);
            return false;
        } else {
            return true;
        }
    }

    public synchronized boolean isSanctioning(Identifier id) {
        return checkSanctionStatus(sanctioners, id);
    }

    public boolean isExplicitCollectionMode() {
        return explicitCollectionMode;
    }

    public void setSelectedStrategies(List<Strategy> selectedStrategies) {
        this.selectedStrategies = selectedStrategies;
        getRoundConfiguration().setSelectedRules(selectedStrategies);
    }

    public List<Strategy> getSelectedStrategies() {
        return selectedStrategies;
    }

    @Deprecated
    public List<Strategy> getSelectedRules() {
        return selectedStrategies;
    }

    public Point3D getPoint3D(Identifier id) {
        // FIXME: this is broken
        throw new UnsupportedOperationException("3D support is currently unavailable");
    }

    /**
     * Return the zone number of the client with the given ID.
     */
    public int getClientZone(Identifier id) {
        return clientZones.get(id);
    }

    public void moveClient(Direction direction) {
        synchronized (clientPositions) {
            Point newLocation = direction.apply(getCurrentPosition());
            if (isValidPosition(newLocation)) {
                clientData.setPosition(newLocation);
                clientPositions.put(getId(), newLocation);
            }
        }
    }

}
