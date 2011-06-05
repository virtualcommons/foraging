package edu.asu.commons.foraging.event;

import java.util.Map;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.net.Identifier;


public class FacilitatorSanctionUpdateEvent extends AbstractEvent {
    
    private static final long serialVersionUID = 1L;
    private final Map<Identifier, ClientData> clientDataMap;
    private final boolean lastRound;
    
    public FacilitatorSanctionUpdateEvent(Identifier id, Map<Identifier, ClientData> clientDataMap, boolean lastRound) {
        super(id);
        this.clientDataMap = clientDataMap;
        this.lastRound = lastRound;
    }

    public boolean isLastRound() {
        return lastRound;
    }

    public Map<Identifier, ClientData> getClientDataMap() {
        return clientDataMap;
    }

}
