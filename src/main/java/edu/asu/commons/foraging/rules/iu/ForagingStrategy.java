package edu.asu.commons.foraging.rules.iu;

import edu.asu.commons.foraging.rules.Strategy;

/**
 *
 * Strategies that participants can nominate (but not enforce) for Indiana experiments run by Daniel DeCaro in 2011/2012.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public enum ForagingStrategy implements Strategy {
    // FIXME: hard coded for 4 minute rounds, but templatizing this text is a bit of overkill at the moment 
    // figure out how best to move the construction of the description message into the configuration.
    WAIT_ONE_MINUTE("Wait 60 seconds for the screen to fill up with tokens (there will be 180 seconds left on the timer). Then everyone collects tokens for the remaining amount of time."),
    COLLECT_TOKENS_AND_WAIT("Collect 40 tokens, then wait 30 seconds. Repeat this process until time runs out or the tokens are all gone."),
    PRIVATE_PROPERTY("Players divide the field up into four equally-sized areas and can do whatever they want within their area. With four people, each person takes an area around one of the four corners."),
    WAIT_TEN_SECONDS("Collect tokens for 10 seconds, then wait 10 seconds before collecting again. Repeat this process until time runs out or the tokens are all gone."),
    NONE("Everyone can do whatever they want.");
    
    private final String description;
    
    private ForagingStrategy(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String toString() {
        return description;
    }
}
