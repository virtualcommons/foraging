package edu.asu.commons.foraging.client;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.visualization.conceptual.AgentDesignPanel;
import edu.asu.commons.foraging.visualization.forestry.AvatarDesignPanel;
import edu.asu.commons.util.HtmlEditorPane;


/**
 * $Id: InstructionsView.java 529 2010-08-17 00:08:01Z alllee $
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>, Deepali Bhagvat
 * @version $Revision: 529 $
 */

public class InstructionsView {

    private JPanel mainPanel;
    private JScrollPane instructionsScrollPane;
    private HtmlEditorPane instructionsEditorPane;
    private StringBuilder instructionsBuilder = new StringBuilder(2048);
    private JPanel agentDesignPanel = null;
    
    public InstructionsView() {
    	instructionsEditorPane = createInstructionsEditorPane();
        instructionsScrollPane = new JScrollPane(instructionsEditorPane);
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(instructionsScrollPane, BorderLayout.CENTER);
        setInstructions("Please wait patiently until the other participants finish designing their avatars.");
    }
    
    private HtmlEditorPane createInstructionsEditorPane() {
        final HtmlEditorPane htmlPane = new HtmlEditorPane();
        // htmlPane.setPreferredSize(new Dimension(400, 400));
        htmlPane.setEditable(false);
        htmlPane.setFont(new Font("serif", Font.PLAIN, 12));
        return htmlPane;
    }
    
    public void setInstructions(String instructions) {
        instructionsEditorPane.setText(instructions);
        instructionsEditorPane.setCaretPosition(0);
        instructionsEditorPane.repaint();
        instructionsScrollPane.requestFocusInWindow();
    }
    
    public void debrief(ClientDataModel dataModel, boolean lastRound) {
        RoundConfiguration roundConfiguration = dataModel.getRoundConfiguration();
        instructionsBuilder.delete(0, instructionsBuilder.length());
        instructionsBuilder.append(
                String.format("<h3>Your stats in this round:</h3>" +
                        "<ul>" +
                        "<li>Tokens collected: %d</li>" +
                        "<li>Income: $%3.2f</li>" +
                        "</ul>", 
                        dataModel.getCurrentTokens(),
                        dataModel.getCurrentIncome())
                );
        if (roundConfiguration.isPracticeRound()) {
            instructionsBuilder.append("<h3>Note - since this was a practice round you did not earn any income this round.</h3>");
        }
        double showUpPayment = roundConfiguration.getParentConfiguration().getShowUpPayment();
        instructionsBuilder.append(String.format("Your <b>total income</b> so far (including a $%3.2f bonus for showing up) is : $%3.2f<hr>",
                showUpPayment, dataModel.getTotalIncome() + showUpPayment));
        if (lastRound) {
            instructionsBuilder.append(roundConfiguration.getLastRoundDebriefing());
        }
        setInstructions(instructionsBuilder.toString());
    }
    
    /**
     * Only used by {@link GameWindow3D} at the moment.
     * @param instructions
     */
    public void appendInstructions(String instructions) {
        instructionsBuilder.append(instructions);
        setInstructions(instructionsBuilder.toString());
    }

    /*public JScrollPane getInstructionsScrollPane() {
        return instructionsScrollPane;
    }*/
    
    public JPanel getMainPanel() {
    	return mainPanel;
    }
    
    public void addAgentDesignPanel(String visualizationType, ForagingClient client) {
    	if (visualizationType.equals("forestry")) {
	    	agentDesignPanel = new AvatarDesignPanel(client);
    	}
    	else if(visualizationType.equals("abstract")) {
    		agentDesignPanel = new AgentDesignPanel(client);
    	}
    	else
    		return;
//    	instructionsScrollPane.setVisible(false);
    	mainPanel.add(agentDesignPanel, BorderLayout.SOUTH);
    	mainPanel.validate();
    }
    
    public void removeAgentDesignPanel() {
    	if (agentDesignPanel != null) {
    		mainPanel.remove(agentDesignPanel);
    		instructionsScrollPane.setVisible(true);
    		mainPanel.update(mainPanel.getGraphics());
    		mainPanel.validate();
    		agentDesignPanel = null;
    	}
    }
}
