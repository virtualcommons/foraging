package edu.asu.commons.foraging.client;

import edu.asu.commons.foraging.model.Direction;

public class RandomBot extends Bot.SimpleBot {

    public final static BotType TYPE = BotType.RANDOM;

    public BotType getBotType() {
        return TYPE;
    }

    public int getActionsPerSecond() {
        return 8;
    }

    public double getMovementProbability() {
        return 0.8d;
    }

    @Override
    public Direction getNextMove(ClientDataModel model) {
        return Direction.random();
    }

}
