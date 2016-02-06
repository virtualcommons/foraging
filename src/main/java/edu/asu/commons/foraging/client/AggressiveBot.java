package edu.asu.commons.foraging.client;

public class AggressiveBot extends Bot.SimpleBot {

    private static final long serialVersionUID = 6342394422312674232L;

    public final static int ACTIONS_PER_SECOND = 10;

    public final static double MOVEMENT_PROBABILITY = 0.8d;

    public final static double HARVEST_PROBABILITY = 0.9d;

    public AggressiveBot() {
        super(ACTIONS_PER_SECOND, MOVEMENT_PROBABILITY, HARVEST_PROBABILITY);
    }

    public BotType getBotType() {
        return BotType.AGGRESSIVE;
    }

}
