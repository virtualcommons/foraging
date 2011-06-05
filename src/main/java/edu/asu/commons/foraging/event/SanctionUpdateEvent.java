package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.net.Identifier;

public class SanctionUpdateEvent extends AbstractEvent {
	
	private static final long serialVersionUID = 3456876534653456891L;
	
	private GroupDataModel groupDataModel;
	
	public SanctionUpdateEvent(Identifier id, GroupDataModel groupDataModel) {
		super(id);
		this.groupDataModel = groupDataModel;
	}
	
	public GroupDataModel getGroupDataModel() {
		return groupDataModel;
	}	
}
