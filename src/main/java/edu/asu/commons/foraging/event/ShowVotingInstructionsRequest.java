package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.event.ShowRequest;
import edu.asu.commons.net.Identifier;

public class ShowVotingInstructionsRequest extends AbstractEvent implements ShowRequest<ShowVotingInstructionsRequest> {

    private static final long serialVersionUID = 3774308614796618926L;

    public ShowVotingInstructionsRequest(Identifier id) {
        super(id);
    }
    @Override
    public ShowVotingInstructionsRequest clone(Identifier id) {
        return new ShowVotingInstructionsRequest(id);
    }
}
