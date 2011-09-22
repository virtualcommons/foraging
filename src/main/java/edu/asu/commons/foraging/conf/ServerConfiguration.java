package edu.asu.commons.foraging.conf;

import edu.asu.commons.conf.ExperimentConfiguration;

/**
 * $Id: ServerConfiguration.java 529 2010-08-17 00:08:01Z alllee $
 * 
 * Contains the know-how for parsing and programmatically accessing the
 * server's configuration file properties. The forager server's config file
 * specifies per-server settings, per-round settings are managed by
 * RoundConfiguration.
 *
 * FIXME: Recoverable exceptions that are handled shouldn't spit out their
 * stack trace without some additional info stating that they are mostly
 * harmless.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @see 
 * @version $Revision: 529 $
 */
public class ServerConfiguration extends ExperimentConfiguration.Base<RoundConfiguration> {

    private static final long serialVersionUID = -1737412253553943902L;
    
    private final static String DEFAULT_LOG_FILE_DESTINATION = "foraging-server.log";
    
    public ServerConfiguration() {
        super();
    }
    
    public ServerConfiguration(String configurationDirectory) {
        super(configurationDirectory);
    }
    
    @Override
    protected RoundConfiguration createRoundConfiguration(String roundConfigurationResource) {
        return new RoundConfiguration(roundConfigurationResource);
    }
    
    public String getLogFileDestination() {
        return assistant.getStringProperty("log", DEFAULT_LOG_FILE_DESTINATION);
    }

    public boolean shouldUpdateFacilitator() {
        return assistant.getBooleanProperty("update-facilitator", false);
    }
    
    public boolean isCensoredChat() {
        return assistant.getBooleanProperty("censored-chat", false);
    }
    
    public boolean shouldInitialize3D() {
        for (RoundConfiguration configuration : getAllParameters()) {
            if (configuration.is3dExperiment()) {
                return true;
            }
        }
        return false;
    }
    public boolean shouldInitialize2D() {
        for (RoundConfiguration configuration : getAllParameters()) {
            if (configuration.is2dExperiment()) {
                return true;
            }
        }
        return false;
    }
    
    public double getShowUpPayment() {
        return assistant.getDoubleProperty("show-up-payment", 5.0d);
    }

    public double getQuizCorrectAnswerReward() {
        return assistant.getDoubleProperty("quiz-correct-answer-reward", 0.50d);
    }
    
    public String getWelcomeInstructions() {
        return assistant.getStringProperty("welcome-instructions", "Please wait quietly and do not open or close any programs on this computer.");
    }
    
    public String getGeneralInstructions() {
        return assistant.getStringProperty("general-instructions", "");
    }
    
    public String getFieldOfVisionInstructions() {
        return assistant.getProperty("field-of-vision-instructions", 
                "Your view of the resource will be limited in this round.  The area visible to you will be shaded.");
    }
    
    

		
}
