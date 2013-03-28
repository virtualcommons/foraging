package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;



/**
 * $Id$
 * 
 * A request made by a client to sanction another client.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>, Deepali Bhagvat
 * @version $Revision$
 */
public class RealTimeSanctionRequest extends AbstractPersistableEvent {

    private static final long serialVersionUID = -7099757477811030731L;
    
    private final Identifier target;
    
    public RealTimeSanctionRequest(Identifier sanctioner, Identifier target) {
        super(sanctioner);
        this.target = target;
    }
    
    public Identifier getTarget() {
        return target;
    }
    
    public Identifier getSource() {
        return id;
    }
    
    public String toString() {
        return String.format("Sanction request from %s -> %s", getId(), target); 
    }

}
