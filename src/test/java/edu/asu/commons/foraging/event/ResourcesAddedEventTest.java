package edu.asu.commons.foraging.event;

import static org.junit.Assert.*;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.foraging.model.Resource;

public class ResourcesAddedEventTest {
    
    private final TreeSet<PersistableEvent> actions = new TreeSet<PersistableEvent>();

    @Before
    public void setUp() {
        Random random = new Random();
        for (int numberOfEvents = 0; numberOfEvents < 6; numberOfEvents++) {
            HashSet<Resource> resources = new HashSet<Resource>();
            for (int numberOfResources = 0; numberOfResources < 300; numberOfResources++) {
                Resource resource = new Resource(new Point(random.nextInt(), random.nextInt()));
                resources.add(resource);
            }
            ResourcesAddedEvent event = new ResourcesAddedEvent(null, resources);
            actions.add(event);
        }
    }
    @Test
    public void testSerialization() throws Exception {
        Thread.sleep(100);
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("test.save"));
        oos.writeObject(actions);
        oos.flush();
        oos.close();
        assertTrue(new File("test.save").exists());
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("test.save"));
        TreeSet<PersistableEvent> persistedActions = (TreeSet<PersistableEvent>) ois.readObject();
        for (PersistableEvent persistedEvent: persistedActions) {
            assertTrue(actions.contains(persistedEvent));
        }
    }
}
