package edu.asu.commons.foraging.model;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import edu.asu.commons.foraging.model.Resource;


public class ResourceTest {
    
    private final Set<Resource> resources = new HashSet<Resource>();
    
    private final static int RESOURCE_GRID_WIDTH = 256;
    private final static int RESOURCE_GRID_HEIGHT = 256;
    private final static int MAXIMUM_RESOURCE_AGE = 10;
    
    private final static Random RNG = new Random(0);
    
    @Before
    public void setUp() {
        fillResourceGrid();
    }
    
    private void fillResourceGrid() {
        for (int x = 0; x < RESOURCE_GRID_WIDTH; x++) {
            for (int y = 0; y < RESOURCE_GRID_HEIGHT; y++) {
                Point point = new Point(x, y);
                resources.add(new Resource(point, RNG.nextInt(MAXIMUM_RESOURCE_AGE)));
            }
        }        
    }
    
    @Test
    public void testResourceEquality() {
        for (Resource resource: resources) {
            assertEquals(resource, new Resource(new Point(resource.getPosition())));
        }
    }
    
    @Test
    public void testResourceRemoval() {
        for (int x = 0; x < RESOURCE_GRID_WIDTH; x++) {
            for (int y = 0; y < RESOURCE_GRID_HEIGHT; y++) {
                Point point = new Point(x, y);
                assertTrue(resources.remove(new Resource(point)));
            }
        }
        assertTrue(resources.isEmpty());
    }
    
    @Test
    public void testAddDuplicates() {
        fillResourceGrid();
        assertEquals(RESOURCE_GRID_HEIGHT * RESOURCE_GRID_WIDTH, resources.size());
    }

}
