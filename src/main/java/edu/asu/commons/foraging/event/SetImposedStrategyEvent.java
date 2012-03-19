package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.rules.Strategy;
import edu.asu.commons.net.Identifier;

public class SetImposedStrategyEvent extends AbstractEvent {

	private static final long serialVersionUID = -6046837892041909032L;
	
	private Strategy strategy;
	
	public SetImposedStrategyEvent(Identifier id, Strategy strategy) {
		super(id);
		this.strategy = strategy;
	}

	public Strategy getStrategy() {
		return strategy;
	}

}
