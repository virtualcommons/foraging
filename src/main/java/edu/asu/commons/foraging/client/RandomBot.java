package edu.asu.commons.foraging.client;

import edu.asu.commons.foraging.model.Direction;

public class RandomBot extends Bot.SimpleBot {

    private static final long serialVersionUID = 8669701168883630901L;
    public final static int ACTIONS_PER_SECOND = 8;
    public final static double HARVEST_PROBABILITY = 0.9d;
    public final static double MOVEMENT_PROBABILITY = 0.9d;
    
    public RandomBot() {
        super(ACTIONS_PER_SECOND, MOVEMENT_PROBABILITY, HARVEST_PROBABILITY);
    }

    @Override
    public BotType getBotType() {
        return BotType.RANDOM;
    }

    @Override
    public Direction getNextMove() {
        return Direction.random();
    }

}
