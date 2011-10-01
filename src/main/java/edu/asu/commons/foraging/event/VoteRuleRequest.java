package edu.asu.commons.foraging.event;


import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.rules.ForagingRule;
import edu.asu.commons.net.Identifier;

/**
 * $Id: EnforcementRankingRequest.java 522 2010-06-30 19:17:48Z alllee $
 * 
 * Sent from a client to the server signaling that the client 
 * has updated the votes to the given options
 * 
 * @author <a href='allen.lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 522 $
 */

public class VoteRuleRequest extends AbstractPersistableEvent {

    private static final long serialVersionUID = 4360213814026474451L;
    private int index;
    private ForagingRule rule;
  
    public VoteRuleRequest(Identifier id, int index, ForagingRule rule) {
        super(id, rule.toString());
        this.index = index;
        this.rule = rule;
    }
    
    public int getIndex() {
        return index;
    }
    
    public ForagingRule getRule() {
        return rule;
    }
    
    @Override
    public String toString() {
        return String.format("%s voted for rule # %s (%s)", id, index, rule); 
    }


}
