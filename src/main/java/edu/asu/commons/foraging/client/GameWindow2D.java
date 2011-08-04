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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
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
import edu.asu.commons.foraging.event.QuizCompletedEvent;
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
public class GameWindow2D extends JPanel implements GameWindow {

    private static final long serialVersionUID = -7733523846114902166L;

    // the data model
    private final ClientDataModel dataModel;

    // instructions components. 

    private Component currentCenterComponent;

    private JScrollPane instructionsScrollPane;

    private  HtmlEditorPane instructionsEditorPane;

    private JPanel messagePanel;
    private JScrollPane messageScrollPane;
    private JTextPane messageTextPane;

    private JPanel labelPanel;

    // FIXME: this shouldn't be public
    public static Duration duration;

    private ChatPanel chatPanel;

    private JLabel informationLabel;

    private JLabel timeLeftLabel;

    private JPanel subjectWindow;

    private ForagingClient client;

    private SubjectView subjectView;

    public  Timer timer;

    private final StringBuilder instructionsBuilder = new StringBuilder();

    private EventChannel channel;

    // FIXME: replace switchXXXPanel with CardLayout switching.
    private CardLayout cardLayout;

    // private EnergyLevel energyLevel;

    public GameWindow2D(ForagingClient client, Dimension size) {
        this.client = client;
        this.dataModel = client.getDataModel();
        // FIXME: set the actual screen size dimensions after this JPanel has been initialized... 
        this.channel = client.getEventChannel();
        // feed subject view the available screen size so that
        // it can adjust appropriately when given a board size
        Dimension subjectViewSize = new Dimension((int) Math.floor(size.getWidth()),
                (int) Math.floor(size.getHeight() * 0.85)); 
        subjectView = new SubjectView(subjectViewSize, dataModel);
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
                // only when we transition from 3D -> 2D experiment.  Find out why.
                subjectView.repaint();
            }
        });
    }

    /** 
     * In certain cases, init() _can_ be called before endRound() is finished.  Need to lock
     * access!
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
                // System.err.println("In action performed with event: " + e);
                HtmlEditorPane.FormActionEvent formEvent = (HtmlEditorPane.FormActionEvent) e;
                Properties actualAnswers = formEvent.getData();
                //                actualAnswers.list(System.err);
                List<String> incorrectAnswers = new ArrayList<String>();
                List<String> correctAnswers = new ArrayList<String>();

                // iterate through expected answers
                for (Map.Entry<String, String> entry : configuration.getQuizAnswers().entrySet()) {
                    String questionNumber = entry.getKey();
                    String expectedAnswer = entry.getValue();
                    System.out.println(expectedAnswer);
                    if (! expectedAnswer.equals(actualAnswers.getProperty(questionNumber)) ) {
                        // flag the incorrect response
                        incorrectAnswers.add(questionNumber);
                    }
                    else {
                    	correctAnswers.add(questionNumber);
                    }
                }
                
                client.transmit(new QuizResponseEvent(client.getId(), actualAnswers, incorrectAnswers));
                
                if (incorrectAnswers.isEmpty()) {
                    // notify the server and also notify the participant.
                    StringBuilder builder = new StringBuilder(configuration.getInstructions());
                    builder.append("<br><b>Congratulations!</b> You have answered all questions correctly.");
                    setInstructions(builder.toString());
                    client.transmit(new QuizCompletedEvent(client.getId()));
                    setQuestionColors(correctAnswers, "black");
                }
                else {
                    // FIXME: highlight the incorrect answers?
                    Collections.sort(incorrectAnswers);
                    Collections.sort(correctAnswers);
                    StringBuilder builder = new StringBuilder().append(instructionsBuilder);
                    
                    HTMLEditorKit editorKit = (HTMLEditorKit) instructionsEditorPane.getEditorKit();
                    StyleSheet styleSheet = editorKit.getStyleSheet();
                    StringBuilder correctString = new StringBuilder();
                    if (! correctAnswers.isEmpty()) {
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
                        correctString.append(String.format("<li>Your answer [ %s ] was incorrect for question %s.", 
                                actualAnswers.get(incorrectQuestionNumber),
                                incorrectQuestionNumber));
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

//    private void startEnforcementVotingTimer() {
//        if (timer == null) {
//            //FIXME: Need to fetch this value from the round4.xml
//            duration = Duration.create(dataModel.getRoundConfiguration().getEnforcementVotingDuration());
//            timer = new Timer(1000, new ActionListener() {
//                public void actionPerformed(ActionEvent event) {
//                    if (duration.hasExpired()) {
//                        timeLeftLabel.setText("Voting is now disabled.");
//                        timer.stop();
//                        timer = null;
//                        getEnforcementPanel().sendEnforcementVotes();
//                        displayVotingWaitMessage();
//                    }
//                    else {
//                        timeLeftLabel.setText( String.format("Voting period will end in %d seconds.", duration.getTimeLeft() / 1000L) );
//                    }
//                }
//            });
//            timer.start();
//        }
//    }


//    private void startRegulationVotingTimer() {
//
//        if (timer == null) {
//            duration = Duration.create(dataModel.getRoundConfiguration().getRegulationVotingDuration());
//
//            timer = new Timer(1000, new ActionListener() {
//                public void actionPerformed(ActionEvent event) {
//                    if (duration.hasExpired()) {
//                        timeLeftLabel.setText("Voting is now disabled. Next round begins shortly.");
//
//                        //new code
//                        //Need to add the enforcementVotingPane over here
//                        //instead of the instructionsScrollPane                       
//                        timer.stop();
//                        timer = null;
//                        //remove(sanctioningPanel);
//                        //getSanctioningPanel().stopTimer();
//                        getRegulationPanel().sendRegulationVotes();
//                        displayVotingWaitMessage();
//                    }
//                    else {
//                        timeLeftLabel.setText( String.format("Voting period will end in %d seconds.", duration.getTimeLeft() / 1000L) );
//                    }
//                }
//            });
//            timer.start();
//        }
//    }
//
//    private void startSanctionVotingTimer() { 
//    	if (timer == null) {
//    		 duration = Duration.create(dataModel.getRoundConfiguration().getSanctionVotingDuration());
//             timer = new Timer(1000, new ActionListener() {
//                public void actionPerformed(ActionEvent event) {
//                    if (duration.hasExpired()) {
//                        timeLeftLabel.setText("Voting is now disabled. Next round begins shortly.");
//                        timer.stop();                 
//                        timer = null;
//                        sendSanctionDecisionVotes();
//                        displayVotingWaitMessage();
//                    }
//                    else {
//                        timeLeftLabel.setText( String.format("Voting period will now end in %d seconds.", duration.getTimeLeft() / 1000L) );
//                    }
//                }
//            });
//            timer.start();
//    	}
//     }

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
//                        if (roundConfiguration.isVotingAndRegulationEnabled()) {
//                        	initializeSanctionDecisionPanel();
//                        }
                    }
                    else {
                        timeLeftLabel.setText( String.format("Chat will end in %d seconds.", duration.getTimeLeft() / 1000L) );
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
            
            // To display the token artifacts only if the player lies in the 
            // field of vision
            
            Point clientPosition = dataModel.getCurrentPosition();
            
            for (Identifier id: dataModel.getAllClientIdentifiers()) {
                ClientData clientData = clientDataMap.get(id);
                String formatString = "";
                if (id.equals(dataModel.getId())) {
                    formatString = " [%d (you) : %d] ";
                    builder.append(String.format(formatString, clientData.getAssignedNumber(), clientData.getCurrentTokens()));   
                }
                else {                	 
                	if(!dataModel.getRoundConfiguration().isFieldOfVisionEnabled()){
                		formatString = " [%d : %d] ";
                        builder.append(String.format(formatString, clientData.getAssignedNumber(), clientData.getCurrentTokens()));   
                	}
                	else {
                    	double radius = dataModel.getRoundConfiguration().getViewSubjectsRadius();
                    	Circle fieldOfVision = new Circle(clientPosition, radius);                	
                		if(fieldOfVision.contains(clientData.getPosition())) {                	
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
        instructionsEditorPane.setText(s);
        instructionsEditorPane.setCaretPosition(0);
        instructionsEditorPane.repaint();
        instructionsScrollPane.requestFocusInWindow();
    }

    private HtmlEditorPane createInstructionsEditorPane() {
        // JEditorPane pane = new JEditorPane("text/html",
        //       "Costly Sanctioning Experiment");        
        final HtmlEditorPane htmlPane = new HtmlEditorPane();
        htmlPane.setPreferredSize(new Dimension(400, 400));
        htmlPane.setEditable(false);
        htmlPane.setBackground(Color.WHITE);
        htmlPane.setFont(new Font("sansserif", Font.PLAIN, 12));
        return htmlPane;
    }

    private void initGuiComponents() {
        // FIXME: replace with CardLayout for easier switching between panels
        //        cardLayout = new CardLayout();

        setLayout(new BorderLayout(4, 4));
        instructionsEditorPane = createInstructionsEditorPane();
        instructionsScrollPane = new JScrollPane(instructionsEditorPane);
        add(instructionsScrollPane, BorderLayout.CENTER);
        currentCenterComponent = instructionsScrollPane;
        // setup the Subject Window, add the experiment view
        subjectWindow = new JPanel(new BorderLayout(4, 4));
        subjectWindow.setBackground(Color.WHITE);
        subjectWindow.setForeground(Color.BLACK);
        subjectWindow.add(subjectView, BorderLayout.CENTER);
        //        setBackground(SubjectView.FIELD_OF_VISION_COLOR);
        setBackground(Color.WHITE);
        // replace with progress bar.
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
        add(labelPanel, BorderLayout.NORTH);

        // add message window.
        messagePanel = new JPanel(new BorderLayout());
        //        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.add(new JLabel("Messages"), BorderLayout.NORTH);
        messageTextPane = new JTextPane();
        messageTextPane.setEditable(false);
        messageTextPane.setFont(new Font("arial", Font.BOLD, 12));
        messageTextPane.setBackground(Color.WHITE);


        addStyles(messageTextPane.getStyledDocument());
        messageScrollPane = new JScrollPane(messageTextPane);
        Dimension scrollPaneSize = new Dimension(getPreferredSize().width, 50);
        messageScrollPane.setMinimumSize(scrollPaneSize);
        messageScrollPane.setPreferredSize(scrollPaneSize);
        messageScrollPane.setMaximumSize(scrollPaneSize);
        messagePanel.add(messageScrollPane, BorderLayout.CENTER);
        add(messagePanel, BorderLayout.SOUTH);
        
        addKeyListener( createGameWindowKeyListener() );
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        // resize listener
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent event) {
                Component component = event.getComponent();
                // offset by 35 to allow for chat message box
                int subjectViewHeight = component.getHeight() - 35;
                Dimension size = new Dimension(component.getWidth(), subjectViewHeight);
                subjectView.setScreenSize(size);
                subjectView.setImageSizes();
                GameWindow2D.this.revalidate();
                GameWindow2D.this.repaint();
            }
        });
        // add component listeners, chat panel, and sanctioning window IF chat/sanctioning are enabled, and after the end of the round...
    }

    /**
     * IMPORTANT: this method handles client keyboard inputs within the game.  
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
                        if(dataModel.isHarvestingAllowed()) {
                            event = new CollectTokenRequest(client.getId());
                        }
                        else {
                            displayErrorMessage("You cannot harvest at this time.");
                        }
                        break;
                        // real-time sanctioning keycode handling
                    case KeyEvent.VK_1: case KeyEvent.VK_2: case KeyEvent.VK_3: 
                    case KeyEvent.VK_4: case KeyEvent.VK_5: case KeyEvent.VK_6: 
                    case KeyEvent.VK_7: case KeyEvent.VK_8: case KeyEvent.VK_9:
                        if (! dataModel.isSanctioningAllowed()) {
                            // get rid of magic constants
                            displayErrorMessage("You may not reduce other participants tokens at this time.");
                            return;
                        }
                        if (client.canPerformRealTimeSanction()) {
                            //System.out.println("Can do sanctioning");
                            int assignedNumber = keyChar - 48;
                            Identifier sanctionee = dataModel.getClientId(assignedNumber);
                            if (sanctionee == null || sanctionee.equals(dataModel.getId())) {
                                // don't allow self-flagellation :-).
                                return;
                            }
                            // only allow sanctions for subjects within this subject's field of vision
                            Point subjectPosition = dataModel.getClientDataMap().get(sanctionee).getPoint();
                            if (dataModel.getClientData().isSubjectInFieldOfVision(subjectPosition)) {
                                //	System.out.println("sanctioning event sent");
                                event = new RealTimeSanctionRequest(dataModel.getId(), sanctionee);
                                // below function must be used for enforcement type4
//                                dataModel.sanction(dataModel.getId(), sanctionee);                            	
                            }                            
                            else {
                                displayErrorMessage("The participant is out of range.");
                                return;
                            }
                        }
                        break;
                        // reset token distribution request handling
                    case KeyEvent.VK_R:
                        if (canResetTokenDistribution()) {
                            event = new ResetTokenDistributionRequest(client.getId());
                        }
                        else return;
                        break;
                    case KeyEvent.VK_M:
                        if (! dataModel.getRoundConfiguration().isAlwaysInExplicitCollectionMode()) {
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
                    if (dataModel.getRoundConfiguration().isAlwaysInExplicitCollectionMode()) {
                    	Point newPosition = direction.apply(dataModel.getCurrentPosition());
                    	dataModel.getClientData().setPosition(newPosition);
                    	subjectView.repaint();
                    }
                     */
                }
                if (keyReleased) {
                    // FIXME: have client directly render these requests?  Would
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

    private void addCenterComponent(Component newCenterComponent) {
        if (currentCenterComponent != null) {
            currentCenterComponent.setVisible(false);
            remove(currentCenterComponent);
            add(newCenterComponent, BorderLayout.CENTER);
            newCenterComponent.setVisible(true);
        }
        currentCenterComponent = newCenterComponent;
        revalidate();
    }

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
//                    Dimension subjectWindowSize = subjectView.getSize();
//                    Dimension totalSize = getParent().getSize();
//                    System.err.println("subject window size: " + subjectWindowSize);
//                    System.err.println("total size: " + totalSize);
//                    Dimension chatPanelSize = new Dimension((totalSize.width - subjectWindowSize.width) / 2, (totalSize.height - subjectWindowSize.height) / 2);
//                    System.err.println("chat panel size: " + chatPanelSize);
                    Dimension chatPanelSize = new Dimension(100, getSize().height);
                    chatPanel.setPreferredSize(chatPanelSize);
                    add(chatPanel, BorderLayout.EAST);
                }
                add(messagePanel, BorderLayout.SOUTH);
                addCenterComponent(subjectWindow);

                requestFocusInWindow();
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
        //     String chatHandle = getChatHandle(source);
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
        instructionsBuilder.append(
                String.format("<h3>Your stats in this round:</h3>" +
                        "<ul>" +
                        "<li>Tokens collected: %d</li>" +
                        "<li>Income: $%3.2f</li>" +
                        "</ul>", 
                        event.getCurrentTokens(),
                        getIncome(event.getCurrentTokens()))
        );
        double showUpFee = dataModel.getRoundConfiguration().getParentConfiguration().getShowUpPayment();
        instructionsBuilder.append(String.format("Your <b>total income</b> so far (including a $%3.2f bonus for showing up) is : $%3.2f<hr>",
                showUpFee, dataModel.getTotalIncome() + showUpFee));
        if (event.isLastRound()) {
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

    public void showInstructions() {
        RoundConfiguration roundConfiguration = dataModel.getRoundConfiguration();
        instructionsBuilder.delete(0, instructionsBuilder.length());

        roundConfiguration.buildInstructions(instructionsBuilder);

//        if (roundConfiguration.isFirstRound()) {
//            instructionsBuilder.append(roundConfiguration.getGeneralInstructions());
//        }
//        if (roundConfiguration.isFieldOfVisionEnabled()) {
//            instructionsBuilder.append(roundConfiguration.getFieldOfVisionInstructions());
//        }
//        instructionsBuilder.append(roundConfiguration.getInstructions());
//        

        // and add the quiz instructions if the quiz is enabled.
        if (roundConfiguration.isQuizEnabled()) {
            instructionsEditorPane.setActionListener(null);
            instructionsEditorPane.setActionListener(createQuizListener(roundConfiguration));
        }

        setInstructions(instructionsBuilder.toString());
    }
    public void switchInstructionsPane() {
        instructionsEditorPane.setText("<b>Please wait while we compute your new token totals.</b>");
        addCenterComponent(instructionsScrollPane);
    }

    public void displayActiveEnforcementMechanism() {    	
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String activeRegulation = dataModel.getActiveRegulation().getText();
                if (activeRegulation == null || activeRegulation.trim().isEmpty()) {
                    activeRegulation = "No regulation specified.";
                }
                instructionsBuilder.append("<hr/><h2>Active regulation</h2><hr/><p>").append(activeRegulation).append("</p>");
                instructionsBuilder.append("<hr/><h2>Active enforcement mechanism</h2><hr/><p>").append(dataModel.getActiveEnforcementMechanism().getDescription()).append("</p>");
                setInstructions(instructionsBuilder.toString());
                addCenterComponent(instructionsScrollPane);
            }
        });    	
    }
    
    public void displaySanctionMechanism() {    	
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	instructionsBuilder.delete(0, instructionsBuilder.length());
                instructionsBuilder.append("<h2>Your group voted for the following enforcement mechanism: </h2><hr/><p>").append(dataModel.getActiveSanctionMechanism().getDescription()).append("</p>");
//                instructionsBuilder.append("<hr/><h2>Active enforcement mechanism</h2><hr/><p>").append(dataModel.getActiveEnforcementMechanism().getDescription()).append("</p>");
                setInstructions(instructionsBuilder.toString());
                addCenterComponent(instructionsScrollPane);
            }
        });    	
    }

    private void displayVotingWaitMessage() {    	
        setInstructions("<h3>Please wait while we finish collecting information from all the participants.</h3>");
        addCenterComponent(instructionsScrollPane);
    }

//    public void displayActiveRegulation() {    	
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                String activeRegulation = dataModel.getActiveRegulation().getText();
//                if (activeRegulation == null || activeRegulation.trim().isEmpty()) {
//                    activeRegulation = "No regulation specified.";
//                }
//                setInstructions(
//                        "<h3>The following regulation received the most votes:</h3><p>" + activeRegulation + "</p>");
//                addCenterComponent(instructionsScrollPane);
//                startRegulationDisplayTimer();
//            }
//        });    	
//    }

    public void updateDebriefing(final PostRoundSanctionUpdateEvent event) {
        Runnable runnable = new Runnable() {
            public void run() {
                postSanctionDebriefingText(event);
                addCenterComponent(instructionsScrollPane);                
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
                    addCenterComponent(panel);
                }
                else {
                    instructionsEditorPane.setText("Waiting for updated round totals from the server...");
                    addCenterComponent(instructionsScrollPane);
                }
                // generate debriefing text from data culled from the Event
                addDebriefingText(event);
                messageTextPane.setText("");
            }
        };
        try {
            SwingUtilities.invokeAndWait(runnable);
        } 
        catch (InterruptedException ignored) { ignored.printStackTrace(); } 
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void initializeChatPanel() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ChatPanel chatPanel = getChatPanel();
                chatPanel.initialize();
                remove( messagePanel );
                addCenterComponent( chatPanel );
                startChatTimer();
            }
        });
    }
}
