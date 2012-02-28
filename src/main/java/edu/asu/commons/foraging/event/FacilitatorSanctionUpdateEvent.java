package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;


public class FacilitatorSanctionUpdateEvent extends AbstractEvent {
    
    private static final long serialVersionUID = 1L;
    private final ServerDataModel serverDataModel;
    
    public FacilitatorSanctionUpdateEvent(Identifier id, ServerDataModel serverDataModel) {
        super(id);
        this.serverDataModel = serverDataModel;
    }

    public boolean isLastRound() {
        return serverDataModel.isLastRound();
    }

	public ServerDataModel getServerDataModel() {
		return serverDataModel;
	}

}
