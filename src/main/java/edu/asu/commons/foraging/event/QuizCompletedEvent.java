package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.net.Identifier;


/**
 * $Id: QuizCompletedEvent.java 4 2008-07-25 22:51:44Z alllee $
 *
 * @author Allen Lee
 * @version $Revision: 4 $
 */

public class QuizCompletedEvent extends AbstractEvent {
    
	private static final long serialVersionUID = -4620951150514838395L;
	
	private QuizResponseEvent response;

	public QuizCompletedEvent(Identifier id, QuizResponseEvent response) {
        super(id);
        this.response = response;
    }
	
	public String toString() {
	    return String.format("quiz completed event: %s", response);
	}
}

