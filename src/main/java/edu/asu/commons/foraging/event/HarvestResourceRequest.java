package edu.asu.commons.foraging.event;


import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.net.Identifier;

public class HarvestResourceRequest extends AbstractPersistableEvent {

    private static final long serialVersionUID = 4989168893074124917L;
    
    private final Resource resource;
    
    public Resource getResource() {
        return resource;
    }

    public HarvestResourceRequest(Identifier id, Resource resource) {
        super(id);
        this.resource = resource;
    }
    
    public String toString() {
        return String.format("%s harvesting %s", id, resource);
    }

}
