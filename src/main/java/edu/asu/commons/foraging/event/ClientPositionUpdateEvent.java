package edu.asu.commons.foraging.event;

import java.awt.Point;
import java.util.Map;
import java.util.Queue;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.net.Identifier;



/**
 * $Id$
 * 
 * Only contains the differences between rounds since this event is sent quite frequently.
 * 
 * FIXME: optimize for space further if possible.
 * 
 * @author Deepali Bhagvat
 * @author Allen Lee
 * @version $Revision$
 */
public class ClientPositionUpdateEvent extends AbstractEvent {

    private static final long serialVersionUID = -128693557750400520L;

    private final Resource[] addedResources;
    private final Resource[] removedResources;
    // FIXME: merge these two using a Pair
    private final Map<Identifier, Integer> clientTokens;
    private final Map<Identifier, Point> clientPositions;
    private final Queue<RealTimeSanctionRequest> latestSanctions;
    
    private final long timeLeft;
    
    public ClientPositionUpdateEvent(ClientData data, Resource[] addedResources, Resource[] removedResources, Map<Identifier, Integer> clientTokens,
            Map<Identifier, Point> clientPositions, long timeLeft) {
        super(data.getId());
        this.addedResources = addedResources;
        this.removedResources = removedResources;
        this.clientTokens = clientTokens;
        this.clientPositions = clientPositions;
        this.timeLeft = timeLeft;
        this.latestSanctions = data.getLatestSanctions();
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

    public Map<Identifier, Point> getClientPositions() {
        return clientPositions;
    }

    public Map<Identifier, Integer> getClientTokens() {
        return clientTokens;
    }
}
