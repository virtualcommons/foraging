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
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.util.Utils;

/**
 * $Id: SummaryProcessor.java 526 2010-08-06 01:25:27Z alllee $
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 526 $
 */
class SummaryProcessor extends SaveFileProcessor.Base {
    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        ServerDataModel serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
        List<GroupDataModel> groups = new ArrayList<GroupDataModel>(serverDataModel.getGroups());
        for (GroupDataModel group: groups) {
            int totalConsumedGroupTokens = 0;
            ArrayList<String> clientTokens = new ArrayList<String>();
            ArrayList<ClientData> clientDataList = new ArrayList<ClientData>(group.getClientDataMap().values());
            Collections.sort(clientDataList, new Comparator<ClientData>() {
            	@Override
            	public int compare(ClientData a, ClientData b) {
            		return Integer.valueOf(a.getAssignedNumber()).compareTo(b.getAssignedNumber());
            	}
            });
            for (ClientData data : clientDataList) {
                clientTokens.add(String.format("%s, %s, %s", data.getId(), data.getAssignedNumber(), data.getTotalTokens()));
                totalConsumedGroupTokens += data.getTotalTokens();
            }
            writer.println(
                    String.format("%s, %s, %s, %s",
                            group,
                            Utils.join(',', clientTokens),
                            group.getResourceDistributionSize(),
                            totalConsumedGroupTokens,
                            Utils.join(',', group.getResourceDistribution().keySet())
                    ));
        }
        Map<GroupDataModel, SortedSet<ChatRequest>> chatRequestMap = new HashMap<GroupDataModel, SortedSet<ChatRequest>>();
        SortedSet<ChatRequest> allChatRequests = savedRoundData.getChatRequests();
        if (! allChatRequests.isEmpty()) {
            ChatRequest first = allChatRequests.first();
            for (ChatRequest request: savedRoundData.getChatRequests()) {
                GroupDataModel group = serverDataModel.getGroup(request.getSource());
                if (chatRequestMap.containsKey(group)) {
                    chatRequestMap.get(group).add(request);
                }
                else {
                    TreeSet<ChatRequest> chatRequests = new TreeSet<ChatRequest>();
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

    }
    @Override
    public String getOutputFileExtension() {
        return "-summary.txt";
    }
}