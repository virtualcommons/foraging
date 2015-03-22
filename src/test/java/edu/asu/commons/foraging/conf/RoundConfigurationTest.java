package edu.asu.commons.foraging.conf;

import java.text.NumberFormat;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import edu.asu.commons.net.Identifier;
import edu.asu.commons.foraging.model.ClientData;

 

public class RoundConfigurationTest {
    
    private final static String SURVEY_ID = "boogaloo-252";
    private RoundConfiguration roundConfiguration;
    
    
    @Before
    public void setUp() {
        ServerConfiguration serverConfiguration = new ServerConfiguration("configuration/iu/2011/vote-punish");
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

    @Test
    public void testClientDebriefingGeneration() {
        ClientData data = new ClientData(new Identifier.Mock());
        ServerConfiguration serverConfiguration = roundConfiguration.getParentConfiguration();
        data.addCorrectQuizAnswers(5);
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        double quizEarnings = serverConfiguration.getQuizEarnings(data);
        int currentTokens = 15;
        int totalTokens = 60;
        double totalEarnings = quizEarnings + serverConfiguration.getShowUpPayment() + (totalTokens * serverConfiguration.getDollarsPerToken());
        System.err.println("total earnings: " + totalEarnings);
        data.setCurrentTokens(currentTokens);
        data.setTotalTokens(totalTokens);
        String debriefing = roundConfiguration.generateClientDebriefing(data, false);
        System.err.println("debriefing: " + debriefing);
        assertTrue(debriefing.contains("Quiz earnings: " + formatter.format(quizEarnings)));
        assertTrue(debriefing.contains(formatter.format(currentTokens*serverConfiguration.getDollarsPerToken())));
        // FIXME: this doesn't work currently because ClientData.totalIncome is only added to during addTokens()
        // assertTrue(debriefing.contains(formatter.format(totalEarnings)));
    }

}
