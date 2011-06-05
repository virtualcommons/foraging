package edu.asu.commons.foraging.event;


import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.net.Identifier;

public abstract class ResourceLockingRequest extends AbstractPersistableEvent {

    private static final long serialVersionUID = -7245497630743932394L;
    private final Resource resource;
    
    public ResourceLockingRequest(Identifier id, Resource resource) {
        super(id);
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }
}
