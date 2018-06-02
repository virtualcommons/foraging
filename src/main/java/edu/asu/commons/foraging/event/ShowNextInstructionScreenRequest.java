package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.event.ShowRequest;
import edu.asu.commons.net.Identifier;

/**
 * Facilitator-generated signal informing clients to show the next instructions request
 */
public class ShowNextInstructionScreenRequest extends AbstractEvent implements ShowRequest<ShowNextInstructionScreenRequest> {

    private static final long serialVersionUID = 3191928870607652346L;

    public ShowNextInstructionScreenRequest(Identifier id) {
        super(id);
    }
    @Override
    public ShowNextInstructionScreenRequest clone(Identifier id) {
        return new ShowNextInstructionScreenRequest(id);
    }
}
