package edu.asu.commons.foraging.data;

import edu.asu.commons.experiment.Persister;
import edu.asu.commons.experiment.SaveFileProcessor;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Invokes various SaveFileProcessorS to convert the foraging binary or XML data files.
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 */
public class ForagingSaveFileConverter {

    private final static Logger logger = Logger.getLogger(ForagingSaveFileConverter.class.getName());

    static final int DEFAULT_AGGREGATE_TIME_INTERVAL = 5;

    private Options options = new Options();
    private CommandLineParser parser = new DefaultParser();
    private HelpFormatter formatter = new HelpFormatter();

    public ForagingSaveFileConverter() {
        options.addOption("x", "xml", false, "convert XStream XML files instead of serialized .save files");
        options.addOption("b", "bots", false, "generate single player bot statistics");
        options.addOption("h", "help", false, "Usage instructions");
    }

    public CommandLine parse(String[] args) {
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            printHelp();
            throw new RuntimeException(e);
        }
    }

    public void printHelp() {
        formatter.printHelp("ant convert -Ddata.dir=<savefile.dir> -D<options>", options);
    }

    public boolean convert(String saveDataDirectory) {
        return convert(saveDataDirectory, parse(new String[0]));
    }

    public boolean convert(String saveDataDirectory, CommandLine commandLine) {
        boolean useXml = commandLine.hasOption("xml");
        boolean hasBots = commandLine.hasOption("bots");
        File allSaveFilesDirectory = new File(saveDataDirectory);
        if (allSaveFilesDirectory.exists() && allSaveFilesDirectory.isDirectory()) {
            List<SaveFileProcessor> processors = new ArrayList<>();
            if (hasBots) {
                logger.info("Processing bot data.");
                processors.addAll(
                        Arrays.asList(
                                new SummaryProcessor(),
                                new AllDataProcessor(),
                                new BotDataProcessor(),
                                new BotSummaryDataProcessor()
                        )
                );
            } else {
                logger.info("Processing all data");
                processors.addAll(
                        Arrays.asList(
                                new AllDataProcessor(),
                                new ResourceOverTimeProcessor(),
                                new AggregateTimeIntervalProcessor(),
                                new SummaryProcessor(),
                                new AggregateTokenSpatialDistributionProcessor(),
                                new CollectedTokenSpatialDistributionProcessor(),
                                new MovementStatisticsProcessor(),
                                new BayesianAggregateProcessor(),
                                // new MovieCreatorProcessor(),
                                new ForagingRuleProcessor(),
                                new AggregateCollectedTokenNeighborProcessor()
                        )
                );
            }
            logger.info("Processors: " + processors);
            Persister.processSaveFiles(allSaveFilesDirectory, processors, useXml);
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        ForagingSaveFileConverter converter = new ForagingSaveFileConverter();
        CommandLine cmd = converter.parse(args);
        logger.info("Command line options: " + Arrays.asList(cmd.getOptions()));
        if (converter.convert(args[0], cmd)) {
            System.err.println("Successfully converted files in " + args[0]);
        } else {
            System.err.println(args[0] + " doesn't appear to be a valid save file directory.");
        }
    }

}
