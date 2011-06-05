package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.net.Identifier;


/**
 * $Id: BeginChatRoundRequest.java 4 2008-07-25 22:51:44Z alllee $
 *
 * The facilitator uses this Request to signal the server that the timed
 * communication can now begin.
 *
 * @author Allen Lee
 * @version $Revision: 4 $
 */

public class BeginChatRoundRequest extends AbstractEvent {

    private static final long serialVersionUID = -1664493865684899462L;
    
    private GroupDataModel groupDataModel;

    public BeginChatRoundRequest(Identifier id) {
        super(id);
    }
    
    public BeginChatRoundRequest(Identifier id, GroupDataModel groupDataModel) {
        super(id);
        this.groupDataModel = groupDataModel;
    }
    
    public GroupDataModel getGroupDataModel() {
        return groupDataModel;
    }

}
