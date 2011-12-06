package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.event.ChatRequest;
import edu.asu.commons.event.ClientRequest;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * Event used to forward censored chat requests to the facilitator. 
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 522 $
 */
public class FacilitatorCensoredChatRequest extends AbstractPersistableEvent implements ClientRequest {

    private static final long serialVersionUID = -120847705184240264L;

    private ChatRequest censoredChatRequest;
    
    public FacilitatorCensoredChatRequest(Identifier id, ChatRequest request) {
        super(id);
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
    
    public void setId(Identifier id) {
        super.id = id;
    }
    

}
