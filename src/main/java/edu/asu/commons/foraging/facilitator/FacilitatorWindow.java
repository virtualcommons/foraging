package edu.asu.commons.foraging.facilitator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.FacilitatorEndRoundEvent;
import edu.asu.commons.foraging.event.FacilitatorSanctionUpdateEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.net.Identifier;



public class FacilitatorWindow extends JPanel {

    private static final long serialVersionUID = -9067316316468488000L;

    private Facilitator facilitator;
    
    private FacilitatorChatPanel facilitatorChatPanel;

    // private JFrame frame;	
    private Dimension windowDimension;

    private JScrollPane informationScrollPane;

    private JEditorPane informationEditorPane;

    private JScrollPane viewPane;

    private JLabel timeLeftLabel;

    private JLabel messageLabel;

    private JPanel informationPanel;

    private Dimension groupViewDimension = new Dimension(400, 400);

    private int viewSpacing = 50;

    private JMenuItem startExperimentMenuItem;

    private JMenuItem stopExperimentMenuItem;

    private JMenuItem showInstructionsMenuItem;
    
    private JMenuItem startRoundMenuItem;

    private JMenuItem stopRoundMenuItem;

    private JMenuItem loadExperimentMenuItem;

    private JMenuBar menuBar;

    private int completedQuizzes;

    private JMenuItem startChatMenuItem;

    public FacilitatorWindow(Dimension dimension, Facilitator facilitator) {
        this.facilitator = facilitator;
        windowDimension = dimension;
        initGuiComponents();
        createMenu();
        // FIXME: only applicable for standalone java app version - also
        // seems to be causing a NPE for some reason
        //        centerOnScreen();
        //        frame.setVisible(true);		
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
        
//        repaint();
    }

    /*
     * This method gets called at the start of each round including start of the experiment
     */
    public void displayGame() {
        // switchCenterComponent(informationPanel, viewPane);
    	showInstructionsMenuItem.setEnabled(false);
        startRoundMenuItem.setEnabled(false);
        stopRoundMenuItem.setEnabled(true);
        startExperimentMenuItem.setEnabled(false);
        stopExperimentMenuItem.setEnabled(true);
        // initViewPanel();
        // repaint();
    }

    private JMenuBar createMenu() {
        JMenu menu = new JMenu("Experiment");
        menuBar = new JMenuBar();

        menu.setMnemonic(KeyEvent.VK_E);

        startExperimentMenuItem = new JMenuItem("Start");
        startExperimentMenuItem.setMnemonic(KeyEvent.VK_S);
        startExperimentMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                facilitator.sendBeginExperimentRequest();
            }
        });
        menu.add(startExperimentMenuItem);

        stopExperimentMenuItem = new JMenuItem("Stop");
        stopExperimentMenuItem.setMnemonic(KeyEvent.VK_O);
        stopExperimentMenuItem.setEnabled(false);
        stopExperimentMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("FIXME: Cannot stop experiments right now");
//                facilitator.sendStopExperimentRequest();
                //loadConfigurationMenu.setEnabled(true);
//                displayInstructions();
            }
        });
        menu.add(stopExperimentMenuItem);
        menuBar.add(menu);

        //Round menu
        menu = new JMenu("Round");
        menu.setMnemonic(KeyEvent.VK_R);

        startChatMenuItem = new JMenuItem("Start chat");
        startChatMenuItem.setEnabled(true);
        startChatMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
        		facilitator.sendBeginChatRoundRequest();
        	}
        });
        menu.add(startChatMenuItem);
        
        showInstructionsMenuItem = new JMenuItem("Show Instructions");
        showInstructionsMenuItem.setMnemonic(KeyEvent.VK_I);
        showInstructionsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                facilitator.sendShowInstructionsRequest();
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
        menuBar.add(menu);

        //Configuration menu
        menu = new JMenu("Configuration");
        menu.setMnemonic(KeyEvent.VK_C);

        JMenuItem menuItem = new JMenuItem("Load");
        menuItem.setMnemonic(KeyEvent.VK_L);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ConfigurationDialog(facilitator, (facilitator
                        .isExperimentRunning() || facilitator.isReplaying()));
            }
        });
        menu.add(menuItem);
        
       /* JMenuItem treatment = new JMenuItem("Treatment");
        treatmnet.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		new 
        	}
        })*/

        menuBar.add(menu);

        return menuBar;
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }
    private void initGuiComponents() {
        setLayout(new BorderLayout(4, 4));
        //		setBackground(Color.WHITE);

        informationEditorPane = new JEditorPane("text/html",
                "CSAN Facilitator Instructions");
        informationEditorPane.setPreferredSize(new Dimension(400, 400));
        informationEditorPane.setEditable(false);
        
        facilitatorChatPanel = new FacilitatorChatPanel(facilitator);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        informationScrollPane = new JScrollPane(informationEditorPane,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        informationPanel = new JPanel(new BorderLayout());
        messageLabel = new JLabel("Messages");
        informationPanel.add(messageLabel, BorderLayout.NORTH);
        informationPanel.add(informationScrollPane, BorderLayout.CENTER);

        setInstructions(facilitator.getServerConfiguration().getFacilitatorInstructions());

        viewPane = new JScrollPane();
        Dimension minimumSize = new Dimension(200, 200);
        informationPanel.setMinimumSize(minimumSize);
        splitPane.add(informationPanel);
        splitPane.add(facilitatorChatPanel.getComponent());
        add(splitPane, BorderLayout.CENTER);
    }

    public void initViewPanel() {
        RoundConfiguration roundConfiguration = facilitator.getCurrentRoundConfiguration();
        System.err.println("round configuration: " + roundConfiguration);
        viewPane.getViewport().removeAll();

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        timeLeftLabel = new JLabel("Round has not begun yet.");
        labelPanel.add(new JLabel("Round "
                + facilitator.getServerConfiguration().getCurrentRoundNumber()
                + "           "));
        labelPanel.add(timeLeftLabel);

        JPanel gridPanel = new JPanel();
        gridPanel.setForeground(Color.BLACK);
        //Display group views based on the window size, group view size and no. of groups		
        Set<GroupDataModel> groups = facilitator.getServerGameState().getGroups();
        System.out.println("# of groups: " + groups.size());
        int cols = windowDimension.width / (groupViewDimension.width + viewSpacing);
        int rows = 1;
        if (cols != 0) {
            rows = groups.size() / cols;
            if ((groups.size() % cols) > 0)
                ++rows;
        }
        gridPanel.setLayout(new GridLayout(rows, cols));
        System.out.println("Rows = " + rows + " Cols = " + cols);

        int groupCounter = 1;
        for (GroupDataModel group : groups) {
            JPanel groupPanel = new JPanel(new BorderLayout(4, 4));
            GroupView groupView = new GroupView(groupViewDimension, group);
            groupView.setup(roundConfiguration);

            JPanel groupViewPanel = new JPanel();
            //			groupViewPanel.setBackground(Color.blue);
            groupViewPanel.setLayout(new BoxLayout(groupViewPanel,
                    BoxLayout.Y_AXIS));
            JLabel label = new JLabel("Group " + groupCounter, JLabel.CENTER);
            //			label.setBackground(Color.RED);
            groupViewPanel.add(label);
            groupViewPanel.add(groupView);

            groupPanel.add(groupViewPanel, BorderLayout.CENTER);

            Dimension horizFillerDim = new Dimension(groupViewDimension.width
                    + viewSpacing, viewSpacing / 2);
            Dimension vertFillerDim = new Dimension(viewSpacing / 2,
                    groupViewDimension.height + viewSpacing);
            Box.Filler horizontalFiller = new Box.Filler(horizFillerDim,
                    horizFillerDim, horizFillerDim);
            Box.Filler verticalFiller = new Box.Filler(vertFillerDim,
                    vertFillerDim, vertFillerDim);
            groupPanel.add(verticalFiller, BorderLayout.EAST);
            groupPanel.add(verticalFiller, BorderLayout.WEST);
            groupPanel.add(horizontalFiller, BorderLayout.SOUTH);
            groupPanel.add(horizontalFiller, BorderLayout.NORTH);

            gridPanel.add(groupPanel);
            ++groupCounter;
        }
        JPanel viewPanel = new JPanel();
        viewPanel.setLayout(new BoxLayout(viewPanel, BoxLayout.Y_AXIS));
        viewPanel.add(labelPanel);
        viewPanel.add(gridPanel);
        viewPane.getViewport().add(viewPanel);
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

    public void updateMenuItems() {
        startExperimentMenuItem.setEnabled(true);
        stopExperimentMenuItem.setEnabled(false);
        startRoundMenuItem.setEnabled(false);
        showInstructionsMenuItem.setEnabled(false);
    }

    /*
     private void centerOnScreen() {
     Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
     Dimension windowDimension = frame.getSize();
     int x = (screenDimension.width - windowDimension.width ) / 2;
     int y = (screenDimension.height - windowDimension.height) / 2;
     frame.setLocation(x, y);
     }
     */

    public void updateWindow(long timeLeft) {
        timeLeftLabel.setText("Time left: " + (timeLeft / 1000));
        repaint();
    }

    public void displayDebriefing(FacilitatorEndRoundEvent event) {
        Map<Identifier, ClientData> clientDataMap = event.getClientDataMap();
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("<h3>Round %d Results</h3>", facilitator.getCurrentRoundConfiguration().getRoundNumber()));
        builder.append("<table><thead><th>Participant</th><th>Current tokens</th><th>Current Income</th><th>Total Income</th></thead><tbody>");
        TreeSet<Identifier> orderedSet = new TreeSet<Identifier>(clientDataMap.keySet());
        for (Identifier clientId : orderedSet) {
            ClientData data = clientDataMap.get(clientId);
            // FIXME: hack... inject the configuration into the client data so that getIncome() will return something appropriate.
            // should just refactor getIncome or remove it from ClientData entirely.
            builder.append(String.format(
                            "<tr><td>%s</td>" +
                            "<td align='center'>%d</td>" +
                            "<td align='center'>$%3.2f</td>" +
                            "<td align='center'>$%3.2f</td></tr>",
                            clientId.toString(), 
                            data.getCurrentTokens(), 
                            getIncome(data.getCurrentTokens()),
                            data.getTotalIncome() + facilitator.getServerConfiguration().getShowUpPayment()));
        }
        builder.append("</tbody></table><hr>");
        if (event.isLastRound()) {
            builder.append("<h2><font color='blue'>The experiment is over.  Please prepare payments.</font></h2>");
        }
        informationEditorPane.setText(builder.toString());
        // switchCenterComponent(viewPane, informationPanel);
        //startRoundMenuItem.setEnabled(true);
        showInstructionsMenuItem.setEnabled(true);
        stopRoundMenuItem.setEnabled(false);
    }

    private double getIncome(float numTokens) {
        RoundConfiguration configuration = facilitator.getCurrentRoundConfiguration();
        if (configuration == null) {
            // FIXME: last minute hack.
            return 0.02f * numTokens;
        }
        if (configuration.isPracticeRound()) {
            return 0.0f;
        }
        return configuration.getDollarsPerToken() * numTokens;
    }

    public void endRound(FacilitatorEndRoundEvent endRoundEvent) {
        displayDebriefing(endRoundEvent);
        if (endRoundEvent.isLastRound()) {
            facilitator.endExperiment();
        }
        else {
            // FIXME: get rid of 
            facilitator.getServerConfiguration().nextRound();
        }
        completedQuizzes = 0;
        messageLabel.setText("No messages");
    }

    public void configureForReplay() {
        //Enable the replay menus
        loadExperimentMenuItem.setEnabled(true);

        //Disable all other menus
        startExperimentMenuItem.setEnabled(false);
        stopExperimentMenuItem.setEnabled(false);
        startRoundMenuItem.setEnabled(false);
        stopRoundMenuItem.setEnabled(false);
    }

    public void quizCompleted() {
        completedQuizzes++;
        messageLabel.setText("Completed quizzes: " + completedQuizzes);
    }

    public void updateDebriefing(FacilitatorSanctionUpdateEvent event) {
        Map<Identifier, ClientData> clientDataMap = event.getClientDataMap();
        StringBuilder buffer = new StringBuilder();
        buffer.append("<h3>Updated Facilitator Debriefing:</h3>");
        buffer.append("<table><thead><th>Participant</th><th>Current tokens</th><th>Current Income</th><th>Total Income</th></thead><tbody>");
        TreeSet<Identifier> orderedSet = new TreeSet<Identifier>(clientDataMap.keySet());
        for (Identifier clientId : orderedSet) {
            ClientData data = clientDataMap.get(clientId);
            // FIXME: hack... inject the configuration into the client data so that getIncome() will return something appropriate.
            // should just refactor getIncome or remove it from ClientData entirely.
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
}
