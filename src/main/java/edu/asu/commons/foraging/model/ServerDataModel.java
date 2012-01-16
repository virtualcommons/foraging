package edu.asu.commons.foraging.model;


import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import edu.asu.commons.event.Event;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.event.EventTypeChannel;
import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.foraging.event.AddClientEvent;
import edu.asu.commons.foraging.event.ExplicitCollectionModeRequest;
import edu.asu.commons.foraging.event.HarvestFruitRequest;
import edu.asu.commons.foraging.event.HarvestResourceRequest;
import edu.asu.commons.foraging.event.LockResourceRequest;
import edu.asu.commons.foraging.event.MovementEvent;
import edu.asu.commons.foraging.event.RealTimeSanctionRequest;
import edu.asu.commons.foraging.event.ResetTokenDistributionRequest;
import edu.asu.commons.foraging.event.ResourceAddedEvent;
import edu.asu.commons.foraging.event.ResourcesAddedEvent;
import edu.asu.commons.foraging.event.TokenCollectedEvent;
import edu.asu.commons.foraging.event.TokenMovedEvent;
import edu.asu.commons.foraging.event.TokensMovedEvent;
import edu.asu.commons.foraging.event.UnlockResourceRequest;
import edu.asu.commons.foraging.graphics.FractalTerrain;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * The ForagerServerGameState is the data model needed on the server side.
 * 
 * 
 * @author Allen Lee, Deepali Bhagvat
 * @version $Revision$
 */
public class ServerDataModel extends ForagingDataModel {

    private static final long serialVersionUID = 8166812955398387600L;

    private transient Logger logger = Logger.getLogger( getClass().getName() );
    
    private final static NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance();

    private transient Random random = new Random();

    private transient FractalTerrain terrain;
    
    private transient boolean dirty = false;
    
    private final static String[] CHAT_HANDLES = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S" };


	// Maps client Identifiers to the GroupDataModel that the client belongs to 
    private final Map<Identifier, GroupDataModel> clientsToGroups = new HashMap<Identifier, GroupDataModel>();

    public ServerDataModel() {
        super(EventTypeChannel.getInstance());
    }

    public ServerDataModel(EventChannel channel) {
        super(channel);
    }
    

    public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

    /**
     * Invoked when we try to reconstruct a server game state given a time-ordered Set of
     * PersistableEvents that was previously saved.  
     */
    public void apply(PersistableEvent event) {
        // now we write the actual game action...
        // iterate through all stored Persistable Actions, executing them onto
        // the ForagerServerGameState.
        if (event instanceof AddClientEvent) {
            AddClientEvent addClientEvent = (AddClientEvent) event;
            ClientData clientData = addClientEvent.getClientData();
            GroupDataModel group = addClientEvent.getGroup();
            group.setServerDataModel(this);
            addClientToGroup(clientData, group);
            // XXX: this must occur after we add the client to the group because addClientToGroup() sets
            // the position according to the spacing algorithm.
            clientData.setPosition(addClientEvent.getPosition());
        }
        else if (event instanceof ResourcesAddedEvent) {
            ResourcesAddedEvent resourcesAddedEvent = (ResourcesAddedEvent) event;
            addResources(resourcesAddedEvent.getGroup(), resourcesAddedEvent.getResources());
            setDirty(true);
        }
        else if (event instanceof MovementEvent) {
            MovementEvent movementEvent = (MovementEvent) event;
            moveClient(movementEvent.getId(), movementEvent.getDirection());
            setDirty(true);
        }
        else if (event instanceof ResourceAddedEvent) {
            ResourceAddedEvent resourceAddedEvent = (ResourceAddedEvent) event;
            addResource(resourceAddedEvent.getGroup(), resourceAddedEvent.getResource());
            setDirty(true);
        } 
        else if (event instanceof RealTimeSanctionRequest) {
            // currently unhandled.
        	setDirty(true);
        }
        else if (event instanceof ResetTokenDistributionRequest) {
            getGroup(event.getId()).resetResourceDistribution();
            setDirty(true);
        }
        else if (event instanceof TokenCollectedEvent) {
            TokenCollectedEvent tokenCollectedEvent = (TokenCollectedEvent) event;
            getGroup(event.getId()).removeResource(tokenCollectedEvent.getLocation());
            setDirty(true);
        }
        else if (event instanceof ExplicitCollectionModeRequest) {
            ExplicitCollectionModeRequest request = (ExplicitCollectionModeRequest) event;
            getClientData(request.getId()).setExplicitCollectionMode(request.isExplicitCollectionMode());
        }
        else {
            logger.warning("unapplied event:" + event);
        }
    }

    public synchronized void removeClient(Identifier id) {
        GroupDataModel groupDataModel = clientsToGroups.remove(id);
        groupDataModel.removeClient(id);
    }

    public synchronized void addClient(ClientData clientData) {
        // iterate through all existing groups and try to add to them.
        for (GroupDataModel group : getGroups()) {
            if (group.isFull()) {
                continue;
            }
            addClientToGroup(clientData, group);
            return;
        }
        // all the groups are full, create a new one and add them
        GroupDataModel group = new GroupDataModel(this);
        addClientToGroup(clientData, group);
    }

    public synchronized void addClientToGroup(ClientData clientData, GroupDataModel group) {
        group.addClient(clientData);
        clientsToGroups.put(clientData.getId(), group);
        clientData.getId().setChatHandle(CHAT_HANDLES[group.size() - 1]);
        channel.handle(new AddClientEvent(clientData, group, clientData.getPosition()));
    }

    public void addResource(GroupDataModel group, Resource resource) {
        group.addResource(resource);
        channel.handle(new ResourceAddedEvent(group, resource));
    }

    public void moveResources(GroupDataModel group, Collection<Point> removedPoints, Collection<Point> addedPoints) {
        // first remove all resources
        group.moveResources(removedPoints, addedPoints);
        channel.handle(new TokensMovedEvent(removedPoints, addedPoints));
    }

    public void addResources(GroupDataModel group, Set<Resource> resources) {
        group.addResources(resources);
        channel.handle(new ResourcesAddedEvent(group, resources));
    }

    public void moveResource(GroupDataModel group, Point oldLocation, Point newLocation) {
        group.addResource(newLocation);
        group.removeResource(oldLocation);
        channel.handle(new TokenMovedEvent(oldLocation, newLocation));
    }

    public void moveResources(GroupDataModel group, List<Point> oldLocations, List<Point> newLocations) {

    }

    public void cleanupRound() {
        for (GroupDataModel group: clientsToGroups.values()) {
            group.cleanupRound();
        }
    }

    public Set<Point> getResourcePositions(Identifier id) {
        return clientsToGroups.get(id).getResourcePositions();
    }

    public Point createRandomPoint() {
        int x = random.nextInt(getBoardWidth());
        int y = random.nextInt(getBoardHeight());
        return new Point(x, y);
    }

    public FractalTerrain getTerrain() {
        return terrain;
    }

    public void setTerrain(FractalTerrain terrain) {
        this.terrain = terrain;
    }

    public void postProcessCleanup() {
        for (GroupDataModel group : clientsToGroups.values()) {
            group.clearDiffLists();
            if (isSanctioningEnabled()) {
                group.resetSanctionCounts();
            }
        }
    }

    public void clear() {
        // XXX: we no longer remove the Groups from the ServerGameState since we want persistent groups.
        // This should be configurable?
        for (Iterator<GroupDataModel> iter = clientsToGroups.values().iterator(); iter.hasNext(); ) {
            GroupDataModel group = iter.next();
            group.clear();
            iter.remove();
        }
    }

    public Map<Identifier, ClientData> getClientDataMap() {
        Map<Identifier, ClientData> clientDataMap = new HashMap<Identifier, ClientData>();
        for (Map.Entry<Identifier, GroupDataModel> entry : clientsToGroups.entrySet()) {
            Identifier id = entry.getKey();
            GroupDataModel group = entry.getValue();
            clientDataMap.put(id, group.getClientData(id));
        }
        return clientDataMap;
    }

    public void moveClient(Identifier id, Direction d) {
        getGroup(id).moveClient(id, d);
        channel.handle(new MovementEvent(id, d));
    }

    public Point getClientPosition(Identifier id) {
        GroupDataModel group = clientsToGroups.get(id);
        return group.getClientPosition(id);
    }

    public int getNumberOfClients() {
        return clientsToGroups.keySet().size();
    }

    public Set<GroupDataModel> getGroups() {
        Set<GroupDataModel> groups = new LinkedHashSet<GroupDataModel>();
        groups.addAll(clientsToGroups.values());
        return groups;
    }
    
    public List<GroupDataModel> getOrderedGroups() {
        ArrayList<GroupDataModel> groups = new ArrayList<GroupDataModel>(clientsToGroups.values());
        Collections.sort(groups);
        return groups;
    }

    protected ClientData getClientData(Identifier id) {
        return getGroup(id).getClientData(id);
    }

    public GroupDataModel getGroup(Identifier id) {
        GroupDataModel group = clientsToGroups.get(id);
        if (group == null) {
            throw new IllegalArgumentException("No group available for id:" + id);
        }
        return group;
    }

    /**
     * Returns a Map<Identifier, Point> representing the latest client
     * positions.
     */
    public Map<Identifier, Point> getClientPositions(Identifier clientId) {
        GroupDataModel group = clientsToGroups.get(clientId);
        if (group == null) {
            throw new IllegalArgumentException("No group assigned to client id: " + clientId);
        }
        return group.getClientPositions();
    }

    public int getTokensConsumedBy(Identifier id) {
        return clientsToGroups.get(id).getCurrentTokens(id);
    }



    public boolean lockResource(LockResourceRequest event) {
        // lock resource
        System.err.println("Modifying lock status for resource: " + event.getResource() + " from station: " + event.getId());
        return clientsToGroups.get(event.getId()).lockResource(event);
    }

    public void unlockResource(UnlockResourceRequest event) {
        clientsToGroups.get(event.getId()).unlockResource(event);
    }

    public void harvestResource(HarvestResourceRequest event) {
        // harvest resource
        Identifier id = event.getId();
        GroupDataModel group = clientsToGroups.get(id);
        group.harvestResource(id, event.getResource());
    }

    public void harvestFruits(HarvestFruitRequest event) {
        Identifier id = event.getId();
        GroupDataModel group = clientsToGroups.get(id);
        group.harvestFruits(id, event.getResource());
    }

    public void clearDiffLists() {
        for (GroupDataModel group : getGroups()) {
            group.clearDiffLists();
        }
    }

    public Queue<RealTimeSanctionRequest> getLatestSanctions(Identifier id) {
        return clientsToGroups.get(id).getClientData(id).getLatestSanctions();
    }

    public void resetSanctionCount(Identifier id) {
        clientsToGroups.get(id).getClientData(id).resetLatestSanctions();
    }

    public Map<Identifier, ClientData> getClientDataMap(Identifier clientId) {
        GroupDataModel group = clientsToGroups.get(clientId);
        return group.getClientDataMap();
    }

    public void setGroups(Collection<GroupDataModel> groups) {
        for (GroupDataModel group: groups) {
            group.setServerDataModel(this);

            for (Identifier id: group.getClientIdentifiers()) {
                clientsToGroups.put(id, group);
            }
        }
    }

    /**
     * Resets this server data model by performing the following:
     * 
     * 1. Sets event channel to a no-op event channel.
     */
    public void setNullEventChannel() {
        super.channel = new EventTypeChannel() {
            public void handle(Event event) { }
        };
    }

    public void resetGroupResourceDistributions() {
        for (GroupDataModel group: getGroups()) {
            group.resetResourceDistribution();
        }
    }

    public void reinitialize() {
        setNullEventChannel();
        resetGroupResourceDistributions();
        // initialize all client positions
        for (GroupDataModel group: getGroups()) {
            for (ClientData clientData: group.getClientDataMap().values()) {
                clientData.initializePosition();
            }
        }
    }

    public boolean isLastRound() {
        return getRoundConfiguration().isLastRound();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        for (GroupDataModel group: getGroups()) {
            group.setServerDataModel(this);
            // should we clear the resource distribution for all groups as well?  However, this means we won't be able to get the residual token counts
            // from the  
            // group.resetResourceDistribution();
        }
        super.channel = new EventTypeChannel();
        logger = Logger.getLogger( getClass().getName() );
        random = new Random();
    }

    public void unapply(PersistableEvent persistableEvent) {
        logger.warning("unapply() not implemented yet: " + persistableEvent);
    }

    public String calculateTrustGame(ClientData playerOne, ClientData playerTwo) {
        if (playerOne.getId().equals(playerTwo.getId())) {
        	String errorMessage = playerOne + " tried to calculate trust game with self, aborting";
        	logger.warning(errorMessage);
        	return errorMessage;
        }
        double playerOneAmountToKeep = playerOne.getTrustGamePlayerOneAmountToKeep();
        double[] playerTwoAmountsToKeep = playerTwo.getTrustGamePlayerTwoAmountsToKeep();
        
        double amountSent = 1.0d - playerOneAmountToKeep;
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s (Player 1) sent %s to %s (Player 2)\n", playerOne, CURRENCY_FORMATTER.format(amountSent), playerTwo));
        if (amountSent > 0) {
            double playerTwoEarnings = 0.0d;
            int index = 0;
            if (amountSent == 0.25d) {
                index = 0;
            } else if (amountSent == 0.50d) {
                index = 1;
            } else if (amountSent == 0.75d) {
                index = 2;
            } else if (amountSent == 1.0d) {
                index = 3;
            }
            playerTwoEarnings = playerTwoAmountsToKeep[index];
            double totalAmountSent = 3 * amountSent;
            double amountReturnedToP1 = totalAmountSent - playerTwoEarnings;
            double playerOneEarnings = playerOneAmountToKeep + amountReturnedToP1;
            String playerOneLog = String.format("%s (Player 1) kept %s and received %s back from Player two for a total of %s", 
            		playerOne, 
            		CURRENCY_FORMATTER.format(playerOneAmountToKeep), 
            		CURRENCY_FORMATTER.format(amountReturnedToP1), 
            		CURRENCY_FORMATTER.format(playerOneEarnings));
            builder.append(playerOneLog).append("\n");
            playerOne.logTrustGame(playerOneLog);
            playerOne.addTrustGameEarnings(playerOneAmountToKeep + amountReturnedToP1);
            String playerTwoLog = String.format("%s (Player 2) and earned %s", playerTwo, playerTwoEarnings);
            builder.append(playerTwoLog).append("\n");
            playerTwo.logTrustGame(playerTwoLog);
            playerTwo.addTrustGameEarnings(playerTwoEarnings);
        }
        else {
            String playerOneLog = String.format("%s (Player 1) sent nothing to Player 2 and earned %s", playerOne, playerOneAmountToKeep);
            playerOne.logTrustGame(playerOneLog);
            playerOne.addTrustGameEarnings(playerOneAmountToKeep);
            playerTwo.logTrustGame(playerOneLog + " - you were player two and didn't receive anything.");
        }
        return builder.toString();
    }

    @Override
    public List<Identifier> getAllClientIdentifiers() {
        return new ArrayList<Identifier>(clientsToGroups.keySet());

    }
}
