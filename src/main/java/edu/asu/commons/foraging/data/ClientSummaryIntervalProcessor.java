package edu.asu.commons.foraging.data;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.conf.RoundConfiguration;
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
class ClientSummaryIntervalProcessor extends SaveFileProcessor.Base {

    private ServerDataModel serverDataModel;

    public ClientSummaryIntervalProcessor() {
        setIntervalDelta(ForagingSaveFileConverter.DEFAULT_AGGREGATE_TIME_INTERVAL);
    }

    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        RoundConfiguration roundConfiguration = (RoundConfiguration) savedRoundData.getRoundParameters();
        serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
        serverDataModel.reinitialize(roundConfiguration);
        SortedSet<PersistableEvent> actions = savedRoundData.getActions();
        Map<Identifier, ClientStatistics> statistics = new HashMap<>();
        for (Identifier id : serverDataModel.getActorMap().keySet()) {
            statistics.put(id, new ClientStatistics(id));
        }
        writer.println("Seconds, Player ID, Number of moves, Tokens collected, Skipped tokens, Straight moves, Quadrant, Quadrant Tokens Collected");
        for (PersistableEvent event : actions) {
            long elapsedTime = savedRoundData.getElapsedTimeInSeconds(event);
            if (isIntervalElapsed(elapsedTime)) {
                writeData(writer, savedRoundData, statistics);
            } else {
                Identifier id = event.getId();
                ClientStatistics clientStats = statistics.get(id);
                if (event instanceof MovementEvent) {
                    GroupDataModel group = serverDataModel.getGroup(id);
                    ClientData client = group.getClientData(id);
                    clientStats.handleMovementEvent((MovementEvent) event, group, client);
                }
                else if (event instanceof TokenCollectedEvent) {
                    TokenCollectedEvent tce = (TokenCollectedEvent) event;
                    clientStats.handleTokenCollectedEvent(tce, Quadrant.forPoint(tce.getLocation(), roundConfiguration.getBoardSize()));
                }
                serverDataModel.apply(event);
            }
        }
    }

    private void writeData(PrintWriter writer, SavedRoundData data, Map<Identifier, ClientStatistics> statistics) {
        // write out data for each client, then clear all their stats
        for (ClientStatistics clientStats: statistics.values()) {
            for (Quadrant quadrant: Quadrant.values()) {
                String line = Utils.join(',', getIntervalEnd(), clientStats.toCsvString(quadrant)) ;
                writer.println(line);
            }
            // clear interval stats
            clientStats.clear();
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
        public int getArrayIndex() {
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
                    throw new RuntimeException("Invalid value: " + value);
            }
        }
               /**
         * Returns a integer representing the quadrant (1 = top right, 2 = top left, 3 = bottom left, 4 = bottom right)
         * @param location
         * @return
         */
        public static Quadrant forPoint(Point location, Dimension dimension) {
            int boardHeight = dimension.height;
            int boardWidth = dimension.width;
            int midpointX = boardWidth / 2;
            int midpointY = boardHeight / 2;
            if (location.x <= midpointX && location.y <= midpointY) {
                return Quadrant.TOP_LEFT;
            }
            else if (location.x <= midpointX && location.y > midpointY) {
                return Quadrant.BOTTOM_LEFT;
            }
            else if (location.x > midpointX && location.y <= midpointY) {
                return Quadrant.TOP_RIGHT;
            }
            else if (location.x > midpointX && location.y > midpointY) {
                return Quadrant.BOTTOM_RIGHT;
            }
            else {
                throw new RuntimeException("location not assignable to any quadrant: " + location);
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
            clear();
        }

        public void clear() {
            this.numberOfMoves = 0;
            this.tokensCollected = 0;
            this.skippedTokens = 0;
            this.maxStraightMoves = 0;
            this.previousDirection = Direction.NONE;
            this.numberOfTimesSanctioned = 0;
            this.numberOfTimesSanctioning = 0;
            Arrays.fill(quadrantTokensCollected, 0);
        }


        public int getQuadrantTokensCollected(Quadrant quadrant) {
            return quadrantTokensCollected[quadrant.getArrayIndex()];
        }

        public void handleTokenCollectedEvent(TokenCollectedEvent event, Quadrant quadrant) {
            this.tokensCollected++;
            quadrantTokensCollected[quadrant.getArrayIndex()]++;
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

        public String toCsvString(Quadrant quadrant) {
            return Utils.join(',', id.getUUID(), numberOfMoves, tokensCollected, skippedTokens,
                    maxStraightMoves, quadrant.name(), getQuadrantTokensCollected(quadrant));
        }

    }
}
