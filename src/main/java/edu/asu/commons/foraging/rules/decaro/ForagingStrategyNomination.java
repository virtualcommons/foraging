package edu.asu.commons.foraging.rules.decaro;

import edu.asu.commons.foraging.rules.Strategy;

public class ForagingStrategyNomination {
	
	private final Strategy strategy;
	private final Integer nominations;
	private final boolean selected;
	public ForagingStrategyNomination(Strategy strategy, Integer nominations, boolean selected) {
		this.strategy = strategy;
		this.nominations = nominations;
		this.selected = selected;
	}
	public Strategy getStrategy() {
		return strategy;
	}
	public Integer getNominations() {
		return nominations;
	}
	public boolean isSelected() {
		return selected;
	}

}
