package edu.asu.commons.foraging.data;

import java.awt.Point;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.ClientPoseUpdate;
import edu.asu.commons.foraging.event.HarvestFruitRequest;
import edu.asu.commons.foraging.event.HarvestResourceRequest;
import edu.asu.commons.foraging.event.MovementEvent;
import edu.asu.commons.foraging.event.ResourceAddedEvent;
import edu.asu.commons.foraging.event.ResourcesAddedEvent;
import edu.asu.commons.foraging.event.TokenCollectedEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ResourceDispenser;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.foraging.model.StochasticGenerator;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.util.Utils;

/**
 * Generates average token probabilities that a token should appear at a given location over defined time intervals.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 526 $
 */
public class AggregateTimeIntervalProcessor extends SaveFileProcessor.Base {
    public AggregateTimeIntervalProcessor() {
        setIntervalDelta(ForagingSaveFileConverter.DEFAULT_AGGREGATE_TIME_INTERVAL);
    }
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        // populate the ordered identifiers, try directly from the participant tokens map that
        // is persisted in later versions of the experiment.
        ServerDataModel serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
        Map<Identifier, ClientMovementTokenCount> clientStatistics = ClientMovementTokenCount.createMap(serverDataModel);
        for (ClientData clientData: serverDataModel.getClientDataMap().values()) {
            clientData.initializePosition();
        }
        RoundConfiguration roundConfiguration = (RoundConfiguration) savedRoundData.getRoundParameters();
        TreeSet<Identifier> orderedIdentifiers = new TreeSet<>(serverDataModel.getClientDataMap().keySet());
        List<GroupDataModel> groups = serverDataModel.getOrderedGroups();
        
        List<String> movementHeader = new ArrayList<>();
        List<String> collectedTokensHeader = new ArrayList<>();
        for (Identifier id: orderedIdentifiers) {
            movementHeader.add(id + " moves");
            collectedTokensHeader.add(id + " tokens collected");
        }

        // headers for average probability of a token for each group
        List<String> tokenProbabilityGroupNumberHeader = new ArrayList<>();
        // headers for tokens left in each group
        List<String> tokensLeftGroupNumberHeader = new ArrayList<>();
        List<String> distanceHeader = new ArrayList<>();

        for (GroupDataModel group: groups) {
            String groupNumber = group.toString(); 
            tokenProbabilityGroupNumberHeader.add(groupNumber + " avg token P");
            tokensLeftGroupNumberHeader.add(groupNumber + " tokens left");
            
            List<Identifier> ids = new ArrayList<>(group.getOrderedClientIdentifiers());
            for (int i = 0; i < ids.size();  i++) {
                Identifier id = ids.get(i);
                for (int j = i+1; j < ids.size(); j++) {
                    Identifier secondId = ids.get(j);
                    distanceHeader.add(String.format("%s (%s -> %s)", groupNumber, id, secondId));
                }
            }
        }

        // write out the header
        String header = Utils.join(',', "Period",
                // moves taken
                Utils.join(',', movementHeader),
                // tokens 
                Utils.join(',', collectedTokensHeader),
                // group token probabilities
                Utils.join(',', tokenProbabilityGroupNumberHeader),
                // group total tokens left
                Utils.join(',', tokensLeftGroupNumberHeader),
                // distance between participants
                Utils.join(',', distanceHeader)
        );
        writer.println(header);

        for (PersistableEvent event: savedRoundData.getActions()) {
            long secondsElapsed = savedRoundData.getElapsedTimeInSeconds(event);
            // see if the current persistable event is past the threshold,
            // meaning we should take a snapshot of our currently
            // accumulated stats
            if (isIntervalElapsed(secondsElapsed)) {
                // generate group expected token counts
                writeAggregateStatistics(writer, serverDataModel,
                        clientStatistics, orderedIdentifiers, groups);
            }
            // next, process the current persistable event 
            ClientMovementTokenCount stats = clientStatistics.get(event.getId());
            if (event instanceof MovementEvent) {
                MovementEvent movementEvent = (MovementEvent) event;
                serverDataModel.moveClient(movementEvent.getId(), movementEvent.getDirection());
                stats.moves++;
            }
            else if (event instanceof ClientPoseUpdate) {
                ClientPoseUpdate clientPoseUpdate = (ClientPoseUpdate) event;
                serverDataModel.getClientDataMap().get(event.getId()).setPosition(clientPoseUpdate.getPosition());
                stats.moves++;
            }
            else if (event instanceof TokenCollectedEvent) {
                stats.tokens++;
                TokenCollectedEvent tokenCollectedEvent = (TokenCollectedEvent) event;
                GroupDataModel group = serverDataModel.getGroup(tokenCollectedEvent.getId());
                assert serverDataModel.getGroups().contains(group);
                group.removeResource(tokenCollectedEvent.getLocation());
            }
            else if (event instanceof HarvestFruitRequest) {
//                HarvestFruitRequest request = (HarvestFruitRequest) event;
                stats.tokens += roundConfiguration.getTokensPerFruits();
            }
            else if (event instanceof HarvestResourceRequest) {
                HarvestResourceRequest request = (HarvestResourceRequest) event;
                stats.tokens += roundConfiguration.ageToTokens(request.getResource().getAge());
            }
            else if (event instanceof ResourceAddedEvent) {
                ResourceAddedEvent resourceAddedEvent = (ResourceAddedEvent) event;
                assert serverDataModel.getGroups().contains(resourceAddedEvent.getGroup());
                resourceAddedEvent.getGroup().addResource(resourceAddedEvent.getPosition());
            }
            else if (event instanceof ResourcesAddedEvent) {
                ResourcesAddedEvent resourcesAddedEvent = (ResourcesAddedEvent) event;
                assert serverDataModel.getGroups().contains(resourcesAddedEvent.getGroup());
                resourcesAddedEvent.getGroup().addResources(resourcesAddedEvent.getResources());
            }
        }
        writeAggregateStatistics(writer, serverDataModel,
                clientStatistics, orderedIdentifiers, groups);
    }

    private void writeAggregateStatistics(PrintWriter writer,
            ServerDataModel serverDataModel,
            Map<Identifier, ClientMovementTokenCount> clientStatistics,
            TreeSet<Identifier> orderedIdentifiers,
            List<GroupDataModel> groups) {
        List<Double> expectedTokenProbabilities = getExpectedTokenProbabilities(serverDataModel);
        // report summary stats and reset
        List<Integer> movesTaken = new ArrayList<>();
        List<Integer> harvestedTokens = new ArrayList<>();
        List<Integer> tokensLeft = getTokensLeft(groups);
        List<Double> distances = getClientDistances(groups);
        for (Identifier id : orderedIdentifiers) {
            ClientMovementTokenCount stats = clientStatistics.get(id);
            movesTaken.add(stats.moves);
            harvestedTokens.add(stats.tokens);
            stats.reset();
        }
        String dataline = 
            Utils.join(',', getIntervalEnd(),
                    Utils.join(',', movesTaken),
                    Utils.join(',', harvestedTokens),
                    Utils.join(',', expectedTokenProbabilities),
                    Utils.join(',', tokensLeft),
                    Utils.join(',', distances)
            );
        writer.println(dataline);
    }

    private List<Double> getClientDistances(List<GroupDataModel> groups) {
        List<Double> distances = new ArrayList<>();
        for (GroupDataModel group: groups) {
            List<Identifier> ids = new ArrayList<>(group.getOrderedClientIdentifiers());
            for (int i = 0; i < ids.size();  i++) {
                Identifier id = ids.get(i);
                for (int j = i+1; j < ids.size(); j++) {
                    Identifier secondId = ids.get(j);
                    distances.add(group.getClientPosition(id).distance(group.getClientPosition(secondId)));
                }
            }
        }
        return distances;
    }

    private List<Double> getExpectedTokenProbabilities(ServerDataModel serverDataModel) {
        List<Double> expectedTokens = new ArrayList<Double>();
        List<GroupDataModel> groups = serverDataModel.getOrderedGroups();
        ResourceDispenser dispenser = new ResourceDispenser(serverDataModel);
        StochasticGenerator generator = dispenser.getDensityDependentGenerator();
        for (GroupDataModel group: groups) {
            double tokenProbabilitySum = 0;
            for (int x = 0; x < serverDataModel.getBoardWidth(); x++) {
                for (int y = 0; y < serverDataModel.getBoardHeight(); y++) {
                    if (! group.getResourcePositions().contains(new Point(x, y))) {
                        tokenProbabilitySum += generator.getProbabilityForCell(group, x, y);    
                    }
                }
            }
            expectedTokens.add(tokenProbabilitySum);
        }
        return expectedTokens;
    }

    private List<Integer> getTokensLeft(Collection<GroupDataModel> groups) {
        List<Integer> tokensLeft = new ArrayList<>();
        for (GroupDataModel group: groups) {
            tokensLeft.add(group.getResourceDistributionSize());
        }
        return tokensLeft;
    }

    @Override
    public String getOutputFileExtension() {
        return "-aggregated-time-interval-data.txt";
    }


}