package edu.asu.commons.foraging.event;

import java.awt.Point;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;
/**
 * $Id$
 * 
 * Signifies that a token moved from source to destination.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class TokenMovedEvent extends AbstractPersistableEvent {

    private static final long serialVersionUID = -8130009188192973062L;
    private final Point source;
    private final Point destination;
    
    public TokenMovedEvent(Point source, Point destination) {
        super(Identifier.NULL);
        this.source = source;
        this.destination = destination;
    }
    
    public Point getSource() {
        return source;
    }

    public Point getDestination() {
        return destination;
    }


}
