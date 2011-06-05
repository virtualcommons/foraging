package edu.asu.commons.foraging.event;

import java.awt.Point;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.net.Identifier;



/**
 * $Id: ClientPositionUpdateEvent.java 4 2008-07-25 22:51:44Z alllee $
 * 
 * Only contains the differences between rounds since this event is sent quite frequently.
 * 
 * FIXME: optimize for space further if possible.
 * 
 * @author Deepali Bhagvat
 * @author Allen Lee
 * @version $Revision: 4 $
 */
public class ClientPositionUpdateEvent extends AbstractEvent {

    private static final long serialVersionUID = -128693557750400520L;

    private final Resource[] addedResources;
    private final Resource[] removedResources;
    private final Map<Identifier, Integer> clientTokens;
    private final Map<Identifier, Point> clientPositions;
    private final Queue<RealTimeSanctionRequest> latestSanctions;
    
    private final long timeLeft;
    
    public ClientPositionUpdateEvent(ClientData clientData, long timeLeft) {
        this(clientData.getId(), clientData.getGroupDataModel(), timeLeft);
    }

    public ClientPositionUpdateEvent(Identifier id, GroupDataModel group, long timeLeft) {
        super(id);
        Set<Resource> addedTokensSet = group.getAddedResources();
        this.addedResources = addedTokensSet.toArray(new Resource[addedTokensSet.size()]);
        Set<Resource> removedTokensSet = group.getRemovedResources();
        this.removedResources = removedTokensSet.toArray(new Resource[removedTokensSet.size()]);
        this.timeLeft = timeLeft;
        this.clientTokens = group.getClientTokens();
        this.clientPositions = group.getClientPositions();
        this.latestSanctions = group.getClientData(id).getLatestSanctions();
    }
    
    public int getCurrentTokens() {
        return getCurrentTokens( getId() );
    }
    
    public int getCurrentTokens(Identifier id) {
        return clientTokens.get(id);
    }
    
    public Queue<RealTimeSanctionRequest> getLatestSanctions() {
        return latestSanctions;
    }

    public Resource[] getAddedTokens() {
        return addedResources;
    }

    public Resource[] getRemovedTokens() {
        return removedResources;
    }

    public Point getClientPosition() {
        return getClientPosition(id);
    }

    public Point getClientPosition(Identifier id) {
        return clientPositions.get(id);
    }
    
    public long getTimeLeft() {
        return timeLeft;
    }
}
