//package edu.asu.commons.event;
package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.net.Identifier;


/**
 * $Id: RegulationSubmissionUpdateEvent.java 522 2010-06-30 19:17:48Z alllee $
 * 
 * Sent from the server to all clients about the updated vote stats.
 * 
 * @author <a href='dbarge@asu.edu'>Deepak Barge</a>
 * @version $Revision: 522 $
 */

public class RegulationSubmissionUpdateEvent extends AbstractEvent {

    private static final long serialVersionUID = 475300882222383637L;
    
    private GroupDataModel groupDataModel;
            
	public RegulationSubmissionUpdateEvent(Identifier id, GroupDataModel groupDataModel) {
		super(id);
		this.groupDataModel = groupDataModel;
	}

	public GroupDataModel getGroupDataModel() {
		return groupDataModel;
	}

}
