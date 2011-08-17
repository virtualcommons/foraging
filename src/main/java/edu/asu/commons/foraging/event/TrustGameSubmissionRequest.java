package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;

public class TrustGameSubmissionRequest extends AbstractPersistableEvent {
    
    private int playerOneAmountToKeep;
    
    private int[] playerTwoAmountsToKeep;

    public int getPlayerOneAmountToKeep() {
        return playerOneAmountToKeep;
    }

    public int[] getPlayerTwoAmountsToKeep() {
        return playerTwoAmountsToKeep;
    }

    public TrustGameSubmissionRequest(Identifier id, int playerOneAmountToKeep, int[] playerTwoAmountsToKeep) {
        super(id);
        this.playerOneAmountToKeep = playerOneAmountToKeep;
        this.playerTwoAmountsToKeep = playerTwoAmountsToKeep;
    }
    
}
