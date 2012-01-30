package edu.asu.commons.foraging.facilitator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeSet;

import javax.jnlp.ClipboardService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.text.BadLocationException;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.FacilitatorEndRoundEvent;
import edu.asu.commons.foraging.event.FacilitatorSanctionUpdateEvent;
import edu.asu.commons.foraging.event.QuizCompletedEvent;
import edu.asu.commons.foraging.event.TrustGameResultsFacilitatorEvent;
import edu.asu.commons.foraging.event.TrustGameSubmissionEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.ui.HtmlEditorPane;
import edu.asu.commons.ui.UserInterfaceUtils;

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
    @SuppressWarnings("unused")
    private JMenuItem showVotingInstructionsMenuItem;
    @SuppressWarnings("unused")
    private JMenuItem showVoteScreenMenuItem;
    @SuppressWarnings("unused")
    private JMenuItem showSurveyInstructionsMenuItem;

    private HtmlEditorPane messageEditorPane;

    private StringBuilder instructionsBuilder;
    private int completedQuizzes;
    private int completedTrustGames;

    private ClipboardService clipboardService;

	private JMenuItem showExitInstructionsMenuItem;

    public FacilitatorWindow(Dimension dimension, Facilitator facilitator) {
        this.facilitator = facilitator;
        initGuiComponents();
        createMenu();
        // FIXME: only applicable for standalone java app version - also
        // seems to be causing a NPE for some reason
        // centerOnScreen();
        // frame.setVisible(true);
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

        // repaint();
    }

    /*
     * This method gets called at the start of each round including start of the experiment
     */
    public void displayGame() {
        startChatMenuItem.setEnabled(false);
        showInstructionsMenuItem.setEnabled(false);
        startRoundMenuItem.setEnabled(false);
        stopRoundMenuItem.setEnabled(true);
    }

    private JMenuBar createMenu() {
        menuBar = new JMenuBar();
        // Round menu
        JMenu menu = new JMenu("Round");
        menu.setMnemonic(KeyEvent.VK_R);
        

        showInstructionsMenuItem = new JMenuItem("Show Instructions");
        showInstructionsMenuItem.setMnemonic(KeyEvent.VK_I);
        showInstructionsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                facilitator.sendShowInstructionsRequest();
                addMessage("Instructions have been shown.");
                startRoundMenuItem.setEnabled(true);
            }
        });
        menu.add(showInstructionsMenuItem);
        
        startRoundMenuItem = new JMenuItem("Start");
        startRoundMenuItem.setMnemonic(KeyEvent.VK_T);
        startRoundMenuItem.setEnabled(false);
        startRoundMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                facilitator.sendBeginRoundRequest();
            }
        });
        menu.add(startRoundMenuItem);

        stopRoundMenuItem = new JMenuItem("Stop");
        stopRoundMenuItem.setMnemonic(KeyEvent.VK_P);
        stopRoundMenuItem.setEnabled(false);
        stopRoundMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                facilitator.sendEndRoundRequest();
            }
        });
        menu.add(stopRoundMenuItem);

        showExitInstructionsMenuItem = createMenuItem(menu, "Show exit instructions", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                facilitator.sendShowExitInstructionsRequest();
            }
        });
        
        startChatMenuItem = new JMenuItem("Start chat");
        startChatMenuItem.setEnabled(true);
        startChatMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                facilitator.sendBeginChatRoundRequest();
            }
        });
        menu.add(startChatMenuItem);

        showTrustGameMenuItem = new JMenuItem("Show Trust Game");
        showTrustGameMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                facilitator.sendShowTrustGameRequest();
            }
        });
        menu.add(showTrustGameMenuItem);

        
        
        menuBar.add(menu);
        
        // voting menu
        menu = new JMenu("Voting");

        showVotingInstructionsMenuItem = createMenuItem(menu, "Show voting instructions", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                facilitator.sendShowVotingInstructionsRequest();
            }
        });
        showVoteScreenMenuItem = createMenuItem(menu, "Show voting screen", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                facilitator.sendShowVoteScreenRequest();
            }
        });
        menuBar.add(menu);

        // survey menu
        menu = new JMenu("Survey");
        showSurveyInstructionsMenuItem = createMenuItem(menu, "Show survey instructions", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                facilitator.sendShowSurveyInstructionsRequest();
            }
        });
        menuBar.add(menu);

        // Configuration menu
        menu = new JMenu("Configuration");
        menu.setMnemonic(KeyEvent.VK_C);

        JMenuItem menuItem = new JMenuItem("Load");
        menuItem.setMnemonic(KeyEvent.VK_L);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ConfigurationDialog(facilitator, (facilitator.isExperimentRunning() || facilitator.isReplaying()));
            }
        });
        menu.add(menuItem);

        // create copy to clipboard menu item
        createMenuItem(menu, "Copy to clipboard", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                ClipboardService service = getClipboardService();
                if (service != null) {
                    HtmlSelection selection = new HtmlSelection(text);
                    service.setContents(selection);
                }
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

    // FIXME: get rid of duplication here & displayDebriefing..
    public void updateDebriefing(FacilitatorSanctionUpdateEvent event) {
        Map<Identifier, ClientData> clientDataMap = event.getClientDataMap();
        StringBuilder buffer = new StringBuilder();
        buffer.append("<h3>Updated Facilitator Debriefing:</h3>");
        buffer.append("<table><thead><th>Participant</th><th>Current tokens</th><th>Current Income</th><th>Total Income</th></thead><tbody>");
        TreeSet<Identifier> orderedSet = new TreeSet<Identifier>(clientDataMap.keySet());
        for (Identifier clientId : orderedSet) {
            ClientData data = clientDataMap.get(clientId);
            buffer.append(String.format(
                    "<tr><td>%s</td>" +
                            "<td align='center'>%d</td>" +
                            "<td align='center'>$%3.2f</td>" +
                            "<td align='center'>$%3.2f</td></tr>",
                    clientId.toString(),
                    data.getCurrentTokens(),
                    getIncome(data.getCurrentTokens()),
                    getIncome(data.getTotalTokens())));
        }
        buffer.append("</tbody></table><hr>");
        if (event.isLastRound()) {
            buffer.append("<h2><font color='blue'>The experiment is over.  Please prepare payments.</font></h2>");
        }
        informationEditorPane.setText(buffer.toString());
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
            RoundConfiguration upcomingRound = roundConfiguration.nextRound();
            boolean showInstructionsNext = true;
            if (upcomingRound.isTrustGameEnabled()) {
                showTrustGameMenuItem.setEnabled(true);
                addMessage("TRUST GAME: Run a trust game next.  Click on the Round menu and select Show Trust Game<");
                showInstructionsNext = false;
            }
            if (upcomingRound.isChatRoundEnabled()) {
                startChatMenuItem.setEnabled(true);
                addMessage("COMMUNICATION ROUND: There is a communication round configured to run at the end of this round.  Click on the Round menu and select Start Chat Round");
                showInstructionsNext = false;
            }
            if (showInstructionsNext) {
                addMessage("SHOW INSTRUCTIONS: Click on the Round menu and select Show instructions when ready.");
            }
        }
        informationEditorPane.setText(instructionsBuilder.toString());
    }

    private double getIncome(float numTokens) {
        RoundConfiguration configuration = facilitator.getCurrentRoundConfiguration();
        if (configuration.isPracticeRound()) {
            return 0.0f;
        }
        return configuration.getDollarsPerToken() * numTokens;
    }

    public void endRound(FacilitatorEndRoundEvent endRoundEvent) {
        System.out.println("Ending round: " + endRoundEvent);
        ServerDataModel serverDataModel = endRoundEvent.getServerDataModel();
        displayDebriefing(serverDataModel);
        completedQuizzes = 0;
        completedTrustGames = 0;
    }

    public void setRoundConfiguration(RoundConfiguration roundConfiguration) {

    }

    public void configureForReplay() {
        // Enable the replay menus
        loadExperimentMenuItem.setEnabled(true);

        // Disable all other menus
        startRoundMenuItem.setEnabled(false);
        stopRoundMenuItem.setEnabled(false);
    }

    public void addMessage(String message) {
        try {
            messageEditorPane.getDocument().insertString(0, "-----\n" + message + "\n", null);
        } catch (BadLocationException exception) {
            exception.printStackTrace();
        }
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

    public ClipboardService getClipboardService() {
        if (clipboardService == null) {
            try {
                clipboardService = (ClipboardService) ServiceManager.lookup(JAVAX_JNLP_CLIPBOARD_SERVICE);
            } catch (UnavailableServiceException e) {
                e.printStackTrace();
                addMessage("Unable to load the ClipboardService for all your clipboard needs.  Sorry!");
            }
        }
        return clipboardService;
    }

    private static class HtmlSelection implements Transferable {

        private static DataFlavor[] htmlFlavors = new DataFlavor[3];
        private final String html;
        static {
            try {
                htmlFlavors[0] = new DataFlavor("text/html;class=java.lang.String");
                htmlFlavors[1] = new DataFlavor("text/html;class=java.io.Reader");
                htmlFlavors[2] = new DataFlavor("text/html;charset=unicode;class=java.io.InputStream");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public HtmlSelection(String html) {
            this.html = html;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return htmlFlavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return Arrays.asList(htmlFlavors).contains(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (String.class.equals(flavor.getRepresentationClass())) {
                return html;
            } 
            else if (Reader.class.equals(flavor.getRepresentationClass())) {
                return new StringReader(html);
            } 
            else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                return new ByteArrayInputStream(html.getBytes());
            }
            throw new UnsupportedFlavorException(flavor);
        }

    }

}
