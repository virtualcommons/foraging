//package edu.asu.commons.event;
package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.model.RegulationData;
import edu.asu.commons.net.Identifier;


/**
 * $Id: RegulationUpdateEvent.java 522 2010-06-30 19:17:48Z alllee $
 * 
 * Notifices clients of the regulation data receiving the highest vote ranking.
 * 
 * @author <a href='dbarge@asu.edu'>Deepak Barge</a>
 * @version $Revision: 522 $
 */

public class RegulationUpdateEvent extends AbstractEvent {

    private static final long serialVersionUID = 475300882222383637L;
    
    private RegulationData regulationData;
            
	public RegulationUpdateEvent(Identifier id, RegulationData regulationData) {
		super(id);
		this.regulationData = regulationData;
	}

	public RegulationData getRegulationData() {
		return regulationData;
	}

}
