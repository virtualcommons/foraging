package edu.asu.commons.foraging.data;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.QuizResponseEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.util.Utils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Provides an analysis-friendly player resource summary CSV for a given experiment round with number of tokens
 * collected this round for each participant, number of movement actions taken, and sanction costs and penalties
 */
public class QuizProcessor extends SaveFileProcessor.Base {

    @Override
    public String getOutputFileExtension() {
        return "-quiz-responses.txt";
    }

    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        RoundConfiguration roundConfiguration = (RoundConfiguration) savedRoundData.getRoundParameters();
        if (!roundConfiguration.isQuizEnabled()) {
            // short circuit if there wasn't a quiz here
            return;
        }
        ServerDataModel serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
        serverDataModel.reinitialize((RoundConfiguration) savedRoundData.getRoundParameters());
        List<GroupDataModel> groups = serverDataModel.getOrderedGroups();
        Map<Identifier, QuizResponseEvent> responses = new HashMap<>();
        String savedRoundDateTime = Utils.join('-', savedRoundData.extractDateTime());
        for (PersistableEvent event : savedRoundData.getActions()) {
            if (event instanceof QuizResponseEvent) {
                responses.put(event.getId(), (QuizResponseEvent) event);
            }
        }
        writer.println("Group ID, Assigned Number, Participant UUID, Question Number, Response, Correct Answer, Correct?");
        Map<String, String> quizAnswers = roundConfiguration.getQuizAnswers();
        for (GroupDataModel group : groups) {
            ArrayList<ClientData> clientDataList = new ArrayList<>(group.getClientDataMap().values());
            clientDataList.sort(Comparator.comparingInt(ClientData::getAssignedNumber));
            String groupId = getDateTimeGroupId(group, savedRoundDateTime);
            for (ClientData data : clientDataList) {
                QuizResponseEvent event = responses.get(data.getId());
                Properties clientQuizResponses = event.getResponses();
                for (Map.Entry<String, String> entry : quizAnswers.entrySet()) {
                    String clientResponse = clientQuizResponses.getProperty(entry.getKey());
                    String correctAnswer = entry.getValue();
                    writer.println(Utils.join(',',
                            groupId,
                            data.getId().getUUID(),
                            data.getAssignedNumber(),
                            entry.getKey(),
                            clientResponse,
                            correctAnswer,
                            correctAnswer.equals(clientResponse)
                            )
                    );
                }
            }
        }
    }

    public String getDateTimeGroupId(GroupDataModel group, String dateTimeString) {
        return String.format("group-%s_%s", group.getGroupId(), dateTimeString);
    }

}
