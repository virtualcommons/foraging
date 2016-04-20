package edu.asu.commons.foraging.client;

import java.awt.Point;
import java.io.Serializable;
import java.util.Random;
import java.util.logging.Logger;

import edu.asu.commons.experiment.Experiment;
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

    public Point getPosition();

    public void setCurrentPosition(Point location);

    public int getActionsPerSecond();
    
    public void resetActionsTakenPerSecond();

    public double getMovementProbability();

    public Direction getNextMove();

    public void initializePosition(RoundConfiguration configuration);
    
    public int getTicksToWait();
    
    public void setTicksToWait(int ticksToWait);

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
        private Point targetLocation;

        private double harvestProbability;
        private double movementProbability;
        private int actionsPerSecond;
        private int botNumber = 0;
        private int numberOfActionsTaken = 0;
        private GroupDataModel model;
        private int ticksToWait;

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
                logger.warning(String.format("Number of actions taken %d exceeds allowable actions per second %d",
                        numberOfActionsTaken, actionsPerSecond));
                return;
            }
            // next, check if we have a wait enforced on us
            else if (ticksToWait > 0) {
                logger.warning("waiting for " + ticksToWait);
                ticksToWait--;
                return;
            }
            // if neither, check if we are sitting on top of a token
            if (model.isResourceAt(getPosition())) {
                if (random.nextDouble() <= getHarvestProbability()) {
                    model.collectToken(this);
                }
            }
            // or figure out our next move and roll the dice to see if we can go.
            else {
                Direction nextMove = getNextMove();
                if (random.nextDouble() <= getMovementProbability()) {
                    model.move(this, nextMove);
                }
            }
            numberOfActionsTaken++;
        }

        public void resetActionsTakenPerSecond() {
            this.numberOfActionsTaken = 0;
        }

        public Point getPosition() {
            return currentPosition;
        }

        public void setCurrentPosition(Point currentPosition) {
            this.currentPosition = currentPosition;
        }

        public Direction getNextMove() {
            if (! hasTarget()) {
                setNewTargetLocation();
            }
            Direction nextMove = Direction.towards(getPosition(), getTargetLocation());
            logger.info("Target location: " + getTargetLocation());
            logger.info("Moving in direction: " + nextMove);
            if (nextMove == Direction.NONE) {
                // at target location
                reachedTargetLocation();
            }
            return nextMove;
        }
        
        protected void setNewTargetLocation() {
            targetLocation = getNearestToken();
            if (targetLocation == null) {
                // pick a random location on the board
                int x = random.nextInt(model.getRoundConfiguration().getResourceWidth());
                int y = random.nextInt(model.getRoundConfiguration().getResourceDepth());
                targetLocation = new Point(x, y);
            }
        }

        protected void reachedTargetLocation() {
            this.targetLocation = null;
            // FIXME: parameterize this?
            this.ticksToWait = random.nextInt(5);
        }

        protected Point getNearestToken() {
            Point currentLocation = getPosition();
            Point nearestToken = null;
            double nearestTokenDistance = Double.MAX_VALUE;
            // naive implementation, scans all positions
            // Could instead scan in a concentric ring around the current location and stop at the first hit.
            for (Point resourcePosition : model.getResourcePositions()) {
                double distance = currentLocation.distanceSq(resourcePosition);
                if (distance < nearestTokenDistance) {
                    nearestTokenDistance = distance;
                    nearestToken = resourcePosition;
                }
            }
            return nearestToken;
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
            logger.info("setting current bot position to " + getPosition());
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

        public Point getTargetLocation() {
            return targetLocation;
        }
        
        public boolean hasTarget() {
            return targetLocation != null;
        }

        public int getTicksToWait() {
            return ticksToWait;
        }

        public void setTicksToWait(int ticksToWait) {
            this.ticksToWait = ticksToWait;
        }

    }
}
