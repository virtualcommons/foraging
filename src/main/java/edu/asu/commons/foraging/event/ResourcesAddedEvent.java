package edu.asu.commons.foraging.event;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.net.Identifier;

/**
 * $Id: ResourcesAddedEvent.java 4 2008-07-25 22:51:44Z alllee $
 * FoodAddedEvent
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 4 $
 */
public class ResourcesAddedEvent extends AbstractPersistableEvent {

    private static final long serialVersionUID = 7223287520439659769L;

    private final Set<Resource> resources;

    private final GroupDataModel group;

    public ResourcesAddedEvent(GroupDataModel group, Set<Resource> resources) {
        super(Identifier.NULL);
        this.group = group;
        this.resources = resources;
    }

    public Set<Resource> getResources() {
        return resources;
    }
    
    public Set<Point> getResourcePositions() {
        // how nice it would be to just do a return Collections.map( _.position, resources);
        Set<Point> points = new HashSet<Point>();
        for (Resource resource: resources) {
            points.add(resource.getPosition());
        }
        return points;
    }

    public GroupDataModel getGroup() {
        return group;
    }

    public String toString() {
        return String.format("Multiple tokens added at position [%s]", getResources());
    }

}