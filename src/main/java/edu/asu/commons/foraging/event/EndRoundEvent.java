package edu.asu.commons.foraging.event;

import java.util.List;
import java.util.Map;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.net.Identifier;



/**
 * $Id: EndRoundEvent.java 4 2008-07-25 22:51:44Z alllee $
 * 
 * This event carries all data relevant to the ending of a round, including the
 * debriefing message, the time (in milliseconds) that the client should wait
 * until the next round begins, and an ordered List containing all the Events
 * needed to reconstruct an image of the path of this object over the course of
 * the experiment (contains ClientUpdateEvents and FoodAddedEvents).
 * 
 * @author Deepali Bhagvat
 * @author Allen Lee
 * @version $Revision: 4 $
 */
public class EndRoundEvent extends AbstractEvent {

    private static final long serialVersionUID = 6667696400657435774L;
    private final boolean lastRound;
    private final ClientData clientData;
    
//    private final List<PersistableEvent> actions;

    public EndRoundEvent(Identifier id, ClientData clientData, boolean lastRound) {
        super(id);
        this.clientData = clientData;
        this.lastRound = lastRound;
    }

    /**
     * Returns all clients in this group.
     */
    public Map<Identifier, ClientData> getClientDataMap() {
        return clientData.getGroupDataModel().getClientDataMap();
    }
    
    public GroupDataModel getGroupDataModel() {
        return clientData.getGroupDataModel();
    }

    public int getCurrentTokens() {
        return clientData.getCurrentTokens();
    }

    public int getTotalNumberOfTokens() {
        return clientData.getTotalTokens();
    }
    
    public List<String> getTrustGameLog() {
        return clientData.getTrustGameLog();
    }
    
    public ClientData getClientData() {
        return clientData;
    }

    public boolean isLastRound() {
        return lastRound;
    }
}
