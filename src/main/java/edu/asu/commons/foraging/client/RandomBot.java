package edu.asu.commons.foraging.client;

import edu.asu.commons.foraging.model.Direction;
import edu.asu.commons.foraging.model.GroupDataModel;

public class RandomBot extends Bot.SimpleBot {

    public final static BotType TYPE = BotType.RANDOM;
    
    public RandomBot() {
        super(9, 1.0d, 0.6d);
    }

    public BotType getBotType() {
        return TYPE;
    }

    @Override
    public Direction getNextMove(GroupDataModel model) {
        return Direction.random();
    }

}
