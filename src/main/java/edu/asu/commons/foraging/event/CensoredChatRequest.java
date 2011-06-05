package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.event.ChatRequest;
import edu.asu.commons.event.ClientRequest;
import edu.asu.commons.net.Identifier;

/**
 * $Id: CensoredChatRequest.java 522 2010-06-30 19:17:48Z alllee $
 * 
 * Client chat request that should be forwarded on to the facilitator for approval.
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 522 $
 */
public class CensoredChatRequest extends AbstractPersistableEvent implements ClientRequest {

    private static final long serialVersionUID = -120847705184240264L;
    
    private Identifier target;
    
    public CensoredChatRequest(Identifier source, String message) {
        this(source, message, Identifier.ALL);
    }
    
    public CensoredChatRequest(Identifier source, String message, Identifier target) {
        super(source, message);
        this.target = target;
    }
    
    public Identifier getTarget() {
        return target;
    }
    
    public ChatRequest toApprovedChatRequest() {
        return new ChatRequest(getId(), getMessage(), getTarget());
    }
    
    public ChatRequest toDeniedChatRequest() {
        return new ChatRequest(getId(), String.format("Your message: [%s] was not approved.", getMessage()), getId());
    }

}
