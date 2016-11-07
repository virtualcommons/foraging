package edu.asu.commons.foraging.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.asu.commons.experiment.Persister;
import edu.asu.commons.experiment.SaveFileProcessor;

/**
 * <p>
 * Save file processors used to convert binary data files from the foraging experiment.
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 526 $
 */
public class ForagingSaveFileConverter {
    
    static final int DEFAULT_AGGREGATE_TIME_INTERVAL = 5;

    public static boolean convert(String saveDataDirectory, boolean useXml) {
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
            Persister.processSaveFiles(allSaveFilesDirectory, processors, useXml);
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java " + ForagingSaveFileConverter.class + " <save-data-directory> <xml>");
            System.exit(0);
        }
        boolean useXml = false;
        if (args.length == 2) {
            useXml = "xml".equals(args[1]);
        }
        if (convert(args[0], useXml)) {
        	System.err.println("Successfully converted files in " + args[0]);
        }
        else {
        	System.err.println(args[0] + " doesn't appear to be a valid save file directory.");
        }
    }



}
