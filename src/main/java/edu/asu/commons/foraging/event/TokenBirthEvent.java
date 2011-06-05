package edu.asu.commons.foraging.event;

import java.awt.Point;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;

/**
 * $Id: TokenBirthEvent.java 78 2009-03-03 03:36:25Z alllee $
 * 
 * Signifies that a resource at location source gave "birth" at location offspring.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 78 $
 */
public class TokenBirthEvent extends AbstractPersistableEvent {

    private static final long serialVersionUID = -5583615580820624952L;
    private final Point parent;
    private final Point offspring;

    /**
     * No Identifier is necessary since this is a system generated event.
     * @param source
     * @param offspring
     */
    public TokenBirthEvent(Point source, Point offspring) {
        super(Identifier.NULL);
        this.parent = source;
        this.offspring = offspring;
    }
    
    public Point getParent() {
        return parent;
    }

    public Point getOffspring() {
        return offspring;
    }


}
