package edu.asu.commons.foraging.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.asu.commons.experiment.Persister;
import edu.asu.commons.experiment.SaveFileProcessor;

/**
 * $Id$
 * <p>
 * Save file processors used to convert binary data files from the foraging experiment.
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 526 $
 */
public class ForagingSaveFileConverter {
    
    static final int DEFAULT_AGGREGATE_TIME_INTERVAL = 5;

    public static boolean convert(String saveDataDirectory) {
        File allSaveFilesDirectory = new File(saveDataDirectory);
        if (allSaveFilesDirectory.exists() && allSaveFilesDirectory.isDirectory()) {
            List<SaveFileProcessor> processors = new ArrayList<SaveFileProcessor>();
            processors.addAll(Arrays.asList(
                    new AllDataProcessor(), 
                    new AggregateTimeIntervalProcessor(), 
                    new SummaryProcessor(),
                    new AggregateTokenSpatialDistributionProcessor(),
                    new CollectedTokenSpatialDistributionProcessor(),  
                    new MovementStatisticsProcessor(),
                    new MovieCreatorProcessor(),
                    new AggregateCollectedTokenNeighborProcessor()
            ));
            Persister.processSaveFiles(allSaveFilesDirectory, processors);
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java " + ForagingSaveFileConverter.class + " <save-data-directory>");
            System.exit(0);
        }
        if (convert(args[0])) {
        	System.err.println("Successfully converted files in " + args[0]);
        }
        else {
        	System.err.println(args[0] + " doesn't appear to be a valid save file directory.");
        }
    }



}
