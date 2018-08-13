package edu.asu.commons.foraging.facilitator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.jnlp.ClipboardService;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.FacilitatorEndRoundEvent;
import edu.asu.commons.foraging.event.QuizCompletedEvent;
import edu.asu.commons.foraging.event.TrustGameResultsFacilitatorEvent;
import edu.asu.commons.foraging.event.TrustGameSubmissionEvent;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.foraging.rules.Strategy;
import edu.asu.commons.foraging.rules.iu.ForagingStrategy;
import edu.asu.commons.ui.HtmlEditorPane;
import edu.asu.commons.ui.HtmlSelection;
import edu.asu.commons.ui.UserInterfaceUtils;

/**
 * The primary facilitator interface panel.
 * 
 * @author Allen Lee
 */
@SuppressWarnings("unused")
public class FacilitatorWindow extends JPanel {

    private static final String JAVAX_JNLP_CLIPBOARD_SERVICE = "javax.jnlp.ClipboardService";

    private static final long serialVersionUID = -9067316316468488000L;

    private Facilitator facilitator;

    private FacilitatorChatPanel facilitatorChatPanel;

    private JScrollPane informationScrollPane;

    private JEditorPane informationEditorPane;

    private JLabel timeLeftLabel;

    private JMenuItem showInstructionsMenuItem;

    private JMenuItem startRoundMenuItem;

    private JMenuItem stopRoundMenuItem;

    private JMenuItem loadExperimentMenuItem;

    private JMenuBar menuBar;

    private JMenuItem startChatMenuItem;
    private JMenuItem showTrustGameMenuItem;
    private JMenuItem showNextInstructionScreenMenuItem;
    private JMenuItem showVotingInstructionsMenuItem;
    private JMenuItem showVoteScreenMenuItem;
    private JMenuItem showSurveyInstructionsMenuItem;
    private JMenuItem showExitInstructionsMenuItem;
    private JMenuItem imposeStrategyMenuItem;

    private HtmlEditorPane messageEditorPane;

    private StringBuilder instructionsBuilder;
    private int completedQuizzes;
    private int completedTrustGames;

    private ClipboardService clipboardService;

    private Map<Strategy, Integer> imposedStrategies = new HashMap<>();

    public FacilitatorWindow(Dimension dimension, Facilitator facilitator) {
        this.facilitator = facilitator;
        initGuiComponents();
        createMenu();
    }

    public void initializeReplay() {
        throw new UnsupportedOperationException("Replay currently unimplemented.");
    }

    public void initializeReplayRound() {
        throw new UnsupportedOperationException("Replay currently unimplemented.");
    }

    /*
     * This method gets called after the end of each round
     */
    public void displayInstructions() {

    }

    /*
     * This method gets called at the start of each round including start of the experiment
     */
    public void displayGame() {
        startChatMenuItem.setEnabled(false);
        showInstructionsMenuItem.setEnabled(false);
        showNextInstructionScreenMenuItem.setEnabled(false);
        startRoundMenuItem.setEnabled(false);
        stopRoundMenuItem.setEnabled(true);
    }

    private JMenuBar createMenu() {
        menuBar = new JMenuBar();
        // Round menu
        JMenu menu = new JMenu("Round");
        menu.setMnemonic(KeyEvent.VK_R);

        showInstructionsMenuItem = new JMenuItem("Show instructions");
        showInstructionsMenuItem.setMnemonic(KeyEvent.VK_I);
        showInstructionsMenuItem.addActionListener(e -> {
            facilitator.sendShowInstructionsRequest();
            addMessage("Instructions have been shown.");
            startRoundMenuItem.setEnabled(true);
        });
        menu.add(showInstructionsMenuItem);
        showNextInstructionScreenMenuItem = createMenuItem(menu, "Show next instruction screen", e -> facilitator.sendShowNextInstructionScreenRequest());
        startRoundMenuItem = new JMenuItem("Start");
        startRoundMenuItem.setMnemonic(KeyEvent.VK_T);
        startRoundMenuItem.setEnabled(false);
        startRoundMenuItem.addActionListener(e -> facilitator.sendBeginRoundRequest());
        menu.add(startRoundMenuItem);

        stopRoundMenuItem = new JMenuItem("Stop");
        stopRoundMenuItem.setMnemonic(KeyEvent.VK_P);
        stopRoundMenuItem.setEnabled(false);
        stopRoundMenuItem.addActionListener(e -> facilitator.sendEndRoundRequest());
        menu.add(stopRoundMenuItem);

        boolean hasTrustGame = false;
        boolean hasDedicatedChatRound = false;
        for (RoundConfiguration configuration: getFacilitator().getServerConfiguration().getAllParameters()) {
            if (configuration.isTrustGameEnabled()) {
                hasTrustGame = true;
            }
            if (configuration.isChatEnabled()) {
                hasDedicatedChatRound = true;
            }
        }

        startChatMenuItem = new JMenuItem("Start chat");
        startChatMenuItem.setEnabled(hasDedicatedChatRound);
        startChatMenuItem.addActionListener(e -> facilitator.sendBeginChatRoundRequest());
        menu.add(startChatMenuItem);

        showTrustGameMenuItem = new JMenuItem("Show trust game");
        showTrustGameMenuItem.setEnabled(hasTrustGame);
        showTrustGameMenuItem.addActionListener(e -> facilitator.sendShowTrustGameRequest());
        menu.add(showTrustGameMenuItem);

        menuBar.add(menu);

        showExitInstructionsMenuItem = createMenuItem(menu,
                "Show exit instructions",
                e -> facilitator.sendShowExitInstructionsRequest()
        );


        // voting menu
        menu = new JMenu("Voting");

        showVotingInstructionsMenuItem = createMenuItem(menu, "Show voting instructions", e -> facilitator.sendShowVotingInstructionsRequest());
        showVoteScreenMenuItem = createMenuItem(menu, "Show voting screen", e -> facilitator.sendShowVoteScreenRequest());

        imposeStrategyMenuItem = createMenuItem(menu, "Add imposed strategy", e -> {
            ForagingStrategy selection = (ForagingStrategy) JOptionPane.showInputDialog(FacilitatorWindow.this, "Select the strategy to impose:\n",
                    "Impose Strategy",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    ForagingStrategy.values(),
                    ForagingStrategy.NONE
                    );
            if (selection == null)
                return;
            Integer distribution = imposedStrategies.get(selection);
            if (distribution == null) {
                distribution = 0;
            }
            imposedStrategies.put(selection, distribution + 1);
            addMessage("Current strategy distribution: " + imposedStrategies);
        });
        createMenuItem(menu, "Clear imposed strategies", e -> {
            imposedStrategies.clear();
            addMessage("Cleared strategy distribution: " + imposedStrategies);
        });
        createMenuItem(menu, "Send imposed strategy distribution", e -> facilitator.sendImposeStrategyEvent(imposedStrategies));
        menuBar.add(menu);

        // survey menu
        menu = new JMenu("Survey");
        showSurveyInstructionsMenuItem = createMenuItem(menu, "Show survey instructions", e -> facilitator.sendShowSurveyInstructionsRequest());
        menuBar.add(menu);

        // Configuration menu
        menu = new JMenu("Configuration");
        menu.setMnemonic(KeyEvent.VK_C);

        JMenuItem menuItem = new JMenuItem("Load");
        menuItem.setMnemonic(KeyEvent.VK_L);
        menuItem.addActionListener(e -> {
            new ConfigurationDialog(facilitator, (facilitator.isExperimentRunning() || facilitator.isReplaying()));
        });
        menu.add(menuItem);

        createMenuItem(menu, "Reconnect", e -> facilitator.connect());

        // create copy to clipboard menu item
        createMenuItem(menu, "Copy to clipboard", e -> {
            String text = informationEditorPane.getSelectedText();
            if (text == null || text.trim().isEmpty()) {
                addMessage("No text selected, copying all text in the editor pane to the clipboard.");
                text = informationEditorPane.getText();
                if (text == null || text.trim().isEmpty()) {
                    // if text is still empty, give up
                    JOptionPane.showMessageDialog(FacilitatorWindow.this, "Unable to find any text to copy to the clipboard.");
                    return;
                }
            }
            ClipboardService service = UserInterfaceUtils.getClipboardService();
            if (service != null) {
                HtmlSelection selection = new HtmlSelection(text);
                service.setContents(selection);
            }
            else {
                addMessage("Clipboard service is only available when run as a WebStart application.");
            }
        });

        menuBar.add(menu);

        return menuBar;
    }

    private JMenuItem createMenuItem(JMenu menu, String name, ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        return menuItem;
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    private void initGuiComponents() {
        setLayout(new BorderLayout(3, 3));
        // setBackground(Color.WHITE);

        informationEditorPane = UserInterfaceUtils.createInstructionsEditorPane();

        informationScrollPane = new JScrollPane(informationEditorPane);

        setInstructions(facilitator.getServerConfiguration().getFacilitatorInstructions());

        JPanel messagePanel = new JPanel(new BorderLayout());
        JLabel messagePanelLabel = new JLabel("System messages");
        messagePanelLabel.setFont(UserInterfaceUtils.DEFAULT_PLAIN_FONT);
        messagePanel.add(messagePanelLabel, BorderLayout.NORTH);
        Dimension minimumSize = new Dimension(600, 50);
        messagePanel.setMinimumSize(minimumSize);
        informationScrollPane.setMinimumSize(minimumSize);
        messageEditorPane = UserInterfaceUtils.createInstructionsEditorPane();
        JScrollPane messageScrollPane = new JScrollPane(messageEditorPane);
        messagePanel.add(messageScrollPane, BorderLayout.CENTER);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, informationScrollPane, messagePanel);
        add(splitPane, BorderLayout.CENTER);
        double proportion = 0.7d;
        splitPane.setDividerLocation(proportion);
        splitPane.setResizeWeight(proportion);
        // add censored chat component if necessary
        if (facilitator.getServerConfiguration().isCensoredChat()) {
            facilitatorChatPanel = new FacilitatorChatPanel(facilitator);
            add(facilitatorChatPanel.getComponent(), BorderLayout.SOUTH);
        }
    }

    private void setInstructions(String contents) {
        informationEditorPane.setText(contents);
        informationEditorPane.setCaretPosition(0);
        informationEditorPane.repaint();
        informationScrollPane.requestFocusInWindow();
    }

    public Facilitator getFacilitator() {
        return facilitator;
    }

    public void updateWindow(long timeLeft) {
        timeLeftLabel.setText("Time left: " + (timeLeft / 1000));
        repaint();
    }

    public void displayDebriefing(ServerDataModel serverDataModel) {
        RoundConfiguration roundConfiguration = serverDataModel.getRoundConfiguration();
        System.err.println("Displaying debriefing: " + roundConfiguration);
        instructionsBuilder = new StringBuilder(roundConfiguration.generateFacilitatorDebriefing(serverDataModel));
        showInstructionsMenuItem.setEnabled(true);
        stopRoundMenuItem.setEnabled(false);
        if (serverDataModel.isLastRound()) {
            instructionsBuilder.append(facilitator.getServerConfiguration().getFinalRoundFacilitatorInstructions());
        }
        else {
// FIXME: this doesn't work with repeating rounds. Perhaps the server needs to pass in upcoming RoundConfiguration
            RoundConfiguration upcomingRound = roundConfiguration.nextRound();
            showNextInstructionScreenMenuItem.setEnabled(upcomingRound.isMultiScreenInstructionsEnabled());
            System.err.println("Upcoming round: " + upcomingRound.getRoundIndexLabel());
            boolean showInstructionsNext = true;
            if (upcomingRound.isTrustGameEnabled()) {
                showTrustGameMenuItem.setEnabled(true);
                addMessage("TRUST GAME: Run a trust game next.  Click on the Round menu and select Show Trust Game<");
                showInstructionsNext = false;
            }
            startChatMenuItem.setEnabled(true);
            if (upcomingRound.isChatRoundEnabled()) {
                addMessage("COMMUNICATION ROUND: There is a communication round configured to run at the end of this round.  Click on the Round menu and select Start Chat Round");
                showInstructionsNext = false;
            }
            if (showInstructionsNext) {
                addMessage("SHOW INSTRUCTIONS: Click on the Round menu and select Show instructions when ready.");
            }
        }
        informationEditorPane.setText(instructionsBuilder.toString());
    }

    public void endRound(FacilitatorEndRoundEvent endRoundEvent) {
        System.out.println("Ending round: " + endRoundEvent);
        ServerDataModel serverDataModel = endRoundEvent.getServerDataModel();
        displayDebriefing(serverDataModel);
        completedQuizzes = 0;
        completedTrustGames = 0;
    }

    public void configureForReplay() {
        // Enable replay menus
        loadExperimentMenuItem.setEnabled(true);
        // Disable all other menus
        startRoundMenuItem.setEnabled(false);
        stopRoundMenuItem.setEnabled(false);
    }

    public void addMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    messageEditorPane.getDocument().insertString(0, "-----\n" + message + "\n", null);
                    messageEditorPane.setCaretPosition(0);
                } catch (BadLocationException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    public void quizCompleted(QuizCompletedEvent event) {
        completedQuizzes++;
        addMessage(String.format("%d completed quizzes (%s)", completedQuizzes, event));
    }

    public void trustGameSubmitted(TrustGameSubmissionEvent event) {
        completedTrustGames++;
        addMessage(String.format("%d completed trust games (%s)", completedTrustGames, event));
    }

    public void updateTrustGame(TrustGameResultsFacilitatorEvent event) {
        addMessage("Received new trust game payment data, recalculating debriefing.");
        displayDebriefing(event.getServerDataModel());
        for (String result : event.getTrustGameLog()) {
            addMessage(result);
        }
    }

}
