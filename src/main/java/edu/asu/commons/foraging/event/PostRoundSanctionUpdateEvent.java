package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.model.ClientData;



public class PostRoundSanctionUpdateEvent extends AbstractEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final ClientData clientData;
    private final boolean lastRound;
    private final RoundConfiguration roundConfiguration;

    public boolean isLastRound() {
        return lastRound;
    }

    public PostRoundSanctionUpdateEvent(ClientData clientData, RoundConfiguration configuration, boolean lastRound) {
        super(clientData.getId());
        this.clientData = clientData;
        this.lastRound = lastRound;
        this.roundConfiguration = configuration;
    }
    
    public ClientData getClientData() {
        return clientData;
    }

    public RoundConfiguration getRoundConfiguration() {
        return roundConfiguration;
    }
}
