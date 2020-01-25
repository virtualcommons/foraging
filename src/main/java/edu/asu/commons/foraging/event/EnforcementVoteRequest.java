package edu.asu.commons.foraging.event;


import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.rules.decaro.ForagingStrategy;
import edu.asu.commons.net.Identifier;

/**
 * Signals that a client has cast a vote for enforcement or no enforcement.
 * 
 * @author <a href='allen.lee@asu.edu'>Allen Lee</a>
 */

public class EnforcementVoteRequest extends AbstractPersistableEvent {

    private static final long serialVersionUID = 4360213814026474451L;

    private boolean monetaryPenaltiesEnabled;

    public EnforcementVoteRequest(Identifier id, boolean monetaryPenaltiesEnabled) {
        super(id, String.format("%s voted for monetary penalties: %s", id, monetaryPenaltiesEnabled));
        this.monetaryPenaltiesEnabled = monetaryPenaltiesEnabled;
    }

    public boolean isMonetaryPenaltiesEnabled() {
        return monetaryPenaltiesEnabled;
    }

    public String getMonetaryPenaltiesLabel() {
        return monetaryPenaltiesEnabled ? "ENABLED" : "DISABLED";
    }

    @Override
    public String toString() {
        return String.format("%s voted for monetary penalties: %s", id, getMonetaryPenaltiesLabel());
    }



}
