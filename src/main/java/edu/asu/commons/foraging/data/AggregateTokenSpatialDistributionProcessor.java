package edu.asu.commons.foraging.data;

import java.awt.Dimension;
import java.awt.Point;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.event.TokenCollectedEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 526 $
 */
class AggregateTokenSpatialDistributionProcessor extends SaveFileProcessor.Base {
        public AggregateTokenSpatialDistributionProcessor() {
            setSecondsPerInterval(ForagingSaveFileConverter.DEFAULT_AGGREGATE_TIME_INTERVAL);
        }
        @Override
        public void process(SavedRoundData savedRoundData, PrintWriter writer) {
            ServerDataModel serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
            SortedSet<PersistableEvent> actions = savedRoundData.getActions();
            Map<Identifier, ClientSpatialDistribution> clientSpatialDistributionMap = new HashMap<>();
            Dimension boardSize = serverDataModel.getRoundConfiguration().getBoardSize();
            System.err.println("board size: " + boardSize);
            for (Identifier id: serverDataModel.getActorMap().keySet()) {
                clientSpatialDistributionMap.put(id, new ClientSpatialDistribution(boardSize));
            }
            for (PersistableEvent event : actions) {
            	long elapsedTime = savedRoundData.getElapsedTimeInSeconds(event);
            	if (isIntervalElapsed(elapsedTime)) {
                    writeData(writer, serverDataModel, clientSpatialDistributionMap);	
            	}
                if (event instanceof TokenCollectedEvent) {
                    TokenCollectedEvent tokenCollectedEvent = (TokenCollectedEvent) event;
                    Point point = tokenCollectedEvent.getLocation();
                    Identifier id = tokenCollectedEvent.getId();
                    ClientSpatialDistribution spatialDistribution = clientSpatialDistributionMap.get(id);
                    spatialDistribution.columnCounts[point.x]++;
                    spatialDistribution.rowCounts[point.y]++;
                    spatialDistribution.tokens++;
                }
            }
            // write last interval out
            writeData(writer, serverDataModel, clientSpatialDistributionMap);
        }

        private void writeData(
                PrintWriter writer,
                ServerDataModel serverDataModel,
                Map<Identifier, ClientSpatialDistribution> clientSpatialDistributionMap) 
        {
            List<GroupDataModel> groups = serverDataModel.getOrderedGroups();
            for (GroupDataModel group: groups) {
                String groupLabel = group.toString();
                writer.println("Time, Identifier, Group, # tokens, row stdev, column stdev");
                double groupWeightedSpatialMetric = 0.0d;
                int totalTokens = 0;
                for (Identifier id: group.getClientIdentifiers()) {
                    ClientSpatialDistribution spatialDistribution = clientSpatialDistributionMap.get(id);
                    spatialDistribution.calculateStandardDeviation();
                    groupWeightedSpatialMetric += spatialDistribution.weightedSpatialMetric;
                    writer.println(String.format("%d, %s, %s, %s, %s, %s", getIntervalEnd(), id, groupLabel, spatialDistribution.tokens, spatialDistribution.rowStandardDeviation, spatialDistribution.columnStandardDeviation));
                    totalTokens += spatialDistribution.tokens;
                }
                groupWeightedSpatialMetric /= totalTokens;
                writer.println(groupLabel + " weighted spatial metric: " + groupWeightedSpatialMetric);
            }
            // clear data after processing all groups.
            for (ClientSpatialDistribution spatialDistribution : clientSpatialDistributionMap.values()) {
            	spatialDistribution.zeroRowColumnCounts();
            }
		}

        @Override
        public String getOutputFileExtension() {
            return "-aggregated-spatial-distribution.txt";
        }
    }
