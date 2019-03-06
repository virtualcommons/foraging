package edu.asu.commons.foraging.data;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
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
import edu.asu.commons.foraging.event.RuleSelectedUpdateEvent;
import edu.asu.commons.foraging.event.RuleVoteRequest;
import edu.asu.commons.foraging.event.SanctionAppliedEvent;
import edu.asu.commons.foraging.event.TokenCollectedEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;

/**
 * Provides summary information for a given given round.
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 */
class SummaryProcessor extends SaveFileProcessor.Base {
    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        ServerDataModel serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
        serverDataModel.reinitialize((RoundConfiguration) savedRoundData.getRoundParameters());
        List<GroupDataModel> groups = serverDataModel.getOrderedGroups();
        for (PersistableEvent event: savedRoundData.getActions()) {
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
        }

        writer.println("Participant UUID, Assigned Number, Group, Total Cumulative Tokens, Sanction costs, Sanction penalties");
        for (GroupDataModel group: groups) {
            int totalTokensHarvested = 0;
            ArrayList<ClientData> clientDataList = new ArrayList<>(group.getClientDataMap().values());
            Collections.sort(clientDataList, (a, b) ->
                    Integer.valueOf(a.getAssignedNumber()).compareTo(b.getAssignedNumber())
            );
            String groupId = "Group " + group.getGroupId();
            for (ClientData data : clientDataList) {
                writer.println(String.format("%s, %s, %s, %s, %s, %s",
                        data.getId().getUUID(),
                        data.getAssignedNumber(),
                        groupId,
                        data.getTotalTokens(), data.getSanctionCosts(), data.getSanctionPenalties()));
                totalTokensHarvested += data.getTotalTokens();
            }
            writer.println(String.format("%s, %s, %s", groupId, group.getResourceDistributionSize(), totalTokensHarvested));
        }
        Map<GroupDataModel, SortedSet<ChatRequest>> chatRequestMap = new HashMap<>();
        SortedSet<ChatRequest> allChatRequests = savedRoundData.getChatRequests();
        if (! allChatRequests.isEmpty()) {
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
                        writer.println(String.format("%s: %s (%s)", request.getSource(), request.toString(), (request.getCreationTime() - first.getCreationTime())/1000L));
                    }
                }
            }
        }
        writer.println("=========================================");
        writer.println("Time, Participant UUID, Token Collected?, Chat");
        Map<Identifier, RuleVoteRequest> ruleVoteRequests = new HashMap<>();
        ArrayList<RuleSelectedUpdateEvent> ruleSelectedEvents = new ArrayList<>();
        for (PersistableEvent action: savedRoundData.getActions()) {
            if (action instanceof ChatRequest) {
                writer.println(String.format("%s, %s, %s, %s", 
                        savedRoundData.toSecondString(action), action.getId().getUUID(), 0, action.toString()));
            }
            else if (action instanceof TokenCollectedEvent) {
                writer.println(String.format("%s, %s, %s", 
                        savedRoundData.toSecondString(action), action.getId().getUUID(), "token collected"));
            }
            else if (action instanceof RuleVoteRequest) {
                ruleVoteRequests.put(action.getId(), (RuleVoteRequest) action);
            }
            else if (action instanceof RuleSelectedUpdateEvent) {
                ruleSelectedEvents.add((RuleSelectedUpdateEvent) action);
            }
        }
        if (! ruleVoteRequests.isEmpty()) {
            writer.println("=== Selected rules ===");
            for (RuleSelectedUpdateEvent event: ruleSelectedEvents) {
                writer.println(event.toString());
            }
            for (GroupDataModel group: groups) {
                ArrayList<ClientData> clientDataList = new ArrayList<>(group.getClientDataMap().values());
                Collections.sort(clientDataList, (a, b) -> {
                    return Integer.valueOf(a.getAssignedNumber()).compareTo(b.getAssignedNumber());
                });
                
                writer.println("=== Voting results for " + group.toString() + "===");
                for (ClientData data: clientDataList) {
                    RuleVoteRequest request = ruleVoteRequests.get(data.getId());
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
