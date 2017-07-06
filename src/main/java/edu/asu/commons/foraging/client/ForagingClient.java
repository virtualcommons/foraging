package edu.asu.commons.foraging.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import edu.asu.commons.client.BaseClient;
import edu.asu.commons.event.ClientMessageEvent;
import edu.asu.commons.event.Event;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.event.EventTypeProcessor;
import edu.asu.commons.event.SetConfigurationEvent;
import edu.asu.commons.event.ShowExitInstructionsRequest;
import edu.asu.commons.event.ShowInstructionsRequest;
import edu.asu.commons.event.SocketIdentifierUpdateRequest;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.conf.ServerConfiguration;
import edu.asu.commons.foraging.event.AgentInfoRequest;
import edu.asu.commons.foraging.event.BeginChatRoundRequest;
import edu.asu.commons.foraging.event.ClientMovementRequest;
import edu.asu.commons.foraging.event.ClientPositionUpdateEvent;
import edu.asu.commons.foraging.event.CollectTokenRequest;
import edu.asu.commons.foraging.event.EndRoundEvent;
import edu.asu.commons.foraging.event.MovementEvent;
import edu.asu.commons.foraging.event.PostRoundSanctionRequest;
import edu.asu.commons.foraging.event.PostRoundSanctionUpdateEvent;
import edu.asu.commons.foraging.event.RealTimeSanctionRequest;
import edu.asu.commons.foraging.event.ResetTokenDistributionRequest;
import edu.asu.commons.foraging.event.RoundStartedEvent;
import edu.asu.commons.foraging.event.RuleSelectedUpdateEvent;
import edu.asu.commons.foraging.event.RuleVoteRequest;
import edu.asu.commons.foraging.event.SetImposedStrategyEvent;
import edu.asu.commons.foraging.event.ShowSurveyInstructionsRequest;
import edu.asu.commons.foraging.event.ShowTrustGameRequest;
import edu.asu.commons.foraging.event.ShowVoteScreenRequest;
import edu.asu.commons.foraging.event.ShowVotingInstructionsRequest;
import edu.asu.commons.foraging.event.SinglePlayerClientUpdateEvent;
import edu.asu.commons.foraging.event.SinglePlayerUpdateRequest;
import edu.asu.commons.foraging.event.SurveyIdSubmissionRequest;
import edu.asu.commons.foraging.event.SynchronizeClientEvent;
import edu.asu.commons.foraging.event.TrustGameSubmissionRequest;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.rules.iu.ForagingStrategy;
import edu.asu.commons.foraging.server.ForagingServer;
import edu.asu.commons.foraging.ui.GameWindow;
import edu.asu.commons.foraging.ui.GameWindow2D;
import edu.asu.commons.foraging.ui.GameWindow3D;
import edu.asu.commons.net.SocketIdentifier;
import edu.asu.commons.ui.UserInterfaceUtils;
import edu.asu.commons.util.Duration;
import edu.asu.commons.util.Utils;

/**
 * Foraging experiment client, for 2D / 3D experiments (3D now defunct, need to refactor out)
 * 
 * @author <a href='mailto:mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 */

public class ForagingClient extends BaseClient<ServerConfiguration, RoundConfiguration> {

    enum ClientState {
        // not connected to the server at all
        UNCONNECTED,
        // connected, but in between rounds
        WAITING,
        // connected and currently running a round
        RUNNING
    };

    private ClientState state = ClientState.UNCONNECTED;

    private GameWindow gameWindow;

    private ClientDataModel dataModel;

    private MessageQueue messageQueue;

    private JPanel clientPanel = new JPanel();
    private Logger logger = Logger.getLogger(getClass().getName());

    public ForagingClient(ServerConfiguration configuration) {
        super(configuration);
        dataModel = new ClientDataModel(this);
        clientPanel.setLayout(new BorderLayout());
        if (configuration.shouldInitialize2D()) {
            gameWindow = new GameWindow2D(this);
        } else if (configuration.shouldInitialize3D()) {
            gameWindow = new GameWindow3D(this);
        }
        clientPanel.add(gameWindow.getPanel(), BorderLayout.CENTER);
    }

    @Override
    protected void postConnect() {
        // FIXME: this is hacky, using client side socket id as the "authoritative" id. client side socket id helps
        // disambiguate NATted clients but may cause other issues
        SocketIdentifier socketId = (SocketIdentifier) getId();
        transmit(new SocketIdentifierUpdateRequest(socketId, socketId.getStationNumber()));
        state = ClientState.WAITING;
    }

    public GameWindow2D getGameWindow2D() {
        return (GameWindow2D) gameWindow;
    }

    public GameWindow3D getGameWindow3D() {
        return (GameWindow3D) gameWindow;
    }

    public GameWindow getGameWindow() {
        return gameWindow;
    }

    public void sendAvatarInfo(boolean male, Color hairColor, Color skinColor, Color shirtColor, Color trouserColor, Color shoesColor) {
        transmit(new AgentInfoRequest(getId(), male, hairColor, skinColor, shirtColor, trouserColor, shoesColor));
        getGameWindow3D().removeAgentDesigner();
    }

    public void sendAgentInfo(Color color) {
        transmit(new AgentInfoRequest(getId(), color));
        getGameWindow3D().removeAgentDesigner();
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void initializeEventProcessors() {
        addEventProcessor(new EventTypeProcessor<SetConfigurationEvent>(SetConfigurationEvent.class) {
            public void handle(SetConfigurationEvent event) {
                RoundConfiguration configuration = (RoundConfiguration) event.getParameters();
                dataModel.setRoundConfiguration(configuration);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        gameWindow.init();
                    }
                });

            }
        });

        addEventProcessor(new EventTypeProcessor<ShowInstructionsRequest>(ShowInstructionsRequest.class) {
            public void handle(ShowInstructionsRequest request) {
                getGameWindow().showInstructions(request.isSummarized());
            }
        });
        addEventProcessor(new EventTypeProcessor<ShowTrustGameRequest>(ShowTrustGameRequest.class) {
            public void handle(ShowTrustGameRequest request) {
                getGameWindow().showTrustGame();
            }
        });
        addEventProcessor(new EventTypeProcessor<SetImposedStrategyEvent>(SetImposedStrategyEvent.class) {
            @Override
            public void handle(SetImposedStrategyEvent event) {
                dataModel.setSelectedStrategies(Arrays.asList(event.getStrategy()));
            }
        });
        addEventProcessor(new EventTypeProcessor<ShowVotingInstructionsRequest>(ShowVotingInstructionsRequest.class) {
            public void handle(ShowVotingInstructionsRequest request) {
                getGameWindow2D().showInitialVotingInstructions();
            }
        });
        addEventProcessor(new EventTypeProcessor<RuleSelectedUpdateEvent>(RuleSelectedUpdateEvent.class) {
            @Override
            public void handle(RuleSelectedUpdateEvent event) {
                dataModel.setSelectedStrategies(event.getSelectedStrategies());
                getGameWindow2D().showVotingResults(event.getSelectedStrategies(), event.getVotingResults());
            }
        });
        addEventProcessor(new EventTypeProcessor<ShowVoteScreenRequest>(ShowVoteScreenRequest.class) {
            public void handle(ShowVoteScreenRequest request) {
                getGameWindow2D().showVotingScreen();
            }
        });
        addEventProcessor(new EventTypeProcessor<ShowSurveyInstructionsRequest>(ShowSurveyInstructionsRequest.class) {
            public void handle(ShowSurveyInstructionsRequest request) {
                getGameWindow2D().showSurveyInstructions();
            }
        });
        addEventProcessor(new EventTypeProcessor<RoundStartedEvent>(RoundStartedEvent.class) {
            public void handle(RoundStartedEvent event) {
                setId(event.getId());
                dataModel.initialize(event.getGroupDataModel());
                logger.info("initializing data model to group datamodel: " + dataModel.getClientData().getPosition());
                messageQueue.start();
            }
        });

        addEventProcessor(new EventTypeProcessor<EndRoundEvent>(EndRoundEvent.class) {
            public void handle(final EndRoundEvent event) {
                if (isRoundInProgress()) {
                    dataModel.setGroupDataModel(event.getGroupDataModel());
                    getGameWindow().endRound(event);
                    messageQueue.stop();
                    state = ClientState.WAITING;
                }
            }
        });
        addEventProcessor(new EventTypeProcessor<SinglePlayerClientUpdateEvent>(SinglePlayerClientUpdateEvent.class) {
            public void handle(SinglePlayerClientUpdateEvent event) {
                dataModel.update(event);
                getGameWindow().update(event.getTimeLeft());
            }
        });
        addEventProcessor(new EventTypeProcessor<ClientPositionUpdateEvent>(ClientPositionUpdateEvent.class) {
            public void handle(ClientPositionUpdateEvent event) {
                if (isRoundInProgress()) {
                    dataModel.update(event);
                    getGameWindow2D().collectTokens(event.getCollectedTokenPositions());
                    getGameWindow().update(event.getTimeLeft());
                }
            }
        });
        addEventProcessor(new EventTypeProcessor<SynchronizeClientEvent>(SynchronizeClientEvent.class) {
            public void handle(SynchronizeClientEvent event) {
                dataModel.setGroupDataModel(event.getGroupDataModel());
                getGameWindow().update(event.getTimeLeft());
            }
        });
        addEventProcessor(new EventTypeProcessor<ShowExitInstructionsRequest>(ShowExitInstructionsRequest.class) {
            @Override
            public void handle(ShowExitInstructionsRequest request) {
                GroupDataModel groupDataModel = (GroupDataModel) request.getDataModel();
                if (groupDataModel != null) {
                    dataModel.setGroupDataModel((GroupDataModel) request.getDataModel());
                }
                getGameWindow2D().showExitInstructions();
            }
        });
        initialize2DEventProcessors();
        // initialize3DEventProcessors();
        messageQueue = new MessageQueue();
    }

    // private void initialize3DEventProcessors() {
    // addEventProcessor(new EventTypeProcessor<LockResourceEvent>(LockResourceEvent.class) {
    // public void handle(LockResourceEvent event) {
    // // tell the game window to highlight the appropriate resource
    // getGameWindow3D().highlightResource(event);
    // }
    // });
    // }

    private void initialize2DEventProcessors() {
        addEventProcessor(new EventTypeProcessor<BeginChatRoundRequest>(BeginChatRoundRequest.class) {
            public void handle(BeginChatRoundRequest request) {
                dataModel.initialize(request.getGroupDataModel());
                getGameWindow2D().initializeChatPanel();
            }
        });
        addEventProcessor(new EventTypeProcessor<PostRoundSanctionUpdateEvent>(PostRoundSanctionUpdateEvent.class) {
            public void handle(PostRoundSanctionUpdateEvent event) {
                getGameWindow2D().updateDebriefing(event);
            }
        });

        addEventProcessor(new EventTypeProcessor<ClientMessageEvent>(ClientMessageEvent.class) {
            public void handle(ClientMessageEvent event) {
                getGameWindow2D().displayMessage(event.toString());
            }
        });
    }

    public boolean canPerformRealTimeSanction() {
        return dataModel.isMonitor()
                || (dataModel.isSanctioningAllowed() && dataModel.getCurrentTokens() > 0);
    }

    public void transmit(PostRoundSanctionRequest request) {
        if (state == ClientState.WAITING) {
            // System.out.println("Sending post round sanction request");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    getGameWindow2D().showInstructionsPanel();
                }
            });
            super.transmit(request);
        }
    }

    /**
     * Utility class for throttling client-side messages.
     * 
     */
    private class MessageQueue implements Runnable {
        private final static int DEFAULT_MESSAGES_PER_SECOND = 10;

        private final LinkedList<Event> actions = new LinkedList<>();

        private final ArrayList<MovementEvent> batchedMovements = new ArrayList<>();

        private boolean running;

        private int messagesPerSecond = DEFAULT_MESSAGES_PER_SECOND;
        private int messagesSent;

        private int averageMessagesPerSecond;

        private Duration secondTick = Duration.create(1);

        public MessageQueue() {
            EventChannel channel = ForagingClient.this.getEventChannel();
            channel.add(this, new EventTypeProcessor<RealTimeSanctionRequest>(RealTimeSanctionRequest.class) {
                public void handle(RealTimeSanctionRequest event) {
                    add(event);
                }
            });
            channel.add(this, new EventTypeProcessor<ClientMovementRequest>(ClientMovementRequest.class) {
                public void handle(ClientMovementRequest request) {
                    if (isRoundInProgress()) {
                        add(request);
                    }
                }
            });
            channel.add(this, new EventTypeProcessor<CollectTokenRequest>(CollectTokenRequest.class) {
                public void handle(CollectTokenRequest request) {
                    if (isRoundInProgress()) {
                        transmit(request);
                    }
                }
            });
            channel.add(this, new EventTypeProcessor<ResetTokenDistributionRequest>(ResetTokenDistributionRequest.class) {
                public void handle(ResetTokenDistributionRequest event) {
                    if (isRoundInProgress() && dataModel.getRoundConfiguration().isPracticeRound()) {
                        transmit(event);
                    }
                }
            });
            channel.add(this, new EventTypeProcessor<MovementEvent>(MovementEvent.class) {
                public void handle(MovementEvent event) {
                    synchronized (batchedMovements) {
                        batchedMovements.add(event);
                    }
                }
            });
        }

        private void add(Event request) {
            if (messagesSent == 0 && actions.isEmpty()) {
                // first message this second, bypass the queue and send it right away.
                transmit(request);
                messagesSent++;
            } else if (messagesSent < messagesPerSecond) {
                actions.addLast(request);
            } else {
                // otherwise, discard the event (and notify the participant?)
                System.err.println("Discarding event: " + request + " - already sent " + messagesSent);
            }
        }

        public void start() {
            running = true;
            new Thread(this).start();
        }

        public void stop() {
            running = false;
            actions.clear();
            messagesSent = 0;
        }

        public void run() {
            getGameWindow().startRound();
            state = ClientState.RUNNING;
            secondTick.start();
            while (running) {
                Event request = get();
                if (request != null) {
                    transmit(request);
                }
                Utils.sleep(ForagingServer.SERVER_SLEEP_INTERVAL);
                Thread.yield();
            }
        }

        public Event get() {
            secondTick.onTick((duration) -> {
                messagesSent = 0;
                synchronized (batchedMovements) {
                    if (!batchedMovements.isEmpty()) {
                        transmit(new SinglePlayerUpdateRequest(getDataModel().getClientData(), batchedMovements));
                        batchedMovements.clear();
                    }
                }

            });
            if (actions.isEmpty()) {
                return null;
            }
            messagesSent++;
            return actions.removeFirst();
        }

        public int getEnergyLevel() {
            int energyLevel = messagesPerSecond - averageMessagesPerSecond;
            return energyLevel <= 0 ? 1 : energyLevel;
        }
    }

    public int getEnergyLevel() {
        return messageQueue.getEnergyLevel();
    }

    public ClientDataModel getDataModel() {
        return dataModel;
    }

    public RoundConfiguration getCurrentRoundConfiguration() {
        return dataModel.getRoundConfiguration();
    }

    public static void main(String[] args) {
        Runnable createGuiRunnable = new Runnable() {
            public void run() {
                // System.out.println("inside client");
                // Dimension defaultDimension = new Dimension(600, 600);
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Couldn't set native look and feel: " + e);
                }
                JFrame frame = new JFrame();
                ForagingClient client = new ForagingClient(new ServerConfiguration());
                client.connect();
                frame.setTitle("Client Window: " + client.getId());
                frame.add(client.clientPanel);
                UserInterfaceUtils.maximize(frame);
            }
        };
        SwingUtilities.invokeLater(createGuiRunnable);
    }

    public void sendTrustGameSubmissionRequest(double playerOneAmountToKeep, double[] playerTwoAmountsToKeep) {
        transmit(new TrustGameSubmissionRequest(getId(), playerOneAmountToKeep, playerTwoAmountsToKeep));
        // switch back to instructions window
        getGameWindow2D().trustGameSubmitted();
    }

    public void sendSurveyId(String surveyId) {
        getId().setSurveyId(surveyId);
        transmit(new SurveyIdSubmissionRequest(getId(), surveyId));
        getGameWindow2D().surveyIdSubmitted();
    }

    public void sendRuleVoteRequest(ForagingStrategy selectedRule) {
        if (selectedRule != null) {
            transmit(new RuleVoteRequest(getId(), selectedRule));
        }
        getGameWindow2D().strategyNominationSubmitted();
    }

    public boolean isRoundInProgress() {
        return state == ClientState.RUNNING;
    }
}
