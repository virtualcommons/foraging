package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.event.ShowRequest;
import edu.asu.commons.foraging.rules.decaro.VoteType;
import edu.asu.commons.net.Identifier;

public class ShowVotingInstructionsRequest extends AbstractEvent implements ShowRequest<ShowVotingInstructionsRequest> {

    private static final long serialVersionUID = 3774308614796618926L;

    private final VoteType voteType;

    public ShowVotingInstructionsRequest(Identifier id) {
        this(id, VoteType.STRATEGY);
    }

    public ShowVotingInstructionsRequest(Identifier id, VoteType voteType) {
        super(id);
        this.voteType = voteType;
    }

    public VoteType getVoteType() {
        return voteType;
    }

    @Override
    public ShowVotingInstructionsRequest clone(Identifier id) {
        return new ShowVotingInstructionsRequest(id, voteType);
    }
}
