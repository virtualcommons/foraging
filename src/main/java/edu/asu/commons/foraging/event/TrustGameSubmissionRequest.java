package edu.asu.commons.foraging.event;

import java.util.ArrayList;
import java.util.List;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;

public class TrustGameSubmissionRequest extends AbstractPersistableEvent {
    
    private static final long serialVersionUID = -3516907265559144744L;

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
        List<Double> list = new ArrayList<Double>();
        for (double amount: playerTwoAmountsToKeep) {
            list.add(amount);
        }
        return String.format("%s (P1: %s) (P2: %s)", getId(), playerOneAmountToKeep, list);
    }
}
