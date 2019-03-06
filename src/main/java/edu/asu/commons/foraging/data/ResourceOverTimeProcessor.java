package edu.asu.commons.foraging.data;

import java.io.PrintWriter;
import java.util.List;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.event.ClientPoseUpdate;
import edu.asu.commons.foraging.event.MovementEvent;
import edu.asu.commons.foraging.event.ResourceAddedEvent;
import edu.asu.commons.foraging.event.ResourcesAddedEvent;
import edu.asu.commons.foraging.event.TokenCollectedEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.util.Utils;

/**
 * Generates a resource over time data file in 1s intervals
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 526 $
 */
public class ResourceOverTimeProcessor extends SaveFileProcessor.Base {
    public ResourceOverTimeProcessor() {
        setIntervalDelta(1);
    }

    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        // populate ordered identifiers, first try directly from the participant tokens map that
        // is persisted in later versions of the experiment.
        ServerDataModel serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
        for (ClientData clientData : serverDataModel.getClientDataMap().values()) {
            clientData.initializePosition();
        }
        List<GroupDataModel> groups = serverDataModel.getOrderedGroups();
        writer.println("Group, Time, Resource Size");
        for (PersistableEvent event : savedRoundData.getActions()) {
            long secondsElapsed = savedRoundData.getElapsedTimeInSeconds(event);
            // see if the current persistable event is past the threshold,
            // meaning we should take a snapshot of our currently
            // accumulated stats
            if (isIntervalElapsed(secondsElapsed)) {
                // generate group expected token counts
                writeGroupData(writer, groups);
                serverDataModel.setDirty(false);
            }
            // next, process the current persistable event
            serverDataModel.apply(event);
        }
        if (serverDataModel.isDirty()) {
            writeGroupData(writer, groups);
        }
    }

    private void writeGroupData(PrintWriter writer, List<GroupDataModel> groups) {
        for (GroupDataModel group : groups) {
            writer.println(Utils.join(',', group.toString(), getIntervalEnd(), group.getResourceDistributionSize()));
        }
    }

    @Override
    public String getOutputFileExtension() {
        return "-resource-over-time.txt";
    }

}
