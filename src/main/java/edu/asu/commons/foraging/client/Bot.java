package edu.asu.commons.foraging.client;

import java.awt.Point;

import edu.asu.commons.foraging.model.Direction;
import edu.asu.commons.foraging.model.GroupDataModel;

public interface Bot {

    public BotType getBotType();
    
    public Point getCurrentPosition();
    
    public void setCurrentPosition(Point location);

    public int getActionsPerSecond();

    public double getMovementProbability();

    public Direction getNextMove(GroupDataModel model);

    public abstract class SimpleBot implements Bot {

        public Direction getNextMove(GroupDataModel model) {
            Point closestToken = getClosestToken(model);
            Point currentLocation = getCurrentPosition();
            if (closestToken == null) {
                return Direction.random();
            }
            else {
                int dx = currentLocation.x - closestToken.x;
                int dy = currentLocation.y - closestToken.y;
                if (dx > 0) {
                    return Direction.LEFT;
                }
                else if (dx < 0) {
                    return Direction.RIGHT;
                }
                else if (dy > 0) {
                    return Direction.UP;
                }
                else if (dy < 0) {
                    return Direction.DOWN;
                }
                else {
                    return Direction.random();
                }
            }
        }

        protected Point getClosestToken(GroupDataModel model) {
            Point currentLocation = getCurrentPosition();
            Point closestToken = null;
            double closestTokenDistance = Double.MAX_VALUE;
            for (Point resourcePosition : model.getResourcePositions()) {
                double distance = currentLocation.distanceSq(resourcePosition);
                if (distance < closestTokenDistance) {
                    closestTokenDistance = distance;
                    closestToken = resourcePosition;
                }
            }
            return closestToken;
        }

    }
}
