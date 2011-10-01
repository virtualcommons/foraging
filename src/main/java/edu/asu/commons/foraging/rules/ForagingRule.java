package edu.asu.commons.foraging.rules;

/**
 * $Id$
 * 
 * A set of rules for the Indiana experiments run by Daniel DeCaro, Fall 2011.  
 * 
 * Split into a .iu subpackage if it turns out these aren't generic enough.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public enum ForagingRule {
    WAIT_TWO_MINUTES("Wait 2 minutes for the screen to fill up with green tokens. Then everyone collect tokens for the remaining amount of time."),
    PRIVATE_PROPERTY("Each person gets a set area within which they can do whatever they want with the green tokens. With four people, each person gets one of the four corners."),
    LIMITED_COLLECTION_RATE("Each person collects green tokens at a certain rate: 1 token every 3 seconds (i.e., count to 3 between each token you collect)."),
    MAINTAIN_TOKEN_CLUSTERS("When someone collects from a cluster of green tokens (i.e., 3 or more tokens that are touching each other) that person leaves at least 2 tokens touching each other."),
    LEAVE_TEN_FOR_REGROWTH("Collect green tokens until only about 10 are left; at that point, everyone must wait at least 1 minute before collecting any more tokens."),
    NONE("No rule (Everyone can do whatever they want).");
    
    private final String description;
    
    private ForagingRule(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String toString() {
        return description;
    }
}
