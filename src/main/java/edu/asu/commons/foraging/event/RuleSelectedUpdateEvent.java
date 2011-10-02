package edu.asu.commons.foraging.event;


import java.util.List;

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

public class RuleSelectedUpdateEvent extends AbstractPersistableEvent {

    private static final long serialVersionUID = 4360213814026474451L;
    private final ForagingRule selectedRule;
    private final List<ForagingRule> candidates;
  
    public RuleSelectedUpdateEvent(Identifier id, ForagingRule rule, List<ForagingRule> candidates) {
        super(id, rule.toString());
        this.selectedRule = rule;
        this.candidates = candidates;
    }
    
    public ForagingRule getSelectedRule() {
        return selectedRule;
    }
    
    @Override
    public String toString() {
        return selectedRule.toString();
    }

    public List<ForagingRule> getCandidates() {
        return candidates;
    }


}
