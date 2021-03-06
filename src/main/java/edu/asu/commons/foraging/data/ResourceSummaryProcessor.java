package edu.asu.commons.foraging.data;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.SanctionAppliedEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.util.Utils;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * FIXME: Duplicates a fair amount of SummaryProcessor
 *
 *
 */
public class ResourceSummaryProcessor extends SaveFileProcessor.Base {

    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        ServerDataModel serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
        serverDataModel.reinitialize((RoundConfiguration) savedRoundData.getRoundParameters());
        List<GroupDataModel> groups = serverDataModel.getOrderedGroups();
        for (PersistableEvent event: savedRoundData.getActions()) {
            if (event instanceof SanctionAppliedEvent) {
                SanctionAppliedEvent sanctionEvent = (SanctionAppliedEvent) event;
                Identifier id = sanctionEvent.getId();
                ClientData source = serverDataModel.getClientData(id);
                source.addSanctionCosts(sanctionEvent.getSanctionCost());
                ClientData target = serverDataModel.getClientData(sanctionEvent.getTarget());
                target.addSanctionPenalties(sanctionEvent.getSanctionPenalty());
            }
        }

        writer.println("Group ID, Group UUID, Participant UUID, Assigned Number, Current Tokens, Total Cumulative Tokens, Sanction costs, Sanction penalties, Group Resources Left");
        String dateTimeString = extractDateTime(savedRoundData.getSaveFilePath());
        for (GroupDataModel group: groups) {
            ArrayList<ClientData> clientDataList = new ArrayList<>(group.getClientDataMap().values());
            clientDataList.sort(Comparator.comparingInt(ClientData::getAssignedNumber));
            String groupId = String.format("group-%s_%s", group.getGroupId(), dateTimeString);
            int resourcesLeft = group.getResourceDistributionSize();
            for (ClientData data : clientDataList) {
                writer.println(Utils.join(',',
                        groupId,
                        group.getUUID(),
                        data.getId().getUUID(),
                        data.getAssignedNumber(),
                        data.getCurrentTokens(),
                        data.getTotalTokens(),
                        data.getSanctionCosts(),
                        data.getSanctionPenalties(),
                        resourcesLeft
                        )
                );
            }
        }
    }

    /**
     * Given a save file path like `/code/experiment-data/t1/01-24-2019/17.21.54/round-9.save`
     * return "01-24-2019-17.21.54"
     *
     * FIXME: push into sesef SavedRoundData
     * @param saveFilePath
     * @return
     */
    public String extractDateTime(String saveFilePath) {
        Path path = Paths.get(saveFilePath);
        int numberOfElements = path.getNameCount();
        return String.format("%s-%s", path.getName(numberOfElements-3), path.getName(numberOfElements-2));
    }

    @Override
    public String getOutputFileExtension() {
        return "-player-resource-summary.txt";
    }
}
