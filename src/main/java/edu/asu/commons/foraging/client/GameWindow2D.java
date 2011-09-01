package edu.asu.commons.foraging.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import edu.asu.commons.event.Event;
import edu.asu.commons.event.EventChannel;
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
import edu.asu.commons.net.Identifier;
import edu.asu.commons.util.Duration;
import edu.asu.commons.util.HtmlEditorPane;

/**
 * $Id: GameWindow2D.java 529 2010-08-17 00:08:01Z alllee $
 * 
 * The client-side view for forager - can be used by standalone Java
 * applications or Applets.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 529 $
 */
public class GameWindow2D implements GameWindow {

    private final ClientDataModel dataModel;

    private final static String INSTRUCTIONS_PANEL_NAME = "Foraging instructions panel";
    private final static String GAME_PANEL_NAME = "Game panel";
    private final static String TRUST_GAME_PANEL_NAME = "Trust game panel";
    // standalone chat panel
    private final static String CHAT_PANEL_NAME = "Chat panel";

    protected static final String POST_ROUND_SANCTIONING_PANEL_NAME = null;

    private String currentCardPanel = INSTRUCTIONS_PANEL_NAME;

    // private Component currentCenterComponent;

    private JPanel mainPanel;

    // instructions components.
    private JScrollPane instructionsScrollPane;
    private HtmlEditorPane instructionsEditorPane;

    private JPanel messagePanel;
    private JScrollPane messageScrollPane;
    private JTextPane messageTextPane;

    private JPanel labelPanel;

    // FIXME: this shouldn't be public
    public static Duration duration;

    private ChatPanel chatPanel;

    private JLabel informationLabel;

    private JLabel timeLeftLabel;

    private JPanel gamePanel;

    private ForagingClient client;

    private SubjectView subjectView;

    public Timer timer;

    private final StringBuilder instructionsBuilder = new StringBuilder();

    private EventChannel channel;

    private CardLayout cardLayout;

    // private EnergyLevel energyLevel;

    public GameWindow2D(ForagingClient client, Dimension size) {
        this.client = client;
        this.dataModel = client.getDataModel();
        // FIXME: set the actual screen size dimensions after this JPanel has been initialized...
        this.channel = client.getEventChannel();
        // feed subject view the available screen size so that
        // it can adjust appropriately when given a board size
        // int width = (int) Math.min(Math.floor(size.getWidth()), Math.floor(size.getHeight() * 0.85));

        initGuiComponents(size);
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
                subjectView.repaint();
            }
        });
    }

    /**
     * In certain cases, init() _can_ be called before endRound() is finished. Need to lock
     * access!
     * 
     * @param event
     */
    public void init() {
        RoundConfiguration roundConfiguration = dataModel.getRoundConfiguration();
        if (roundConfiguration.isFirstRound()) {
            setInstructions(roundConfiguration.getWelcomeInstructions());
        }
        // don't display next round time, instead wait for the
        // facilitator signal.
        timeLeftLabel.setText("Waiting for facilitator's signal.");
        informationLabel.setText("Waiting for facilitator's signal.");
        // add the next round instructions to the existing debriefing text set by the previous
        // EndRoundEvent.
    }

    private void setQuestionColors(List<String> questionNumbers, String color) {
        HTMLEditorKit editorKit = (HTMLEditorKit) instructionsEditorPane.getEditorKit();
        StyleSheet styleSheet = editorKit.getStyleSheet();
        for (String questionNumber : questionNumbers) {
            String styleString = String.format(".%s { color: %s; }", questionNumber, color);
            styleSheet.addRule(styleString);
        }
    }

    private ActionListener createQuizListener(final RoundConfiguration configuration) {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                HtmlEditorPane.FormActionEvent formEvent = (HtmlEditorPane.FormActionEvent) e;
                Properties actualAnswers = formEvent.getData();
                List<String> incorrectAnswers = new ArrayList<String>();
                List<String> correctAnswers = new ArrayList<String>();

                // iterate through expected answers
                Map<String, String> quizAnswers = configuration.getQuizAnswers();
                for (Map.Entry<String, String> entry : quizAnswers.entrySet()) {
                    String questionNumber = entry.getKey();
                    String expectedAnswer = entry.getValue();
                    if (expectedAnswer.equals(actualAnswers.getProperty(questionNumber))) {
                        correctAnswers.add(questionNumber);
                    }
                    else {
                        // flag the incorrect response
                        incorrectAnswers.add(questionNumber);
                    }
                }

                client.transmit(new QuizResponseEvent(client.getId(), actualAnswers, incorrectAnswers));
                StringBuilder builder = new StringBuilder();
                setQuestionColors(correctAnswers, "black");
                setQuestionColors(incorrectAnswers, "red");
                if (incorrectAnswers.isEmpty()) {
                    builder.append(configuration.getInstructions());
                    // notify the server and also notify the participant.
                    builder.append("<br><b>Congratulations!</b> You have answered all questions correctly.");
                    setInstructions(builder.toString());
                }
                else {
                    String currentInstructions = instructionsBuilder.toString();
                    // remove all inputs.
                    currentInstructions = currentInstructions.replaceAll("<input.*value=\"[\\w]+\">", "");
                    System.err.println("new instructions: " + currentInstructions);
                    builder.append(currentInstructions);
                    Collections.sort(incorrectAnswers);
                    Collections.sort(correctAnswers);
                    HTMLEditorKit editorKit = (HTMLEditorKit) instructionsEditorPane.getEditorKit();
                    StyleSheet styleSheet = editorKit.getStyleSheet();
                    StringBuilder correctString = new StringBuilder();
                    if (!correctAnswers.isEmpty()) {
                        correctString.append("<h3>Correctly answered questions</h3><ul>");
                        // FIXME: extract style modifications to method
                        for (String correctQuestionNumber : correctAnswers) {
                            String styleString = String.format(".%s { color: black; }", correctQuestionNumber);
                            styleSheet.addRule(styleString);
                            correctString.append(String.format("<li>Your answer [ %s ] was correct for question %s.",
                                    actualAnswers.get(correctQuestionNumber),
                                    correctQuestionNumber));
                        }
                        correctString.append("</ul>");
                    }

                    correctString.append("<h3>Incorrectly answered questions</h3><ul>");
                    for (String incorrectQuestionNumber : incorrectAnswers) {
                        String styleString = String.format(".%s { color: red; }", incorrectQuestionNumber);
                        styleSheet.addRule(styleString);
                        correctString.append(String.format("<li>Your answer [ %s ] was incorrect for question %s.  The correct answer was [ %s ].  %s",
                                actualAnswers.get(incorrectQuestionNumber),
                                incorrectQuestionNumber,
                                quizAnswers.get(incorrectQuestionNumber),
                                configuration.getQuizExplanation(incorrectQuestionNumber)
                                ));
                    }
                    correctString.append("</ul>");
                    builder.append(correctString);
                    setInstructions(builder.toString());
                }
            }
        };
    }

    /**
     * Invoked when a subject collected a token at Point p.
     * 
     * @param position
     */
    public void collectToken(Point position) {
        subjectView.collectToken(position);
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
            Map<Identifier, ClientData> clientDataMap = dataModel.getClientDataMap();
            Point clientPosition = dataModel.getCurrentPosition();

            for (Identifier id : dataModel.getAllClientIdentifiers()) {
                ClientData clientData = clientDataMap.get(id);
                String formatString = "";
                if (id.equals(dataModel.getId())) {
                    formatString = " [%d (you) : %d] ";
                    builder.append(String.format(formatString, clientData.getAssignedNumber(), clientData.getCurrentTokens()));
                }
                else {
                    if (!dataModel.getRoundConfiguration().isFieldOfVisionEnabled()) {
                        formatString = " [%d : %d] ";
                        builder.append(String.format(formatString, clientData.getAssignedNumber(), clientData.getCurrentTokens()));
                    }
                    else {
                        double radius = dataModel.getRoundConfiguration().getViewSubjectsRadius();
                        Circle fieldOfVision = new Circle(clientPosition, radius);
                        if (fieldOfVision.contains(clientData.getPosition())) {
                            formatString = " [%d : %d] ";
                            builder.append(String.format(formatString, clientData.getAssignedNumber(), clientData.getCurrentTokens()));
                        }
                        else {
                            formatString = " [%d : XX] ";
                            builder.append(String.format(formatString, clientData.getAssignedNumber()));
                        }
                    }
                }
            }
            return builder.toString();
        }
        else {
            int tokensConsumed = dataModel.getCurrentTokens();
            return String.format("Income: $%3.2f  |  Tokens collected: %d     ",
                    getIncome(tokensConsumed),
                    tokensConsumed);
        }
    }

    private String getTimeLeftLabelText(long roundTimeLeft) {
        long secondsLeft = roundTimeLeft / 1000L;
        return "Time left: " + secondsLeft + " second(s)";
    }

    private void setInstructions(String s) {
//        System.err.println("Setting instructions to " + s);
        instructionsEditorPane.setText(s);
        instructionsEditorPane.repaint();
        getPanel().repaint();
    }

    private HtmlEditorPane createInstructionsEditorPane() {
        // JEditorPane pane = new JEditorPane("text/html",
        // "Costly Sanctioning Experiment");
        final HtmlEditorPane htmlPane = new HtmlEditorPane();
        htmlPane.setEditable(false);
        htmlPane.setDoubleBuffered(true);
        htmlPane.setBackground(Color.WHITE);
        htmlPane.setFont(new Font("sansserif", Font.PLAIN, 12));
        return htmlPane;
    }

    private void initGuiComponents(Dimension size) {
        // FIXME: replace with CardLayout for easier switching between panels
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        Dimension subjectViewSize = new Dimension((int) size.getWidth(), (int) (size.getHeight() * 0.85));
        subjectView = new SubjectView(subjectViewSize, dataModel);

        // add instructions panel card
        instructionsEditorPane = createInstructionsEditorPane();
        instructionsScrollPane = new JScrollPane(instructionsEditorPane);
        instructionsScrollPane.setDoubleBuffered(true);
        instructionsScrollPane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
        instructionsScrollPane.setName(INSTRUCTIONS_PANEL_NAME);
        add(instructionsScrollPane);

        // add game panel card
        // FIXME: use a more flexible LayoutManager so that in-round chat isn't so fubared.
        gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBackground(Color.WHITE);
        gamePanel.setName(GAME_PANEL_NAME);
        gamePanel.add(subjectView, BorderLayout.CENTER);
        // add labels to game panel
        // FIXME: replace with progress bar.
        timeLeftLabel = new JLabel("Connecting ...");
        informationLabel = new JLabel("Tokens collected: 0     ");
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
        messageTextPane = new JTextPane();
        messageTextPane.setEditable(false);
        messageTextPane.setFont(new Font("arial", Font.BOLD, 12));
        messageTextPane.setBackground(Color.WHITE);
        addStyles(messageTextPane.getStyledDocument());
        messageScrollPane = new JScrollPane(messageTextPane);
        // Dimension scrollPaneSize = new Dimension(getPreferredSize().width, 50);
        // messageScrollPane.setMinimumSize(scrollPaneSize);
        // messageScrollPane.setPreferredSize(scrollPaneSize);
        // messageScrollPane.setMaximumSize(scrollPaneSize);
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
                // offset by 35 pixels to allow for message box
                Dimension screenSize = new Dimension(component.getWidth(), component.getHeight() - 50);
                subjectView.setScreenSize(screenSize);
                subjectView.setImageSizes();
                getPanel().revalidate();
                showPanel(currentCardPanel);
            }
        });
        // add component listeners, chat panel, and sanctioning window IF chat/sanctioning are enabled, and after the end of the round...
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
                int keyChar = (int) keyEvent.getKeyChar();
                int keyCode = keyEvent.getKeyCode();
                Event event = null;
                // directions are the most common action, do them first.
                Direction direction = Direction.valueOf(keyCode);
                if (direction == null) {
                    // check to see if the key is something else.
                    switch (keyCode) {
                    // token request handling
                        case KeyEvent.VK_SPACE:
                            if (dataModel.isHarvestingAllowed()) {
                                event = new CollectTokenRequest(client.getId());
                            }
                            else {
                                displayErrorMessage("You cannot harvest at this time.");
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
                                // get rid of magic constants
                                displayErrorMessage("You may not reduce other participants tokens at this time.");
                                return;
                            }
                            if (client.canPerformRealTimeSanction()) {
                                // System.out.println("Can do sanctioning");
                                int assignedNumber = keyChar - 48;
                                Identifier sanctionee = dataModel.getClientId(assignedNumber);
                                if (sanctionee == null || sanctionee.equals(dataModel.getId())) {
                                    // don't allow self-flagellation :-).
                                    return;
                                }
                                // only allow sanctions for subjects within this subject's field of vision
                                Point subjectPosition = dataModel.getClientDataMap().get(sanctionee).getPoint();
                                if (dataModel.getClientData().isSubjectInFieldOfVision(subjectPosition)) {
                                    // System.out.println("sanctioning event sent");
                                    event = new RealTimeSanctionRequest(dataModel.getId(), sanctionee);
                                    // below function must be used for enforcement type4
                                    // dataModel.sanction(dataModel.getId(), sanctionee);
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
                                getChatPanel().setTextFieldFocus();
                            }
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

    public void startRound() {
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
                    ChatPanel chatPanel = getChatPanel();
                    chatPanel.initialize();
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
        // String chatHandle = getChatHandle(source);
        // messageTextPane.setForeground(color);
        // StyledDocument document = messageTextPane.getStyledDocument();
        // try {
        // document.insertString(document.getLength(), errorMessage + "\n", document.getStyle("bold"));
        // messageTextPane.setCaretPosition(document.getLength());
        // }
        // catch (BadLocationException e) {
        // e.printStackTrace();
        // throw new RuntimeException(e);
        // }
    }

    // FIXME: add to some common GUI package?
    private void addStyles(StyledDocument styledDocument) {
        // and why not have something like... StyleContext.getDefaultStyle() to
        // replace this junk
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(
                StyleContext.DEFAULT_STYLE);
        // Style regularStyle = styledDocument.addStyle("regular",
        // defaultStyle);
        StyleConstants.setFontFamily(defaultStyle, "Helvetica");
        StyleConstants.setBold(styledDocument.addStyle("bold", defaultStyle),
                true);
        StyleConstants.setItalic(styledDocument
                .addStyle("italic", defaultStyle), true);
    }

    private double getIncome(float numTokens) {
        if (dataModel.getRoundConfiguration().isPracticeRound()) {
            return 0.0f;
        }
        return dataModel.getRoundConfiguration().getDollarsPerToken() * numTokens;
    }

    private void addDebriefingText(EndRoundEvent event) {
        instructionsBuilder.delete(0, instructionsBuilder.length());
        // FIXME: should be round-specific? We're not resetting correct quiz answers either.
        int correctQuizAnswers = event.getClientData().getCorrectQuizAnswers();
        double quizReward = correctQuizAnswers * dataModel.getRoundConfiguration().getQuizCorrectAnswerReward();
        instructionsBuilder.append(
                String.format("<h3>Your stats in this round:</h3>" +
                        "<ul>" +
                        "<li>Tokens collected: %d</li>" +
                        "<li>Income: $%3.2f</li>" +
                        "<li>Quiz questions answered correctly: %d (%3.2f)</li>" +
                        "</ul>",
                        event.getCurrentTokens(), getIncome(event.getCurrentTokens()), correctQuizAnswers, quizReward)
                );
        double showUpPayment = dataModel.getRoundConfiguration().getParentConfiguration().getShowUpPayment();
        instructionsBuilder.append(String.format("Your <b>total income</b> so far (including a $%3.2f bonus for showing up) is : $%3.2f<hr>",
                showUpPayment, dataModel.getTotalIncome() + showUpPayment));

        if (event.isLastRound()) {
            for (String trustGameLog : event.getTrustGameLog()) {
                instructionsBuilder.append(trustGameLog);
            }
            instructionsBuilder.append(client.getDataModel().getRoundConfiguration().getLastRoundDebriefing());
            timeLeftLabel.setText("The experiment is now over.");
        }
        setInstructions(instructionsBuilder.toString());
    }

    private void postSanctionDebriefingText(final PostRoundSanctionUpdateEvent event) {
        instructionsBuilder.delete(0, instructionsBuilder.length());
        ClientData clientData = event.getClientData();
        // FIXME: split into tokens used to sanction others and tokens taken
        // away by other people.
        instructionsBuilder.append(
                String.format("<h3>Your statistics from the last round have been updated as follows:</h3>" +
                        "<ul>" +
                        "<li>Tokens collected last round: %d</li>" +
                        "<li>Tokens subtracted by other players: %d</li>" +
                        "<li>Tokens used to subtract tokens from other players: %d</li>" +
                        "<li>Net earned tokens in the last round: %d</li>" +
                        "<li>Net income from the last round: $%3.2f</li>" +
                        "</ul>",
                        clientData.getTokensCollectedLastRound(),
                        clientData.getSanctionPenalties(),
                        clientData.getSanctionCosts(),
                        clientData.getCurrentTokens(),
                        getIncome(clientData.getCurrentTokens()))
                );
        instructionsBuilder.append(String.format("Your <b>total income</b> so far is: $%3.2f<hr>",
                getIncome(clientData.getTotalTokens())));
        if (event.isLastRound()) {
            instructionsBuilder.append(client.getDataModel().getRoundConfiguration().getLastRoundDebriefing());
        }
        setInstructions(instructionsBuilder.toString());

    }

    private ChatPanel getChatPanel() {
        if (chatPanel == null) {
            chatPanel = new ChatPanel(client);
        }
        return chatPanel;
    }

    public void showTrustGame() {
        RoundConfiguration roundConfiguration = dataModel.getRoundConfiguration();
        if (roundConfiguration.isTrustGameEnabled()) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            JEditorPane trustGameInstructionsEditorPane = new JEditorPane();
            trustGameInstructionsEditorPane.setContentType("text/html");
            trustGameInstructionsEditorPane.setEditorKit(new HTMLEditorKit());
            trustGameInstructionsEditorPane.setEditable(false);
            trustGameInstructionsEditorPane.setBackground(Color.WHITE);
            JScrollPane scrollPane = new JScrollPane(trustGameInstructionsEditorPane);
            trustGameInstructionsEditorPane.setText(client.getCurrentRoundConfiguration().getTrustGameInstructions());
            panel.add(scrollPane);

            TrustGamePanel trustGamePanel = new TrustGamePanel(client);
            // trustGamePanel.setPreferredSize(new Dimension(300, 400));
            JScrollPane trustGameScrollPane = new JScrollPane(trustGamePanel);
            panel.add(trustGameScrollPane);
            panel.setName(TRUST_GAME_PANEL_NAME);
            // addCenterComponent(panel);
            // panel.revalidate();
            // panel.repaint();
            add(panel);
            showPanel(TRUST_GAME_PANEL_NAME);
        }
    }

    public void showInstructions() {
        final RoundConfiguration roundConfiguration = dataModel.getRoundConfiguration();
        instructionsBuilder.delete(0, instructionsBuilder.length());
        roundConfiguration.buildInstructions(instructionsBuilder);
        // and add the quiz instructions if the quiz is enabled.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (roundConfiguration.isQuizEnabled()) {
                    instructionsEditorPane.setActionListener(null);
                    instructionsEditorPane.setActionListener(createQuizListener(roundConfiguration));
                }
                setInstructions(instructionsBuilder.toString());
                switchInstructionsPane();
                instructionsEditorPane.setCaretPosition(0);
            }
        });
    }

    public void switchInstructionsPane() {
        showPanel(INSTRUCTIONS_PANEL_NAME);
    }

    private void showPanel(final String panelName) {
        this.currentCardPanel = panelName;
        JPanel panel = getPanel();
        cardLayout.show(panel, panelName);
        panel.repaint();
    }

    public void updateDebriefing(final PostRoundSanctionUpdateEvent event) {
        Runnable runnable = new Runnable() {
            public void run() {
                postSanctionDebriefingText(event);
                switchInstructionsPane();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    public void endRound(final EndRoundEvent event) {
        Runnable runnable = new Runnable() {
            public void run() {
                if (dataModel.getRoundConfiguration().isPostRoundSanctioningEnabled()) {
                    // add sanctioning text and slap the PostRoundSanctioningPanel in
                    PostRoundSanctioningPanel panel = new PostRoundSanctioningPanel(event, dataModel.getRoundConfiguration(), client);
                    panel.setName(POST_ROUND_SANCTIONING_PANEL_NAME);
                    add(panel);
                    showPanel(POST_ROUND_SANCTIONING_PANEL_NAME);
                }
                else {
                    instructionsEditorPane.setText("Waiting for updated round totals from the server...");
                    switchInstructionsPane();
                }
                if (chatPanel != null) {
                    // FIXME: figure out what to do here.
                    getPanel().remove(chatPanel);
                    chatPanel = null;
                }
                // generate debriefing text from data culled from the Event
                addDebriefingText(event);
                // messageTextPane.setText("");
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
                ChatPanel chatPanel = getChatPanel();
                chatPanel.initialize();
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
}
