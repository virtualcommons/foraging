package edu.asu.commons.foraging.event;


import java.util.List;
import java.util.Map;

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

public class RuleSelectedUpdateEvent extends AbstractPersistableEvent {

    private static final long serialVersionUID = 4360213814026474451L;
    private final List<ForagingStrategy> selectedRules;
    private final Map<ForagingStrategy, Integer> votingResults;
  
    public RuleSelectedUpdateEvent(Identifier id, List<ForagingStrategy> selectedRules, Map<ForagingStrategy, Integer> votingResults) {
        super(id, selectedRules.toString());
        this.selectedRules = selectedRules;
        this.votingResults = votingResults;
    }
    
    public ForagingStrategy getSelectedRule() {
        return selectedRules.get(0);
    }
    
    public List<ForagingStrategy> getSelectedRules() {
        return selectedRules;
    }
    
    @Override
    public String toString() {
        return String.format("Selected first rule from %s", selectedRules);
    }

    public Map<ForagingStrategy, Integer> getVotingResults() {
        return votingResults;
    }


}
