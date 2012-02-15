package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.event.ShowRequest;
import edu.asu.commons.net.Identifier;

public class ShowTrustGameRequest extends AbstractEvent implements ShowRequest<ShowTrustGameRequest> {

    private static final long serialVersionUID = 3774308614796618926L;

    public ShowTrustGameRequest(Identifier id) {
        super(id);
    }
    
    @Override
    public ShowTrustGameRequest clone(Identifier id) {
        return new ShowTrustGameRequest(id);
    }
}
