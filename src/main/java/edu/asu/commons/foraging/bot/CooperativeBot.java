package edu.asu.commons.foraging.bot;

public class CooperativeBot extends Bot.SimpleBot {

    private static final long serialVersionUID = 4732276788271013068L;
    public final static int ACTIONS_PER_SECOND = 10;
    public final static double HARVEST_PROBABILITY = 0.2d;
    public final static double MOVEMENT_PROBABILITY = 0.8d;

    public CooperativeBot() {
        super(ACTIONS_PER_SECOND, MOVEMENT_PROBABILITY, HARVEST_PROBABILITY);
    }

    @Override
    public BotType getBotType() {
        return BotType.IGNORE_PLAYER;
    }

}
