package edu.asu.commons.foraging.event;

import java.awt.Point;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.net.Identifier;

/**
 * Signals that the client wants to collect a token at its present location.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class CollectTokenRequest extends AbstractEvent {
    
    private static final long serialVersionUID = 101809140032425355L;
    
    private final Point position;

    public CollectTokenRequest(Identifier id) {
        this(id, null);
    }
    
    public CollectTokenRequest(Identifier id, Point position) {
        super(id);
        this.position = position;
    }
    
    public Point getPosition() {
        return position;
    }
    
    public boolean isSinglePlayer() {
        return position != null;
    }

}
