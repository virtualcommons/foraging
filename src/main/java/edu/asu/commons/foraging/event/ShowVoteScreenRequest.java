package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.net.Identifier;

public class ShowVoteScreenRequest extends AbstractEvent implements ShowRequest<ShowVoteScreenRequest> {

    private static final long serialVersionUID = 3774308614796618926L;

    public ShowVoteScreenRequest(Identifier id) {
        super(id);
    }
    
    @Override
    public ShowVoteScreenRequest copy(Identifier id) {
        return new ShowVoteScreenRequest(id);
    }
}
