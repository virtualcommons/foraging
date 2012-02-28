package edu.asu.commons.foraging.event;

import java.util.Map;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.event.FacilitatorRequest;
import edu.asu.commons.foraging.rules.Strategy;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * Notifies the server of a facilitator imposed strategy distribution.  
 *  
 * @author alllee
 */
public class ImposeStrategyEvent extends AbstractPersistableEvent implements FacilitatorRequest {

	private static final long serialVersionUID = -7231412845435362871L;

	private final Map<Strategy, Integer> strategyDistribution;

	public ImposeStrategyEvent(Identifier id, Map<Strategy, Integer> strategyDistribution) {
		super(id, "Imposed strategy: " + strategyDistribution);
		this.strategyDistribution = strategyDistribution;
	}

	public Map<Strategy, Integer> getStrategyDistribution() {
		return strategyDistribution;
	}
	
	@Override
	public String toString() {
		return "Imposed strategy distribution: " + strategyDistribution;
	}

}
