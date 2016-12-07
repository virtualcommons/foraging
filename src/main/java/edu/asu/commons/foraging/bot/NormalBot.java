package edu.asu.commons.foraging.bot;

public class NormalBot extends Bot.SimpleBot {

    private static final long serialVersionUID = 4732276788271013068L;

    public final static int ACTIONS_PER_SECOND = 6;

    public final static double HARVEST_PROBABILITY = 0.6d;

    public final static double MOVEMENT_PROBABILITY = 0.9d;

    private BotType botType = BotType.NORMAL;

    public NormalBot() {
        super(ACTIONS_PER_SECOND, MOVEMENT_PROBABILITY, HARVEST_PROBABILITY);
    }

    public NormalBot(int actionsPerSecond, double movementProbability, double harvestProbability) {
        super(actionsPerSecond, movementProbability, harvestProbability);
        botType = BotType.CUSTOM;
    }

    @Override
    public BotType getBotType() {
        return botType;
    }

}
