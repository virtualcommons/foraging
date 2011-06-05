package edu.asu.commons.foraging.event;


import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.net.Identifier;

public class LockResourceRequest extends ResourceLockingRequest {

    private static final long serialVersionUID = 4989168893074124917L;
    
    public LockResourceRequest(Identifier id, Resource resource) {
        super(id, resource);
    }

}
