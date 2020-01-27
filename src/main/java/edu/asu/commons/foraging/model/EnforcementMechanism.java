package edu.asu.commons.foraging.model;

public enum EnforcementMechanism {
    
    NONE("No enforcement", "Everybody can harvest.  Nobody can subtract tokens from others"), 
    EVERYONE_CAN_SANCTION("Everybody can reduce", "Every participant can reduce the tokens of other participants by pressing the number key associated with that participant."), 
    RANDOM_MONITOR("Random monitor", "Randomly, one of the participants is selected to be the monitoring participant.  This participant cannot harvest but can subtract tokens from other participants by pressing the number key associated with that participant.  At the end of the round each harvesting participant pays 25% of their earned tokens to the monitoring participant."),
    // after (round duration / # of clients) seconds
    // each monitor receives 25% of the tokens collected during their monitoring time. 
    ROTATING_MONITOR("Rotating monitor", "Each participant is given an equal amount of time to be a monitor, with the ability to reduce the tokens of other participants by pressing the number key associated with that participant.");
    
    private final String title;
    private final String description;
    
    EnforcementMechanism(String title, String description) {
        this.title = title;
        this.description = description;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean hasMonitor() {
        return isRandomMonitor() || isRotatingMonitor();
    }
    
    public boolean isSanctioningEnabled() {
        return this == EVERYONE_CAN_SANCTION;
    }
    
    public boolean isRandomMonitor() {
        return this == RANDOM_MONITOR;
    }
    
    public boolean isRotatingMonitor() {
        return this == ROTATING_MONITOR;
    }
    
    public static EnforcementMechanism get(int index) {
        if (index < values().length && index >= 0) { 
            return values()[index];
        }
        System.err.println("Returning NONE.  No enforcement mechanism for out-of-bounds index: " + index);
        return NONE;
//        throw new IllegalArgumentException("No enforcement mechanism for index: " + index);
    }
    
}
