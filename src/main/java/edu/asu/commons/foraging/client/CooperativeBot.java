package edu.asu.commons.foraging.client;

public class CooperativeBot extends Bot.SimpleBot {

    public final static int ACTIONS_PER_SECOND = 8;
    public final static double HARVEST_PROBABILITY = 0.4d;
    public final static double MOVEMENT_PROBABILITY = 0.9d;

    public CooperativeBot() {
        super(ACTIONS_PER_SECOND, MOVEMENT_PROBABILITY, HARVEST_PROBABILITY);
    }

    @Override
    public BotType getBotType() {
        return BotType.COOPERATIVE;
    }

}
