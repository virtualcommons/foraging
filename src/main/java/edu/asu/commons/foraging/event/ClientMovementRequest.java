package edu.asu.commons.foraging.event;

import java.awt.Point;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.event.ClientRequest;
import edu.asu.commons.foraging.model.Direction;
import edu.asu.commons.net.Identifier;



/**
 * $Id: ClientMovementRequest.java 350 2009-10-30 22:18:42Z alllee $
 * 
 * Client signal informing the server that the client has moved in Direction d
 * and has ended up at Point p.
 * 
 * @author <a href='mailto:alllee@cs.indiana.edu'>Allen Lee</a>
 * @version $Revision: 350 $
 */

public class ClientMovementRequest extends AbstractEvent implements ClientRequest {

    private static final long serialVersionUID = -871452113459811998L;

    private final Direction direction;
    
    private Point position;

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public ClientMovementRequest(Identifier source, Direction direction) {
        super(source);
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public String toString() {
        return "Client update: " + getId() + "\n\tDirection: " + direction;
    }

}
