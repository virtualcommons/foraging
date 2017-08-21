package edu.asu.commons.foraging.model;

import edu.asu.commons.net.Identifier;

import java.awt.Point;

/**
 * Marker interface for Bots, ClientData.
 *
 */
public interface Actor {

    Identifier getId();
    Point getPosition();
    GroupDataModel getGroupDataModel();
    void addToken(Point location);

}
