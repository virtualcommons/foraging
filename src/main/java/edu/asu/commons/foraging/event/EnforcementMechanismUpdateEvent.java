//package edu.asu.commons.event;
package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.net.Identifier;


/**
 * $Id: EnforcementMechanismUpdateEvent.java 522 2010-06-30 19:17:48Z alllee $
 * 
 * Sent from the server to all clients about the 
 * updated vote stats
 * @author <a href='dbarge@asu.edu'>Deepak Barge</a>
 * @version $Revision: 522 $
 */

public class EnforcementMechanismUpdateEvent extends AbstractEvent {

    private static final long serialVersionUID = 5373346980670885924L;
    
    // FIXME: just send the entire group data model instead?
    private GroupDataModel groupDataModel;
    
	public EnforcementMechanismUpdateEvent(Identifier id, GroupDataModel groupDataModel) {
	    super(id);
	    this.groupDataModel = groupDataModel;
	}

	public GroupDataModel getGroupDataModel() {
		return groupDataModel;
	}

	
}
