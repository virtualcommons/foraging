package edu.asu.commons.foraging.server;

import edu.asu.commons.event.EventChannel;
import edu.asu.commons.experiment.Persister;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.conf.ServerConfiguration;

public class ForagingPersister extends Persister<ServerConfiguration, RoundConfiguration> {
    
    public ForagingPersister(ServerConfiguration configuration) {
        super(configuration);
    }
    
    public ForagingPersister(EventChannel channel, ServerConfiguration configuration) {
        super(channel, configuration);
    }

    // FIXME: path only works for unix.  Should detect OS instead and have a Windows-safe fail safe directory.
    @Override
    protected String getFailSafeSaveDirectory() {
        return "/tmp/foraging-failsafe";
    }
}
