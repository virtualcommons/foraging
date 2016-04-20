package edu.asu.commons.foraging.event;

import java.awt.Point;
import java.util.Map;
import java.util.Set;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.net.Identifier;



/**
 * Single player update event containing just the client data tokens consumed and bot position(s). 
 * @author Allen Lee
 * @version $Revision$
 */
public class SinglePlayerClientUpdateEvent extends AbstractEvent {

    private static final long serialVersionUID = -128693557750400520L;

    private final Map<Identifier, Integer> clientTokens;
    private final Map<Identifier, Point> clientPositions;
    private final Resource[] addedResources;
    private final Point[] removedResources;
    private final long timeLeft;
    
    public SinglePlayerClientUpdateEvent(Identifier id, long timeLeft, 
            Map<Identifier, Point> otherClientPositions,
            Map<Identifier, Integer> clientTokens,
            Resource[] addedResources,
            Point[] removedResources) 
    {
        super(id);
        this.timeLeft = timeLeft;
        this.clientPositions = otherClientPositions;
        this.clientTokens = clientTokens;
        this.addedResources = addedResources;
        this.removedResources = removedResources;
    }

    public Resource[] getAddedResources() {
        return addedResources;
    }

    public Point[] getRemovedResources() {
        return removedResources;
    }

    public int getCurrentTokens() {
        return getCurrentTokens( getId() );
    }
    
    public int getCurrentTokens(Identifier id) {
        return clientTokens.get(id);
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
