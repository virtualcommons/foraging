package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.net.Identifier;

public class TrustGameSubmissionEvent extends AbstractEvent {

    private static final long serialVersionUID = 1957461200448076811L;
    private TrustGameSubmissionRequest request;
    
    public TrustGameSubmissionEvent(Identifier id, TrustGameSubmissionRequest request) {
        super(id);
        this.request = request;
    }

    public TrustGameSubmissionRequest getRequest() {
        return request;
    }
    
    public String toString() {
        return String.format("Trust game strategy submitted: %s", request);
    }

}
