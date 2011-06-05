package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;

/**
 * $Id: SanctionAppliedEvent.java 522 2010-06-30 19:17:48Z alllee $
 * 
 * Persistable event marking an applied sanction.
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 522 $
 */

public class SanctionAppliedEvent extends AbstractPersistableEvent {

    private static final long serialVersionUID = 8025713540660478354L;
    
    private Identifier target;
    private int sanctionCost;
    private int sanctionPenalty;
    
    public SanctionAppliedEvent(Identifier source) {
        super(source);
    }

    public Identifier getTarget() {
        return target;
    }

    public void setTarget(Identifier target) {
        this.target = target;
    }

    public int getSanctionCost() {
        return sanctionCost;
    }

    public void setSanctionCost(int sanctionCost) {
        this.sanctionCost = sanctionCost;
    }

    public int getSanctionPenalty() {
        return sanctionPenalty;
    }

    public void setSanctionPenalty(int sanctionPenalty) {
        this.sanctionPenalty = sanctionPenalty;
    }
    

}
