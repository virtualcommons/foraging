package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.net.Identifier;

public class ShowSurveyInstructionsRequest extends AbstractEvent implements ShowRequest<ShowSurveyInstructionsRequest> {

    private static final long serialVersionUID = 3774308614796618926L;

    public ShowSurveyInstructionsRequest(Identifier id) {
        super(id);
    }
    
    @Override
    public ShowSurveyInstructionsRequest copy(Identifier id) {
        return new ShowSurveyInstructionsRequest(id);
    }
}
