package edu.asu.commons.foraging.event;

import edu.asu.commons.event.FacilitatorRequest;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * Marker interface for facilitator requests that should just pass through directly to the client.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public interface ShowRequest<T extends ShowRequest<T>> extends FacilitatorRequest {
    public T copy(Identifier id);
}
