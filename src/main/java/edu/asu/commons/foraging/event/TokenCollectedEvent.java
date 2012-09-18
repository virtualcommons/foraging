package edu.asu.commons.foraging.event;

import java.awt.Point;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 *
 * Persistable event signifying that a token was collected at some Point getLocation() by 
 * the participant with Identifier getId().
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class TokenCollectedEvent extends AbstractPersistableEvent {

    // FIXME: crappy but keep it for backwards compatibility.
    private static final long serialVersionUID = 1L;
    private final Point location;
    
    public TokenCollectedEvent(Identifier id, Point location) {
        super(id);
        this.location = location;
    }
    
    public Point getLocation() {
        return location;
    }


}
