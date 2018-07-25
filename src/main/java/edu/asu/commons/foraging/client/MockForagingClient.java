package edu.asu.commons.foraging.client;

import edu.asu.commons.event.Event;
import edu.asu.commons.foraging.conf.ServerConfiguration;

public class MockForagingClient extends ForagingClient {

    public MockForagingClient() {
        super(new ServerConfiguration("configuration/"));
    }

    @Override
    public void transmit(Event event) {
    }


}
