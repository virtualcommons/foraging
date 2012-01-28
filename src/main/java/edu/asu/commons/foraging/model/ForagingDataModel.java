package edu.asu.commons.foraging.model;

import java.awt.Point;

import edu.asu.commons.event.EventChannel;
import edu.asu.commons.experiment.DataModel;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.conf.ServerConfiguration;



/**
 * $Id$ The
 * 
 * Base class for the foraging client side and server side data models.
 * 
 * @author Allen Lee
 * @version $Revision$
 */

public abstract class ForagingDataModel implements DataModel<ServerConfiguration, RoundConfiguration> {

    private final static long serialVersionUID = 7171398197039872068L;
    
    protected transient EventChannel channel;
    protected transient boolean sanctioningEnabled;
    protected transient boolean experiment2d;
    protected transient int boardWidth;
    protected transient int boardHeight;
    
    private RoundConfiguration roundConfiguration;

    public ForagingDataModel(EventChannel channel) {
        this.channel = channel;
    }
    
    public EventChannel getEventChannel() {
        return channel;
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public int getBoardHeight() {
        return boardHeight;
    }
    
    public ServerConfiguration getExperimentConfiguration() {
    	if (roundConfiguration != null) {
        	return roundConfiguration.getParentConfiguration();    		
    	}
    	return null;
    }

    public RoundConfiguration getRoundConfiguration() {
        return roundConfiguration;
    }

    // FIXME: considers game world to be flat rectilinear, may need to adapt to
    // torus (wraparound on vertical edge only) for other experiments.
    public boolean isValidPosition(Point p) {
        return isValidPosition(p.x, p.y);
    }
    
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < boardWidth && y >= 0 && y < boardHeight;
    }

    public void setRoundConfiguration(RoundConfiguration configuration) {
        this.roundConfiguration = configuration;
        sanctioningEnabled = configuration.isSanctioningEnabled();
        experiment2d = configuration.is2dExperiment();
        boardHeight = configuration.getResourceDepth();
        boardWidth = configuration.getResourceWidth();
        
    }
    
    public boolean isSanctioningEnabled() {
        return sanctioningEnabled;
    }
    
    public boolean is2dExperiment() {
        return experiment2d;
    }
}
