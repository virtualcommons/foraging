package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.net.Identifier;

public class ShowInstructionsRequest extends AbstractEvent implements ShowRequest<ShowInstructionsRequest> {
	
	private static final long serialVersionUID = 3774308614796618926L;

	public ShowInstructionsRequest(Identifier id) {
        super(id);
    }
	
	@Override
	public ShowInstructionsRequest copy(Identifier id) {
	    return new ShowInstructionsRequest(id);
	}
	
}
