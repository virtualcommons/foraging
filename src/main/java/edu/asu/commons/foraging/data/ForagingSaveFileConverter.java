package edu.asu.commons.foraging.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.asu.commons.experiment.Persister;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;
import org.apache.commons.cli.*;

/**
 * Invokes various SaveFileProcessorS to convert the foraging binary or XML data files.
 *
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 */
public class ForagingSaveFileConverter {
    
    static final int DEFAULT_AGGREGATE_TIME_INTERVAL = 5;

    public static boolean convert(String saveDataDirectory, CommandLine commandLine) {
        boolean useXml = commandLine.hasOption("xml");
        boolean hasBots = commandLine.hasOption("bots");
        File allSaveFilesDirectory = new File(saveDataDirectory);
        if (allSaveFilesDirectory.exists() && allSaveFilesDirectory.isDirectory()) {
            List<SaveFileProcessor> processors = new ArrayList<>();
            processors.addAll(Arrays.asList(
                    new AllDataProcessor(),
                    new ResourceOverTimeProcessor(),
                    new AggregateTimeIntervalProcessor(), 
                    new SummaryProcessor(),
                    new AggregateTokenSpatialDistributionProcessor(),
                    new CollectedTokenSpatialDistributionProcessor(),  
                    new MovementStatisticsProcessor(),
//                    new MovieCreatorProcessor(),
                    new ForagingRuleProcessor(),
                    new AggregateCollectedTokenNeighborProcessor()
            ));
            if (hasBots) {
                processors.add(new BotDataProcessor());
            }
            Persister.processSaveFiles(allSaveFilesDirectory, processors, useXml);
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("x", "xml", false, "convert XStream XML files instead of serialized .save files");
        options.addOption("b", "bots", false, "generate single player bot statistics");
        options.addOption("h", "help", false, "Usage instructions");
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (convert(args[0], cmd)) {
                System.err.println("Successfully converted files in " + args[0]);
            }
            else {
                System.err.println(args[0] + " doesn't appear to be a valid save file directory.");
            }
        } catch (ParseException e) {
            e.printStackTrace();
            formatter.printHelp("ant convert -Dsavefile.dir=<savefile.dir> -D<options>", options);
        }
    }



}
