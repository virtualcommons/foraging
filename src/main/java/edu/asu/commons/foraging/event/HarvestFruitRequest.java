package edu.asu.commons.foraging.event;


import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.net.Identifier;

public class HarvestFruitRequest extends AbstractPersistableEvent {

	private static final long serialVersionUID = -2674710065706669710L;
	
	private final Resource resource;
    
    public Resource getResource() {
        return resource;
    }

    public HarvestFruitRequest(Identifier id, Resource resource) {
        super(id);
        this.resource = resource;
    }
}
