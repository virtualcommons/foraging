package edu.asu.commons.foraging.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.asu.commons.event.EventChannel;
import edu.asu.commons.experiment.DataModel;
import edu.asu.commons.foraging.bot.Bot;
import edu.asu.commons.foraging.bot.BotFactory;
import edu.asu.commons.foraging.bot.BotType;
import edu.asu.commons.foraging.bot.BotIdentifier;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.conf.ServerConfiguration;
import edu.asu.commons.foraging.event.ClientPositionUpdateEvent;
import edu.asu.commons.foraging.event.EnforcementRankingRequest;
import edu.asu.commons.foraging.event.LockResourceRequest;
import edu.asu.commons.foraging.event.MonitorTaxEvent;
import edu.asu.commons.foraging.event.MovementEvent;
import edu.asu.commons.foraging.event.PostRoundSanctionRequest;
import edu.asu.commons.foraging.event.TokenCollectedEvent;
import edu.asu.commons.foraging.event.UnlockResourceRequest;
import edu.asu.commons.foraging.rules.Strategy;
import edu.asu.commons.foraging.rules.iu.ForagingStrategy;
import edu.asu.commons.foraging.ui.Circle;
import edu.asu.commons.net.Identifier;

/**
 * Represents a collection of Clients and associates them with a token distribution. In the
 * case of a shared resource model where all clients share the same world space, there will
 * be a single group and hence a single token distribution for all clients.
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @author Deepali Bhagvat
 */

public class GroupDataModel implements Comparable<GroupDataModel>, DataModel<ServerConfiguration, RoundConfiguration> {

    private static final long serialVersionUID = -4756267788191037505L;

    private transient Logger logger = Logger.getLogger(getClass().getName());

    // the subset of clients in ServerDataModel
    private final Map<Identifier, ClientData> clients = new HashMap<>();
    // FIXME: making this transient causes a NPE in the facilitator, should be transient however.
    private final Map<Point, Resource> resourceDistribution = new HashMap<>();
    private final List<Bot> bots = new ArrayList<>();

    private final transient Map<Identifier, Resource> resourceOwners = new HashMap<>();
    private transient Set<Resource> removedResources;
    private transient Set<Resource> addedResources;

    private transient ServerDataModel serverDataModel;

    private final long groupId;
    private volatile static long nextGroupId = 0;

    private volatile int receivedEnforcementRankings = 0;
    private volatile int receivedRegulationRankings = 0;
    private volatile int receivedSanctionRankings = 0;
    private EnforcementMechanism activeEnforcementMechanism = EnforcementMechanism.NONE;
    private SanctionMechanism activeSanctionMechanism = SanctionMechanism.NONE;
    private RegulationData activeRegulation;
    private Strategy imposedStrategy;

    private List<ClientData> waitingMonitors;

    private ClientData activeMonitor;

    private ArrayList<RegulationData> submittedRegulations = new ArrayList<RegulationData>();

    private ArrayList<Strategy> selectedRules;

    // Used when assigning clients to zones/teams
    private int nextZone = 0;
    private int[] currentTeamSize = {0, 0};

    public GroupDataModel(ServerDataModel serverDataModel) {
        this(serverDataModel, nextGroupId++);
    }

    public GroupDataModel(ServerDataModel serverDataModel, long groupId) {
        this.serverDataModel = serverDataModel;
        this.groupId = groupId;
        this.removedResources = new HashSet<>();
        this.addedResources = new HashSet<>();
    }

    public void handleSanctionRequest(PostRoundSanctionRequest sanctionRequest) {
        Map<Identifier, Integer> sanctions = sanctionRequest.getSanctions();
        for (Map.Entry<Identifier, Integer> entry : sanctions.entrySet()) {
            Identifier id = entry.getKey();
            int change = entry.getValue();
            if (id.equals(sanctionRequest.getId())) {
                // this is a sanction cost
                clients.get(id).postRoundSanctionCost(change);
            } else {
                clients.get(id).postRoundSanctionPenalty(change);
            }
        }
    }

    public int getNumberOfNeighboringTokens(Point referencePoint) {
        int numberOfNeighboringTokens = 0;
        int currentX = referencePoint.x;
        int currentY = referencePoint.y;
        int endX = currentX + 2;
        int endY = currentY + 2;
        for (int x = currentX - 1; x < endX; x++) {
            for (int y = currentY - 1; y < endY; y++) {
                Point point = new Point(x, y);
                if (point.equals(referencePoint))
                    continue;
                // only add a point to the neighborhood set if it doesn't already have a resource.
                if (serverDataModel.isValidPosition(point) && resourceDistribution.containsKey(point)) {
                    numberOfNeighboringTokens++;
                }
            }
        }
        return numberOfNeighboringTokens;

    }

    private double rankToValue(int rank) {
        switch (rank) {
            case 0:
                return 1.0d;
            case 1:
                return 0.5d;
            case 2:
                return 0.33d;
            case 3:
                return 0.25d;
            case 4:
                return 0.2d;
            default:
                System.err.println("Trying to convert invalid rank to value: " + rank);
                return 0;
        }
    }

    public int size() {
        return clients.size();
    }

    public boolean rotateMonitorIfNecessary() {
        if (activeEnforcementMechanism.isRotatingMonitor() && !waitingMonitors.isEmpty()) {
            applyMonitorTax();
            // set active monitor back to harvest
            activeMonitor.setForagingRole(ForagingRole.HARVEST);
            if (waitingMonitors.isEmpty()) {
                logger.warning("no waiting monitors left but still trying to rotate: " + activeMonitor);
                return false;
            } else {
                activeMonitor = waitingMonitors.remove(0);
                activeMonitor.setForagingRole(ForagingRole.MONITOR);
                return true;
            }
        }
        return false;
    }

    public RegulationData generateRegulationRankings() {
        int numberOfRegulations = submittedRegulations.size();
        double[] regulationVotingTally = new double[numberOfRegulations];
        Arrays.fill(regulationVotingTally, 0.0d);
        int maxRankingIndex = 0;
        double maxRankingValue = 0.0d;
        for (ClientData clientData : clients.values()) {
            int[] regulationRankings = clientData.getRegulationRankings();
            logger.info("client: " + clientData.getId() + " ranked regulations: " + regulationRankings);
            for (int rank = 0; rank < numberOfRegulations; rank++) {
                int actualRegulationIndex = regulationRankings[rank];
                if (actualRegulationIndex == -1) {
                    continue;
                }
                regulationVotingTally[actualRegulationIndex] += rankToValue(rank);
                submittedRegulations.get(actualRegulationIndex).setRank(regulationVotingTally[actualRegulationIndex]);
                if (regulationVotingTally[actualRegulationIndex] > maxRankingValue) {
                    maxRankingValue = regulationVotingTally[actualRegulationIndex];
                    maxRankingIndex = actualRegulationIndex;
                }
            }
        }
        activeRegulation = submittedRegulations.get(maxRankingIndex);
        logger.info("active regulation: " + activeRegulation.getText() + " max ranking value: " + maxRankingValue);
        return activeRegulation;
    }

    public RegulationData getActiveRegulation() {
        return activeRegulation;
    }

    public void setActiveRegulation(RegulationData activeRegulation) {
        this.activeRegulation = activeRegulation;
    }

    public void setActiveEnforcementMechanism(EnforcementMechanism enforcementMechanism) {
        this.activeEnforcementMechanism = enforcementMechanism;
    }

    // FIXME: this algorithm is very similar to generateRegulationRankings, extract to other method.
    public EnforcementMechanism generateEnforcementRankings() {
        // resetEnforcementRankingCount();
        // FIXME: change to round config parameter instead?
        double[] enforcementVotingTally = new double[EnforcementMechanism.values().length];
        Arrays.fill(enforcementVotingTally, 0.0d);
        int maxRankingIndex = 0;
        double maxRankingValue = 0.0d;
        for (ClientData clientData : clients.values()) {
            int[] enforcementRankings = clientData.getEnforcementRankings();
            logger.info("XXX: enforcement rankings: " + enforcementRankings);
            for (int rank = 0; rank < enforcementRankings.length; rank++) {
                // 0 is top choice
                int actualEnforcementIndex = enforcementRankings[rank];
                if (actualEnforcementIndex == -1) {
                    // nothing selected for this rank
                    continue;
                }
                enforcementVotingTally[actualEnforcementIndex] += rankToValue(rank);
                // keep a tally of the max index so we don't have to make another pass.
                if (enforcementVotingTally[actualEnforcementIndex] > maxRankingValue) {
                    maxRankingValue = enforcementVotingTally[actualEnforcementIndex];
                    maxRankingIndex = actualEnforcementIndex;
                }
            }
        }
        activeEnforcementMechanism = EnforcementMechanism.get(maxRankingIndex);
        logger.info("Active enforcement mechanism: " + activeEnforcementMechanism + " with rank " + maxRankingValue);
        if (activeEnforcementMechanism.hasMonitor()) {
            // pick a random person from the clients
            ArrayList<ClientData> clientDataList = new ArrayList<ClientData>(clients.values());
            Collections.shuffle(clientDataList);
            // pick the first client from the shuffled list and set their role to MONITOR
            activeMonitor = clientDataList.remove(0);
            activeMonitor.setForagingRole(ForagingRole.MONITOR);
            // set the rest of the clients to the HARVEST role
            for (ClientData clientData : clientDataList) {
                clientData.setForagingRole(ForagingRole.HARVEST);
            }
            if (activeEnforcementMechanism.isRotatingMonitor()) {
                waitingMonitors = clientDataList;
            }
        } else if (activeEnforcementMechanism.isSanctioningEnabled()) {
            for (ClientData clientData : clients.values()) {
                clientData.setForagingRole(ForagingRole.SANCTION_AND_HARVEST);
            }
        }
        return activeEnforcementMechanism;
    }

    public SanctionMechanism generateSanctionRankings() {
        // resetSanctionRankingCount();
        // FIXME: change to round config parameter instead?
        double[] sanctionVotingTally = new double[SanctionMechanism.values().length];
        Arrays.fill(sanctionVotingTally, 0.0d);
        int maxRankingIndex = 0;
        double maxRankingValue = 0.0d;
        for (ClientData clientData : clients.values()) {
            int[] sanctionRankings = clientData.getEnforcementRankings();
            System.out.println("Server ranks: " + sanctionRankings[0] + " " + sanctionRankings[1]);
            logger.info("XXX: sanction rankings: " + Arrays.asList(sanctionRankings));
            for (int rank = 0; rank < sanctionRankings.length; rank++) {
                // 0 is top choice
                int actualSanctionIndex = sanctionRankings[rank];
                if (actualSanctionIndex == -1) {
                    // nothing selected for this rank
                    continue;
                }
                sanctionVotingTally[actualSanctionIndex] += rankToValue(rank);
                // sanctionVotingTally[actualSanctionIndex] += rankToValue(actualSanctionIndex);
                // keep a tally of the max index so we don't have to make another pass.
                if (sanctionVotingTally[actualSanctionIndex] > maxRankingValue) {
                    maxRankingValue = sanctionVotingTally[actualSanctionIndex];
                    maxRankingIndex = actualSanctionIndex;
                }
            }
        }
        activeSanctionMechanism = SanctionMechanism.get(maxRankingIndex);
        logger.info("Active sanction mechanism: " + activeSanctionMechanism.getTitle() + " with rank " + maxRankingValue);

        setActiveEnforcementMechanism(EnforcementMechanism.get(maxRankingIndex));

        logger.info("Active enforcement mechanism: " + activeEnforcementMechanism.getTitle() + " with rank " + maxRankingValue);

        return activeSanctionMechanism;
    }

    public boolean hasReceivedAllEnforcementRankings() {
        return receivedEnforcementRankings >= clients.size();
    }

    public boolean hasReceivedAllRegulations() {
        return submittedRegulations.size() >= clients.size();
    }

    /**
     * Used to reset the food distribution for a client during a practice
     * round.
     */
    public void resetResourceDistribution() {
        for (ClientData clientState : clients.values()) {
            clientState.reset();
        }
        getRemovedResources().addAll(resourceDistribution.values());
        getAddedResources().clear();
        resourceDistribution.clear();
    }

    /**
     * Perform all cleanup.
     */
    public void cleanupRound() {
        resourceDistribution.clear();
        clearDiffLists();
        activeEnforcementMechanism = EnforcementMechanism.NONE;
        activeSanctionMechanism = SanctionMechanism.NONE;
        submittedRegulations.clear();
        activeMonitor = null;
    }

    public boolean isResourceAt(Point position) {
        return resourceDistribution.containsKey(position);
    }

    public void addResource(Point position) {
        addResource(position, 0);
    }

    public void addResource(Point position, int age) {
        addResource(new Resource(position, age));
    }

    public void addResource(Resource resource) {
        Point position = resource.getPosition();
        synchronized (resourceDistribution) {
            resourceDistribution.put(position, resource);
        }
        getAddedResources().add(resource);
    }

    void addResources(Collection<Point> locations) {
        synchronized (resourceDistribution) {
            for (Point point : locations) {
                Resource resource = new Resource(point);
                resourceDistribution.put(point, resource);
                getAddedResources().add(resource);
            }
        }
    }

    public void addResources(Set<Resource> resources) {
        synchronized (resourceDistribution) {
            for (Resource resource : resources) {
                Point position = resource.getPosition();
                resourceDistribution.put(position, resource);
                getAddedResources().add(resource);
            }
        }
    }

    void moveResources(Collection<Point> removedResources, Collection<Point> addedResources) {
        synchronized (resourceDistribution) {
            for (Point oldLocation : removedResources) {
                Resource oldResource = resourceDistribution.remove(oldLocation);
                getRemovedResources().add(oldResource);
            }
            for (Point newLocation : addedResources) {
                Resource newResource = new Resource(newLocation);
                resourceDistribution.put(newLocation, newResource);
                getAddedResources().add(newResource);
            }
        }
    }

    /**
     * Currently only invoked when replaying a round and stepping backwards.
     *
     * @param position
     */
    public void removeResource(Point position) {
        synchronized (resourceDistribution) {
            resourceDistribution.remove(position);
        }
    }

    public Set<Identifier> getClientIdentifiers() {
        return Collections.unmodifiableSet(clients.keySet());
    }

    public Set<Identifier> getOrderedClientIdentifiers() {
        return new TreeSet<Identifier>(clients.keySet());
    }

    public Point getClientPosition(Identifier id) {
        ClientData clientData = clients.get(id);
        if (clientData == null) {
            getLogger().severe("getClientPosition on an id with no ClientData mapping: " + id + " clients: " + clients);
            return new Point(0, 0);
        }
        return clientData.getPoint();
    }

    public Map<Identifier, Point> getClientPositions() {
        Map<Identifier, Point> positions = new HashMap<>();
        for (ClientData clientData : clients.values()) {
            positions.put(clientData.getId(), clientData.getPoint());
        }
        for (Bot bot : bots) {
            positions.put(bot.getId(), bot.getPosition());
        }
        return positions;
    }

    public ClientData getClientData(Identifier id) {
        return clients.get(id);
    }

    public int getCurrentTokens(Identifier id) {
        ClientData state = clients.get(id);
        if (state == null) {
            // FIXME: perhaps we should just return 0 instead.
            getLogger().severe("no client state available for: " + id);
            return 0;
        }
        return state.getCurrentTokens();
    }

    public Map<Identifier, Integer> getClientTokens() {
        Map<Identifier, Integer> clientTokensMap = new HashMap<>();
        for (ClientData data : clients.values()) {
            clientTokensMap.put(data.getId(), data.getCurrentTokens());
        }
        for (Bot bot : bots) {
            clientTokensMap.put(bot.getId(), bot.getCurrentTokens());
        }
        return clientTokensMap;
    }

    public int getResourceDistributionSize() {
        return resourceDistribution.size();
    }

    public Set<Identifier> getClientIdentifiersWithin(Circle circle) {
        HashSet<Identifier> ids = new HashSet<Identifier>();
        for (ClientData data : clients.values()) {
            if (circle.contains(data.getPosition())) {
                ids.add(data.getId());
            }
        }
        return ids;
    }

    public boolean move(Bot bot, Direction direction) {
        Point newPosition = direction.apply(bot.getPosition());
        if (serverDataModel.isValidPosition(newPosition) && isCellAvailable(newPosition)) {
            bot.setPosition(newPosition);
            getEventChannel().handle(new MovementEvent(bot.getId(), direction));
            return true;
        }
        return false;
    }

    public Bot getBot(BotIdentifier id) {
        for (Bot bot : bots) {
            if (bot.getId().equals(id)) {
                return bot;
            }
        }
        getLogger().severe("no bot found with id: " + id);
        return bots.get(0);
    }

    /**
     * Moves the client corresponding to id in direction d.
     *
     * @param id
     * @param direction
     * @return
     */
    public void moveClient(Identifier id, Direction direction) {
        if (id instanceof BotIdentifier) {
            Bot bot = getBot((BotIdentifier) id);
            move(bot, direction);
            return;
        }
        ClientData clientData = clients.get(id);
        Point newPosition = direction.apply(clientData.getPoint());
        // System.err.println(String.format("Moving client %s in direction %s to position %s", id, direction, newPosition));

        if (serverDataModel.isValidPosition(newPosition)) {
            // check occupancy
            if (isCellAvailable(newPosition) && isCellAllowed(clientData, newPosition)) {
                clientData.setPosition(newPosition);
                // if the client is explicitly collecting, then movement does not automatically
                // collect a token.
                if (clientData.isExplicitCollectionMode()) {
                    return;
                }
                collectToken(clientData);
            }
        }
    }

    public boolean isCellAvailable(Point position) {
        RoundConfiguration currentRoundConfiguration = getRoundConfiguration();
        if (currentRoundConfiguration.isOccupancyEnabled()) {
            int maximumOccupancyPerCell = currentRoundConfiguration.getMaximumOccupancyPerCell();
            int currentOccupancy = 0;
            for (Point otherPosition : getClientPositions().values()) {
                if (position.equals(otherPosition)) {
                    currentOccupancy++;
                }
                if (currentOccupancy >= maximumOccupancyPerCell) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Return true if the participant represented by clientData is allowed to
     * move to the given position (assuming the position is valid and
     * available), based on zone rules.
     */
    private boolean isCellAllowed(ClientData clientData, Point position) {
        RoundConfiguration roundConfiguration = getRoundConfiguration();
        if (roundConfiguration.areZonesAssigned() && roundConfiguration.isTravelRestricted(clientData.getZone())) {
            int positionZone = position.y < serverDataModel.getBoardHeight() / 2 ? 0 : 1;
            if (positionZone != clientData.getZone()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a Point based on the assigned number (1..N) where N is the number of clients per group. The Point
     * corresponds to an x,y location where y is the midpoint of the resource grid and the x values will be evenly
     * distributed across the width of the board.
     */
    public Point getInitialPosition(int assignedNumber) {
        RoundConfiguration roundConfiguration = getRoundConfiguration();
        int clientsPerGroup = roundConfiguration.getClientsPerGroup();
        double cellWidth = roundConfiguration.getResourceWidth() / (double) clientsPerGroup;
        int x = (int) ((cellWidth / 2) + (cellWidth * (assignedNumber - 1)));
        int y = roundConfiguration.getResourceDepth() / 2;
        return new Point(x, y);
    }


    public void collectToken(ClientData clientData) {
        Point position = clientData.getPoint();
        synchronized (resourceDistribution) {
            if (resourceDistribution.containsKey(position)) {
                getRemovedResources().add(resourceDistribution.remove(position));
                clientData.addToken(position);
                getEventChannel().handle(new TokenCollectedEvent(clientData.getId(), position));
            }
        }
    }

    public void collectToken(Bot bot) {
        Point position = bot.getPosition();
        synchronized (resourceDistribution) {
            if (resourceDistribution.containsKey(position)) {
                getRemovedResources().add(resourceDistribution.remove(position));
                bot.addToken(position);
                getEventChannel().handle(new TokenCollectedEvent(bot.getId(), position));
            }
        }
    }

    public void clearDiffLists() {
        if (removedResources != null) {
            removedResources.clear();
        }
        if (addedResources != null) {
            addedResources.clear();
        }
    }

    public void addClient(ClientData clientData) {
        // Assign the client to a zone/team, if this round has zone assignment
        if (getRoundConfiguration().areZonesAssigned()) {
            int thisZone = nextZone;
            clientData.setZone(thisZone);
            currentTeamSize[thisZone]++;
            nextZone = (thisZone + 1) % 2;
            if (currentTeamSize[nextZone] >= getRoundConfiguration().getMaxTeamSize(nextZone)) {
                // The next team is full, so continue to assign to this team
                nextZone = thisZone;
            }
        } else {
            clientData.setZone(0);
        }

        clients.put(clientData.getId(), clientData);
        clientData.setAssignedNumber(clients.size());
        clientData.setGroupDataModel(this);
        clientData.initializePosition();
    }

    public void removeClient(Identifier id) {
        clients.remove(id);
    }

    public boolean isFull() {
        return clients.size() == serverDataModel.getRoundConfiguration().getClientsPerGroup();
    }

    public void clear() {
        clients.clear();
        nextZone = 0;
        currentTeamSize[0] = currentTeamSize[1] = 0;
        cleanupRound();
    }

    public boolean isResourceDistributionEmpty() {
        return resourceDistribution.isEmpty();
    }

    public void setServerDataModel(ServerDataModel state) {
        resourceDistribution.clear();
        this.serverDataModel = state;
    }

    public Map<Identifier, ClientData> getClientDataMap() {
        return new HashMap<Identifier, ClientData>(clients);
    }

    public void resetSanctionCounts() {
        for (ClientData data : clients.values()) {
            data.resetLatestSanctions();
        }
    }

    public RoundConfiguration getRoundConfiguration() {
        return serverDataModel.getRoundConfiguration();
    }

    private boolean isResourceOwner(Identifier id, Resource resource) {
        Resource lockedResource = resourceOwners.get(id);
        if (lockedResource == null) {
            getLogger().severe(String.format("%s harvesting fruits for resource [%s] it doesn't own", id, resource));
            return false;
        }
        if (!lockedResource.equals(resource)) {
            getLogger().severe(String.format("%s harvesting fruits for resource [%s] it doesn't own - it actually owns [%s]", id, resource, lockedResource));
            return false;
        }
        return true;
    }

    public void harvestResource(Identifier id, Resource resource) {
        if (isResourceOwner(id, resource)) {
            ClientData clientData = clients.get(id);
            clientData.addTokens(getRoundConfiguration().ageToTokens(resource.getAge()));
            Point position = resource.getPosition();
            getRemovedResources().add(resourceDistribution.remove(position));
            resourceOwners.remove(id);
        }
    }

    public void harvestFruits(Identifier id, Resource resource) {
        if (isResourceOwner(id, resource)) {
            ClientData clientData = clients.get(id);
            clientData.addTokens(getRoundConfiguration().getTokensPerFruits());
            getResourceFromDistribution(resource).setAge(getRoundConfiguration().getMaximumResourceAge() - 1);
            resourceOwners.remove(id);
        }
    }

    private Resource getResourceFromDistribution(Resource remoteResource) {
        return resourceDistribution.get(remoteResource.getPosition());
    }

    /**
     * Returns true if the resource is in the resource distribution and is not
     * already owned by a different resource owner.
     *
     * @param request a LockResourceRequest containing the local/remote resource
     * @return
     */
    public boolean lockResource(LockResourceRequest request) {
        Identifier id = request.getId();
        Resource remoteResource = request.getResource();
        Resource localResource = getResourceFromDistribution(remoteResource);
        if (!resourceDistribution.containsKey(remoteResource.getPosition())) {
            getLogger().warning(String.format("Trying to lock a resource [%s] that is no longer present.", remoteResource));
            return false;
        }
        Resource ownedResource = resourceOwners.get(id);
        if (ownedResource != null && ownedResource.equals(localResource)) {
            getLogger().warning("Resource is already owned by the same owner: " + localResource + id);
            return true;
        }
        if (resourceOwners.containsValue(localResource)) {
            getLogger().warning("Resource is already owned by another owner: " + localResource + id);
            return false;
        }
        resourceOwners.put(id, localResource);
        return true;
    }

    public void unlockResource(UnlockResourceRequest request) {
        resourceOwners.remove(request.getId());
    }

    public Set<Point> getResourcePositions() {
        synchronized (resourceDistribution) {
            return new HashSet<>(resourceDistribution.keySet());
        }
    }

    public Map<Point, Resource> getResourceDistribution() {
        synchronized (resourceDistribution) {
            return new HashMap<>(resourceDistribution);
        }
    }

    /**
     * Only invoked by the client side.
     *
     * @param event
     */
    public void updateDiffs(ClientPositionUpdateEvent event) {
        for (ClientData clientData : clients.values()) {
            Identifier id = clientData.getId();
            clientData.setCurrentTokens(event.getCurrentTokens(id));
            clientData.setPosition(event.getClientPosition(id));
        }
        synchronized (resourceDistribution) {
            for (Resource resource : event.getRemovedTokens()) {
                resourceDistribution.remove(resource.getPosition());
            }
            for (Resource resource : event.getAddedTokens()) {
                resourceDistribution.put(resource.getPosition(), resource);
            }
        }
    }

    private Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    public Set<Resource> getRemovedResources() {
        if (removedResources == null) {
            removedResources = new HashSet<>();
        }
        return removedResources;
    }

    public Set<Resource> getAddedResources() {
        if (addedResources == null) {
            addedResources = new HashSet<>();
        }
        return addedResources;
    }

    public long getGroupId() {
        return groupId;
    }

    public String toString() {
        return "Group #" + groupId;
    }

    @Override
    public int compareTo(GroupDataModel other) {
        return Long.valueOf(groupId).compareTo(other.groupId);
    }

    public EnforcementMechanism getActiveEnforcementMechanism() {
        return activeEnforcementMechanism;
    }

    public SanctionMechanism getActiveSanctionMechanism() {
        return activeSanctionMechanism;
    }

    public ClientData getActiveMonitor() {
        return activeMonitor;
    }

    public void submitEnforcementRanking(EnforcementRankingRequest request) {
        ClientData clientData = clients.get(request.getId());
        clientData.setEnforcementRankings(request.getRankings());
        receivedEnforcementRankings++;
    }

    public boolean isRotatingMonitor() {
        return activeEnforcementMechanism != null && activeEnforcementMechanism.isRotatingMonitor();
    }

    public boolean hasReceivedAllRegulationRankings() {
        return receivedRegulationRankings >= clients.size();
    }

    public boolean hasReceivedAllSanctionRankings() {
        return receivedSanctionRankings >= clients.size();
    }

    public List<RegulationData> getSubmittedRegulations() {
        return submittedRegulations;
    }

    public void applyMonitorTax() {
        if (activeMonitor == null) {
            logger.severe("Trying to apply monitor tax for null monitor.");
            return;
        }
        if (activeMonitor.isTaxReceived()) {
            logger.severe("active monitor: " + activeMonitor + " already received tax.");
            return;
        }
        ArrayList<ClientData> clientDataList = new ArrayList<ClientData>(clients.values());
        clientDataList.remove(activeMonitor);
        Map<Identifier, Integer> monitorTaxes = new HashMap<Identifier, Integer>();
        int totalTax = 0;
        for (ClientData clientData : clientDataList) {
            int monitorTax = clientData.applyMonitorTax();
            totalTax += monitorTax;
            activeMonitor.addTokens(monitorTax);
            monitorTaxes.put(clientData.getId(), monitorTax);
        }
        logger.info("active monitor: " + activeMonitor + " received tax: " + totalTax);
        activeMonitor.setTaxReceived();
        // persist monitor tax
        serverDataModel.getEventChannel().handle(new MonitorTaxEvent(activeMonitor.getId(), monitorTaxes, totalTax));
    }

    @Override
    public List<Identifier> getAllClientIdentifiers() {
        return new ArrayList<Identifier>(clients.keySet());
    }

    @Override
    public EventChannel getEventChannel() {
        return serverDataModel.getEventChannel();
    }

    public Map<Strategy, Integer> generateVotingResults() {
        return generateVotingResults(getRoundConfiguration().isImposedStrategyEnabled());
    }

    public Map<Strategy, Integer> generateVotingResults(boolean imposedStrategyEnabled) {
        Map<Strategy, Integer> tallyMap = new HashMap<Strategy, Integer>();
        selectedRules = new ArrayList<Strategy>();
        if (imposedStrategyEnabled) {
            // short circuits to use the imposed strategy
            tallyMap.put(getImposedStrategy(), 1);
            selectedRules.add(getImposedStrategy());
            return tallyMap;
        }
        for (ClientData client : clients.values()) {
            ForagingStrategy rule = client.getVotedRule();
            Integer count = tallyMap.get(rule);
            if (count == null) {
                count = 0;
            }
            tallyMap.put(rule, count + 1);
        }

        Integer maxSeenValue = 0;
        for (Map.Entry<Strategy, Integer> entry : tallyMap.entrySet()) {
            Integer currentValue = entry.getValue();
            // getLogger().info("rule : " + entry.getKey() + " has a vote value of " + currentValue);

            if (currentValue > maxSeenValue) {
                maxSeenValue = currentValue;
                // getLogger().info("That was better than " + maxSeenValue + " - clearing out the old rule set and adding this one.");
                selectedRules.clear();
                selectedRules.add(entry.getKey());
            } else if (currentValue == maxSeenValue) {
                // getLogger().info("that was the same as " + maxSeenValue + " - adding this one." + selectedRules);
                selectedRules.add(entry.getKey());
            }
        }
        // getLogger().info("tally map is: " + tallyMap);
        getLogger().info("picking first rule from " + selectedRules);
        Collections.shuffle(selectedRules);
        return tallyMap;
    }

    public List<Strategy> getSelectedRules() {
        return selectedRules;
    }

    public Strategy getSelectedRule() {
        return selectedRules.get(0);
    }

    @Override
    public ServerConfiguration getExperimentConfiguration() {
        return serverDataModel.getExperimentConfiguration();
    }

    public Strategy getImposedStrategy() {
        return imposedStrategy;
    }

    public void setImposedStrategy(Strategy imposedStrategy) {
        this.imposedStrategy = imposedStrategy;
    }

    public void addBots(int botsPerGroup, BotType botType) {
        int size = clients.size();
        RoundConfiguration configuration = getRoundConfiguration();
        double movementProbability = configuration.getRobotMovementProbability();
        double harvestProbability = configuration.getRobotHarvestProbability();
        double tokenProximityScalingFactor = configuration.getTokenProximityScalingFactor();
        int actionsPerSecond = configuration.getRobotMovesPerSecond();
        BotFactory botFactory = BotFactory.getInstance();
        synchronized (bots) {
            bots.clear();
            for (int i = 0; i < botsPerGroup; i++) {
                int botNumber = size + i + 1;
                Bot bot = botFactory.create(botType, this, botNumber)
                        .setMovementProbability(movementProbability)
                        .setHarvestProbability(harvestProbability)
                        .setActionsPerSecond(actionsPerSecond)
                        .setTokenProximityScalingFactor(tokenProximityScalingFactor);

                bot.initialize(configuration);
                bots.add(bot);
            }
        }
    }

    public Map<Identifier, Bot> getBotMap() {
        return bots.stream().collect(Collectors.toMap(Bot::getId, b -> b));
    }

    public int getNumberOfBots() {
        return bots.size();
    }

    public void activateBots(boolean resetBotActions) {
        // FIXME: should do this in parallel or randomize iteration order for > 1 bot
        for (Bot bot : bots) {
            bot.act();
            if (resetBotActions) {
                bot.resetActionsTaken();
            }
        }
    }

    public void clearBotActionsTaken() {
        for (Bot bot : bots) {
            bot.resetActionsTaken();
        }
    }

    public Map<Identifier, Point> getBotPositions() {
        return bots.stream().collect(Collectors.toMap(Bot::getId, Bot::getPosition));
    }

}
