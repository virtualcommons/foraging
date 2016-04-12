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

    private final List<MovementEvent> clientMovements = new ArrayList<>();
    private final List<MovementEvent> botMovements = new ArrayList<>();
    
    private final ClientData clientData;
    
    public SinglePlayerUpdateRequest(ClientData clientData) {
        super(clientData.getId());
        this.clientData = clientData;
    }
    
    public List<MovementEvent> getClientMovements() {
        return clientMovements;
    }

    public List<MovementEvent> getBotMovements() {
        return botMovements;
    }

    public ClientData getClientData() {
        return clientData;
    }

    public String toString() {
        return String.format("Single player update for id %1s.\n\t Client movements %2s\n\tBot movements %3s",
                getId(), clientMovements, botMovements);
    }

}
