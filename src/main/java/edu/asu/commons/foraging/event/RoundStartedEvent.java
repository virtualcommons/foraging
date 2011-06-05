package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.event.ExperimentUpdateEvent;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.net.Identifier;





/**
 * $Id: RoundStartedEvent.java 497 2010-03-29 20:10:49Z alllee $
 * 
 * Signals clients that the beginning of a round has begun, carrying 
 * relevant round parameters so the client can initialize its game window
 * accordingly.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 497 $
 */
public class RoundStartedEvent extends AbstractEvent implements ExperimentUpdateEvent {

    private static final long serialVersionUID = -7632265994663707336L;
    private final GroupDataModel groupDataModel;

    public RoundStartedEvent(Identifier id, GroupDataModel groupDataModel) {
        super(id);
        this.groupDataModel = groupDataModel;
    }

    public GroupDataModel getGroupDataModel() {
        return groupDataModel;
    }

}
