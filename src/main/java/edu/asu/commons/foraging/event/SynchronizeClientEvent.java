package edu.asu.commons.foraging.event;

import java.awt.Point;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.net.Identifier;


/**
 * $Id$
 * 
 * Maintains a Map of all clients and their locations (denoted by
 * java.awt.Point-S) as well as a list of food positions, if the food is visible.
 * 
 * Only sends the client data map for a given Group, not the entire server game state.
 * 
 * @author Deepali Bhagvat
 * @author Allen Lee
 * @version $Revision$
 */
public class SynchronizeClientEvent extends AbstractEvent {

    private static final long serialVersionUID = -128693557750400520L;

    private final GroupDataModel groupDataModel;
    private final long timeLeft;
    
    public SynchronizeClientEvent(ClientData clientData, long timeLeft) {
        this(clientData.getId(), clientData.getGroupDataModel(), timeLeft);
    }
    
    public SynchronizeClientEvent(Identifier id, GroupDataModel groupDataModel, long timeLeft) {
        super(id);
        this.groupDataModel = groupDataModel;
        this.timeLeft = timeLeft;
    }
    
    public GroupDataModel getGroupDataModel() {
        return groupDataModel;
    }

    public int getCurrentTokens() {
        return groupDataModel.getCurrentTokens( getId() );
    }

    public Map<Identifier, ClientData> getClientDataMap() {
        return groupDataModel.getClientDataMap();
    }

    public Set<Point> getTokenPositions() {
        return groupDataModel.getResourcePositions();
    }

    public Point getClientPosition() {
        return groupDataModel.getClientPosition( getId() );
    }

    public Point getClientPosition(Identifier id) {
        return groupDataModel.getClientPosition(id);
    }

    public long getTimeLeft() {
        return timeLeft;
    }

    public Queue<RealTimeSanctionRequest> getLatestSanctions() {
        return groupDataModel.getClientData( getId() ).getLatestSanctions();
    }
}
