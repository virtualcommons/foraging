package edu.asu.commons.foraging.bot;

import edu.asu.commons.net.Identifier;

/**
 * Marker Identifier for all Bots.
 */
public class BotIdentifier extends Identifier.Base<BotIdentifier> {
    private static final long serialVersionUID = 1609142256924017761L;

    public String toString() {
        return "BotIdentifier " + super.toString();
    }
}
