package edu.asu.commons.foraging.bot;

import edu.asu.commons.foraging.model.GroupDataModel;

import java.awt.Point;

public class ChasePlayerBot extends Bot.SimpleBot {

    private static final long serialVersionUID = 6342394422312674232L;

    public final static int ACTIONS_PER_SECOND = 10;

    public final static double MOVEMENT_PROBABILITY = 0.9d;

    public final static double HARVEST_PROBABILITY = 1.0d;

    public final static double DEFAULT_BOT_TOKEN_DISTANCE_WEIGHT = 0.3d;

    public ChasePlayerBot() {
        super(ACTIONS_PER_SECOND, MOVEMENT_PROBABILITY, HARVEST_PROBABILITY);
    }

    public BotType getBotType() {
        return BotType.CHASE_PLAYER;
    }

    @Override
    public Point getTargetToken() {
        // return the Point closest to the participant and the bot
        GroupDataModel model = getGroupDataModel();
        Point participantLocation = getParticipantPosition();
        Point botLocation = getPosition();
        double minimumDistance = Double.MAX_VALUE;
        Point targetTokenLocation = null;
        for (Point tokenLocation : model.getResourcePositions()) {
            double distanceMeasure = participantLocation.distanceSq(tokenLocation) +
                    (getTokenProximityScalingFactor() * botLocation.distanceSq(tokenLocation));
            if (distanceMeasure <= minimumDistance) {
                minimumDistance = distanceMeasure;
                targetTokenLocation = tokenLocation;
            }
        }
        if (targetTokenLocation == null) {
            targetTokenLocation = getRandomLocation();
        }
        return targetTokenLocation;
    }

    @Override
    public int getMaxTicksToWait() {
        return 1;
    }

}
