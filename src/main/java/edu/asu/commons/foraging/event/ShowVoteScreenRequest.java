package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.event.ShowRequest;
import edu.asu.commons.foraging.rules.decaro.VoteType;
import edu.asu.commons.net.Identifier;

public class ShowVoteScreenRequest extends AbstractEvent implements ShowRequest<ShowVoteScreenRequest> {

    private static final long serialVersionUID = 3774308614796618926L;

    // used to distinguish between displaying a strategy voting screen or enforcement voting screen
    private final VoteType voteType;

    public ShowVoteScreenRequest(Identifier id) {
        this(id, VoteType.STRATEGY);
    }

    public ShowVoteScreenRequest(Identifier id, VoteType voteType) {
        super(id);
        this.voteType = voteType;
    }

    public VoteType getVoteType() {
        return voteType;
    }

    @Override
    public ShowVoteScreenRequest clone(Identifier id) {
        return new ShowVoteScreenRequest(id, voteType);
    }
}
