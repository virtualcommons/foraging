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

import java.io.PrintWriter;
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
        for (PersistableEvent event: actions) {
            // log ChatRequests since there are ChatEvents generated for each message broadcast to a participant
            // e.g., 4 ChatEvents for a broadcast message from player A to players B, C, D, and E
            if (event instanceof ChatRequest) {
                ChatRequest request = (ChatRequest) event;
                Identifier sourceId = request.getSource();
                GroupDataModel group = dataModel.getGroup(sourceId);
                printWriter.println(
                        Utils.join(',',
                                savedRoundData.toSecondString(event),
                                group.toString(),
                                sourceId.getUUID(),
                                sourceId.getChatHandle(),
                                request.getMessage())
                );
            }
        }
    }

    @Override
    public String getOutputFileExtension() {
        return "-chat.csv";
    }
}
