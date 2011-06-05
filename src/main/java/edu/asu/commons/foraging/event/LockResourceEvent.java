package edu.asu.commons.foraging.event;


import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.net.Identifier;

public class LockResourceEvent extends AbstractPersistableEvent {
    
    private static final long serialVersionUID = -5928216418605179776L;
    
    private final Resource resource;
    private final boolean lockOwner;
    
    public LockResourceEvent(Identifier id, Resource resource, boolean lockOwner) {
        super(id);
        this.resource = resource;
        this.lockOwner = lockOwner;
    }

    public Resource getResource() {
        return resource;
    }

    public boolean isLockOwner() {
        return lockOwner;
    }
    

}
