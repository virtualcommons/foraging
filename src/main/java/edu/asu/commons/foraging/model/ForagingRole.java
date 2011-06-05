package edu.asu.commons.foraging.model;

public enum ForagingRole {
    
    HARVEST("Harvest"), 
    MONITOR("Monitor"), 
    SANCTION_AND_HARVEST("Sanction and harvest");
    
    private final String label;
    
    ForagingRole(String label) {
        this.label = label;
    }
    
    public String toString() {
        return label;
    }
    
    public String getLabel() {
        return label;
    }
    
    public boolean isMonitor() {
        return this == MONITOR;
    }
    
    public boolean isSanctioningAllowed() {
        return this != HARVEST;
    }
    
    public boolean isHarvestingAllowed() {
    	return this != MONITOR;
    }

}
