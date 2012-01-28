package edu.asu.commons.foraging.event;


import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.rules.iu.ForagingStrategy;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * Sent from a client to the server signaling that the client 
 * has updated the votes to the given options
 * 
 * @author <a href='allen.lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

public class RuleVoteRequest extends AbstractPersistableEvent {

    private static final long serialVersionUID = 4360213814026474451L;
    private ForagingStrategy rule;
  
    public RuleVoteRequest(Identifier id, ForagingStrategy rule) {
        super(id, rule.toString());
        this.rule = rule;
    }
    
    public ForagingStrategy getRule() {
        return rule;
    }
    
    @Override
    public String toString() {
        return String.format("%s voted for rule [%s]", id, rule); 
    }


}
