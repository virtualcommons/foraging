package edu.asu.commons.foraging.event;

import java.awt.Point;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;
/**
 * $Id: TokenMovedEvent.java 78 2009-03-03 03:36:25Z alllee $
 * 
 * Signifies that a token moved from source to destination.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 78 $
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
