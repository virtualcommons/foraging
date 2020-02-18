package edu.asu.commons.foraging.bot;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.model.Actor;
import edu.asu.commons.foraging.model.Direction;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.net.Identifier;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

public interface Bot extends Actor {

    /**
     * The main entry point into a bot's behavior, invoked by the server every N milliseconds.
     * <p>
     * FIXME: consider injecting the model / state of the world for the bot as a parameter
     */
    void act();

    BotType getBotType();

    Identifier getId();

    int getCurrentTokens();

    void addToken(Point location);

    Point getPosition();

    Bot setPosition(Point location);

    int getActionsPerSecond();

    Bot setActionsPerSecond(int actionsPerSecond);

    void resetActionsTaken();

    double getMovementProbability();

    Bot setMovementProbability(double movementProbability);

    double getHarvestProbability();

    Bot setHarvestProbability(double harvestProbability);

    double getTokenProximityScalingFactor();

    Bot setTokenProximityScalingFactor(double tokenProximityScalingFactor);

    Direction getNextMove();

    void initialize(RoundConfiguration configuration);

    int getTicksToWait();

    Bot setTicksToWait(int ticksToWait);

    int getBotNumber();

    Bot setBotNumber(int botNumber);

    Bot setGroupDataModel(GroupDataModel model);

    Bot NULL = new Bot() {

        @Override
        public void act() {
        }

        @Override
        public BotType getBotType() {
            return null;
        }

        @Override
        public Identifier getId() {
            return Identifier.NULL;
        }

        @Override
        public Point getPosition() {
            return new Point(-1, -1);
        }

        @Override
        public GroupDataModel getGroupDataModel() {
            return null;
        }

        @Override
        public int getCurrentTokens() {
            return 0;
        }

        @Override
        public void addToken(Point location) {
        }

        @Override
        public Bot setPosition(Point location) {
            return this;
        }

        @Override
        public int getActionsPerSecond() {
            return 0;
        }

        @Override
        public Bot setActionsPerSecond(int actionsPerSecond) {
            return null;
        }

        @Override
        public void resetActionsTaken() {

        }

        @Override
        public Bot setMovementProbability(double movementProbability) {
            return this;
        }

        @Override
        public Bot setHarvestProbability(double harvestProbability) {
            return this;
        }

        @Override
        public Bot setTokenProximityScalingFactor(double tokenProximityScalingFactor) {
            return this;
        }

        @Override
        public double getMovementProbability() {
            return 0;
        }

        @Override
        public double getHarvestProbability() {
            return 0;
        }

        @Override
        public double getTokenProximityScalingFactor() {
            return 0;
        }

        @Override
        public Direction getNextMove() {
            return Direction.NONE;
        }

        @Override
        public void initialize(RoundConfiguration configuration) {

        }

        @Override
        public int getTicksToWait() {
            return 0;
        }

        @Override
        public Bot setTicksToWait(int ticksToWait) {
            return this;
        }

        @Override
        public int getBotNumber() {
            return -1;
        }

        @Override
        public Bot setBotNumber(int botNumber) {
            return this;
        }

        @Override
        public Bot setGroupDataModel(GroupDataModel model) {
            return this;
        }
    };

    /**
     * Provides simple default bot state and behavior.
     * <p>
     * 1. bots have "energy" (number of actions per second),
     * 2. bots expend one unit of energy to harvest a token or move
     * 3. bots have probabilities (floating point number between 0 and 1) that must be exceeded before they will move
     * or harvest a token if they are currently on top of a token.
     * 4. randomized behavior if harvest probability fails or movement is unsuccessful (due to player blockage).
     */
    enum EmptyBoardBehavior {
        FOLLOW_PLAYER, RANDOM_WALKS, EDGES;
        private final static Random random = new Random();
        public static EmptyBoardBehavior random() {
            EmptyBoardBehavior[] values = EmptyBoardBehavior.values();
            int index = random.nextInt(values.length);
            return values[index];
        }
    }


    abstract class SimpleBot implements Bot, Serializable {

        private static final long serialVersionUID = 2437093153712520070L;
        public final static int DEFAULT_ACTIONS_PER_SECOND = 8;
        public final static double DEFAULT_MOVEMENT_PROBABILITY = 0.9d;
        public final static double DEFAULT_HARVEST_PROBABILITY = 0.9d;

        public final static int DEFAULT_MAX_TICKS_TO_WAIT = 3;

        private final Identifier identifier = new BotIdentifier();

        private Point currentPosition;
        private int currentTokens = 0;

        private Point targetLocation;

        private double harvestProbability;
        private double movementProbability;
        private double tokenProximityScalingFactor;
        private int actionsPerSecond;
        private int botNumber = 0;
        private int numberOfActionsTaken = 0;
        private GroupDataModel model;
        private Identifier participantId;
        private EmptyBoardBehavior emptyBoardBehavior;
        private int ticksToWait;
        private int edge = 0;

        private transient Random random = new Random();

        protected transient Logger logger = Logger.getLogger(getClass().getName());

        public SimpleBot() {
            this(DEFAULT_ACTIONS_PER_SECOND, DEFAULT_MOVEMENT_PROBABILITY, DEFAULT_HARVEST_PROBABILITY);
        }

        public SimpleBot(GroupDataModel groupDataModel, int botNumber) {
            setGroupDataModel(groupDataModel).setBotNumber(botNumber);
        }

        public SimpleBot(int actionsPerSecond, double movementProbability, double harvestProbability) {
            this.actionsPerSecond = actionsPerSecond;
            this.movementProbability = movementProbability;
            this.harvestProbability = harvestProbability;
        }

        /**
         * Simple algorithm for bot behavior:
         * <p>
         * 1. if energy has been expended, return
         * 2. if induced delay (ticksToWait) has been set, decrement and return
         * 3. if the bot is currently on top of a resource, consider whether to harvest it. If harvested, induce delay.
         * If not harvested, pick a new random location to visit
         * 4. if the bot is not currently on top of a resource, select the closest token and move towards it
         * 5. if movement was unsuccessful due to blockage, pick a new random location to visit.
         */
        public void act() {
            // first, check number of actions taken vs actions per second
            if (numberOfActionsTaken > actionsPerSecond) {
                logger.info(String.format("Number of actions taken %d exceeds allowable actions per second %d",
                        numberOfActionsTaken, actionsPerSecond));
                return;
            }
            // next, check if we have a wait enforced on us
            else if (ticksToWait > 0) {
                ticksToWait--;
                return;
            }
            // if neither, check if bot is sitting on top of a token and check if it should harvest it.
            Point botLocation = getPosition();
            if (model.isResourceAt(botLocation) && random.nextDouble() <= getHarvestProbability()) {
                // FIXME: more sophisticated algorithm should look at density / proximity to participant instead of naive
                // dice roll
                model.collectToken(this);
            }
            // figure out our next move and roll the dice to see if we can go.
            else {
                Direction nextMove = getNextMove();
                if (random.nextDouble() <= getMovementProbability()) {
                    // FIXME: need a more sophisticated pathfinding algorithm if we want to enable 
                    // max cell occupancy and blockage so the bot can move around a player if they are directly
                    // in their way
                    boolean successfulMove = model.move(this, nextMove);
                    // logger.info("move " + nextMove + " was successful? " + successfulMove);
                }
                setTicksToWait(1);
            }
            numberOfActionsTaken++;
        }

        public void resetActionsTaken() {
            this.numberOfActionsTaken = 0;
        }

        public Point getPosition() {
            return currentPosition;
        }

        public Bot setPosition(Point location) {
            this.currentPosition = location;
            return this;
        }

        public Direction getNextMove() {
            if (! hasTarget()) {
                if (model.isResourceDistributionEmpty()) {
                    // FIXME: might be better modeled as object dispatch also
                    switch (emptyBoardBehavior) {
                        case FOLLOW_PLAYER:
                            targetLocation = getParticipantPosition();
                            break;
                        case RANDOM_WALKS:
                            // pick a corner
                            if (random.nextBoolean()) {
                                targetLocation = getRandomCorner();
                            }
                            else {
                                targetLocation = getRandomLocation();
                            }
                            break;
                        default:
                            targetLocation = getRandomLocation();
                            break;
                    }
                    logger.info("Set bot target location on empty board to: " + targetLocation);
                }
                else if (! model.isResourceAt(targetLocation)) {
                    setNewTargetLocation();
                }
            }
            Direction nextMove = Direction.towards(getPosition(), getTargetLocation());
            if (nextMove == Direction.NONE) {
                // at target location
                reachedTargetLocation();
            }
            return nextMove;
        }

        protected Point getRandomCorner() {
            int x = 0;
            int y = 0;
            RoundConfiguration configuration = model.getRoundConfiguration();
            if (random.nextBoolean()) {
                x = configuration.getResourceWidth() - 1;
            }
            if (random.nextBoolean()) {
                y = configuration.getResourceDepth() - 1;
            }
            return new Point(x, y);
        }

        protected void setNewTargetLocation() {
            targetLocation = getTargetToken();
            if (targetLocation == null) {
                // pick a random location on the board
                targetLocation = getRandomLocation();
            }
        }

        protected void reachedTargetLocation() {
            this.targetLocation = null;
            this.ticksToWait = random.nextInt(getMaxTicksToWait());
        }

        protected Point getRandomLocation() {
            RoundConfiguration configuration = model.getRoundConfiguration();
            int x = random.nextInt(configuration.getResourceWidth());
            int y = random.nextInt(configuration.getResourceDepth());
            return new Point(x, y);
        }

        protected Point getRandomTokenLocation() {
            Set<Point> resourcePositions = model.getResourcePositions();
            int randomIndex = random.nextInt(resourcePositions.size());
            for (Point point: resourcePositions) {
                if (randomIndex-- == 0) {
                    return point;
                }
            }
            return getRandomLocation();
        }

        protected Point getTargetToken() {
            Point currentLocation = getPosition();
            Point nearestToken = null;
            double nearestTokenDistance = Double.MAX_VALUE;
            // naive implementation, scans all positions
            for (Point resourcePosition : model.getResourcePositions()) {
                double distance = currentLocation.distanceSq(resourcePosition);
                if (distance < nearestTokenDistance) {
                    nearestTokenDistance = distance;
                    nearestToken = resourcePosition;
                }
            }
            return nearestToken;
        }

        public Bot setHarvestProbability(double harvestProbability) {
            this.harvestProbability = harvestProbability;
            return this;
        }

        public Bot setMovementProbability(double movementProbability) {
            this.movementProbability = movementProbability;
            return this;
        }

        public Bot setTokenProximityScalingFactor(double tokenProximityScalingFactor) {
            this.tokenProximityScalingFactor = tokenProximityScalingFactor;
            return this;
        }

        public Bot setActionsPerSecond(int actionsPerSecond) {
            this.actionsPerSecond = actionsPerSecond;
            return this;
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

        public double getTokenProximityScalingFactor() {
            return tokenProximityScalingFactor;
        }

        public void initialize(RoundConfiguration roundConfiguration) {
            setPosition(model.getInitialPosition(getBotNumber()));
            this.emptyBoardBehavior = EmptyBoardBehavior.random();
            logger.info("setting current bot position to " + getPosition());
            currentTokens = 0;
        }

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();
            logger = Logger.getLogger(getClass().getName());
            random = new Random();
        }

        public int getBotNumber() {
            return botNumber;
        }

        public Bot setBotNumber(int botNumber) {
            this.botNumber = botNumber;
            return this;
        }

        public Identifier getId() {
            return identifier;
        }

        public Point getParticipantPosition() {
            return model.getClientPosition(participantId);
        }

        public Bot setGroupDataModel(GroupDataModel groupDataModel) {
            this.model = groupDataModel;
            this.participantId = groupDataModel.getClientIdentifiers().iterator().next();
            return this;
        }

        public GroupDataModel getGroupDataModel() {
            return model;
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

        public Bot setTicksToWait(int ticksToWait) {
            this.ticksToWait = ticksToWait;
            return this;
        }

        public int getMaxTicksToWait() {
            return DEFAULT_MAX_TICKS_TO_WAIT;
        }

        public int getCurrentTokens() {
            return currentTokens;
        }

        public void addToken(Point location) {
            this.currentTokens++;
        }

        public String toString() {
            return String.format("%s: %d", getBotType(), getBotNumber());
        }

    }
}
