package edu.asu.commons.foraging.client;

import java.awt.Point;
import java.io.Serializable;
import java.util.Random;
import java.util.logging.Logger;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.model.Direction;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.net.Identifier;

public interface Bot {

    /**
     * The main entry point into a bot's behavior, invoked by the server every N milliseconds.
     * 
     * @param model
     *            the state of the world in which this bot is embedded
     */
    public void act();

    public BotType getBotType();

    public Identifier getIdentifier();

    public Point getCurrentPosition();

    public void setCurrentPosition(Point location);

    public int getActionsPerSecond();
    
    public void resetActionsTakenPerSecond();

    public double getMovementProbability();

    public Direction getNextMove();

    public void initializePosition(RoundConfiguration configuration);

    public int getBotNumber();

    public void setBotNumber(int botNumber);

    public void setGroupDataModel(GroupDataModel model);

    public class BotIdentifier extends Identifier.Base<BotIdentifier> {
        private static final long serialVersionUID = 1609142256924017761L;
    }

    public abstract class SimpleBot implements Bot, Serializable {

        private static final long serialVersionUID = 2437093153712520070L;
        public final static int DEFAULT_ACTIONS_PER_SECOND = 8;
        public final static double DEFAULT_MOVEMENT_PROBABILITY = 0.9d;
        public final static double DEFAULT_HARVEST_PROBABILITY = 0.9d;

        private final Identifier identifier = new BotIdentifier();

        private Point currentPosition;

        private double harvestProbability;
        private double movementProbability;
        private int actionsPerSecond;
        private int botNumber = 0;
        private int numberOfActionsTaken = 0;
        private GroupDataModel model;

        private final transient Random random = new Random();

        protected final transient Logger logger = Logger.getLogger(getClass().getName());

        public SimpleBot() {
            this(DEFAULT_ACTIONS_PER_SECOND, DEFAULT_MOVEMENT_PROBABILITY, DEFAULT_HARVEST_PROBABILITY);
        }

        public SimpleBot(int actionsPerSecond, double movementProbability, double harvestProbability) {
            this.actionsPerSecond = actionsPerSecond;
            this.movementProbability = movementProbability;
            this.harvestProbability = harvestProbability;
        }

        public void act() {
            // first, check number of actions taken vs actions per second
            if (numberOfActionsTaken > actionsPerSecond) {
                logger.info(String.format("Number of actions taken %d exceeds allowable actions per second %d",
                        numberOfActionsTaken, actionsPerSecond));
                return;
            }
            // check if we are sitting on top of a token
            if (model.isResourceAt(getCurrentPosition())) {
                if (random.nextDouble() <= getHarvestProbability()) {
                    model.collectToken(this);
                }
            }
            else {
                Direction nextMove = getNextMove();
                if (random.nextDouble() <= getMovementProbability()) {
                    logger.info("Bot moving " + nextMove);
                    model.move(this, nextMove);
                }
            }
            numberOfActionsTaken++;
        }

        public void resetActionsTakenPerSecond() {
            this.numberOfActionsTaken = 0;
        }

        public Point getCurrentPosition() {
            return currentPosition;
        }

        public void setCurrentPosition(Point currentPosition) {
            this.currentPosition = currentPosition;
        }

        public Direction getNextMove() {
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

        public void setHarvestProbability(double harvestProbability) {
            this.harvestProbability = harvestProbability;
        }

        public void setMovementProbability(double movementProbability) {
            this.movementProbability = movementProbability;
        }

        public void setActionsPerSecond(int actionsPerSecond) {
            this.actionsPerSecond = actionsPerSecond;
        }

        public int getActionsPerSecond() {
            return actionsPerSecond;
        }

        public double getMovementProbability() {
            return movementProbability;
        }

        public double getHarvestProbability() {
            return harvestProbability;
        }

        public void initializePosition(RoundConfiguration roundConfiguration) {
            int clientsPerGroup = roundConfiguration.getClientsPerGroup();
            int groupSize = clientsPerGroup + roundConfiguration.getBotsPerGroup();
            int positionNumber = getBotNumber() + clientsPerGroup;
            int resourceWidth = roundConfiguration.getResourceWidth();
            int resourceHeight = roundConfiguration.getResourceDepth();
            double cellWidth = resourceWidth / (double) groupSize;
            int x = (int) ((cellWidth / 2) + (cellWidth * (positionNumber - 1)));
            int y = resourceHeight / 2;
            setCurrentPosition(new Point(x, y));
        }

        public int getBotNumber() {
            return botNumber;
        }

        public void setBotNumber(int botNumber) {
            this.botNumber = botNumber;
        }

        public Identifier getIdentifier() {
            return identifier;
        }

        public void setGroupDataModel(GroupDataModel groupDataModel) {
            this.model = groupDataModel;
        }

    }
}
