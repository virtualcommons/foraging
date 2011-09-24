package edu.asu.commons.foraging.facilitator;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import edu.asu.commons.event.BeginExperimentRequest;
import edu.asu.commons.event.BeginRoundRequest;
import edu.asu.commons.event.ConfigurationEvent;
import edu.asu.commons.event.EndRoundRequest;
import edu.asu.commons.event.EventTypeProcessor;
import edu.asu.commons.event.FacilitatorMessageEvent;
import edu.asu.commons.event.SetConfigurationEvent;
import edu.asu.commons.facilitator.BaseFacilitator;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.conf.ServerConfiguration;
import edu.asu.commons.foraging.event.BeginChatRoundRequest;
import edu.asu.commons.foraging.event.FacilitatorEndRoundEvent;
import edu.asu.commons.foraging.event.FacilitatorSanctionUpdateEvent;
import edu.asu.commons.foraging.event.FacilitatorUpdateEvent;
import edu.asu.commons.foraging.event.QuizCompletedEvent;
import edu.asu.commons.foraging.event.ShowInstructionsRequest;
import edu.asu.commons.foraging.event.ShowTrustGameRequest;
import edu.asu.commons.foraging.event.TrustGameSubmissionEvent;
import edu.asu.commons.foraging.model.ServerDataModel;

/**
 * $Id: Facilitator.java 529 2010-08-17 00:08:01Z alllee $
 * 
 * @author <a href='anonymouslee@gmail.com'>Allen Lee</a>, Deepali Bhagvat
 * @version $Revision: 529 $
 */
public class Facilitator extends BaseFacilitator<ServerConfiguration> {

    private final static Facilitator INSTANCE = new Facilitator();
    private ServerDataModel serverDataModel;
    private FacilitatorWindow facilitatorWindow;
    private boolean experimentRunning = false;

    private Facilitator() {
        this(new ServerConfiguration());
    }

    @SuppressWarnings("rawtypes")
    public Facilitator(ServerConfiguration configuration) {
        super(configuration);
        addEventProcessor(new EventTypeProcessor<SetConfigurationEvent>(SetConfigurationEvent.class) {

            public void handle(SetConfigurationEvent event) {
                RoundConfiguration configuration = (RoundConfiguration) event.getParameters();
                setServerConfiguration(configuration.getParentConfiguration());
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
                facilitatorWindow.updateDebriefing(event);
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

    public static Facilitator getInstance() {
        return INSTANCE;
    }

    void createFacilitatorWindow(Dimension dimension) {
        facilitatorWindow = new FacilitatorWindow(dimension, this);
        if (getId() == null) {
            // configure for unconnected functionality
            facilitatorWindow.configureForReplay();
        }
    }

    // public void accept(Identifier id, Object event) {
    // if (event instanceof ConfigurationEvent) {
    // ConfigurationEvent configEvent = (ConfigurationEvent) event;
    // setConfiguration(configEvent.getConfiguration());
    // }
    // else if (event instanceof ServerGameStateEvent) {
    // ServerGameStateEvent serverGameStateEvent = (ServerGameStateEvent) event;
    // if (!stopExperiment) {
    //
    // if (serverGameState == null) {
    // System.err.println("about to display game..");
    // experimentRunning = true;
    // // FIXME: could use configuration from this event... serverGameStateEvent.getServerGameState().getConfiguration();
    // serverGameState = serverGameStateEvent.getServerGameState();
    // facilitatorWindow.displayGame();
    // }
    // else {
    // // synchronous updates
    // serverGameState = serverGameStateEvent.getServerGameState();
    // }
    // }
    // facilitatorWindow.updateWindow(serverGameStateEvent.getTimeLeft());
    // // facilitatorWindow.repaint();
    // }
    // else if (event instanceof FacilitatorEndRoundEvent) {
    // FacilitatorEndRoundEvent endRoundEvent = (FacilitatorEndRoundEvent) event;
    // serverGameState = null;
    // facilitatorWindow.endRound(endRoundEvent);
    // }
    // else if (event instanceof FacilitatorSanctionUpdateEvent) {
    // FacilitatorSanctionUpdateEvent fdue = (FacilitatorSanctionUpdateEvent) event;
    // facilitatorWindow.updateDebriefing(fdue);
    // }
    // else if (event instanceof QuizCompletedEvent) {
    // facilitatorWindow.quizCompleted();
    // }
    // }

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

    void sendShowTrustGameRequest() {
        transmit(new ShowTrustGameRequest(getId()));
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

    public ServerDataModel getServerGameState() {
        return serverDataModel;
    }

    public void setRoundParameters(
            List<RoundConfiguration> roundConfiguration) {
        getServerConfiguration().setAllParameters(roundConfiguration);
    }

    public boolean isExperimentRunning() {
        return experimentRunning;
    }

    public boolean isReplaying() {
        return getId() == null;
    }

    public static void main(String[] args) {
        Runnable createGuiRunnable = new Runnable() {

            public void run() {
                Dimension dimension = new Dimension(700, 700);
                Facilitator facilitator = Facilitator.getInstance();
                facilitator.connect();
                JFrame frame = new JFrame();
                frame.setTitle("Facilitator window: " + facilitator.getId());
                frame.setSize((int) dimension.getWidth(), (int) dimension.getHeight());
                facilitator.createFacilitatorWindow(dimension);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(facilitator.getFacilitatorWindow());
                frame.setJMenuBar(facilitator.getFacilitatorWindow().getMenuBar());
                // frame.pack();
                frame.setVisible(true);
            }
        };
        SwingUtilities.invokeLater(createGuiRunnable);
    }

    public RoundConfiguration getCurrentRoundConfiguration() {
        return getServerConfiguration().getCurrentParameters();
    }

    public void setServerGameState(ServerDataModel serverGameState) {
        this.serverDataModel = serverGameState;
    }
}
