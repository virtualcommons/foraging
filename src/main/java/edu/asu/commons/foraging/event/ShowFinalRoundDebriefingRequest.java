package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.event.ShowRequest;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * Facilitator request to show the final round debriefing on each client.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class ShowFinalRoundDebriefingRequest extends AbstractEvent implements ShowRequest<ShowFinalRoundDebriefingRequest> {

    private static final long serialVersionUID = 4971173111061909285L;

    public ShowFinalRoundDebriefingRequest(Identifier id) {
        super(id);
    }
    
    @Override
    public ShowFinalRoundDebriefingRequest copy(Identifier id) {
        return new ShowFinalRoundDebriefingRequest(id);
    }
}
