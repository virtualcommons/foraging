package edu.asu.commons.foraging.rules.iu;

public class ForagingNomination {
	
	private final ForagingStrategy strategy;
	private final Integer nominations;
	private final boolean selectedStrategy;
	public ForagingNomination(ForagingStrategy strategy, Integer nominations, boolean selectedStrategy) {
		this.strategy = strategy;
		this.nominations = nominations;
		this.selectedStrategy = selectedStrategy;
	}
	public ForagingStrategy getStrategy() {
		return strategy;
	}
	public Integer getNominations() {
		return nominations;
	}
	public boolean isSelectedStrategy() {
		return selectedStrategy;
	}

}
