package edu.asu.commons.foraging.event;

import java.util.ArrayList;
import java.util.List;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.event.ClientRequest;
import edu.asu.commons.foraging.model.ClientData;



/**
 * Used by an authoritative client to let the server know what's happened since the last request.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 */

public class SinglePlayerUpdateRequest extends AbstractEvent implements ClientRequest {

    private static final long serialVersionUID = -871452113459811998L;

    private final List<ClientMovementRequest> clientMovements = new ArrayList<>();
    private final List<ClientMovementRequest> botMovements = new ArrayList<>();
    
    private final ClientData clientData;
    
    public SinglePlayerUpdateRequest(ClientData clientData) {
        super(clientData.getId());
        this.clientData = clientData;
    }
    
    public List<ClientMovementRequest> getClientMovements() {
        return clientMovements;
    }

    public List<ClientMovementRequest> getBotMovements() {
        return botMovements;
    }

    public ClientData getClientData() {
        return clientData;
    }

    public String toString() {
        return "Single player client update: " + getId() + "\n\t";
    }

}
