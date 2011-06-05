package edu.asu.commons.foraging.model;

import java.awt.Point;

import edu.asu.commons.event.EventChannel;
import edu.asu.commons.experiment.DataModel;
import edu.asu.commons.foraging.conf.RoundConfiguration;



/**
 * $Id: ForagingDataModel.java 4 2008-07-25 22:51:44Z alllee $ The
 * 
 * Convenience abstract class for storing csan data.
 * 
 * @author Allen Lee
 * @version $Revision: 4 $
 */

public abstract class ForagingDataModel implements DataModel<RoundConfiguration> {

    private final static long serialVersionUID = 7171398197039872068L;
    
    protected transient EventChannel channel;
    protected transient boolean sanctioningEnabled;
    protected transient boolean experiment2d;
    
    private RoundConfiguration roundConfiguration;

    public ForagingDataModel(EventChannel channel) {
        this.channel = channel;
    }
    
    public EventChannel getEventChannel() {
        return channel;
    }

    public int getBoardWidth() {
        return roundConfiguration.getResourceWidth();
    }

    public int getBoardHeight() {
        return roundConfiguration.getResourceDepth();
    }

    public RoundConfiguration getRoundConfiguration() {
        return roundConfiguration;
    }

    // FIXME: considers game world to be flat rectilinear, will need to adapt to
    // torus
    // (wraparound on vertical edge only) for other experiments.
    public boolean isValidPosition(Point p) {
        return p.x >= 0 && p.x < getBoardWidth() && p.y >= 0 && p.y < getBoardHeight();
    }

    public void setRoundConfiguration(RoundConfiguration configuration) {
        this.roundConfiguration = configuration;
        sanctioningEnabled = configuration.isSanctioningEnabled();
        experiment2d = configuration.is2dExperiment();
    }
    
    public boolean isSanctioningEnabled() {
        return sanctioningEnabled;
    }
    
    public boolean is2dExperiment() {
        return experiment2d;
    }
}
