package edu.asu.commons.foraging.model;

public enum SanctionMechanism {
    
	NONE("No Sanctioning","Everybody can harvest. Nobody can subtract tokens from others"),
	EVERYBODY_CAN_SANCTION("Everybody can sanction","Every participant can reduce the tokens of other participants by pressing the number key associated with that participant.");
		
    
    private final String title;
    private final String description;
    
    SanctionMechanism(String title, String description) {
        this.title = title;
        this.description = description;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    
    public static SanctionMechanism get(int index) {
        if (index < values().length && index >= 0) { 
            return values()[index];
        }
        System.err.println("Returning NONE.  No sanction mechanism for out-of-bounds index: " + index);
        return NONE;
//        throw new IllegalArgumentException("No enforcement mechanism for index: " + index);
    }

}
