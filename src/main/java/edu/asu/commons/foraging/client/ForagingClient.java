package edu.asu.commons.foraging.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.LinkedList;

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
import edu.asu.commons.event.SocketIdentifierUpdateRequest;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.conf.ServerConfiguration;
import edu.asu.commons.foraging.event.AgentInfoRequest;
import edu.asu.commons.foraging.event.BeginChatRoundRequest;
import edu.asu.commons.foraging.event.ClientMovementRequest;
import edu.asu.commons.foraging.event.ClientPositionUpdateEvent;
import edu.asu.commons.foraging.event.CollectTokenRequest;
import edu.asu.commons.foraging.event.EndRoundEvent;
import edu.asu.commons.foraging.event.LockResourceEvent;
import edu.asu.commons.foraging.event.PostRoundSanctionRequest;
import edu.asu.commons.foraging.event.PostRoundSanctionUpdateEvent;
import edu.asu.commons.foraging.event.RealTimeSanctionRequest;
import edu.asu.commons.foraging.event.ResetTokenDistributionRequest;
import edu.asu.commons.foraging.event.RoundStartedEvent;
import edu.asu.commons.foraging.event.ShowInstructionsRequest;
import edu.asu.commons.foraging.event.ShowTrustGameRequest;
import edu.asu.commons.foraging.event.SynchronizeClientEvent;
import edu.asu.commons.foraging.event.TrustGameSubmissionRequest;
import edu.asu.commons.net.SocketIdentifier;
import edu.asu.commons.util.Duration;



/**
 * $Id: ForagingClient.java 529 2010-08-17 00:08:01Z alllee $
 * 
 * Client for costly sanctioning experiments.  Encompasses both 2D and 3D.
 * 
 * @author <a href='mailto:mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 529 $
 */

public class ForagingClient extends BaseClient<ServerConfiguration> {

    enum ClientState {
        // not connected to the server at all
        UNCONNECTED, 
        // connected, but in between rounds
        WAITING, 
        // connected and currently running a round
        RUNNING
    };

    private ClientState state = ClientState.UNCONNECTED;

    private GameWindow2D gameWindow2D;
    
    private GameWindow3D gameWindow3D;
    
    private ClientDataModel dataModel;
    
    
    private MessageQueue messageQueue;
    
    private JPanel clientPanel = new JPanel();
    
    public ForagingClient(ServerConfiguration configuration) {
        this(configuration, new Dimension(900, 800));
    }
    
    public ForagingClient(ServerConfiguration configuration, Dimension screenSize) {
        super(configuration);
        dataModel = new ClientDataModel(this);
        clientPanel.setLayout(new BorderLayout());
        if (configuration.shouldInitialize2D()) {
            gameWindow2D = new GameWindow2D(this, screenSize);
            clientPanel.add(gameWindow2D.getPanel(), BorderLayout.CENTER);    
        }
        else if (configuration.shouldInitialize3D()) {
            gameWindow3D = new GameWindow3D(this);
            clientPanel.add(gameWindow3D.getPanel(), BorderLayout.CENTER);    
        }
    }
    

    @Override
    protected void postConnect() {
        SocketIdentifier socketId = (SocketIdentifier) getId();
        transmit(new SocketIdentifierUpdateRequest(socketId, socketId.getStationNumber()));
        state = ClientState.WAITING;
    }

    public GameWindow2D getGameWindow2D() {
        return gameWindow2D;
    }
    
    public GameWindow3D getGameWindow3D() {
        return gameWindow3D;
    }
    
    public GameWindow getGameWindow() {
        if (dataModel.is2dExperiment()) {
            return gameWindow2D;
        }
        else {
            return gameWindow3D;            
        }
    }

    public void sendAvatarInfo(boolean male, Color hairColor, Color skinColor, Color shirtColor, Color trouserColor, Color shoesColor) {
        transmit(new AgentInfoRequest(getId(), male, hairColor, skinColor, shirtColor, trouserColor, shoesColor));
        gameWindow3D.removeAgentDesigner();
    }

    public void sendAgentInfo(Color color) {
        transmit(new AgentInfoRequest(getId(), color));
        gameWindow3D.removeAgentDesigner();
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
                        clientPanel.removeAll();
                        if (dataModel.is2dExperiment()) {
                            clientPanel.add(gameWindow2D.getPanel(), BorderLayout.CENTER);
                            if (gameWindow3D != null) {
                                gameWindow3D.dispose();
                            }
                        }
                        else {
                            clientPanel.add(gameWindow3D.getPanel(), BorderLayout.CENTER);
                        }
                        clientPanel.revalidate();
                        clientPanel.repaint();
                        getGameWindow().init();
                    }
                });

            }
        });
        
        addEventProcessor(new EventTypeProcessor<ShowInstructionsRequest>(ShowInstructionsRequest.class) {
            public void handle(ShowInstructionsRequest request) {
                getGameWindow().showInstructions();
            }
        });
        addEventProcessor(new EventTypeProcessor<ShowTrustGameRequest>(ShowTrustGameRequest.class) {
            public void handle(ShowTrustGameRequest request) {
                getGameWindow().showTrustGame();
            }
        });
        addEventProcessor(new EventTypeProcessor<RoundStartedEvent>(RoundStartedEvent.class) {
            public void handle(RoundStartedEvent event) {
                System.err.println("client starting round: " + dataModel.is2dExperiment());
                dataModel.initialize(event.getGroupDataModel());
                setId(event.getId());
                getGameWindow().startRound();
                if (dataModel.is2dExperiment()) {
                    messageQueue.start();
                }
                state = ClientState.RUNNING;
            }
        });

        addEventProcessor(new EventTypeProcessor<EndRoundEvent>(EndRoundEvent.class) {
            public void handle(final EndRoundEvent event) {
                if (state == ClientState.RUNNING) {
                    dataModel.setGroupDataModel(event.getGroupDataModel());
                    getGameWindow().endRound(event);
                    if (dataModel.is2dExperiment()) {
                        messageQueue.stop();
                    }
                    state = ClientState.WAITING;
                }
            }
        });
        addEventProcessor(new EventTypeProcessor<SynchronizeClientEvent>(SynchronizeClientEvent.class) {
            public void handle(SynchronizeClientEvent event) {
                dataModel.setGroupDataModel(event.getGroupDataModel());
                if (dataModel.is2dExperiment()) {
                    dataModel.update(event, gameWindow2D);
                }
                getGameWindow().update(event.getTimeLeft());
            }
        });
        initialize2DEventProcessors();
        initialize3DEventProcessors();
        messageQueue = new MessageQueue();
    }
    
    private void initialize3DEventProcessors() {
        addEventProcessor(new EventTypeProcessor<LockResourceEvent>(LockResourceEvent.class) {
            public void handle(LockResourceEvent event) {
                // tell the game window to highlight the appropriate resource
                gameWindow3D.highlightResource(event);
            }
        });
    }
    
    private void initialize2DEventProcessors() {
        addEventProcessor(new EventTypeProcessor<BeginChatRoundRequest>(BeginChatRoundRequest.class) {
            public void handle(BeginChatRoundRequest request) {
                dataModel.initialize(request.getGroupDataModel());
                gameWindow2D.initializeChatPanel();
            }
        });
        addEventProcessor(new EventTypeProcessor<PostRoundSanctionUpdateEvent>(PostRoundSanctionUpdateEvent.class) {
            public void handle(PostRoundSanctionUpdateEvent event) {
                gameWindow2D.updateDebriefing(event);
            }
        });
        addEventProcessor(new EventTypeProcessor<ClientPositionUpdateEvent>(ClientPositionUpdateEvent.class) {
            public void handle(ClientPositionUpdateEvent event) {
                if (state == ClientState.RUNNING) {
                    dataModel.updateDiffs(event, gameWindow2D);
                }
            }
        });

        addEventProcessor(new EventTypeProcessor<ClientMessageEvent>(ClientMessageEvent.class) {
            public void handle(ClientMessageEvent event) {
                gameWindow2D.displayMessage(event.toString());
            }
        });
//        addEventProcessor(new EventTypeProcessor<EnforcementMechanismUpdateEvent>(EnforcementMechanismUpdateEvent.class) {
//            public void handle(final EnforcementMechanismUpdateEvent event) {
//            	dataModel.setGroupDataModel(event.getGroupDataModel());
//                gameWindow2D.displayActiveEnforcementMechanism();
//            }
//        });
//        addEventProcessor(new EventTypeProcessor<RegulationSubmissionUpdateEvent>(RegulationSubmissionUpdateEvent.class) {
//            public void handle(final RegulationSubmissionUpdateEvent event) {
//            	dataModel.setGroupDataModel(event.getGroupDataModel());
//                gameWindow2D.initializeRegulationVotingPanel();
//            }
//        });
//        addEventProcessor(new EventTypeProcessor<RegulationUpdateEvent>(RegulationUpdateEvent.class) {
//            public void handle(final RegulationUpdateEvent event) {
//            	dataModel.setActiveRegulation(event.getRegulationData());
//            	gameWindow2D.displayActiveRegulation();
//            }
//        });
//        
//        addEventProcessor(new EventTypeProcessor<SanctionUpdateEvent>(SanctionUpdateEvent.class) {
//            public void handle(final SanctionUpdateEvent event) {
//            	dataModel.setGroupDataModel(event.getGroupDataModel());
//            	gameWindow2D.displaySanctionMechanism();
//            }
//        });
    }

    public boolean canPerformRealTimeSanction() {
    	return dataModel.isMonitor() 
    		|| (dataModel.isSanctioningAllowed() && dataModel.getCurrentTokens() > 0);    	
    }
    
    public void transmit(PostRoundSanctionRequest request) {
        if (state == ClientState.WAITING) {
        	//System.out.println("Sending post round sanction request");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    gameWindow2D.switchInstructionsPane();        
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

        private final LinkedList<Event> actions =
            new LinkedList<Event>();
        
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
                    add(request);
                }
        	});
        	channel.add(this, new EventTypeProcessor<CollectTokenRequest>(CollectTokenRequest.class) {
        	    public void handle(CollectTokenRequest request) {
        	        if (state == ClientState.RUNNING) {
        	            transmit(request);
        	        }
        	    }
        	});
        	channel.add(this, new EventTypeProcessor<ResetTokenDistributionRequest>(ResetTokenDistributionRequest.class) {
                public void handle(ResetTokenDistributionRequest event) {
                    if (state == ClientState.RUNNING && dataModel.getRoundConfiguration().isPracticeRound()) {
                        transmit(event);
                    }
                }
        	});
        }
        
        private void add(Event request) {
            if (messagesSent == 0 && actions.isEmpty()) {
                // first message this second, bypass the queue and send it right away.
//                moveClient(request);
                transmit(request);
                messagesSent++;
            }
            else if ( messagesSent < messagesPerSecond ) {
                actions.addLast(request);
            }
            else {
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
            secondTick.start();
            while (running) {
                Event request = get();
                if (request != null) {
//                    moveClient(request);
                    transmit(request);
                }
//                Utils.sleep(100);
                Thread.yield();
            }
        }

        private void tick() {
            if (secondTick.hasExpired()) {
                secondTick.restart();
                messagesSent = 0;
            }
        }

        public Event get() {
            tick();
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
            	//System.out.println("inside client");
                //Dimension defaultDimension = new Dimension(600, 600);
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } 
                catch(Exception e) {
                    e.printStackTrace();
                    System.err.println("Couldn't set native look and feel: "+ e);
                }
                Dimension defaultDimension = new Dimension(1024, 768);
            	JFrame frame = new JFrame();
                ForagingClient client = new ForagingClient(new ServerConfiguration(), defaultDimension);
                client.connect();
                frame.setTitle("Client Window: " + client.getId());
                frame.add(client.clientPanel);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setPreferredSize(defaultDimension);
                frame.setVisible(true);
                frame.pack();
            }
        };
        SwingUtilities.invokeLater(createGuiRunnable);
    }

    public void sendTrustGameSubmissionRequest(double playerOneAmountToKeep, double[] playerTwoAmountsToKeep) {
        transmit(new TrustGameSubmissionRequest(getId(), playerOneAmountToKeep, playerTwoAmountsToKeep));
    }
}
