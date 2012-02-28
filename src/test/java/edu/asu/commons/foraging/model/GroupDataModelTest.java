package edu.asu.commons.foraging.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.conf.ServerConfiguration;
import edu.asu.commons.foraging.rules.Strategy;
import edu.asu.commons.foraging.rules.iu.ForagingStrategy;
import edu.asu.commons.net.Identifier;


public class GroupDataModelTest {
    
    private ServerDataModel serverDataModel;
    private int numberOfGroups = 3;
    
    @Before
    public void setUp() {
        serverDataModel = new ServerDataModel();
        ServerConfiguration serverConfiguration = new ServerConfiguration("configuration/asu/2011/t1");
        RoundConfiguration roundConfiguration = serverConfiguration.getAllParameters().get(4);
        serverDataModel.setRoundConfiguration(roundConfiguration);
        int numberOfParticipants = roundConfiguration.getClientsPerGroup() * numberOfGroups;
        addClients(numberOfParticipants);
    }

    @Test
    public void testUnanimousVote() {
        for (GroupDataModel group: serverDataModel.getGroups()) {
            for (ForagingStrategy rule: ForagingStrategy.values()) {
                for (ClientData data: group.getClientDataMap().values()) {
                    data.setVotedRule(rule);
                }
                // verify that this is the rule in place.
                Map<ForagingStrategy, Integer> votingResults = group.generateVotingResults();
                assertEquals(1, votingResults.size());
                assertEquals(rule, group.getSelectedRule());
            }
        }
    }

    private void addClients(int numberOfParticipants) {
        serverDataModel.clear();
        for (int i = 0; i < numberOfParticipants; i++) {
            serverDataModel.addClient(new ClientData(new Identifier.Mock()));
        }
    }
    
    @Test
    public void testTiebreaker() {
        addClients(10);
        for (GroupDataModel group: serverDataModel.getGroups()) {
            List<ForagingStrategy> rules = Arrays.asList(ForagingStrategy.values());
            // add some more randomness into the mix.
            Collections.shuffle(rules);
            // only tiebreaker participating rules are the first & second one (after shuffling)
            int ruleIndex = 0;
            List<ForagingStrategy> tieBreakerRules = Arrays.asList(rules.get(0), rules.get(1));
            for (ClientData data: group.getClientDataMap().values()) {
                // for group size of 5, should have votes for 0/1/2/0/1 so rule 0 & 1 are the only ones participating in the tie
                int index = (ruleIndex++) % 3;
                ForagingStrategy votedRule = rules.get(index);
                data.setVotedRule(votedRule);
            }
            Map<ForagingStrategy, Integer> votingResults = group.generateVotingResults();
            assertEquals("There should be 3 rules voted on, total" + votingResults, 3, votingResults.size());
            for (ForagingStrategy tieBreaker: tieBreakerRules) {
                System.err.println("Inspecting tiebreaker: " + tieBreaker);
            	assertEquals(2, votingResults.get(tieBreaker).intValue()); 
            }
            assertTrue(tieBreakerRules.contains(group.getSelectedRule()));
        }
    }
    
    @Test
    public void testImposedStrategyDistribution() {
    	Map<Strategy, Integer> imposedStrategyDistribution = new HashMap<Strategy, Integer>();
    	// test all the same
    	for (ForagingStrategy strategy: ForagingStrategy.values()) {
            imposedStrategyDistribution.clear();
            imposedStrategyDistribution.put(strategy, numberOfGroups);
            serverDataModel.allocateImposedStrategyDistribution(imposedStrategyDistribution);
            for (GroupDataModel group: serverDataModel.getGroups()) {
                assertEquals("mismatched imposed strategies", strategy, group.getImposedStrategy());
            }
        }
    	
    }

}
