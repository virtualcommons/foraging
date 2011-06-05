package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.net.Identifier;

/**
 * $Id: ClientPoseUpdate.java 4 2008-07-25 22:51:44Z alllee $
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>, Deepali Bhagvat
 * @version $Revision: 4 $
 */
public class ClientPoseUpdate extends AbstractPersistableEvent {

    private static final long serialVersionUID = 2431334848968237859L;
    
    private final Point3D position;
    private final float heading;
    private final int animationState;
    private final boolean animationActive;
    
    public ClientPoseUpdate(Identifier id, Point3D position) {
        this(id, position, -1, -1, false);
    }
    
    public ClientPoseUpdate(Identifier id, Point3D position, float heading, int animationState, boolean animationActive) {
        super(id);
        this.position = position;
        this.heading = heading;
        this.animationState = animationState;
        this.animationActive = animationActive;
    }

    public Point3D getPosition() {
        return position;
    }

    public float getHeading() {
        return heading;
    }
    
    public int getAnimationState() {
    	return animationState;
    }

    public boolean isAnimationActive() {
    	return animationActive;
    }
}
