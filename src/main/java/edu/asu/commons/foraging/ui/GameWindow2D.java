package edu.asu.commons.foraging.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import edu.asu.commons.event.ClientReadyEvent;
import edu.asu.commons.event.Event;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.foraging.client.ClientDataModel;
import edu.asu.commons.foraging.client.ForagingClient;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.ClientMovementRequest;
import edu.asu.commons.foraging.event.CollectTokenRequest;
import edu.asu.commons.foraging.event.EndRoundEvent;
import edu.asu.commons.foraging.event.PostRoundSanctionUpdateEvent;
import edu.asu.commons.foraging.event.QuizResponseEvent;
import edu.asu.commons.foraging.event.RealTimeSanctionRequest;
import edu.asu.commons.foraging.event.ResetTokenDistributionRequest;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.Direction;
import edu.asu.commons.foraging.rules.Strategy;
import edu.asu.commons.foraging.rules.iu.ForagingStrategy;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.ui.HtmlEditorPane;
import edu.asu.commons.ui.UserInterfaceUtils;
import edu.asu.commons.util.Duration;

/**
 * $Id$
 * 
 * Primary client-side view for foraging experiment that can be used by standalone Java applications or Applets.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class GameWindow2D implements GameWindow {
    private final static String INSTRUCTIONS_PANEL_NAME = "instructions screen panel";
    private final static String GAME_PANEL_NAME = "foraging game panel";
    private final static String TRUST_GAME_PANEL_NAME = "trust game panel";
    private final static String CHAT_PANEL_NAME = "standalone chat panel";
    private final static String POST_ROUND_SANCTIONING_PANEL_NAME = "post round sanctioning panel";
    private final static String SURVEY_ID_PANEL_NAME = "survey id panel";
    private String currentCardPanel = INSTRUCTIONS_PANEL_NAME;
    
    private final StringBuilder instructionsBuilder = new StringBuilder();
    private final ClientDataModel dataModel;
    private EventChannel channel;

    private JPanel mainPanel;
    // instructions components.
    private JScrollPane instructionsScrollPane;
    private HtmlEditorPane instructionsEditorPane;

    private JPanel messagePanel;
    private JScrollPane messageScrollPane;
    private JTextPane messageTextPane;

    private JPanel labelPanel;
    
    private JPanel surveyIdPanel;

    private ChatPanel chatPanel;

    private JLabel informationLabel;

    private JLabel timeLeftLabel;

    private JPanel gamePanel;

    private ForagingClient client;

    private SubjectView subjectView;

    private CardLayout cardLayout;

    private ChatPanel inRoundChatPanel;
    
    private Timer timer;
    
    // voting components
    private JPanel votingPanel;
    private VotingForm votingForm;
    private HtmlEditorPane votingInstructionsEditorPane;
    private JScrollPane votingInstructionsScrollPane;

    // private EnergyLevel energyLevel;

    public GameWindow2D(ForagingClient client) {
        this.client = client;
        this.dataModel = client.getDataModel();
        // FIXME: set the actual screen size dimensions after this JPanel has been initialized...
        this.channel = client.getEventChannel();
        // feed subject view the available screen size so that
        // it can adjust appropriately when given a board size
        // int width = (int) Math.min(Math.floor(size.getWidth()), Math.floor(size.getHeight() * 0.85));
        initGuiComponents();
    }

    /**
     * Instead of invoking specific update methods we invoke just a single
     * method, update() after we're done changing state.
     */
    public void update(final long roundTimeLeft) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                informationLabel.setText(getInformationLabelText());
                timeLeftLabel.setText(getTimeLeftLabelText(roundTimeLeft));
                // FIXME: subjectView.repaint() causes graphical glitches here
                // only when we transition from 3D -> 2D experiment. Find out why.
                getPanel().repaint();
            }
        });
    }

    /**
     * In certain cases, init() _can_ be called before endRound() is finished. Need to lock
     * access!
     * 
     * @param event
     */
    public synchronized void init() {
        final RoundConfiguration roundConfiguration = dataModel.getRoundConfiguration();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (roundConfiguration.isFirstRound()) {
                    if (roundConfiguration.getParentConfiguration().shouldAskForSurveyId()) {
                        add(getSurveyIdPanel());
                        showPanel(SurveyIdPanel.NAME);
                    }
                    else {
                        setInstructions(roundConfiguration.getWelcomeInstructions());
                    }
                }
                // don't display next round time, instead wait for the
                // facilitator signal.
                timeLeftLabel.setText("Waiting for facilitator's signal.");
                informationLabel.setText("Waiting for facilitator's signal.");
                // add the next round instructions to the existing debriefing text set by the previous
                // EndRoundEvent.
            }
        });


    }

    private void setQuestionColors(List<String> questionNumbers, String color) {
        HTMLEditorKit editorKit = (HTMLEditorKit) instructionsEditorPane.getEditorKit();
        StyleSheet styleSheet = editorKit.getStyleSheet();
        for (String questionNumber : questionNumbers) {
            String styleString = String.format(".%s { color: %s; }", questionNumber, color);
            styleSheet.addRule(styleString);
        }
    }
    
    private ActionListener createClientReadyListener(final String confirmationMessage) {
    	return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    int selectedOption = JOptionPane.showConfirmDialog(getPanel(), 
			            confirmationMessage, 
			            "Continue?", JOptionPane.YES_NO_OPTION);
			    switch (selectedOption) {
			        case JOptionPane.YES_OPTION:
		                setInstructions(dataModel.getExperimentConfiguration().getWaitingRoomInstructions());
		                showInstructionsPanel();
		                client.transmit(new ClientReadyEvent(client.getId(), confirmationMessage));
		                instructionsEditorPane.setActionListener(null);
		                break;
			        default:
			            break;
			    }
			}
    	};
    }

    private ActionListener createQuizListener(final RoundConfiguration configuration) {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                HtmlEditorPane.FormActionEvent formEvent = (HtmlEditorPane.FormActionEvent) e;
                Properties actualAnswers = formEvent.getData();
                List<String> incorrectQuestionNumbers = new ArrayList<String>();
                List<String> correctAnswers = new ArrayList<String>();

                // iterate through expected answers
                Map<String, String> quizAnswers = configuration.getQuizAnswers();
                for (Map.Entry<String, String> entry : quizAnswers.entrySet()) {
                    String questionNumber = entry.getKey();
                    String expectedAnswer = entry.getValue();
                    String actualAnswer = actualAnswers.getProperty(questionNumber);
                    if (actualAnswer == null) {
                    	JOptionPane.showMessageDialog(getPanel(), "Please enter a quiz answer for question " + questionNumber.toUpperCase() + ".");
                    	return;
                    }
                    if (expectedAnswer.equals(actualAnswer)) {
                        correctAnswers.add(questionNumber);
                    }
                    else {
                        // flag the incorrect response
                        incorrectQuestionNumbers.add(questionNumber);
                    }
                }
                client.transmit(new QuizResponseEvent(client.getId(), actualAnswers, incorrectQuestionNumbers));
                setQuestionColors(correctAnswers, "blue");
                setQuestionColors(incorrectQuestionNumbers, "red");
                // RoundConfiguration now builds the appropriate quiz results page.
                StringBuilder builder = new StringBuilder(configuration.getQuizResults(incorrectQuestionNumbers, actualAnswers));
                configuration.buildInstructions(builder);
                setInstructions(builder.toString());
            }
        };
    }

    /**
     * Invoked when a subject collected a token at Point p.
     * 
     * @param position
     */
    public void collectTokens(Point ... positions) {
        subjectView.collectTokens(positions);
    }

    private void startChatTimer() {
        if (timer == null) {
            final RoundConfiguration roundConfiguration = dataModel.getRoundConfiguration();
            final Duration duration = Duration.create(roundConfiguration.getChatDuration());
            timer = new Timer(1000, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (duration.hasExpired()) {
                        timeLeftLabel.setText("Chat is now disabled.");
                        timer.stop();
                        timer = null;
                    }
                    else {
                        timeLeftLabel.setText(String.format("Chat will end in %d seconds.", duration.getTimeLeft() / 1000L));
                    }
                }
            });
            timer.start();
        }
    }

    private String getInformationLabelText() {
        if (dataModel.getRoundConfiguration().shouldDisplayGroupTokens()) {
            StringBuilder builder = new StringBuilder("Tokens collected:");
            // XXX: use this method so that we get the proper ordering of client ids/assigned numbers..
//            Map<Identifier, ClientData> clientDataMap = dataModel.getClientDataMap();
            Point clientPosition = dataModel.getCurrentPosition();

            // FIXME: refactor this ugliness. 
            for (Identifier id : dataModel.getAllClientIdentifiers()) {
//                ClientData clientData = clientDataMap.get(id);
                String formatString = "";
                if (id.equals(dataModel.getId())) {
                    formatString = " [%d (you) : %d] ";
                    builder.append(String.format(formatString, dataModel.getAssignedNumber(id), dataModel.getCurrentTokens(id)));
                }
                else {
                    if (!dataModel.getRoundConfiguration().isFieldOfVisionEnabled()) {
                        formatString = " [%d : %d] ";
                        builder.append(String.format(formatString, dataModel.getAssignedNumber(id), dataModel.getCurrentTokens(id)));
                    }
                    else {
                        double radius = dataModel.getRoundConfiguration().getViewSubjectsRadius();
                        Circle fieldOfVision = new Circle(clientPosition, radius);
                        if (fieldOfVision.contains(dataModel.getClientPosition(id))) {
                            formatString = " [%d : %d] ";
                            builder.append(String.format(formatString, dataModel.getAssignedNumber(id), dataModel.getCurrentTokens(id)));
                        }
                        else {
                            formatString = " [%d : XX] ";
                            builder.append(String.format(formatString, dataModel.getAssignedNumber(id)));
                        }
                    }
                }
            }
            return builder.toString();
        }
        else {
            int tokensConsumed = dataModel.getCurrentTokens();
            return String.format("Income: %s  |  Tokens collected: %d     ",
                    NumberFormat.getCurrencyInstance().format(getIncome(tokensConsumed)),
                    tokensConsumed);
        }
    }

    private String getTimeLeftLabelText(long roundTimeLeft) {
    	return String.format("Time left: %d second(s)", roundTimeLeft / 1000L);
    }

    private void setInstructions(String s) {
//        System.err.println("Setting instructions to " + s);
        instructionsEditorPane.setText(s);
        instructionsEditorPane.repaint();
        getPanel().repaint();
    }

    private void initGuiComponents() {
        // FIXME: replace with CardLayout for easier switching between panels
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        // default sized subject view
        Dimension subjectViewSize = new Dimension(768, 768);
        subjectView = new SubjectView(subjectViewSize, dataModel);

        // add instructions panel card
        instructionsEditorPane = UserInterfaceUtils.createInstructionsEditorPane();
        instructionsScrollPane = new JScrollPane(instructionsEditorPane);
        instructionsScrollPane.setDoubleBuffered(true);
        instructionsScrollPane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
        instructionsScrollPane.setName(INSTRUCTIONS_PANEL_NAME);
        add(instructionsScrollPane);

        // FIXME: use a more flexible LayoutManager so that in-round chat isn't squeezed all the way on the right
        // side of the screen.
        gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBackground(Color.WHITE);
        gamePanel.setName(GAME_PANEL_NAME);
        gamePanel.add(subjectView, BorderLayout.CENTER);
        // add labels to game panel
        // FIXME: replace with progress bar.
        timeLeftLabel = new JLabel("Connecting ...");
        timeLeftLabel.setFont(UserInterfaceUtils.DEFAULT_BOLD_FONT);
        informationLabel = new JLabel("Tokens collected: 0     ");
        informationLabel.setFont(UserInterfaceUtils.DEFAULT_BOLD_FONT);
        // latencyLabel = new JLabel("Latency: 0");
        informationLabel.setBackground(Color.YELLOW);
        informationLabel.setForeground(Color.BLUE);

        labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
        labelPanel.setBackground(Color.WHITE);
        labelPanel.add(timeLeftLabel);
        labelPanel.add(Box.createHorizontalGlue());
        labelPanel.add(informationLabel);
        gamePanel.add(labelPanel, BorderLayout.NORTH);

        // add message window.
        messagePanel = new JPanel(new BorderLayout());
        // messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.add(new JLabel("Messages"), BorderLayout.NORTH);
        // FIXME: setFont doesn't work here the way we want it to.
        messageTextPane = new JTextPane();
        messageTextPane.setEditable(false);
        messageTextPane.setFont(UserInterfaceUtils.DEFAULT_BOLD_FONT);
        messageTextPane.setBackground(Color.WHITE);
        addStyles(messageTextPane.getStyledDocument());
        messageScrollPane = new JScrollPane(messageTextPane);
        Dimension scrollPaneSize = new Dimension(getPanel().getPreferredSize().width, 60);
        messageScrollPane.setMinimumSize(scrollPaneSize);
        messageScrollPane.setPreferredSize(scrollPaneSize);
        messageScrollPane.setMaximumSize(scrollPaneSize);
        messagePanel.add(messageScrollPane, BorderLayout.CENTER);
        gamePanel.add(messagePanel, BorderLayout.SOUTH);

        add(gamePanel);

        mainPanel.addKeyListener(createGameWindowKeyListener());
        mainPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                mainPanel.requestFocusInWindow();
            }
        });

        // resize listener
        mainPanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent event) {
                Component component = event.getComponent();
                Dimension screenSize = new Dimension(component.getWidth(), (int) (component.getHeight() * 0.85d));
                subjectView.setScreenSize(screenSize);
                subjectView.setImageSizes();
                getPanel().revalidate();
                showPanel(currentCardPanel);
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                mainPanel.requestFocusInWindow();
            }
        });
    }

    /**
     * IMPORTANT: this method handles client keyboard inputs within the game.
     * 
     * @return
     */
    private KeyAdapter createGameWindowKeyListener() {
        return new KeyAdapter() {
            private volatile boolean keyReleased;

            // FIXME: the keyReleased/keyPressed stuff here only seems to work on Windows.
            // Linux keyboards generate pairs of keyPressed/keyReleased events in tandem even
            // when you are keeping the key down the whole time!
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                keyReleased = true;
            }

            // FIXME: refactor this method if possible.
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (! client.isRoundInProgress()) {
                    return;
                }
                int keyChar = (int) keyEvent.getKeyChar();
                int keyCode = keyEvent.getKeyCode();
                Event event = null;
                // directions are the most common action, check for them first.
                Direction direction = Direction.valueOf(keyCode);
                if (direction == null) {
                    // check to see if the key is something else.
                    switch (keyCode) {
                    // token request handling
                        case KeyEvent.VK_SPACE:
                            try {
                                if (dataModel.isHarvestingAllowed()) {
                                    event = new CollectTokenRequest(client.getId());
                                }
                                else {
                                    displayErrorMessage("You cannot harvest at this time.");
                                }
                            }
                            catch (RuntimeException exception) {
                                displayErrorMessage("You cannot harvest at this time");
                            }
                            break;
                        // real-time sanctioning keycode handling
                        case KeyEvent.VK_1:
                        case KeyEvent.VK_2:
                        case KeyEvent.VK_3:
                        case KeyEvent.VK_4:
                        case KeyEvent.VK_5:
                        case KeyEvent.VK_6:
                        case KeyEvent.VK_7:
                        case KeyEvent.VK_8:
                        case KeyEvent.VK_9:
                            if (!dataModel.isSanctioningAllowed()) {
                                // FIXME: get rid of magic constants
//                                displayErrorMessage("You may not reduce other participants tokens at this time.");
                                return;
                            }
                            if (client.canPerformRealTimeSanction()) {
                                // System.out.println("Can do sanctioning");
                                int assignedNumber = keyChar - 48;
                                Identifier sanctionee = dataModel.getClientId(assignedNumber);
                                System.err.println("Punishing : " + sanctionee);
                                if (sanctionee == null || sanctionee.equals(dataModel.getId())) {
                                    // don't allow self-flagellation :-).
                                    return;
                                }
                                // only allow sanctions for subjects within this subject's field of vision
                                Point subjectPosition = dataModel.getClientPosition(sanctionee);
                                if (dataModel.getClientData().isSubjectInFieldOfVision(subjectPosition)) {
                                    event = new RealTimeSanctionRequest(dataModel.getId(), sanctionee);
                                    System.out.println("sending sanctioning event : " + event);
                                }
                                else {
                                    displayErrorMessage("The participant is out of range.");
                                    return;
                                }
                            }
                            break;
                        // reset token distribution request handling
                        case KeyEvent.VK_ENTER:
                            if (dataModel.getRoundConfiguration().isInRoundChatEnabled()) {
                                getInRoundChatPanel().setTextFieldFocus();
                            }
                            return;
                        case KeyEvent.VK_R:
                            if (canResetTokenDistribution()) {
                                event = new ResetTokenDistributionRequest(client.getId());
                            }
                            else
                                return;
                            break;
                        case KeyEvent.VK_M:
                            if (!dataModel.getRoundConfiguration().isAlwaysInExplicitCollectionMode()) {
                                dataModel.toggleExplicitCollectionMode();
                            }
                            return;
                        default:
                            System.err.println("Invalid input:" + KeyEvent.getKeyText(keyCode));
                    }
                }
                else {
                    event = new ClientMovementRequest(client.getId(), direction);
                    // move the client directly, this may get overridden later by a client update.
                    /*
                     * if (dataModel.getRoundConfiguration().isAlwaysInExplicitCollectionMode()) {
                     * Point newPosition = direction.apply(dataModel.getCurrentPosition());
                     * dataModel.getClientData().setPosition(newPosition);
                     * subjectView.repaint();
                     * }
                     */
                }
                if (keyReleased) {
                    // FIXME: have client directly render these requests? Would
                    // make the app more "responsive" and less tied to server latency.
                    channel.handle(event);
                    keyReleased = false;
                }
            }
        };

    }

    private boolean canResetTokenDistribution() {
        RoundConfiguration configuration = dataModel.getRoundConfiguration();
        return configuration.isPracticeRound() && configuration.isPrivateProperty();
    }

    // public void addCenterComponent(Component newCenterComponent) {
    // if (currentCenterComponent != null) {
    // currentCenterComponent.setVisible(false);
    // getPanel().remove(currentCenterComponent);
    // getPanel().add(newCenterComponent, BorderLayout.CENTER);
    // newCenterComponent.setVisible(true);
    // }
    // currentCenterComponent = newCenterComponent;
    // getPanel().revalidate();
    // getPanel().repaint();
    // }

    public synchronized void startRound() {
        final RoundConfiguration configuration = dataModel.getRoundConfiguration();
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        // currentExperimentConfiguration = configuration;
        Runnable runnable = new Runnable() {
            public void run() {
                subjectView.setup(configuration);
                // reset the amount of time left in the round on food eaten
                // label to the value from the configuration file.
                // this is NOT dynamic; once the StartRoundEvent is fired off
                // by the server no new clients can connect because the round
                // has begun.
                update(configuration.getRoundDuration().getTimeLeft());
                if (configuration.isInRoundChatEnabled()) {
                    // FIXME: use separate chat panel for in round chat
                    System.err.println("in round chat was enabled");
                    ChatPanel chatPanel = getInRoundChatPanel();
                    chatPanel.initialize(dataModel);
                    Dimension chatPanelSize = new Dimension(250, getPanel().getSize().height);
                    chatPanel.setPreferredSize(chatPanelSize);
                    // FIXME: switch to different layout manager
                    gamePanel.add(chatPanel, BorderLayout.EAST);
                }
                showPanel(GAME_PANEL_NAME);
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    public void displayErrorMessage(String errorMessage) {
        displayMessage(errorMessage, Color.RED);
    }

    public void displayMessage(String message) {
        displayMessage(message, Color.BLACK);
    }

    public void displayMessage(String errorMessage, Color color) {
        messageTextPane.setForeground(color);
        StyledDocument document = messageTextPane.getStyledDocument();
        try {
            document.insertString(document.getLength(), errorMessage + "\n", document.getStyle("bold"));
            messageTextPane.setCaretPosition(document.getLength());
        }
        catch (BadLocationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // FIXME: add to some common GUI package?
    private void addStyles(StyledDocument styledDocument) {
        // and why not have something like... StyleContext.getDefaultStyle() to
        // replace this junk
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(
                StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(defaultStyle, "Helvetica");
        StyleConstants.setBold(styledDocument.addStyle("bold", defaultStyle),
                true);
        StyleConstants.setItalic(styledDocument
                .addStyle("italic", defaultStyle), true);
    }

    private double getIncome(int numTokens) {
        return dataModel.getRoundConfiguration().tokensToDollars(numTokens);
    }
    
    public void showDebriefing(ClientData clientData, boolean showExitInstructions) {
        instructionsBuilder.delete(0, instructionsBuilder.length());
        instructionsBuilder.append(dataModel.getRoundConfiguration().generateClientDebriefing(clientData, showExitInstructions));
        setInstructions(instructionsBuilder.toString());
    }

    // FIXME: replace with StringTemplate
    private void postSanctionDebriefingText(final PostRoundSanctionUpdateEvent event) {
//        instructionsBuilder.delete(0, instructionsBuilder.length());
//        ClientData clientData = event.getClientData();
//        // FIXME: split into tokens used to sanction others and tokens taken
//        // away by other people.
//        instructionsBuilder.append(
//                String.format("<h3>Your statistics from the last round have been updated as follows:</h3>" +
//                        "<ul>" +
//                        "<li>Tokens collected last round: %d</li>" +
//                        "<li>Tokens subtracted by other players: %d</li>" +
//                        "<li>Tokens used to subtract tokens from other players: %d</li>" +
//                        "<li>Net earned tokens in the last round: %d</li>" +
//                        "<li>Net income from the last round: $%3.2f</li>" +
//                        "</ul>",
//                        clientData.getTokensCollectedLastRound(),
//                        clientData.getSanctionPenalties(),
//                        clientData.getSanctionCosts(),
//                        clientData.getCurrentTokens(),
//                        getIncome(clientData.getCurrentTokens()))
//                );
//        instructionsBuilder.append(String.format("Your <b>total income</b> so far is: $%3.2f<hr>",
//                getIncome(clientData.getTotalTokens())));
//        if (event.isLastRound()) {
//            instructionsBuilder.append(client.getDataModel().getLastRoundDebriefing());
//        }
//        setInstructions(instructionsBuilder.toString());

    }

    private ChatPanel getChatPanel() {
        if (chatPanel == null) {
            chatPanel = new ChatPanel(client);
            chatPanel.setName(CHAT_PANEL_NAME);
            add(chatPanel);
        }
        return chatPanel;
    }
    
    private ChatPanel getInRoundChatPanel() {
        if (inRoundChatPanel == null) {
            inRoundChatPanel = new ChatPanel(client, true);
        }
        return inRoundChatPanel;
    }

    public void showTrustGame() {
    	final RoundConfiguration roundConfiguration = dataModel.getRoundConfiguration();
        if (roundConfiguration.isTrustGameEnabled()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    JEditorPane trustGameInstructionsEditorPane = UserInterfaceUtils.createInstructionsEditorPane();
                    JScrollPane scrollPane = new JScrollPane(trustGameInstructionsEditorPane);
                    trustGameInstructionsEditorPane.setText(roundConfiguration.getTrustGameInstructions());
                    panel.add(scrollPane);
                    TrustGamePanel trustGamePanel = new TrustGamePanel(client);
                    JScrollPane trustGameScrollPane = new JScrollPane(trustGamePanel);
                    panel.add(trustGameScrollPane);
                    panel.setName(TRUST_GAME_PANEL_NAME);
                    add(panel);
                    showPanel(TRUST_GAME_PANEL_NAME);
                }
            });
        }
    }
    
    public void trustGameSubmitted() {
    	// FIXME: replace HTML strings with configuration template
        instructionsBuilder.append("<h3>Submission successful</h3><hr><p>Please wait while the rest of the submissions are gathered.</p>");
        setInstructions(instructionsBuilder.toString());
        showInstructionsPanel();
    }

    public void showInstructions() {
        final RoundConfiguration roundConfiguration = dataModel.getRoundConfiguration();
        instructionsBuilder.delete(0, instructionsBuilder.length());
        roundConfiguration.buildAllInstructions(instructionsBuilder);
        // and add the quiz instructions if the quiz is enabled.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (roundConfiguration.isQuizEnabled()) {
                    instructionsEditorPane.setActionListener(null);
                    instructionsEditorPane.setActionListener(createQuizListener(roundConfiguration));
                }
                setInstructions(instructionsBuilder.toString());
                showInstructionsPanel();
                instructionsEditorPane.setCaretPosition(0);
            }
        });
    }
    

    public void showInitialVotingInstructions() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
//            	instructionsEditorPane.setActionListener(null);
//            	instructionsEditorPane.setActionListener(createClientReadyListener("Are you ready to submit your nominations?"));
                setInstructions(dataModel.getRoundConfiguration().getInitialVotingInstructions());
                showInstructionsPanel();
            }
        });
    }
    
    public void showVotingScreen() {
        if (votingPanel == null) {
            votingPanel = new JPanel();
            votingPanel.setLayout(new BoxLayout(votingPanel, BoxLayout.Y_AXIS));
            votingInstructionsEditorPane = UserInterfaceUtils.createInstructionsEditorPane();
            votingInstructionsScrollPane = new JScrollPane(votingInstructionsEditorPane);
            votingInstructionsEditorPane.setText(client.getCurrentRoundConfiguration().getVotingInstructions());
            votingPanel.add(votingInstructionsScrollPane);
            votingForm = new VotingForm(client);
            votingPanel.add(votingForm);
            votingPanel.setName(VotingForm.NAME);
            add(votingPanel);
        }
        showPanel(VotingForm.NAME);
    }

    public void showVotingResults(final List<ForagingStrategy> selectedRules, final Map<ForagingStrategy, Integer> votingResults) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                votingPanel.removeAll();
                votingPanel.add(votingInstructionsScrollPane);
                votingPanel.revalidate();
                RoundConfiguration currentRoundConfiguration = client.getCurrentRoundConfiguration();
                votingInstructionsEditorPane.setText(currentRoundConfiguration.generateVotingResults(selectedRules, votingResults));
                showPanel(VotingForm.NAME);
            }
        });
    }
    
    public void showSurveyInstructions() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	instructionsEditorPane.setActionListener(null);
            	instructionsEditorPane.setActionListener(createClientReadyListener(dataModel.getRoundConfiguration().getSurveyConfirmationMessage()));
                setInstructions(dataModel.getRoundConfiguration().getSurveyInstructions(dataModel.getId()));
                showInstructionsPanel();
            }
        });
    }

    public void showInstructionsPanel() {
        showPanel(INSTRUCTIONS_PANEL_NAME);
    }

    private void showPanel(final String panelName) {
        this.currentCardPanel = panelName;
        JPanel panel = getPanel();
        cardLayout.show(panel, panelName);
        panel.repaint();
        mainPanel.requestFocusInWindow();
    }

    public void updateDebriefing(final PostRoundSanctionUpdateEvent event) {
        Runnable runnable = new Runnable() {
            public void run() {
                postSanctionDebriefingText(event);
                showInstructionsPanel();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    public void showExitInstructions() {
        showDebriefing(dataModel.getClientData(), true);
        showInstructionsPanel();
    }

    public synchronized void endRound(final EndRoundEvent event) {
        Runnable runnable = new Runnable() {
            public void run() {
                if (inRoundChatPanel != null) {
                    getPanel().remove(inRoundChatPanel);
//                    inRoundChatPanel = null;
                }
                RoundConfiguration roundConfiguration = dataModel.getRoundConfiguration();
                if (roundConfiguration.isPostRoundSanctioningEnabled()) {
                    // add sanctioning text and slap the PostRoundSanctioningPanel in
                    PostRoundSanctioningPanel panel = new PostRoundSanctioningPanel(event, roundConfiguration, client);
                    panel.setName(POST_ROUND_SANCTIONING_PANEL_NAME);
                    add(panel);
                    showPanel(POST_ROUND_SANCTIONING_PANEL_NAME);
                }
                else {
                    instructionsEditorPane.setText("Waiting for updated round totals from the server...");
                    showInstructionsPanel();
                }
                showDebriefing(event.getClientData(), false);
            }
        };
        try {
            SwingUtilities.invokeAndWait(runnable);
        } catch (InterruptedException ignored) {
            ignored.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void initializeChatPanel() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // FIXME: figure out how to reconcile this w/ in round chat.
                ChatPanel chatPanel = getChatPanel();
                chatPanel.initialize(dataModel);
                showPanel(CHAT_PANEL_NAME);
                startChatTimer();
            }
        });
    }

    public void add(JComponent component) {
        getPanel().add(component, component.getName());
    }

    @Override
    public JPanel getPanel() {
        return mainPanel;
    }
    
    public JPanel getSurveyIdPanel() {
        if (surveyIdPanel == null) {
            surveyIdPanel = new SurveyIdPanel(client);
        }
        return surveyIdPanel;
    }
    
    @Override
    public void dispose() {
        // no-op, nothing to dispose.
    }

    @Override
    public void requestFocusInWindow() {
        mainPanel.requestFocusInWindow();
        
    }

    public void surveyIdSubmitted() {
        setInstructions(dataModel.getRoundConfiguration().getWelcomeInstructions());
        showInstructionsPanel();
    }

    public void strategyNominationSubmitted() {
        setInstructions(dataModel.getRoundConfiguration().getSubmittedVoteInstructions());
        showInstructionsPanel();
    }

	public void showImposedStrategy(final Strategy strategy) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override public void run() {
				setInstructions(dataModel.getRoundConfiguration().getImposedStrategyInstructions(strategy));		
			}
		});
	}


}
