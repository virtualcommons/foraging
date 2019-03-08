package edu.asu.commons.foraging.data;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.MovementEvent;
import edu.asu.commons.foraging.event.ResourcesAddedEvent;
import edu.asu.commons.foraging.event.TokenCollectedEvent;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.util.Utils;

/**
 * Provides movement distribution summary statistics for each participant:
 *
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 526 $
 */
class MovementStatisticsProcessor extends SaveFileProcessor.Base {
    private Map<Identifier, ClientMovementStatistics> clientStatisticsMap = new LinkedHashMap<>();
    private Map<GroupDataModel, Integer> resourceCountMap = new HashMap<>();

    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        ServerDataModel serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
        RoundConfiguration roundConfiguration = (RoundConfiguration) savedRoundData.getRoundParameters();
        for (GroupDataModel group: serverDataModel.getGroups()) {
            for (Identifier id: group.getOrderedClientIdentifiers()) {
                clientStatisticsMap.put(id, new ClientMovementStatistics(id, roundConfiguration));
            }
            for (Identifier id: group.getBotMap().keySet()) {
                clientStatisticsMap.put(id, new ClientMovementStatistics(id, roundConfiguration));
            }
            resourceCountMap.put(group, 0);
        }

        for (PersistableEvent event: savedRoundData.getActions()) {
            if (event instanceof MovementEvent) {
                MovementEvent movementEvent = (MovementEvent) event;
                Identifier id = movementEvent.getId();
                GroupDataModel groupDataModel = serverDataModel.getGroup(id);
                // only count movements when the resource count is > 0
                if (resourceCountMap.getOrDefault(groupDataModel, 0) > 0) {
                    clientStatisticsMap.get(id).move(movementEvent.getDirection());
                }
                else {
                    // stop counting for this group.
                }
            }
            else if (event instanceof ResourcesAddedEvent) {
                ResourcesAddedEvent resourcesAddedEvent = (ResourcesAddedEvent) event;
                GroupDataModel group = resourcesAddedEvent.getGroup();
                int resources = resourceCountMap.getOrDefault(group, 0);
                resources += resourcesAddedEvent.getResources().size();
                resourceCountMap.put(group, resources);
            }
            else if (event instanceof TokenCollectedEvent) {
                TokenCollectedEvent tokenCollectedEvent = (TokenCollectedEvent) event;
                Identifier id = tokenCollectedEvent.getId();
                GroupDataModel groupDataModel = serverDataModel.getGroup(id);
                int resources = resourceCountMap.getOrDefault(groupDataModel, 0);
                resourceCountMap.put(groupDataModel, resources - 1);
            }
        }
        // tally their very last movement counts 
        // (since ClientMovementStatistics only adds to the movement distribution when they change direction)
        for (ClientMovementStatistics summary: clientStatisticsMap.values()) {
            summary.updateMovementDistribution();
        }
        int maximumMoves = Math.max(serverDataModel.getBoardHeight(), serverDataModel.getBoardWidth());
        List<String> movementHeader = IntStream.range(1, maximumMoves).boxed().map(i -> i + " move(s)").collect(Collectors.toList());
        // write out the header line.
        writer.println(Utils.join(',', "Identifier", Utils.join(',', movementHeader)));

        // and then write out each Identifier's movement distribution.
        for (Map.Entry<Identifier, ClientMovementStatistics> entry : clientStatisticsMap.entrySet()) {
            writer.println(Utils.join(',', entry.getKey().getUUID(),
                    Utils.join(',',
                            Arrays.asList(entry.getValue().getMovementDistribution()))));
        }
    }

    @Override
    public String getOutputFileExtension() {
        return "-movement-summary-statistics.txt";
    }

    @Override
    public void dispose() {
        clientStatisticsMap.clear();
        resourceCountMap.clear();
    }

}
