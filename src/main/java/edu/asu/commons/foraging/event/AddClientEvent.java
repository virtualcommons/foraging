package edu.asu.commons.foraging.event;

import java.awt.Point;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;

/**
 *
 * @author Allen Lee, Deepali Bhagvat
 */
public class AddClientEvent extends AbstractPersistableEvent {

    private static final long serialVersionUID = -3669175838042629378L;

    private Point3D initialPosition;
    private GroupDataModel group;
    private ClientData clientData;

    public AddClientEvent(ClientData clientData, GroupDataModel group, Point position) {
        this(clientData, group, new Point3D(position));
    }
    public AddClientEvent(ClientData clientData, GroupDataModel group, Point3D p) {
        super(clientData.getId());
        this.clientData = clientData;
        this.initialPosition = p;
        this.group = group;
    }

    public Point3D getPosition() {
        return initialPosition;
    }
    public GroupDataModel getGroup() {
        return group;
    }

    public ClientData getClientData() {
        return clientData;
    }
    
    public String toString() {
        return String.format("AddClientEvent: Client [%s] located at position [%s] in group [%s]",
                clientData, initialPosition, group);
    }
}
