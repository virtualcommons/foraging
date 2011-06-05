package edu.asu.commons.foraging.event;

import java.util.Map;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.event.ExperimentUpdateEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;

public class FacilitatorEndRoundEvent extends AbstractEvent implements ExperimentUpdateEvent {

    private static final long serialVersionUID = -9168439610675639286L;

    private final ServerDataModel serverDataModel;
    
    public FacilitatorEndRoundEvent(Identifier id, ServerDataModel serverDataModel) {
        super(id);
        this.serverDataModel = serverDataModel;
    }
    
    public ServerDataModel getServerDataModel() {
        return serverDataModel;
    }

    public boolean isLastRound() {
        return serverDataModel.isLastRound();
    }


    public Map<Identifier, ClientData> getClientDataMap() {
        return serverDataModel.getClientDataMap();
    }
}
