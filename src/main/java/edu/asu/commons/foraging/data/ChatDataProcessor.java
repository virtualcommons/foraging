package edu.asu.commons.foraging.data;

import edu.asu.commons.event.ChatRequest;
import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.util.Utils;
import org.apache.commons.text.StringEscapeUtils;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;

public class ChatDataProcessor extends SaveFileProcessor.Base {
    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter printWriter) {
        RoundConfiguration roundConfiguration = (RoundConfiguration) savedRoundData.getRoundParameters();
        SortedSet<PersistableEvent> actions = savedRoundData.getActions();
        ServerDataModel dataModel = (ServerDataModel) savedRoundData.getDataModel();
        dataModel.reinitialize(roundConfiguration);
        String header = "Timestamp, Group ID, Participant UUID, Player Number, Message";
        printWriter.println(header);
        Path saveFilePath = Paths.get(savedRoundData.getSaveFilePath());
        Path experimentRunPath = saveFilePath.getParent();
        for (GroupDataModel group: dataModel.getGroups()) {
            group.generateNameUUID(experimentRunPath.toString());
        }
        String dateTimeString = extractDateTime(savedRoundData.getSaveFilePath());
        for (PersistableEvent event: actions) {
            // log ChatRequests since there are ChatEvents generated for each message broadcast to a participant
            // e.g., 4 ChatEvents for a broadcast message from player A to players B, C, D, and E
            if (event instanceof ChatRequest) {
                ChatRequest request = (ChatRequest) event;
                Identifier sourceId = request.getSource();
                GroupDataModel group = dataModel.getGroup(sourceId);
                String groupId = getDateTimeGroupId(group, dateTimeString);
                printWriter.println(
                        Utils.join(',',
                                savedRoundData.toSecondString(event),
                                groupId,
                                sourceId.getUUID(),
                                sourceId.getChatHandle(),
                                StringEscapeUtils.escapeCsv(request.getMessage()
                                )
                        )
                );
            }
        }
    }

    @Override
    public String getOutputFileExtension() {
        return "-chat.csv";
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
