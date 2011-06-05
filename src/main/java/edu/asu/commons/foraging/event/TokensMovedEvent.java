package edu.asu.commons.foraging.event;

import java.awt.Point;
import java.util.Collection;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;
/**
 * $Id: TokensMovedEvent.java 78 2009-03-03 03:36:25Z alllee $
 * 
 * Bulk token movement event containing old locations that should be removed and 
 * the new locations that should be added.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 78 $
 */
public class TokensMovedEvent extends AbstractPersistableEvent {

    private static final long serialVersionUID = -8130009188192973062L;
    private final Collection<Point> originalLocations;
    private final Collection<Point> newLocations;
    
    public TokensMovedEvent(Collection<Point> originalLocations, Collection<Point> newLocations) {
        super(Identifier.NULL);
        this.originalLocations = originalLocations;
        this.newLocations = newLocations;
    }
    
    public Collection<Point> getOriginalLocations() {
        return originalLocations;
    }

    public Collection<Point> getNewLocations() {
        return newLocations;
    }


}
