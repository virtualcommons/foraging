package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;


public class ResetTokenDistributionRequest extends AbstractPersistableEvent {
    
    private static final long serialVersionUID = -7006146669272933804L;

    public ResetTokenDistributionRequest(Identifier id) {
        super(id);
    }
    
    public ResetTokenDistributionRequest(ResetTokenDistributionRequest copy) {
        super(copy.id);
    }
}
