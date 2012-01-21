package edu.asu.commons.foraging.rules.iu;

/**
 * $Id$
 * 
 * A set of rules for the Indiana experiments run by Daniel DeCaro in 2011/2012.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public enum ForagingRule {
    // FIXME: hard coded for 4 minute rounds, but templatizing this text is a bit of overkill at the moment unless
    // we move this over to the configuration.
    WAIT_ONE_MINUTE("Wait 60 seconds for the screen to fill up with tokens. Then everyone collects tokens for the remaining amount of time."),
    PRIVATE_PROPERTY("Players divide the field up into four equally-sized areas and can do whatever they want within their area. With four people, each person takes an area around one of the four corners."),
    WAIT_TEN_SECONDS("Wait 10 seconds, then collect 3 tokens.  Repeat this process until the time runs out or all the tokens are gone."),
    COLLECT_TOKENS_AND_WAIT("Collect tokens for 10 seconds.  Then stop and wait for 5 seconds before starting this process again."),
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
