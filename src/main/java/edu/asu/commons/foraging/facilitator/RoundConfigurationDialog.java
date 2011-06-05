package edu.asu.commons.foraging.facilitator;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edu.asu.commons.foraging.conf.RoundConfiguration;


public class RoundConfigurationDialog {

	private RoundConfiguration roundConfiguration;
	
	private JPanel panel = null;	
	private JTextField roundTime = null;	
	private JTextField clientsPerGroup = null;	
	private JTextField foodRate = null;	
	private JCheckBox privateProperty = null;
	private JCheckBox practiceRound = null;
	private JCheckBox displayGroupTokens = null;
	private JTextField boardWidth = null;
	private JTextField boardHeight = null;
	private JTextField sanctionPenalty = null;
	private JTextField sanctionCost = null;	
	private JTextField dollarsPerToken = null;
	private JTextArea instructions = null;
	private boolean readOnly;

		
	public RoundConfigurationDialog(RoundConfiguration roundConfiguration, boolean readOnly) {
		this.roundConfiguration = roundConfiguration;
		this.readOnly = readOnly;
		createPanel();
	}
	
	private void createPanel() {
		if (panel == null) {						
			panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(createBoardSizePanel(), null);
			panel.add(Box.createVerticalGlue());
			panel.add(createSanctionPanel(), null);
			panel.add(Box.createVerticalGlue());
			panel.add(createCheckBoxPanel(), null);			
			panel.add(Box.createVerticalGlue());
			panel.add(createSimpleValuePanel(), null);
			panel.add(Box.createVerticalGlue());
			panel.add(createInstructionsPanel(), null);
			panel.add(Box.createVerticalGlue());
		}
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	private JPanel createSanctionPanel() {
		JPanel sanctionPanel = new JPanel(new GridLayout(2, 4));
		sanctionPanel.setBorder(BorderFactory.createEtchedBorder() );
		
		sanctionPanel.add(new JLabel("Sanction"), null);
		sanctionPanel.add(new JLabel(""), null);
		sanctionPanel.add(new JLabel(""), null);
		sanctionPanel.add(new JLabel(""), null);
		
		sanctionPanel.add(new JLabel("Penalty"), null);
		sanctionPanel.add(getSanctionPenalty(), null);	
		sanctionPanel.add(new JLabel("Cost"), null);
		sanctionPanel.add(getSanctionCost(), null);
		
		return sanctionPanel;
	}
	
	private JPanel createBoardSizePanel() {
		JPanel boardSizePanel = new JPanel(new GridLayout(2, 4));
		boardSizePanel.setBorder(BorderFactory.createEtchedBorder() );
		
		boardSizePanel.add(new JLabel("Board Size"), null);
		boardSizePanel.add(new JLabel(""), null);
		boardSizePanel.add(new JLabel(""), null);
		boardSizePanel.add(new JLabel(""), null);
		
		boardSizePanel.add(new JLabel("Width"), null);
		boardSizePanel.add(getBoardWidth(), null);	
		boardSizePanel.add(new JLabel("Height"), null);
		boardSizePanel.add(getBoardHeight(), null);
		
		return boardSizePanel;
	}
	private JPanel createCheckBoxPanel() {
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
		
		checkBoxPanel.add(this.getPrivateProperty());
		checkBoxPanel.add(this.getPracticeRound());
		checkBoxPanel.add(this.getDisplayGroupTokens());
		
		return checkBoxPanel;
	}
	
	private JPanel createSimpleValuePanel() {
		JPanel simpleValuePanel = new JPanel(new GridLayout(4, 3));	
		//simpleValuePanel.setSize(new Dimension(150, 280));
		
		simpleValuePanel.add(new JLabel("Round Time"), null);
		simpleValuePanel.add(getRoundTime(), null);			
		simpleValuePanel.add(new JLabel("sec"), null);
				
		simpleValuePanel.add(new JLabel("Food Rate"), null);
		simpleValuePanel.add(getFoodRate(), null);			
		simpleValuePanel.add(new JLabel("(0-1)"), null);
		
		simpleValuePanel.add(new JLabel("Clients/Group"), null);
		simpleValuePanel.add(getClientsPerGroup(), null);			
		simpleValuePanel.add(new JLabel(""), null);	
		
		simpleValuePanel.add(new JLabel("Dollars per Token"), null);
		simpleValuePanel.add(getDollarsPerToken(), null);
		simpleValuePanel.add(new JLabel(""), null);
		
		return simpleValuePanel;
	}
	
	private JScrollPane createInstructionsPanel() {
		JPanel panel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(boxLayout);
		
		panel.add(new JLabel("Instructions"), null);
		panel.add(getInstructions(), null);
				
		return new JScrollPane(panel);
	}
	
	
	public JTextField getRoundTime() {
		if (roundTime == null) {			
			roundTime = new JTextField(roundConfiguration.getRoundDuration() + "");
			roundTime.setEditable(!readOnly);
		}
		return roundTime;
	}
	public JTextField getFoodRate() {
		if (foodRate == null) {
			foodRate = new JTextField(roundConfiguration.getRegrowthRate() + "");
			foodRate.setEditable(!readOnly);
		}
		return foodRate;
	}
	public JTextField getClientsPerGroup() {
		if (clientsPerGroup == null) {
			clientsPerGroup = new JTextField(roundConfiguration.getClientsPerGroup() + "");
			clientsPerGroup.setEditable(!readOnly);
		}
		return clientsPerGroup;
	}
	
	public JTextArea getInstructions() {
		if (instructions == null) {
			instructions  = new JTextArea(roundConfiguration.getInstructions());
			instructions.setEditable(!readOnly);
		}
		return instructions;
	}
	
	public JCheckBox getPrivateProperty() {
		if (privateProperty == null) {
			privateProperty = new JCheckBox("Private Property", roundConfiguration.isPrivateProperty());
			privateProperty.setEnabled(!readOnly);
		}
		return privateProperty;
	}
	public JCheckBox getPracticeRound() {
		if (practiceRound == null) {
			practiceRound = new JCheckBox("Practice Round", roundConfiguration.isPracticeRound());
			practiceRound.setEnabled(!readOnly);
		}
		return practiceRound;
	}

	public JCheckBox getDisplayGroupTokens() {
		if (displayGroupTokens == null) {
			displayGroupTokens = new JCheckBox("Display Group Tokens", roundConfiguration.shouldDisplayGroupTokens());
			displayGroupTokens.setEnabled(!readOnly);
		}
		return displayGroupTokens;
	}
	
	public JTextField getBoardHeight() {
		if (boardHeight == null) {
			boardHeight = new JTextField(roundConfiguration.getResourceDepth() + "");
			boardHeight.setEditable(!readOnly);
		}
		return boardHeight;
	}
	public JTextField getBoardWidth() {
		if (boardWidth == null) {
			boardWidth = new JTextField(roundConfiguration.getResourceWidth() + "");
			boardWidth.setEditable(!readOnly);
		}
		return boardWidth;
	}
	public JTextField getSanctionCost() {
		if (sanctionCost == null) {
			sanctionCost = new JTextField(roundConfiguration.getSanctionCost() + "");
			sanctionCost.setEditable(!readOnly);
		}
		return sanctionCost;
	}
	public JTextField getSanctionPenalty() {
		if (sanctionPenalty == null) {
			sanctionPenalty = new JTextField(roundConfiguration.getSanctionMultiplier() + "");
			sanctionPenalty.setEditable(!readOnly);
		}
		return sanctionPenalty;
	}
	
	public JTextField getDollarsPerToken() {
		if (dollarsPerToken == null) {
			dollarsPerToken = new JTextField(roundConfiguration.getDollarsPerToken() + "");
			dollarsPerToken.setEditable(!readOnly);
		}
		return dollarsPerToken;
	}
}
