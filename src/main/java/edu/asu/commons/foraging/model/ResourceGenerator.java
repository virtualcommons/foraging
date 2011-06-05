package edu.asu.commons.foraging.model;

import java.awt.Point;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import edu.asu.commons.foraging.conf.RoundConfiguration;

/**
 * $Id: ResourceGenerator.java 475 2010-02-24 00:39:44Z alllee $
 * 
 * ResourceGenerators add resources directly to the GroupDataModel.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 475 $
 */
public interface ResourceGenerator {
    public void initialize(RoundConfiguration roundConfiguration);
    public Set<Resource> generateInitialDistribution(GroupDataModel group);
    public void generate(GroupDataModel group);
    
    public static abstract class Base implements ResourceGenerator {
        private final Random random = new Random();
        public Set<Resource> generateInitialDistribution(GroupDataModel group) {
            RoundConfiguration configuration = group.getRoundConfiguration();
            int width = configuration.getResourceWidth();
            int height = configuration.getResourceDepth();
            int tokensNeeded = configuration.getInitialNumberOfTokens();
            // FIXME: should this logic be parameterized as well 
            boolean ageMatters = ! configuration.is2dExperiment();
            int maximumResourceAge = configuration.getMaximumResourceAge();
            boolean shouldVaryAge = maximumResourceAge > 0;
            Set<Resource> newResources = new HashSet<Resource>();
            while (newResources.size() < tokensNeeded) {
                Point point = new Point(random.nextInt(width), random.nextInt(height));
                Resource resource = new Resource(point);
                if (! newResources.contains(resource)) {
                    if (ageMatters && shouldVaryAge) {
                        resource.setAge(random.nextInt(maximumResourceAge) + 1);
                    }
                    newResources.add(resource);
                }

            }
            return newResources;
        }
    }
}
