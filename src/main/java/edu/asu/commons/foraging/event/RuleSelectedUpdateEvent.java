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
    private final List<ForagingStrategy> selectedStrategies;
    private final Map<ForagingStrategy, Integer> votingResults;
  
    public RuleSelectedUpdateEvent(Identifier id, List<ForagingStrategy> selectedStrategies, Map<ForagingStrategy, Integer> votingResults) {
        super(id, String.format("Strategies (first is tiebreaker): %s, All nominations: %s", selectedStrategies, votingResults));
        this.selectedStrategies = selectedStrategies;
        this.votingResults = votingResults;
    }
    
    public ForagingStrategy getSelectedRule() {
        return selectedStrategies.get(0);
    }
    
    public List<ForagingStrategy> getSelectedStrategies() {
        return selectedStrategies;
    }
    
    public Map<ForagingStrategy, Integer> getVotingResults() {
        return votingResults;
    }


}
