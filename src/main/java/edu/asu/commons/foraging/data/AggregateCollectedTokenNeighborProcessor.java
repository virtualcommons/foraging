package edu.asu.commons.foraging.data;

import java.awt.Point;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.client.Circle;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.MovementEvent;
import edu.asu.commons.foraging.event.ResourceAddedEvent;
import edu.asu.commons.foraging.event.ResourcesAddedEvent;
import edu.asu.commons.foraging.event.TokenCollectedEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.util.Utils;

/**
 * $Id: AggregateCollectedTokenNeighborProcessor.java 526 2010-08-06 01:25:27Z alllee $
 * 
 * Generates aggregate distributions of the number of neighboring tokens for a collected token in two situations:
 * 1. without regard to visibility
 * 2. only when other subjects are visible.   
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 526 $
 */
public class AggregateCollectedTokenNeighborProcessor extends SaveFileProcessor.Base {
	
	private final static Object[] NEIGHBORING_TOKEN_HEADER = {
		"0", "1", "2", "3", "4", "5", "6", "7", "8"
	};
	
    public AggregateCollectedTokenNeighborProcessor() {
		setSecondsPerInterval(ForagingSaveFileConverter.DEFAULT_AGGREGATE_TIME_INTERVAL);
	}
    
    private boolean hasOtherSubjectsInView(Identifier id, Point location, GroupDataModel group) {
    	RoundConfiguration roundConfiguration = group.getRoundConfiguration();
    	if (roundConfiguration.isFieldOfVisionEnabled()) {
    		Circle circle = new Circle(location, roundConfiguration.getViewSubjectsRadius());
    		for (Map.Entry<Identifier, Point> entry : group.getClientPositions().entrySet()) {
    			if (entry.getKey().equals(id)) {
    				// skip if this is the same client
    				continue;
    			}
    			if (circle.contains(entry.getValue())) {
    				return true;
    			}
    		}
    		return false;
    	}
    	// field of vision isn't enabled, everyone is in everyone else's field of view.
    	return true;
    }
	
	public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        // populate the ordered identifiers, try directly from the participant tokens map that
        // is persisted in later versions of the experiment.
        ServerDataModel serverDataModel = (ServerDataModel) savedRoundData.getDataModel();
        TreeSet<Identifier> orderedIdentifiers = new TreeSet<Identifier>(serverDataModel.getClientDataMap().keySet());
        // write out header for collected tokens statistics.  
        // second token header is the distribution for token harvests when other subjects are in the field of view.
        writer.println(
        		Utils.join(',', "Time", "Client ID", 
        				Utils.join(',', NEIGHBORING_TOKEN_HEADER), 
        				Utils.join(',', NEIGHBORING_TOKEN_HEADER)));
        Map<Identifier, Integer[]> collectedTokenNeighborsWithOtherSubjectsInView = new LinkedHashMap<Identifier, Integer[]>();
        Map<Identifier, Integer[]> collectedTokenNeighbors = new LinkedHashMap<Identifier, Integer[]>();
        for (Identifier id: orderedIdentifiers) {
        	Integer[] neighbors = new Integer[9];
        	Integer[] neighborsWithOtherSubjectsInView = new Integer[9];
        	Arrays.fill(neighbors, 0);
        	Arrays.fill(neighborsWithOtherSubjectsInView, 0);
        	collectedTokenNeighbors.put(id, neighbors);
        	collectedTokenNeighborsWithOtherSubjectsInView.put(id, neighborsWithOtherSubjectsInView);
        }
        // initialize client positions
        for (ClientData clientData: serverDataModel.getClientDataMap().values()) {
            clientData.initializePosition();
        }
        for (PersistableEvent event: savedRoundData.getActions()) {
        	long elapsedTimeInSeconds = savedRoundData.getElapsedTimeInSeconds(event);
        	if (isIntervalElapsed(elapsedTimeInSeconds)) {
        	    writeAggregateStatistics(writer, collectedTokenNeighbors, collectedTokenNeighborsWithOtherSubjectsInView);
        	}
    		if (event instanceof ResourceAddedEvent) {
    			ResourceAddedEvent rae = (ResourceAddedEvent) event;
    			assert serverDataModel.getGroup(rae.getId()).equals(rae.getGroup());
    			rae.getGroup().addResource(rae.getResource());
    		}
    		else if (event instanceof ResourcesAddedEvent) {
    			ResourcesAddedEvent rae = (ResourcesAddedEvent) event;
    			assert serverDataModel.getGroup(rae.getId()).equals(rae.getGroup());
    			rae.getGroup().addResources(rae.getResources());
    		}
    		else if (event instanceof TokenCollectedEvent) {
    			TokenCollectedEvent tce = (TokenCollectedEvent) event;
    			Identifier id = tce.getId();
    			Point location = tce.getLocation();
    			GroupDataModel group = serverDataModel.getGroup(id);
    			int numberOfNeighboringTokens = group.getNumberOfNeighboringTokens(location);
    			collectedTokenNeighbors.get(id)[numberOfNeighboringTokens]++;
    			if (hasOtherSubjectsInView(id, location, group)) {
    				collectedTokenNeighborsWithOtherSubjectsInView.get(id)[numberOfNeighboringTokens]++;
    			}
    		}
    		else if (event instanceof MovementEvent) {
    			MovementEvent movementEvent = (MovementEvent) event;
    			serverDataModel.moveClient(movementEvent.getId(), movementEvent.getDirection());
    		}
        }
        // write out last interval
        writeAggregateStatistics(writer, collectedTokenNeighbors, collectedTokenNeighborsWithOtherSubjectsInView);
	}

    private void writeAggregateStatistics(PrintWriter writer,
            Map<Identifier, Integer[]> collectedTokenNeighbors,
            Map<Identifier, Integer[]> collectedTokenNeighborsWithOtherSubjectsInView) {
        // write all collected data
        for (Map.Entry<Identifier, Integer[]> entry : collectedTokenNeighbors.entrySet()) {
        	Identifier id = entry.getKey();
        	Integer[] neighboringTokens = entry.getValue();
        	Integer[] neighboringTokensWithOtherSubjectsInView = collectedTokenNeighborsWithOtherSubjectsInView.get(id);
        	writer.println(Utils.join(',', getIntervalEnd(), id, 
        			Utils.join(',', Arrays.asList(neighboringTokens)),
        			Utils.join(',', Arrays.asList(neighboringTokensWithOtherSubjectsInView))));
            // clear old neighboring tokens 
        	Arrays.fill(neighboringTokens, 0);
        	Arrays.fill(neighboringTokensWithOtherSubjectsInView, 0);
        }
    }

	@Override
	public String getOutputFileExtension() {
		return "-collected-token-neighbors.txt";
	}
	
	
}