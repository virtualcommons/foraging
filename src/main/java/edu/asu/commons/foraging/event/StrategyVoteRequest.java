package edu.asu.commons.foraging.event;


import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.rules.decaro.ForagingStrategy;
import edu.asu.commons.net.Identifier;

/**
 * Signals that a client has cast a vote for the given Strategy.
 * 
 * @author <a href='allen.lee@asu.edu'>Allen Lee</a>
 */

public class StrategyVoteRequest extends AbstractPersistableEvent {

    private static final long serialVersionUID = 4360213814026474451L;

    private ForagingStrategy strategy;
  
    public StrategyVoteRequest(Identifier id, ForagingStrategy strategy) {
        super(id, strategy.toString());
        this.strategy = strategy;
    }
    
    public ForagingStrategy getRule() {
        return strategy;
    }
    
    @Override
    public String toString() {
        return String.format("%s voted for rule [%s]", id, strategy);
    }



}
