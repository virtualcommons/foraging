package edu.asu.commons.foraging.client;

import edu.asu.commons.foraging.model.Direction;

public class RandomBot extends Bot.SimpleBot {

    private static final long serialVersionUID = 8669701168883630901L;
    public final static BotType TYPE = BotType.RANDOM;
    
    public RandomBot() {
        super(9, 1.0d, 0.6d);
    }

    public BotType getBotType() {
        return TYPE;
    }

    @Override
    public Direction getNextMove() {
        return Direction.random();
    }

}
