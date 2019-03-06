package edu.asu.commons.foraging.data;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.util.Utils;

import java.io.PrintWriter;
import java.util.List;
import java.util.SortedSet;

public class GroupResourceSummaryIntervalProcessor extends SaveFileProcessor.Base {

    public GroupResourceSummaryIntervalProcessor() {
        setIntervalDelta(ForagingSaveFileConverter.DEFAULT_AGGREGATE_TIME_INTERVAL);
    }

    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {

        RoundConfiguration roundConfiguration = (RoundConfiguration) savedRoundData.getRoundParameters();
        ServerDataModel serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
        serverDataModel.reinitialize(roundConfiguration);
        SortedSet<PersistableEvent> actions = savedRoundData.getActions();
        writer.println("Seconds, Group ID, Resources");
        List<GroupDataModel> groups = serverDataModel.getOrderedGroups();
        for (PersistableEvent event : actions) {
            long elapsedTime = savedRoundData.getElapsedTimeInSeconds(event);
            if (isIntervalElapsed(elapsedTime)) {
                writeGroupData(writer, groups);
                serverDataModel.setDirty(false);
            }
            serverDataModel.apply(event);
        }
        if (serverDataModel.isDirty()) {
            writeGroupData(writer, groups);
        }
    }

    private void writeGroupData(PrintWriter writer, List<GroupDataModel> groups) {
        for (GroupDataModel group : groups) {
            writer.println(Utils.join(',', getIntervalEnd(), group.toString(), group.getResourceDistributionSize()));
        }
    }

    @Override
    public String getOutputFileExtension() {
        return "-group-resource-summary.txt";
    }
}
