package edu.asu.commons.foraging.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.rules.iu.ForagingStrategy;
import edu.asu.commons.net.Identifier;
import static org.junit.Assert.*;

public class GroupDataModelTest {
    
    private ServerDataModel serverDataModel;
    
    @Before
    public void setUp() {
        serverDataModel = new ServerDataModel();
        RoundConfiguration configuration = new RoundConfiguration();
        configuration.setProperty("clients-per-group", "5");
        serverDataModel.setRoundConfiguration(configuration);
        for (int i = 0; i < 10; i++) {
            Identifier id = new Identifier.Base() {
            };
            serverDataModel.addClient(new ClientData(id));
        }
        
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
    
    @Test
    public void testTiebreaker() {
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
            	assertEquals(2, votingResults.get(tieBreaker).intValue()); 
            }
            assertTrue(tieBreakerRules.contains(group.getSelectedRule()));
        }
    }

}
