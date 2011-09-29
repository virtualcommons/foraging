package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;

public class SurveyIdSubmissionRequest extends AbstractPersistableEvent {

    private static final long serialVersionUID = -7577341220291009463L;
    
    public SurveyIdSubmissionRequest(Identifier id, String surveyId) {
        super(id, surveyId);
    }
    
    public String getSurveyId() {
        return getMessage();
    }

}
