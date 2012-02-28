package edu.asu.commons.foraging.facilitator;

import java.awt.Dimension;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import edu.asu.commons.event.BeginExperimentRequest;
import edu.asu.commons.event.BeginRoundRequest;
import edu.asu.commons.event.ConfigurationEvent;
import edu.asu.commons.event.EndRoundRequest;
import edu.asu.commons.event.EventTypeProcessor;
import edu.asu.commons.event.FacilitatorMessageEvent;
import edu.asu.commons.event.SetConfigurationEvent;
import edu.asu.commons.event.ShowExitInstructionsRequest;
import edu.asu.commons.event.ShowInstructionsRequest;
import edu.asu.commons.facilitator.BaseFacilitator;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.conf.ServerConfiguration;
import edu.asu.commons.foraging.event.BeginChatRoundRequest;
import edu.asu.commons.foraging.event.FacilitatorEndRoundEvent;
import edu.asu.commons.foraging.event.FacilitatorSanctionUpdateEvent;
import edu.asu.commons.foraging.event.FacilitatorUpdateEvent;
import edu.asu.commons.foraging.event.ImposeStrategyEvent;
import edu.asu.commons.foraging.event.QuizCompletedEvent;
import edu.asu.commons.foraging.event.ShowImposedStrategyRequest;
import edu.asu.commons.foraging.event.ShowSurveyInstructionsRequest;
import edu.asu.commons.foraging.event.ShowTrustGameRequest;
import edu.asu.commons.foraging.event.ShowVoteScreenRequest;
import edu.asu.commons.foraging.event.ShowVotingInstructionsRequest;
import edu.asu.commons.foraging.event.TrustGameResultsFacilitatorEvent;
import edu.asu.commons.foraging.event.TrustGameSubmissionEvent;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.foraging.rules.Strategy;

/**
 * $Id$
 * 
 * @author <a href='anonymouslee@gmail.com'>Allen Lee</a>, Deepali Bhagvat
 * @version $Revision$
 */
public class Facilitator extends BaseFacilitator<ServerConfiguration, RoundConfiguration> {

    private ServerDataModel serverDataModel;
    private FacilitatorWindow facilitatorWindow;
    private boolean experimentRunning = false;
	private Map<Strategy, Integer> imposedStrategyDistribution;

    private Facilitator() {
        this(new ServerConfiguration());
    }

    public Facilitator(ServerConfiguration configuration) {
        super(configuration);
    }
    
    @SuppressWarnings("rawtypes")
    void createFacilitatorWindow(Dimension dimension) {
        facilitatorWindow = new FacilitatorWindow(dimension, this);
        if (getId() == null) {
            // configure for unconnected functionality
            facilitatorWindow.configureForReplay();
        }
        addEventProcessor(new EventTypeProcessor<SetConfigurationEvent>(SetConfigurationEvent.class) {
            public void handle(SetConfigurationEvent event) {
                RoundConfiguration configuration = (RoundConfiguration) event.getParameters();
                setServerConfiguration(configuration.getParentConfiguration());
            }
        });
        addEventProcessor(new EventTypeProcessor<TrustGameResultsFacilitatorEvent>(TrustGameResultsFacilitatorEvent.class){
            @Override
            public void handle(TrustGameResultsFacilitatorEvent event) {
                facilitatorWindow.updateTrustGame(event);
            }
        });
        addEventProcessor(new EventTypeProcessor<FacilitatorUpdateEvent>(FacilitatorUpdateEvent.class) {

            public void handle(FacilitatorUpdateEvent event) {
                if (serverDataModel == null) {
                    experimentRunning = true;
                    serverDataModel = event.getServerDataModel();
                    facilitatorWindow.displayGame();
                } else {
                    serverDataModel = event.getServerDataModel();
                }
            }
        });
        addEventProcessor(new EventTypeProcessor<FacilitatorEndRoundEvent>(FacilitatorEndRoundEvent.class) {
            public void handle(FacilitatorEndRoundEvent event) {
                serverDataModel = null;
                facilitatorWindow.endRound(event);
            }
        });
        addEventProcessor(new EventTypeProcessor<FacilitatorSanctionUpdateEvent>(FacilitatorSanctionUpdateEvent.class) {
            public void handle(FacilitatorSanctionUpdateEvent event) {
            	System.err.println("Updating facilitator after post-round sanctioning.");
                facilitatorWindow.displayDebriefing(event.getServerDataModel());
            }
        });
        addEventProcessor(new EventTypeProcessor<FacilitatorMessageEvent>(FacilitatorMessageEvent.class) {
            public void handle(FacilitatorMessageEvent event) {
                facilitatorWindow.addMessage(event.getMessage());
            }
        });
        addEventProcessor(new EventTypeProcessor<QuizCompletedEvent>(QuizCompletedEvent.class) {
            public void handle(QuizCompletedEvent event) {
                facilitatorWindow.quizCompleted(event);
            }
        });
        addEventProcessor(new EventTypeProcessor<TrustGameSubmissionEvent>(TrustGameSubmissionEvent.class) {
            public void handle(TrustGameSubmissionEvent event) {
                facilitatorWindow.trustGameSubmitted(event);
            }
        });

    }

    /*
     * Send a request to server to start an experiment
     */
    public void sendBeginExperimentRequest() {
        transmit(new BeginExperimentRequest(getId()));
        sendBeginRoundRequest();
    }

    /**
     * Sends a request to show round instructions
     */
    public void sendShowInstructionsRequest() {
        transmit(new ShowInstructionsRequest(getId()));
    }

    public void sendShowTrustGameRequest() {
        transmit(new ShowTrustGameRequest(getId()));
    }
    

    public void sendShowVotingInstructionsRequest() {
        transmit(new ShowVotingInstructionsRequest(getId()));
    }
    
    public void sendShowVoteScreenRequest() {
        transmit(new ShowVoteScreenRequest(getId()));
    }
    
    public void sendShowSurveyInstructionsRequest() {
        transmit(new ShowSurveyInstructionsRequest(getId()));
    }
    
	public void sendShowExitInstructionsRequest() {
		transmit(new ShowExitInstructionsRequest(getId()));
	}

    /*
     * Send a request to start a round
     */

    public void sendBeginRoundRequest() {
        transmit(new BeginRoundRequest(getId()));
    }

    public void sendBeginChatRoundRequest() {
        transmit(new BeginChatRoundRequest(getId()));
    }

    public void endExperiment() {
        // configuration.resetRoundConfiguration();
        // serverGameState = null;
        // stopExperiment = true;
        // experimentRunning = false;
        // facilitatorWindow.updateMenuItems();
    }

    /*
     * Send a request to stop a round
     */
    public void sendEndRoundRequest() {
        transmit(new EndRoundRequest(getId()));
    }

    /*
     * Send a request to set the configuration object
     */
    public void sendSetConfigRequest() {
        transmit(new ConfigurationEvent<ServerConfiguration>(getId(), getServerConfiguration()));
    }

    public FacilitatorWindow getFacilitatorWindow() {
        return facilitatorWindow;
    }

    public void setRoundParameters(List<RoundConfiguration> roundConfiguration) {
        getServerConfiguration().setAllParameters(roundConfiguration);
    }

    public boolean isExperimentRunning() {
        return experimentRunning;
    }

    public boolean isReplaying() {
        return getId() == null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Dimension dimension = new Dimension(700, 700);
                Facilitator facilitator = new Facilitator();
                facilitator.connect();
                JFrame frame = new JFrame();
                frame.setTitle("Facilitator: " + facilitator.getId());
                frame.setSize(dimension);
                facilitator.createFacilitatorWindow(dimension);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(facilitator.getFacilitatorWindow());
                frame.setJMenuBar(facilitator.getFacilitatorWindow().getMenuBar());
                // frame.pack();
                frame.setVisible(true);
            }
        });
    }

    public RoundConfiguration getCurrentRoundConfiguration() {
        return getServerConfiguration().getCurrentParameters();
    }

    public void setServerDataModel(ServerDataModel serverGameState) {
        this.serverDataModel = serverGameState;
    }

	public void sendImposeStrategyEvent(Map<Strategy, Integer> imposedStrategyDistribution) {
		this.imposedStrategyDistribution = imposedStrategyDistribution;
		facilitatorWindow.addMessage("sending imposed strategy: " + imposedStrategyDistribution);
		transmit(new ImposeStrategyEvent(getId(), imposedStrategyDistribution));
	}

	public Map<Strategy, Integer> getImposedStrategyDistribution() {
		return imposedStrategyDistribution;
	}

	public void sendShowImposedStrategy() {
		if (imposedStrategyDistribution == null || imposedStrategyDistribution.isEmpty()) {
			facilitatorWindow.addMessage("No imposed strategies selected, please select a strategy first.");
			return;
		}
		transmit(new ShowImposedStrategyRequest(getId()));
	}

}
