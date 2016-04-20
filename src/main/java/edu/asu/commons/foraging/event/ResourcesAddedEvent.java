package edu.asu.commons.foraging.event;

import java.awt.Point;
import java.util.Set;
import java.util.stream.Collectors;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.net.Identifier;

/**
 * Persistable event marking a collection of resources added to a group.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class ResourcesAddedEvent extends AbstractPersistableEvent {

    private static final long serialVersionUID = 7223287520439659769L;

    private final Set<Resource> resources;

    private final GroupDataModel group;
    
    public ResourcesAddedEvent(Identifier id, GroupDataModel group, Set<Resource> resources) {
        super(id);
        this.group = group;
        this.resources = resources;        
    }

    public ResourcesAddedEvent(GroupDataModel group, Set<Resource> resources) {
        this(Identifier.NULL, group, resources);
    }

    public Set<Resource> getResources() {
        return resources;
    }
    
    public Set<Point> getResourcePositions() {
        return resources.stream().map((resource) -> resource.getPosition()).collect(Collectors.toSet());
    }

    public GroupDataModel getGroup() {
        return group;
    }

    public String toString() {
        return String.format("Multiple tokens added at position [%s]", getResources());
    }

}