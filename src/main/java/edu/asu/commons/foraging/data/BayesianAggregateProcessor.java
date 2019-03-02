package edu.asu.commons.foraging.data;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.event.MovementEvent;
import edu.asu.commons.foraging.event.TokenCollectedEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.Direction;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.util.Utils;

import java.awt.Dimension;
import java.awt.Point;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

/**
 * Generate data for Bayesian analysis using 5 second intervals
 * <p>
 * Number of moves
 * Number of tokens collected
 * Number of times player moved off of a cell with a token and didn't harvest it
 * Movement distribution
 * Quadrant (1,2,3,4)
 * QuadrantTokens
 *
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 */
class BayesianAggregateProcessor extends SaveFileProcessor.Base {

    private ServerDataModel serverDataModel;

    public BayesianAggregateProcessor() {
        setIntervalDelta(ForagingSaveFileConverter.DEFAULT_AGGREGATE_TIME_INTERVAL);
    }

    public int getBoardHeight() {
        return serverDataModel.getBoardHeight();
    }

    public int getBoardWidth() {
        return serverDataModel.getBoardWidth();
    }

    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        this.serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
        SortedSet<PersistableEvent> actions = savedRoundData.getActions();
        Map<Identifier, ClientStatistics> statistics = new HashMap<>();
        for (Identifier id : serverDataModel.getActorMap().keySet()) {
            statistics.put(id, new ClientStatistics(id));
        }

        for (PersistableEvent event : actions) {
            long elapsedTime = savedRoundData.getElapsedTimeInSeconds(event);
            if (isIntervalElapsed(elapsedTime)) {
                writeData(writer, savedRoundData, statistics);
            } else {
                Identifier id = event.getId();
                ClientStatistics clientStats = statistics.get(id);
                GroupDataModel group = serverDataModel.getGroup(id);
                ClientData client = group.getClientData(id);
                if (event instanceof MovementEvent) {
                    clientStats.handleMovementEvent((MovementEvent) event, group, client);
                } else if (event instanceof TokenCollectedEvent) {
                    clientStats.handleTokenCollectedEvent((TokenCollectedEvent) event, group, client)
                }
                serverDataModel.apply(event);
            }

        }
    }

    private void writeData(PrintWriter writer, SavedRoundData data, Map<Identifier, ClientStatistics> statistics) {
        // write out data for each client, then clear all their stats
        writer.println("Seconds, Player ID, Number of moves, Tokens collected, Skipped tokens, Straight moves, Quadrant, Quadrant Tokens Collected");
        for (ClientStatistics clientStats: statistics.values()) {
            for (Quadrant quadrant: Quadrant.values()) {
                String line = Utils.join(',', getIntervalEnd(), clientStats.id.getUUID(),
                        clientStats.numberOfMoves, clientStats.tokensCollected, clientStats.skippedTokens,
                        quadrant.getValue(), clientStats.getQuadrantTokensCollected(quadrant));
                writer.println(line);
            }
            // clear all stats
            clientStats.init();
        }

    }

    @Override
    public String getOutputFileExtension() {
        return "-raw-aggr-bayesian-analysis.txt";
    }

    enum Quadrant {
        TOP_RIGHT(1), TOP_LEFT(2), BOTTOM_LEFT(3), BOTTOM_RIGHT(4);

        private final int value;

        Quadrant(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        /**
         * Returns an array index
         * @return
         */
        public int getOrdinal() {
            return value - 1;
        }

        public static Quadrant fromValue(int value) {
            switch (value) {
                case 1:
                    return TOP_RIGHT;
                case 2:
                    return TOP_LEFT;
                case 3:
                    return BOTTOM_LEFT;
                case 4:
                    return BOTTOM_RIGHT;
                default:
                    throw new RuntimeException("Invalid ordinal: " + value);
            }
        }
    }

    private class ClientStatistics {
        private final Identifier id;
        private int numberOfMoves;
        private int tokensCollected;
        private int[] quadrantTokensCollected = new int[4];
        private int skippedTokens;
        private int maxStraightMoves;
        private int numberOfTimesSanctioned;
        private int numberOfTimesSanctioning;
        private Direction previousDirection = Direction.NONE;

        public ClientStatistics(Identifier id) {
            this.id = id;
            init();
        }

        public void init() {
            this.numberOfMoves = 0;
            this.tokensCollected = 0;
            this.skippedTokens = 0;
            this.maxStraightMoves = 0;
            this.previousDirection = Direction.NONE;
            this.numberOfTimesSanctioned = 0;
            this.numberOfTimesSanctioning = 0;
            Arrays.fill(quadrantTokensCollected, 0);
        }

        /**
         * Returns a integer representing the quadrant (1 = top right, 2 = top left, 3 = bottom left, 4 = bottom right)
         * @param location
         * @return
         */
        public Quadrant getQuadrant(Point location) {
            int boardHeight = BayesianAggregateProcessor.this.getBoardHeight();
            int boardWidth = BayesianAggregateProcessor.this.getBoardWidth();
            int midpointX = boardWidth / 2;
            int midpointY = boardHeight / 2;
            if (location.x < midpointX && location.y < midpointY) {
                return Quadrant.TOP_LEFT;
            }
            else if (location.x < midpointX && location.y >= midpointY) {
                return Quadrant.BOTTOM_LEFT;
            }
            else if (location.x >= midpointX && location.y < midpointY) {
                return Quadrant.TOP_RIGHT;
            }
            else if (location.x >= midpointX && location.y >= midpointY) {
                return Quadrant.BOTTOM_RIGHT;
            }
            else {
                throw new RuntimeException("location not assignable to any quadrant: " + location);
            }
        }

        public int getQuadrantTokensCollected(Quadrant quadrant) {
            return quadrantTokensCollected[quadrant.getOrdinal()];
        }

        public void handleTokenCollectedEvent(TokenCollectedEvent event, GroupDataModel group, ClientData client) {
            this.tokensCollected++;
            int quadrant = getQuadrant(event.getLocation()).getOrdinal();
            quadrantTokensCollected[quadrant]++;
        }

        public void handleMovementEvent(MovementEvent event, GroupDataModel group, ClientData client) {
            this.numberOfMoves++;
            if (group.isResourceAt(client.getPosition())) {
                this.skippedTokens++;
            }
            if (previousDirection.equals(event.getDirection())) {
                this.maxStraightMoves++;
            }
            previousDirection = event.getDirection();
        }

    }
}
