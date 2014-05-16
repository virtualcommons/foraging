package edu.asu.commons.foraging.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.asu.commons.foraging.conf.ServerConfiguration;
import edu.asu.commons.net.Identifier;
import static org.junit.Assert.*;

/**
 * $Id$
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class ResourceDispenserTest {
    
    public final static int NUMBER_OF_CLIENTS = 10;
    
    private ResourceDispenser resourceDispenser;
    private ServerConfiguration serverConfiguration;
    private ServerDataModel serverDataModel;
    
    @Before
    public void setUp() {
        serverDataModel = new ServerDataModel();
        serverConfiguration = new ServerConfiguration("configuration/asu/2011/t1");
        serverDataModel.setRoundConfiguration(serverConfiguration.getCurrentParameters());
        for (int i = 0; i < NUMBER_OF_CLIENTS; i++) {
            ClientData clientData = new ClientData(new Identifier.Base());
            serverDataModel.addClient(clientData);        
        }
        resourceDispenser = new ResourceDispenser(serverDataModel);
        resourceDispenser.initialize();
    }
    
    @Test
    public void testDensityDependentResourceGenerator() {
        for (GroupDataModel group: serverDataModel.getGroups()) {
            assertEquals(42, group.getResourceDistributionSize());            
        }

    }
    
    @After
    public void tearDown() {
        
    }
    
    

}
