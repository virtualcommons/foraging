package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;
import java.util.Arrays;

public class TrustGameSubmissionRequest extends AbstractPersistableEvent {
    
    private static final long serialVersionUID = -4962852585789164775L;

    private double playerOneAmountToKeep;
    
    private double[] playerTwoAmountsToKeep;

    public double getPlayerOneAmountToKeep() {
        return playerOneAmountToKeep;
    }

    public double[] getPlayerTwoAmountsToKeep() {
        return playerTwoAmountsToKeep;
    }

    public TrustGameSubmissionRequest(Identifier id, double playerOneAmountToKeep, double[] playerTwoAmountsToKeep) {
        super(id);
        this.playerOneAmountToKeep = playerOneAmountToKeep;
        this.playerTwoAmountsToKeep = playerTwoAmountsToKeep;
    }
    
    public String toString() {
        return String.format("%s (P1: %d) (P2: %s)", getId(), playerOneAmountToKeep, Arrays.asList(playerTwoAmountsToKeep));
    }
}
