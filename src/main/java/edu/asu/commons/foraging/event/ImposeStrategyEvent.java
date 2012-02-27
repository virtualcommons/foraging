package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.event.FacilitatorRequest;
import edu.asu.commons.foraging.rules.Strategy;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * Notifies the server of a facilitator imposed strategy.  
 *  
 * @author alllee
 */
public class ImposeStrategyEvent extends AbstractPersistableEvent implements FacilitatorRequest {

	private static final long serialVersionUID = -7231412845435362871L;

	private final Strategy strategy;

	public ImposeStrategyEvent(Identifier id, Strategy strategy) {
		super(id, "Imposed strategy: " + strategy);
		this.strategy = strategy;
	}

	public Strategy getStrategy() {
		return strategy;
	}

}
