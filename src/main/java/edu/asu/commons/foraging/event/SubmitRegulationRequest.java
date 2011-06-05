package edu.asu.commons.foraging.event;


import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;

/**
 * $Id: SubmitRegulationRequest.java 522 2010-06-30 19:17:48Z alllee $
 * 
 * 
 * 
 * @author <a href='dbarge@asu.edu'>Deepak Barge</a>
 * @version $Revision: 522 $
 */

public class SubmitRegulationRequest extends AbstractPersistableEvent {

    private static final long serialVersionUID = 475300882222383637L;
    
    /**
     * A communication event with a target of Identifier.ALL is broadcast to all group participants.
     * @param source
     * @param message
     */
    public SubmitRegulationRequest(Identifier id, String regulation) {
        super(id, regulation);
    }
    
    public String getRegulation() {
        return message;
    }

    
}
