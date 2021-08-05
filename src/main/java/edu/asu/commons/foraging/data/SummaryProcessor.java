package edu.asu.commons.foraging.data;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.asu.commons.event.ChatRequest;
import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.MovementEvent;
import edu.asu.commons.foraging.event.StrategySelectedUpdateEvent;
import edu.asu.commons.foraging.event.StrategyVoteRequest;
import edu.asu.commons.foraging.event.SanctionAppliedEvent;
import edu.asu.commons.foraging.event.TokenCollectedEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;

/**
 * Extracts summary information for a given round for display purposes, should not be used for analysis.
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 */
class SummaryProcessor extends SaveFileProcessor.Base {
    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        ServerDataModel serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
        serverDataModel.reinitialize((RoundConfiguration) savedRoundData.getRoundParameters());
        List<GroupDataModel> groups = serverDataModel.getOrderedGroups();
        Map<Identifier, ClientMovementTokenCount> clientMovementTokenCounts = ClientMovementTokenCount.createMap(serverDataModel);
        for (PersistableEvent event: savedRoundData.getActions()) {
            // no need to apply() events to the ServerDataModel and replay them if we only want summary statistics
            // from the ServerDataModel and GroupDataModel as they existed at the end of the round and
            // are serialized
            if (event instanceof SanctionAppliedEvent) {
                SanctionAppliedEvent sanctionEvent = (SanctionAppliedEvent) event;
                Identifier id = sanctionEvent.getId();
                System.err.println("applying sanction costs and penalties to " + sanctionEvent.getId() + " -> " + sanctionEvent.getTarget());
                ClientData source = serverDataModel.getClientData(id);
                source.addSanctionCosts(sanctionEvent.getSanctionCost());
                System.err.println("costs on client data are now " + source.getSanctionCosts());
                ClientData target = serverDataModel.getClientData(sanctionEvent.getTarget());
                target.addSanctionPenalties(sanctionEvent.getSanctionPenalty());
                System.err.println("penalties on client data are now " + target.getSanctionPenalties());
            }
            else if (event instanceof MovementEvent) {
                ClientMovementTokenCount client = clientMovementTokenCounts.get(event.getId());
                client.moves++;
            }
            else if (event instanceof TokenCollectedEvent) {
                ClientMovementTokenCount client = clientMovementTokenCounts.get(event.getId());
                client.tokens++;
            }
        }

        writer.println("Group ID, Participant UUID, Assigned Number, Tokens Collected, Moves, Sanction costs, Sanction penalties");
        for (GroupDataModel group: groups) {
            int totalTokensHarvested = 0;
            ArrayList<ClientData> clientDataList = new ArrayList<>(group.getClientDataMap().values());
            clientDataList.sort(Comparator.comparingInt(ClientData::getAssignedNumber));
            String groupId = group.toString();
            for (ClientData data : clientDataList) {
                ClientMovementTokenCount cmt = clientMovementTokenCounts.get(data.getId());
                writer.println(String.format("%s, %s, %s, %s, %s, %s, %s",
                        groupId,
                        data.getId().getUUID(),
                        cmt.tokens,
                        cmt.moves,
                        data.getAssignedNumber(),
                        data.getSanctionCosts(),
                        data.getSanctionPenalties()));
                totalTokensHarvested += cmt.tokens;
            }
            writer.println(String.format("%s, resources left: %s, total resources harvested: %s", groupId, group.getResourceDistributionSize(), totalTokensHarvested));
        }
        Map<GroupDataModel, SortedSet<ChatRequest>> chatRequestMap = new HashMap<>();
        SortedSet<ChatRequest> allChatRequests = savedRoundData.getChatRequests();
        if (! allChatRequests.isEmpty()) {
            writer.println("=== GROUP CHATS ===");
            ChatRequest first = allChatRequests.first();
            for (ChatRequest request: savedRoundData.getChatRequests()) {
                GroupDataModel group = serverDataModel.getGroup(request.getSource());
                if (chatRequestMap.containsKey(group)) {
                    chatRequestMap.get(group).add(request);
                }
                else {
                    TreeSet<ChatRequest> chatRequests = new TreeSet<>();
                    chatRequests.add(request);
                    chatRequestMap.put(group, chatRequests);
                }
            }
            for (GroupDataModel group: groups) {
                SortedSet<ChatRequest> chatRequests = chatRequestMap.get(group);
                if (chatRequests != null) {
                    writer.println(group.toString());
                    for (ChatRequest request: chatRequests) {
                        writer.println(String.format("%s: %s (%s s)", request.getSource(), request.toString(), (request.getCreationTime() - first.getCreationTime())/1000L));
                    }
                }
            }
        }
        writer.println("=========================================");
        writer.println("Time, Participant UUID, Token Collected?, Chat");
        Map<Identifier, StrategyVoteRequest> ruleVoteRequests = new HashMap<>();
        ArrayList<StrategySelectedUpdateEvent> ruleSelectedEvents = new ArrayList<>();
        for (PersistableEvent action: savedRoundData.getActions()) {
            if (action instanceof ChatRequest) {
                writer.println(String.format("%s, %s, %s, %s", 
                        savedRoundData.toSecondString(action), action.getId().getUUID(), 0, action.toString()));
            }
            else if (action instanceof TokenCollectedEvent) {
                writer.println(String.format("%s, %s, %s", 
                        savedRoundData.toSecondString(action), action.getId().getUUID(), "token collected"));
            }
            else if (action instanceof StrategyVoteRequest) {
                ruleVoteRequests.put(action.getId(), (StrategyVoteRequest) action);
            }
            else if (action instanceof StrategySelectedUpdateEvent) {
                ruleSelectedEvents.add((StrategySelectedUpdateEvent) action);
            }
        }
        if (! ruleVoteRequests.isEmpty()) {
            writer.println("=== Selected rules ===");
            for (StrategySelectedUpdateEvent event: ruleSelectedEvents) {
                writer.println(event.toString());
            }
            for (GroupDataModel group: groups) {
                ArrayList<ClientData> clientDataList = new ArrayList<>(group.getClientDataMap().values());
                clientDataList.sort(Comparator.comparingInt(ClientData::getAssignedNumber));
                
                writer.println("=== Voting results for " + group.toString() + "===");
                for (ClientData data: clientDataList) {
                    StrategyVoteRequest request = ruleVoteRequests.get(data.getId());
                    writer.println(String.format("%s, %s", data.getId(), request.getRule()));
                }
                
            }
        }
    }
    
    @Override
    public String getOutputFileExtension() {
        return "-summary.txt";
    }
}
