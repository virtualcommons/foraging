package edu.asu.commons.foraging.data;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.util.Utils;

import java.io.PrintWriter;
import java.util.SortedSet;

public class BotDataProcessor extends SaveFileProcessor.Base {

    public BotDataProcessor() {
        setSecondsPerInterval(1);
    }

    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
       	RoundConfiguration roundConfiguration = (RoundConfiguration) savedRoundData.getRoundParameters();
        SortedSet<PersistableEvent> actions = savedRoundData.getActions();
        ServerDataModel dataModel = (ServerDataModel) savedRoundData.getDataModel();
        // generate summarized statistics
        // Time (100-200ms resolution), Subject Number, X, Y, Number of tokens collected, Distance to bot, current velocity
        // see https://github.com/virtualcommons/foraging/issues/19
        writer.println(
                Utils.join(',', "Time", "Subject ID", "X", "Y", "Tokens collected", "Distance to bot", "Velocity")
        );
        for (PersistableEvent event: savedRoundData.getActions()) {
            long secondsElapsed = savedRoundData.getElapsedTimeInSeconds(event);
            if (isIntervalElapsed(secondsElapsed)) {
                // write out aggregated stats
            }
        }
    }

    @Override
    public String getOutputFileExtension() {
        return "-bot-data.txt";
    }
}
