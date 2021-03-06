package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.model.Direction;
import edu.asu.commons.net.Identifier;


/**
 * $Id$
 * 
 * @version $Revision$
 * @author Allen Lee
 */
public class MovementEvent extends AbstractPersistableEvent {

    private static final long serialVersionUID = 3661804044615890419L;

    private final Direction direction;

    public MovementEvent(Identifier id, Direction direction) {
        super(id);
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
    
    public String toString() {
        return String.format("MovementEvent: Client [%s] moved [%s]", getId(), direction); 
    }
}
