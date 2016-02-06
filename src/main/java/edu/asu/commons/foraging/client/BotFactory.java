package edu.asu.commons.foraging.client;


public class BotFactory {

    public static final BotFactory INSTANCE = new BotFactory();

    private BotFactory() {
    }

    public final static BotFactory getInstance() {
        return INSTANCE;
    }
    
    public Bot create(BotType botType) {
        switch (botType) {
            case AGGRESSIVE:
                return new AggressiveBot();
            case COOPERATIVE:
                return new CooperativeBot();
            case RANDOM:
            default:
                return new RandomBot();
        }
    }

}
