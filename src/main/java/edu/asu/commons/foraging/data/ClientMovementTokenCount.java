package edu.asu.commons.foraging.data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;

/**
 * 
 * Helper class for maintaining basic movement / token information for Bots or Clients.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 */
public class ClientMovementTokenCount {
    public int moves = 0;
    public int tokens = 0;
    public void reset() {
        moves = 0;
        tokens = 0;
    }
    public static Map<Identifier, ClientMovementTokenCount> createMap(ServerDataModel model) {
        return Stream.concat(model.getClientDataMap().keySet().stream(), model.getBotMap().keySet().stream()).collect(
                Collectors.toMap(Function.identity(), (id) -> new ClientMovementTokenCount())
        );
    }
}