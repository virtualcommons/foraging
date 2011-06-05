package edu.asu.commons.foraging.data;

import java.util.HashMap;
import java.util.Map;

import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;

/**
 * $Id: ClientMovementTokenCount.java 526 2010-08-06 01:25:27Z alllee $
 * 
 * Helper class for maintaining basic movement / token information.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 526 $
 */
class ClientMovementTokenCount {
    int moves = 0;
    int tokens = 0;
    public void reset() {
        moves = 0;
        tokens = 0;
    }
    
    public static Map<Identifier, ClientMovementTokenCount> createMap(ServerDataModel model) {
        Map<Identifier, ClientMovementTokenCount> clientStats = new HashMap<Identifier, ClientMovementTokenCount>();
        for (Identifier id: model.getClientDataMap().keySet()) {
            clientStats.put(id, new ClientMovementTokenCount());
        }
        return clientStats;
    }
}