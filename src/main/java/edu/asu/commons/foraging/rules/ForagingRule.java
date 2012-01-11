package edu.asu.commons.foraging.rules;

/**
 * $Id$
 * 
 * A set of rules for the Indiana experiments run by Daniel DeCaro in 2011/2012.
 * 
 * Split into an .iu or other subpackage if it turns out these aren't generic enough.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public enum ForagingRule {
    // FIXME: hard coded for 4 minute rounds, but templatizing this text is a bit of overkill at the moment unless
    // we move this over to the configuration.
    WAIT_ONE_MINUTE("Wait 60 seconds for the screen to fill up with tokens (the timer will have 180 seconds left). Then everyone collects tokens for the remaining amount of time."),
    PRIVATE_PROPERTY("Players divide the field up into four equally-sized areas and can do whatever they want within their area. With four people, each person takes an area around one of the four corners."),
    LIMITED_COLLECTION_RATE("Each person collects tokens at a rate of 1 token every 4 seconds."),
    MAINTAIN_TOKEN_CLUSTERS("Players leave two tokens untouched when the tokens are in a cluster of three or more that surround an empty cell."),
    LEAVE_TEN_FOR_REGROWTH("Collect tokens until only 10 are left; at that point, everyone must wait at least 30 seconds before collecting any more tokens."),
    NONE("Everyone can do whatever they want.");
    
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
