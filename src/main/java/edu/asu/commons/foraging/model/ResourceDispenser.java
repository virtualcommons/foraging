package edu.asu.commons.foraging.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.ResetTokenDistributionRequest;


/**
 * $Id$ 
 *
 * Creates resource tokens in the game world.  Current implementation generates a 
 * resource token probabilistically.  The probability that a token will be generated
 * in an empty space is 
 * P(t) = r * the number of neighboring cells containing a resource token / totalNumberOfNeighboringCells
 * 
 * @version $Revision$
 */

public class ResourceDispenser {

    private final static Logger logger = Logger.getLogger( ResourceDispenser.class.getName() );

    private final static Map<String, Type> resourceGeneratorTypeMap = new HashMap<String, Type>(3);

    public enum Type {
        DENSITY_DEPENDENT("density-dependent"), TOP_BOTTOM_PATCHY("top-bottom-patchy"), MOBILE("mobile");
        final String name;
        private Type(String name) {
            this.name = name;
            resourceGeneratorTypeMap.put(name, this);
        }
        
        public static Type find(final String name) {
            Type type = resourceGeneratorTypeMap.get(name);
            if (type == null) {
                type = valueOf(name);
                if (type == null) {
                    // FIXME: default value is density-dependent
                    logger.warning("Couldn't find resource generator by name, returning default: " + name);
                    type = DENSITY_DEPENDENT;
                }
            }
            return type;
        }

        public String toString() {
            return name;
        }
    }

    private final ServerDataModel serverDataModel;

    private final Random random = new Random();
    // FIXME: turn these into factory driven based on configuration parameter.
    private ResourceGenerator currentResourceGenerator;
    
    private final StochasticGenerator densityDependentGenerator = new DensityDependentResourceGenerator();
    private final TopBottomPatchGenerator topBottomPatchGenerator = new TopBottomPatchGenerator();
    private final MobileResourceGenerator mobileResourceGenerator = new MobileResourceGenerator();
    
    // FIXME: refactor this so that there's a single generator/strategy that gets used per round?
//    private Generator resourceGenerator;

    public ResourceDispenser(final ServerDataModel serverDataModel) {
        this.serverDataModel = serverDataModel;
    }
    
    public void resetTokenDistribution(ResetTokenDistributionRequest event) {
        if (serverDataModel.getRoundConfiguration().isPracticeRound()) {
            GroupDataModel group = serverDataModel.getGroup(event.getId());
            group.resetResourceDistribution();
            // FIXME: won't work if practice round is patchy
            Set<Resource> resources = currentResourceGenerator.generateInitialDistribution(group);
            serverDataModel.addResources(group, resources);
        }
    }
    
    public void initialize() {
        initialize(serverDataModel.getRoundConfiguration());
    }
    
    public void initialize(RoundConfiguration roundConfiguration) {
        ResourceDispenser.Type resourceGeneratorType = ResourceDispenser.Type.find( roundConfiguration.getResourceGeneratorType() );
        currentResourceGenerator = getResourceGenerator( resourceGeneratorType );
        currentResourceGenerator.initialize(roundConfiguration);
    }
    
    protected ResourceGenerator getResourceGenerator(Type resourceGeneratorType) {
        switch (resourceGeneratorType) {
        case DENSITY_DEPENDENT:
            return densityDependentGenerator;
        case TOP_BOTTOM_PATCHY:
            return topBottomPatchGenerator;
        case MOBILE:
            return mobileResourceGenerator;
        default:
            return densityDependentGenerator;
        }
    }
    
    @Deprecated
    public void updateResourceAge(GroupDataModel group) {       
        for (Resource resource: group.getResourceDistribution().values()) {
            // FIXME: needs to be modded to wraparound.
            resource.setAge(resource.getAge() + 1);
        }
    }
    
    public void generateResources() {
        generateResources( getCurrentResourceGenerator() );
    }

    /**
     * Special-case optimization for 3D visualization only.
     * 
     * @param group
     */
    public void generateResources(GroupDataModel group) {
        densityDependentGenerator.generate(group);
    }
    
    public void generateResources(ResourceGenerator generator) {
        for (GroupDataModel group : serverDataModel.getGroups()) {
//            logger.info("Making food with generator: " + generator + " for group : " + group);
            generator.generate(group);
        }
    }

    public ResourceGenerator getCurrentResourceGenerator() {
        return currentResourceGenerator;
    }
    
    public class MobileResourceGenerator extends ResourceGenerator.Base {
        private double tokenMovementProbability;
        private double tokenBirthProbability;
        public void initialize(RoundConfiguration roundConfiguration) {
            this.tokenMovementProbability = roundConfiguration.getTokenMovementProbability();
            this.tokenBirthProbability = roundConfiguration.getTokenBirthProbability();
            for (GroupDataModel group: serverDataModel.getGroups()) {
                Set<Resource> resources = generateInitialDistribution(group);
                serverDataModel.addResources(group, resources);
            }
        }
        /**
         * Moves all resources one-at-a-time.  Moved resources need to be aware of the updated
         * resource positions, otherwise resources could "disappear".
         * XXX: could optimize by generating a list of all removed resources and then a list of all added resources
         * and then first removing all resources and then adding new resources. 
         * @param group
         */
        public void generate(GroupDataModel group) {
            // getResourcePositions() returns a new HashSet
            // this Set will contain the most up-to-date resource positions as a working copy. 
            final Set<Point> currentResourcePositions = group.getResourcePositions();
            final Set<Point> addedResources = new HashSet<Point>();
            final Set<Point> removedResources = new HashSet<Point>();
            // iterate over a copy so we can update currentResourcePositions.
            // we need to update them one-at-a-time, otherwise a resource might move to a location that
            // has already been moved to...
            final List<Point> shuffledCopy = new ArrayList<Point>(currentResourcePositions);
            Collections.shuffle(shuffledCopy);
            // iterate through a new randomized copy of the points
            for (Point currentResourcePosition: shuffledCopy) {
                if (random.nextDouble() < tokenMovementProbability) {
                    // this token is ready to move.
                    final List<Point> validNeighbors = getValidMooreNeighborhood(currentResourcePosition, currentResourcePositions);
                    if (validNeighbors.isEmpty()) {
                        // this point can't move anywhere.
                        continue;
                    }
                    final Point newPosition = validNeighbors.get(random.nextInt(validNeighbors.size()));
                    // either execute one move at a time or execute a bulk move.
                    addedResources.add(newPosition);
                    removedResources.add(currentResourcePosition);
//                    serverDataModel.moveResource(group, currentResourcePosition, newPosition);
                    currentResourcePositions.remove(currentResourcePosition);
                    currentResourcePositions.add(newPosition);
//                    newResources.add(new Resource(newPosition));
//                    removedResources.add(currentResourcePosition);
                }
            }
            serverDataModel.moveResources(group, removedResources, addedResources);
            shuffledCopy.clear();
            shuffledCopy.addAll(currentResourcePositions);
            Collections.shuffle(shuffledCopy);
            Set<Resource> addedOffspring = new HashSet<Resource>();
            // next, generate offspring.
            // use current resource positions.
            for (Point currentResourcePosition: currentResourcePositions) {
                if (random.nextDouble() < tokenBirthProbability) {
                    final List<Point> validNeighbors = getValidMooreNeighborhood(currentResourcePosition, currentResourcePositions);
                    if (validNeighbors.isEmpty()) {
                        // cannot generate offspring anywhere, is resource-locked.
                        continue;
                    }
                    final Point offspringPosition = validNeighbors.get(random.nextInt(validNeighbors.size()));
                    addedOffspring.add(new Resource(offspringPosition));
                }
            }
            serverDataModel.addResources(group, addedOffspring);
        }
        
        private List<Point> getValidMooreNeighborhood(Point referencePoint, Set<Point> existingPositions) {
            List<Point> neighborhoodPoints = new ArrayList<Point>();
            int currentX = referencePoint.x;
            int currentY = referencePoint.y;
            int endX = currentX + 2;
            int endY = currentY + 2;
            for (int x = currentX - 1; x < endX; x++) {
                for (int y = currentY - 1; y < endY; y++) {
                    Point point = new Point(x, y);
                    // only add a point to the neighborhood set if it doesn't already have a resource.
                    if (serverDataModel.isValidPosition(point) && ! existingPositions.contains(point)) {
                        neighborhoodPoints.add(point);    
                    }
                }
            }
            return neighborhoodPoints;
        }
    }

    public class TopBottomPatchGenerator extends DensityDependentResourceGenerator {
        
        private double topRate;
        private double bottomRate;
        private double topDistribution;
        private double bottomDistribution;

        public void setBottomDistribution(double bottomDistribution) {
            this.bottomDistribution = bottomDistribution;
        }

        public void setTopDistribution(double topDistribution) {
            this.topDistribution = topDistribution;
        }

        public void setBottomRate(double bottomRate) {
            this.bottomRate = bottomRate;
        }

        public void setTopRate(double topRate) {
            this.topRate = topRate;
        }
        
        public void initialize(RoundConfiguration configuration) {
            setBottomDistribution(configuration.getBottomInitialResourceDistribution());
            setBottomRate(configuration.getBottomRegrowthScalingFactor());
            setTopDistribution(configuration.getTopInitialResourceDistribution());
            setTopRate(configuration.getTopRegrowthScalingFactor());
            for (GroupDataModel group: serverDataModel.getGroups()) {
                Set<Resource> resources = generateInitialDistribution(group);
                serverDataModel.addResources(group, resources);
            }
        }
        
        /**
         * Generates an initial distribution for top/bottom patches based on the top/bottom initial distribution
         * configuration parameters.
         */
        @Override
        public Set<Resource> generateInitialDistribution(GroupDataModel group) {
            int width = serverDataModel.getBoardWidth();
            int height = serverDataModel.getBoardHeight() / 2;
            int topTokensNeeded = (int) (width * height * topDistribution);
            logger.info("number of tokens needed on top: " + topTokensNeeded);
            Set<Resource> newResources = new HashSet<Resource>();
            while (newResources.size() < topTokensNeeded) {
                Point point = new Point(random.nextInt(width), random.nextInt(height));
                newResources.add(new Resource(point));
            }
            int bottomTokensNeeded = (int) (width * height * bottomDistribution);
            logger.info("number of tokens needed on bottom:" + bottomTokensNeeded);
            int tokensNeeded = topTokensNeeded + bottomTokensNeeded;
            while (newResources.size() < tokensNeeded) {
                Point point = new Point(random.nextInt(width), random.nextInt(height) + height);
                newResources.add(new Resource(point));
            }
            return newResources;
        }

        @Override
        public void generate(GroupDataModel group) {
            // partition the grid into north and south halves.  
            // regenerate food for the top half.
            int divisionPoint = serverDataModel.getBoardHeight() / 2;
            Set<Resource> newResources = new HashSet<Resource>();
            for (int y = 0; y < divisionPoint; y++) {
                for (int x = 0; x < serverDataModel.getBoardWidth(); x++) {
                    Point currentPoint = new Point(x, y);
                    if (!group.isResourceAt(currentPoint)) {
                        if (random.nextDouble() < getProbabilityForCell(group, x, y, topRate)) {
                            newResources.add(new Resource(currentPoint));
                        }
                    }
                }
            }
            // regenerate food for the bottom half
            for (int y = divisionPoint; y < serverDataModel.getBoardHeight(); y++) {
                for (int x = 0; x < serverDataModel.getBoardWidth(); x++) {
                    Point currentPoint = new Point(x, y);
                    if (!group.isResourceAt(currentPoint)) {
                        if (random.nextDouble() < getProbabilityForCell(group, x, y, bottomRate)) {
                            newResources.add(new Resource(currentPoint));
                        }
                    }
                }
            }
            // add all resources to the server
            serverDataModel.addResources(group, newResources);
        }
        
        @Override
        public double getProbabilityForCell(GroupDataModel group, int x, int y) {
            return getProbabilityForCell(group, x, y,
                    (y < serverDataModel.getBoardHeight() / 2) ? topRate : bottomRate);
        }
        
    }
    


    /**
     * Using the Strategy pattern to factor out algorithm from food dispensing.
     * 
     * New resource dispenser algorithm: 1. iterate through entire grid.. 2. for
     * each cell in the grid, calculate ratio of food-occupied neighboring cells
     * to max number of cells 3. multiply ratio by food probability
     * configuration parameter and 8?. 4. if result > random.nextDouble(), add food to
     * that grid cell.
     */

    public class DensityDependentResourceGenerator extends ResourceGenerator.Base 
    implements StochasticGenerator {
        private double rate;
        
        public void initialize(RoundConfiguration roundConfiguration) {
            this.rate = roundConfiguration.getRegrowthRate();
            for (GroupDataModel group: serverDataModel.getGroups()) {
                Set<Resource> resources = generateInitialDistribution(group);
                logger.info("density dependent resource generator initialized with " + resources.size() + " resources.");
                serverDataModel.addResources(group, resources);
            }
        }
        
        public double getProbabilityForCell(GroupDataModel group, int currentX, int currentY) {
            return getProbabilityForCell(group, currentX, currentY, rate);
        }
        
        protected double getProbabilityForCell(GroupDataModel group, int currentX, int currentY, double rate) {
            return rate * getNeighborsTokenRatio(group, currentX, currentY);
        }

        protected double getNeighborsTokenRatio(final GroupDataModel group, final int currentX, final int currentY) {
            double neighborsWithTokens = 0;
            // start off at -1 to offset the off-by-one we get from adding the
            // current cell.
            double maxNeighbors = -1;
            // use the Moore neighborhood (all 8 cells surrounding the empty cell).
            for (int x = currentX - 1; x < currentX + 2; x++) {
                for (int y = currentY - 1; y < currentY + 2; y++) {
                    Point neighbor = new Point(x, y);
                    // FIXME: if we ever decide to have Group-specific boundaries/territorial
                    // sizes, then we will need to change this.
                    if (serverDataModel.isValidPosition(neighbor)) {
                        maxNeighbors++;
                        if (group.isResourceAt(neighbor)) {
                            neighborsWithTokens++;
                        }
                    }
                }
            }
//            logger.info(String.format("[%f / % f] = %f", neighborsWithFood,
//                    maxNeighbors, neighborsWithFood / maxNeighbors));
            return neighborsWithTokens / maxNeighbors;
        }

        // FIXME: can make this algorithm more efficient.  Instead of scanning
        // across the entire grid, look at existing set of Food points and check
        // their neighbor probabilities
        public void generate(GroupDataModel group) {
            // FIXME: extremely important - add to a scratch space first and then copy over all at once to avoid copy problem.
            Set<Resource> newResources = new HashSet<Resource>();
            for (int y = 0; y < serverDataModel.getBoardHeight(); y++) {
                for (int x = 0; x < serverDataModel.getBoardWidth(); x++) {
                    Point currentPoint = new Point(x, y);
                    if ( ! group.isResourceAt(currentPoint) ) {
                        if (random.nextDouble() < getProbabilityForCell(group, x, y)) {
                            // FIXME: should initial age be parameterizable?
                            newResources.add(new Resource(currentPoint, 1));
                        }
                    }
                }
            }
            serverDataModel.addResources(group, newResources);
        }
    }



    public StochasticGenerator getDensityDependentGenerator() {
        return densityDependentGenerator;
    }

    public TopBottomPatchGenerator getTopBottomPatchGenerator() {
        return topBottomPatchGenerator;
    }

    public MobileResourceGenerator getMobileResourceGenerator() {
        return mobileResourceGenerator;
    }
    
}
