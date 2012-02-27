package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.event.ShowRequest;
import edu.asu.commons.foraging.rules.Strategy;
import edu.asu.commons.net.Identifier;

public class ShowImposedStrategyRequest extends AbstractEvent implements ShowRequest<ShowImposedStrategyRequest> {

	private static final long serialVersionUID = -6046837892041909032L;
	
	private final Strategy strategy;

	public ShowImposedStrategyRequest(Identifier id, Strategy strategy) {
		super(id);
		this.strategy = strategy;
	}

	@Override
	public ShowImposedStrategyRequest clone(Identifier id) {
		return new ShowImposedStrategyRequest(id, strategy);
	}

	public Strategy getStrategy() {
		return strategy;
	}

}
