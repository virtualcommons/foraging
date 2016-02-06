package edu.asu.commons.foraging.client;


public class AggressiveBot extends Bot.SimpleBot {
    
    public AggressiveBot() {
        super(10, 1.0d, 1.0d);
    }
    
    public BotType getBotType() {
        return BotType.AGGRESSIVE;
    }

}
