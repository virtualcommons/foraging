package edu.asu.commons.foraging.model;

import edu.asu.commons.net.Identifier;

import java.awt.Point;

/**
 * Marker interface for Bots, ClientData.
 *
 */
public interface Actor {

    public Identifier getId();
    public Point getPosition();
    public GroupDataModel getGroupDataModel();

}
