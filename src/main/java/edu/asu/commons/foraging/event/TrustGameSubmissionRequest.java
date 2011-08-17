package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;

public class TrustGameSubmissionRequest extends AbstractPersistableEvent {
    
    private static final long serialVersionUID = -4962852585789164775L;

    private Double playerOneAmountToKeep;
    
    private Double[] playerTwoAmountsToKeep;

    public Double getPlayerOneAmountToKeep() {
        return playerOneAmountToKeep;
    }

    public Double[] getPlayerTwoAmountsToKeep() {
        return playerTwoAmountsToKeep;
    }

    public TrustGameSubmissionRequest(Identifier id, Double playerOneAmountToKeep, Double[] playerTwoAmountsToKeep) {
        super(id);
        this.playerOneAmountToKeep = playerOneAmountToKeep;
        this.playerTwoAmountsToKeep = playerTwoAmountsToKeep;
    }
    
}
