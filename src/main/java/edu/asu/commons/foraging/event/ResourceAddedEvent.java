package edu.asu.commons.foraging.event;

import java.awt.Point;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * FoodAddedEvent
 * 
 * @author Allen Lee
 * @author Deepali Bhagvat
 * @version $Revision$
 */
public class ResourceAddedEvent extends AbstractPersistableEvent {

    private static final long serialVersionUID = 7223287520439659769L;

    private final Resource resource;

    private final GroupDataModel group;

    public ResourceAddedEvent(GroupDataModel group, Resource resource) {
        super(Identifier.NULL);
        this.group = group;
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }
    
    public Point getPosition() {
        return resource.getPosition();
    }

    public GroupDataModel getGroup() {
        return group;
    }

    public String toString() {
        return String.format("Resource added at position [%s]", getPosition());
    }

}