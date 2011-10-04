package edu.asu.commons.foraging.client;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import edu.asu.commons.foraging.event.ClientPositionUpdateEvent;
import edu.asu.commons.foraging.event.ExplicitCollectionModeRequest;
import edu.asu.commons.foraging.event.RealTimeSanctionRequest;
import edu.asu.commons.foraging.event.SynchronizeClientEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.EnforcementMechanism;
import edu.asu.commons.foraging.model.ForagingDataModel;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.RegulationData;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.foraging.model.SanctionMechanism;
import edu.asu.commons.foraging.ui.GameWindow2D;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.util.Duration;

/**
 * $Id: ClientDataModel.java 499 2010-03-31 00:58:49Z alllee $
 * 
 * This class should only provide game state relevant to a particular client.
 * 
 * @author Allen Lee
 * @version $Revision: 499 $
 */

public class ClientDataModel extends ForagingDataModel {

    private static final long serialVersionUID = -3424256672940188027L;

    private GroupDataModel groupDataModel;

    private final List<Identifier> allClientIdentifiers = new ArrayList<Identifier>();

    // FIXME: can obtain tokensConsumed from the clientDataMap now.
    private int currentTokens;
    
    // these are the subjects whom we have sanctioned
    private Map<Identifier, Duration> sanctioned = new HashMap<Identifier, Duration>();

    // these are the subjects that have sanctioned us.
    private Map<Identifier, Duration> sanctioners = new HashMap<Identifier, Duration>();

    private ForagingClient client;

    private volatile boolean explicitCollectionMode = false;

    public ClientDataModel(ForagingClient client) {
        super(client.getEventChannel());
        this.client = client;
    }

    public void toggleExplicitCollectionMode() {
        explicitCollectionMode = !explicitCollectionMode;
        client.transmit(new ExplicitCollectionModeRequest(client.getId(), explicitCollectionMode));
    }
    
    public Identifier getMonitorId() {
        if (groupDataModel.getActiveMonitor() != null) {
            return groupDataModel.getActiveMonitor().getId();
        }
        return Identifier.NULL;
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
        return groupDataModel.getResourcePositions();
    }

    public Map<Point, Resource> getResourceDistribution() {
        return groupDataModel.getResourceDistribution();
    }

    public ClientData getClientData() {
        return groupDataModel.getClientData(getId());
    }

    public Identifier getId() {
        return client.getId();
    }

    public void clear() {
        allClientIdentifiers.clear();
        sanctioned.clear();
        sanctioners.clear();
        if (groupDataModel != null) {
            groupDataModel.clear();
            groupDataModel = null;
        }
    }
    
    public List<RegulationData> getSubmittedRegulations() {
    	return groupDataModel.getSubmittedRegulations();
    }
    
    public EnforcementMechanism getActiveEnforcementMechanism() {
    	return groupDataModel.getActiveEnforcementMechanism();
    }
    
    public SanctionMechanism getActiveSanctionMechanism() {
    	return groupDataModel.getActiveSanctionMechanism();
    }
            
    public void initialize(GroupDataModel groupDataModel) {
        clear();
        this.groupDataModel = groupDataModel;
        Map<Identifier, ClientData> clientDataMap = groupDataModel.getClientDataMap();
        Identifier[] ids = new Identifier[clientDataMap.size()];
        for (Map.Entry<Identifier, ClientData> entry : clientDataMap.entrySet()) {
            int index = entry.getValue().getAssignedNumber() - 1;
            ids[index] = entry.getKey();
        }
        allClientIdentifiers.addAll(Arrays.asList(ids));
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
//        if (this.groupDataModel != null) {
//            this.groupDataModel.clear();
//        }
        this.groupDataModel = groupDataModel;
        currentTokens = groupDataModel.getCurrentTokens( getId() );
    }

    public List<Identifier> getAllClientIdentifiers() {
        return allClientIdentifiers;
    }

    public List<ClientData> getOtherClients() {
        List<ClientData> clients = new ArrayList<ClientData>();
        clients.addAll(groupDataModel.getClientDataMap().values());
        clients.remove(getClientData());
        return clients;
    }

    public int getAssignedNumber(Identifier id) {
        return allClientIdentifiers.indexOf(id) + 1;
    }

    /**
     * Updates client positions, current tokens, etc. FIXME: slight optimization
     * hot-spot, 9% of time spent here.
     */
    public void updateDiffs(ClientPositionUpdateEvent event, GameWindow2D window) {
        currentTokens = event.getCurrentTokens();
        groupDataModel.updateDiffs(event);
        handleRealTimeSanctions(event.getLatestSanctions());
        window.update(event.getTimeLeft());
    }

    /**
     * FIXME: lift repeated code from here and update(..)
     * 
     * @param event
     * @param window
     */
    public void update(SynchronizeClientEvent event, GameWindow2D window) {
        if (event.getCurrentTokens() > currentTokens) {
            window.collectToken(event.getClientPosition());
        }
        currentTokens = event.getCurrentTokens();
        // groupDataModel.update(event);
        handleRealTimeSanctions(event.getLatestSanctions());
    }

    private synchronized void handleRealTimeSanctions(Queue<RealTimeSanctionRequest> latestSanctions) {
//        if (!getRoundConfiguration().isRealTimeSanctioningEnabled()) {
//            return;
//        }
        for (RealTimeSanctionRequest sanctionRequest : latestSanctions) {
            System.err.println("Processing real time sanction: from " + sanctionRequest.getSource() + " to " + sanctionRequest.getTarget());
            sanction(sanctionRequest.getSource(), sanctionRequest.getTarget());
//            if (getId().equals(sanctionEvent.getTarget())) {
                // received a penalty, change colors for the duration of the
                // sanction.
//                sanction(sanctionEvent.getSource(), getId());
//            }
        }
    }

    public Point getCurrentPosition() {
        return getClientData().getPosition();
    }

    public int getCurrentTokens() {
        return currentTokens;
    }

    public Map<Identifier, ClientData> getClientDataMap() {
        return new HashMap<Identifier, ClientData>(groupDataModel.getClientDataMap());
    }

    public synchronized boolean isBeingSanctioned(Identifier id) {
        return checkSanctionStatus(sanctioned, id);
    }

    public int getTotalTokens() {
        return groupDataModel.getClientData(getId()).getTotalTokens();
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

    // FIXME: deprecate and remove these later
    public void clearDiffLists() {
        groupDataModel.clearDiffLists();
    }

    public Set<Resource> getAddedResources() {
        return groupDataModel.getAddedResources();
    }

    public Set<Resource> getRemovedResources() {
        return groupDataModel.getRemovedResources();
    }

	public void setActiveRegulation(RegulationData regulationData) {
		groupDataModel.setActiveRegulation(regulationData);
	}
	
	public RegulationData getActiveRegulation() {
		return groupDataModel.getActiveRegulation();
	}
}
