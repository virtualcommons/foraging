package edu.asu.commons.foraging.conf;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.asu.commons.net.Identifier;

public class RoundConfigurationTest {
    
    private final static String SURVEY_ID = "boogaloo-252";
    private RoundConfiguration roundConfiguration;
    
    
    @Before
    public void setUp() {
        ServerConfiguration serverConfiguration = new ServerConfiguration("configuration/indiana-experiments/2011/vote-punish");
        roundConfiguration = serverConfiguration.getAllParameters().get(4);
    }

    @Test
    public void testSurveyTemplate() {
        String uninterpolatedInstructions = roundConfiguration.getSurveyInstructions();
        assertTrue(uninterpolatedInstructions.contains("{"));
        assertTrue(uninterpolatedInstructions.contains("}"));
        assertFalse(uninterpolatedInstructions.contains("http://"));

        Identifier id = new Identifier.Mock() {
            private static final long serialVersionUID = 1231310402707042800L;
            public String getSurveyId() {
                return SURVEY_ID;
            }
        };
        String interpolatedInstructions = roundConfiguration.getSurveyInstructions(id);
        System.err.println("interpolated instructions:" + interpolatedInstructions);
        assertFalse(interpolatedInstructions.contains("{"));
        assertFalse(interpolatedInstructions.contains("}"));
        assertTrue(interpolatedInstructions.contains(SURVEY_ID));
        assertTrue(interpolatedInstructions.contains("http"));
        
    }

}
