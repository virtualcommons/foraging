package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.event.ClientRequest;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * Signals that a client has completed the survey for the given round.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 522 $
 */
public class SurveyCompletedEvent extends AbstractPersistableEvent implements ClientRequest {

    private static final long serialVersionUID = -7081410122722056083L;

    public SurveyCompletedEvent(Identifier id) {
        super(id);
    }

    @Override
    public String toString() {
        return String.format("Survey completed by %s", id);
    }
    
}
