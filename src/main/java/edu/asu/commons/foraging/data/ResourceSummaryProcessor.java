package edu.asu.commons.foraging.data;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.MovementEvent;
import edu.asu.commons.foraging.event.SanctionAppliedEvent;
import edu.asu.commons.foraging.event.TokenCollectedEvent;
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
import java.util.Map;

/**
 * Provides an analysis-friendly player resource summary CSV for a given experiment round with number of tokens
 * collected this round for each participant, number of movement actions taken, and sanction costs and penalties
 */
public class ResourceSummaryProcessor extends SaveFileProcessor.Base {

    @Override
    public String getOutputFileExtension() {
        return "-player-resource-summary.txt";
    }

    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        ServerDataModel serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
        Map<Identifier, ClientMovementTokenCount> clientMovementTokenCounts = ClientMovementTokenCount.createMap(serverDataModel);
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
            else if (event instanceof MovementEvent) {
                ClientMovementTokenCount client = clientMovementTokenCounts.get(event.getId());
                client.moves++;
            }
            else if (event instanceof TokenCollectedEvent) {
                ClientMovementTokenCount client = clientMovementTokenCounts.get(event.getId());
                client.tokens++;
            }
        }

        writer.println("Group ID, Participant UUID, Assigned Number, Current Tokens, Moves, Sanction costs, Sanction penalties");
        String dateTimeString = extractDateTime(savedRoundData.getSaveFilePath());
        for (GroupDataModel group: groups) {
            ArrayList<ClientData> clientDataList = new ArrayList<>(group.getClientDataMap().values());
            clientDataList.sort(Comparator.comparingInt(ClientData::getAssignedNumber));
            String groupId = getDateTimeGroupId(group, dateTimeString);
            for (ClientData data : clientDataList) {
                ClientMovementTokenCount cmt = clientMovementTokenCounts.get(data.getId());
                writer.println(Utils.join(',',
                        groupId,
                        data.getId().getUUID(),
                        data.getAssignedNumber(),
                        cmt.tokens,
                        cmt.moves,
                        data.getSanctionCosts(),
                        data.getSanctionPenalties()
                        )
                );
            }
        }
    }

    public String getDateTimeGroupId(GroupDataModel group, String dateTimeString) {
        return String.format("group-%s_%s", group.getGroupId(), dateTimeString);
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
        // always assumes a path with date time information encoded in the parent
        // directories, above the actual binary save file e.g., ../01-24-2019/17.21.54/round-X.save
        return String.format("%s-%s", path.getName(numberOfElements-3), path.getName(numberOfElements-2));
    }

}
