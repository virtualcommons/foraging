package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;

public class FacilitatorUpdateEvent extends AbstractEvent {

    private static final long serialVersionUID = 5397683982101089592L;
    private ServerDataModel serverDataModel;
    private long timeLeft;

    public FacilitatorUpdateEvent(Identifier id, ServerDataModel serverDataModel, long timeLeft) {
        super(id);
        this.serverDataModel = serverDataModel;
        this.timeLeft = timeLeft;
    }

    public ServerDataModel getServerDataModel(){
        return serverDataModel;
    }

    public long getTimeLeft() {
        return timeLeft;
    }

}
