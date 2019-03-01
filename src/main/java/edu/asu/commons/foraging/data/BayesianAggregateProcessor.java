package edu.asu.commons.foraging.data;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;

import java.io.PrintWriter;
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
    public BayesianAggregateProcessor() {
        setIntervalDelta(ForagingSaveFileConverter.DEFAULT_AGGREGATE_TIME_INTERVAL);
    }

    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        ServerDataModel serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
        SortedSet<PersistableEvent> actions = savedRoundData.getActions();
        Map<Identifier, ClientStatistics> statistics = new HashMap<>();
        for (Identifier id: serverDataModel.getActorMap().keySet()) {
            statistics.put(id, new ClientStatistics(id));
        }

        for (PersistableEvent event : actions) {
            long elapsedTime = savedRoundData.getElapsedTimeInSeconds(event);
            if (isIntervalElapsed(elapsedTime)) {
                // writeData(writer, serverDataModel, clientStatistics);
            }
            else {
                serverDataModel.apply(event);

            }

        }
    }

    private void writeData(PrintWriter writer, SavedRoundData data, ServerDataModel serverDataModel, ClientStatistics statistics) {

    }

    @Override
    public String getOutputFileExtension() {
        return "-raw-aggr-bayesian-analysis.txt";
    }

    private static class ClientStatistics {
        final Identifier id;
        int numberOfMoves;
        int tokensCollected;
        int skippedTokens;
        int maxStraightMoves;

        public ClientStatistics(Identifier id) {
            this.id = id;
        }

    }
}
