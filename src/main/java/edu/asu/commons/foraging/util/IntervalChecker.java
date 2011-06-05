package edu.asu.commons.foraging.util;


public class IntervalChecker {
	
	private int currentInterval;
	
	private long unitsPerInterval;

	public int getCurrentInterval() {
		return currentInterval;
	}

	public void setCurrentInterval(int currentInterval) {
		this.currentInterval = currentInterval;
	}

	public long getUnitsPerInterval() {
		return unitsPerInterval;
	}

	public void setUnitsPerInterval(int unitsPerInterval) {
		this.unitsPerInterval = unitsPerInterval;
	}

	public boolean isIntervalElapsed(long currentTime) {
		if (currentTime > (currentInterval * unitsPerInterval)) {
			currentInterval++;
			return true;
		}
		return false;
	}
	
}
