package edu.asu.commons.foraging.rules.iu;

public class ForagingStrategyNomination {
	
	private final ForagingStrategy strategy;
	private final Integer nominations;
	private final boolean selected;
	public ForagingStrategyNomination(ForagingStrategy strategy, Integer nominations, boolean selected) {
		this.strategy = strategy;
		this.nominations = nominations;
		this.selected = selected;
	}
	public ForagingStrategy getStrategy() {
		return strategy;
	}
	public Integer getNominations() {
		return nominations;
	}
	public boolean isSelected() {
		return selected;
	}

}
