package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.event.ChatRequest;
import edu.asu.commons.event.ClientRequest;
import edu.asu.commons.net.Identifier;

/**
 * $Id: FacilitatorCensoredChatRequest.java 522 2010-06-30 19:17:48Z alllee $
 * 
 * Event used to forward censored chat requests to the facilitator. 
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 522 $
 */
public class FacilitatorCensoredChatRequest extends AbstractPersistableEvent implements ClientRequest {

    private static final long serialVersionUID = -120847705184240264L;

    private ChatRequest censoredChatRequest;
    
    public FacilitatorCensoredChatRequest(Identifier facilitatorId, ChatRequest request) {
        super(facilitatorId);
        this.censoredChatRequest = request;
    }
    
    public ChatRequest getCensoredChatRequest() {
        return censoredChatRequest;
    }
    
    public Identifier getSource() {
        return censoredChatRequest.getId();
    }
    
    public Identifier getTarget() {
        return censoredChatRequest.getTarget();
    }
    

}
