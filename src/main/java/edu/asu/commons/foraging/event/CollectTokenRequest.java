package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.net.Identifier;

/**
 * $Id: CollectTokenRequest.java 4 2008-07-25 22:51:44Z alllee $
 * 
 * Signals that the client wants to collect a token at its present location.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 4 $
 */
public class CollectTokenRequest extends AbstractEvent {
    
    private static final long serialVersionUID = 101809140032425355L;
    
    public CollectTokenRequest(Identifier id) {
        super(id);
    }

}
