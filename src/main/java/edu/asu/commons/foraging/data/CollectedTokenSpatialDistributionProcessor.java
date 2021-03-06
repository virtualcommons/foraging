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
 * Generates group level spatial distribution statistics
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 */
class CollectedTokenSpatialDistributionProcessor extends SaveFileProcessor.Base {
        @Override
        public void process(SavedRoundData savedRoundData, PrintWriter writer) {
            ServerDataModel serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
            SortedSet<PersistableEvent> actions = savedRoundData.getActions();
            Map<Identifier, ClientSpatialDistribution> clientSpatialDistributionMap = new HashMap<>();
            Dimension boardSize = serverDataModel.getRoundConfiguration().getBoardSize();
            for (Identifier id : serverDataModel.getActorMap().keySet()) {
                clientSpatialDistributionMap.put(id, new ClientSpatialDistribution(boardSize));
            }
            for (PersistableEvent event : actions) {
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
            // calculate for group
            writeData(writer, serverDataModel, clientSpatialDistributionMap);
        }

		private void writeData(
				PrintWriter writer,
				ServerDataModel serverDataModel,
				Map<Identifier, ClientSpatialDistribution> clientSpatialDistributionMap) {
			List<GroupDataModel> groups = serverDataModel.getOrderedGroups();
            for (GroupDataModel group: groups) {
                String groupLabel = group.toString();
                writer.println("Identifier, Group, # tokens, row stdev, column stdev");
                double groupWeightedSpatialMetric = 0.0d;
                int totalTokens = 0;
                for (Identifier id: group.getClientIdentifiers()) {
                    ClientSpatialDistribution spatialDistribution = clientSpatialDistributionMap.get(id);
                    spatialDistribution.calculateStandardDeviation();
                    groupWeightedSpatialMetric += spatialDistribution.weightedSpatialMetric;
                    writer.println(String.format("%s, %s, %s, %s, %s", id, groupLabel, spatialDistribution.tokens, spatialDistribution.rowStandardDeviation, spatialDistribution.columnStandardDeviation));
                    totalTokens += spatialDistribution.tokens;
                }
                groupWeightedSpatialMetric /= totalTokens;
                writer.println(groupLabel + " weighted spatial metric: " + groupWeightedSpatialMetric);
            }
		}

        @Override
        public String getOutputFileExtension() {
            return "-spatial-distribution.txt";
        }
    }
