package edu.asu.commons.foraging.bot;

import edu.asu.commons.foraging.model.GroupDataModel;

import java.awt.Point;

public class AggressiveBot extends Bot.SimpleBot {

    private static final long serialVersionUID = 6342394422312674232L;

    public final static int ACTIONS_PER_SECOND = 10;

    public final static double MOVEMENT_PROBABILITY = 0.9d;

    public final static double HARVEST_PROBABILITY = 1.0d;

    public final static double BOT_TOKEN_DISTANCE_WEIGHT = 3.0d;

    public AggressiveBot() {
        super(ACTIONS_PER_SECOND, MOVEMENT_PROBABILITY, HARVEST_PROBABILITY);
    }

    public BotType getBotType() {
        return BotType.AGGRESSIVE;
    }

    @Override
    public Point getNearestToken() {
        // return the Point closest to the participant and the bot
        GroupDataModel model = getGroupDataModel();
        Point participantLocation = model.getClientPositions().values().iterator().next();
        Point botLocation = getPosition();
        double minimumDistance = Double.MAX_VALUE;
        Point targetTokenLocation = null;
        for (Point tokenLocation : model.getResourcePositions()) {
            double measure = participantLocation.distanceSq(tokenLocation) +
                    (BOT_TOKEN_DISTANCE_WEIGHT * botLocation.distanceSq(tokenLocation));
            if (measure <= minimumDistance) {
                minimumDistance = measure;
                targetTokenLocation = tokenLocation;
            }
        }
        if (targetTokenLocation == null) {
            return getRandomLocation();
        }
        logger.info("token closest to bot and player " + targetTokenLocation + " distance: " + minimumDistance);
        return targetTokenLocation;
    }

    @Override
    public int getMaxTicksToWait() {
        return 1;
    }

}
