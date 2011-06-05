package edu.asu.commons.foraging.facilitator;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.conf.ServerConfiguration;




public class ConfigurationDialog {
		
//	private JFrame parentFrame;
	private JFrame tabbedPaneFrame;
	private List<RoundConfigurationDialog> roundConfigurationDialogs = new ArrayList<RoundConfigurationDialog>();			
	private Facilitator facilitator;
	private boolean readOnly = false;
	private ButtonGroup group;
	
	/*
	 * Constructor
	 */
	ConfigurationDialog(Facilitator facilitator, boolean readOnly){
//		this.parentFrame = parentFrame;
		this.facilitator = facilitator;
		this.readOnly = readOnly;
		group = new ButtonGroup();
		tabbedPaneFrame = new JFrame("Configuration Parameters");
		Container content = tabbedPaneFrame.getContentPane();
		content.setLayout(new BorderLayout(7, 7));
		content.add(getConfigurationTabs(), BorderLayout.CENTER);
		content.add(getButtonPanel(), BorderLayout.SOUTH);
		tabbedPaneFrame.pack();
		tabbedPaneFrame.setSize(new Dimension(500, 650));
		tabbedPaneFrame.setResizable(false);
//		centerOnParentFrame();
		tabbedPaneFrame.setVisible(true);			
	}
	
	private JPanel getButtonPanel() {
		JPanel panel = new JPanel();
		//panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setLayout(new GridLayout(1, 3));
		panel.add(createStartExperimentButton());
		panel.add(createOkButton());
		panel.add(createCancelButton());
		return panel;
	}
	
	private JTabbedPane getConfigurationTabs() {
		JTabbedPane configurationTabPane = new JTabbedPane();
	
		ServerConfiguration configuration = facilitator.getServerConfiguration();
		List<RoundConfiguration> rounds = configuration.getAllParameters();
		int roundCount = configuration.getNumberOfRounds();
		for (int roundIndex = 0; roundIndex < roundCount; roundIndex++) {
			RoundConfiguration roundConfiguration = rounds.get(roundIndex);
			RoundConfigurationDialog roundConfigDlg = new RoundConfigurationDialog(roundConfiguration, readOnly);
			roundConfigurationDialogs.add(roundConfigDlg);
			configurationTabPane.addTab("Round " + roundIndex, roundConfigDlg.getPanel());				
		}		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel label = new JLabel("Select one of the treatments");
		panel.add(label);
		panel.add(Box.createVerticalGlue());
//		panel.add(createFullVisionPanel(),null);
		panel.add(Box.createVerticalGlue());
//		panel.add(createLimitedVisionPanel(),null);
		panel.add(Box.createVerticalGlue());
		panel.add(getInstructionPanel(), null);
		panel.add(Box.createVerticalGlue());
		configurationTabPane.addTab("Type of treatment", panel);
		return configurationTabPane;
	}
	
//	private JPanel createFullVisionPanel() {
//		JRadioButton fullVision;
//		fullVision = new JRadioButton("Complete field of vision");
//		fullVision.setActionCommand("1");
//		group.add(fullVision);
//		fullVision.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				updateTreatment(event.getActionCommand());
//				System.out.println(" "+event.getActionCommand());
//			}
//		});
//		JPanel fullVisionPanel = new JPanel(new GridLayout(2, 2));
//		fullVisionPanel.setBorder(BorderFactory.createEtchedBorder() );
//		fullVisionPanel.add(new JLabel("Full"), null);
//		fullVisionPanel.add(new JLabel(""), null);	
//		fullVisionPanel.add(new JLabel("Vision"), null);
//		fullVisionPanel.add(fullVision, null);
//		return fullVisionPanel;
//	}
//	
//	private JPanel createLimitedVisionPanel() {
//		JRadioButton limitedVision;
//		limitedVision = new JRadioButton("Limited field of vision");
//		limitedVision.setActionCommand("0");
//		group.add(limitedVision);
//		limitedVision.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				updateTreatment(event.getActionCommand());
//				System.out.println(" "+event.getActionCommand());
//			}
//		});
//		JPanel limitedVisionPanel = new JPanel(new GridLayout(2, 2));
//		limitedVisionPanel.setBorder(BorderFactory.createEtchedBorder() );
//		limitedVisionPanel.add(new JLabel("Limited"), null);
//		limitedVisionPanel.add(new JLabel(""), null);	
//		limitedVisionPanel.add(new JLabel("Vision"), null);
//		limitedVisionPanel.add(limitedVision, null);
//		return limitedVisionPanel;
//	}

	
	private JScrollPane getInstructionPanel() {
		ServerConfiguration configuration = facilitator.getServerConfiguration();
		
		JPanel panel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(boxLayout);
		
		panel.add(new JLabel("Instructions"), null);
		panel.add(new JTextArea(configuration.getGeneralInstructions()), null);
				
		return new JScrollPane(panel);
	}

	private JButton createStartExperimentButton() {
		JButton startExperimentButton = new JButton("Start Exp");
		startExperimentButton.setEnabled(!readOnly);
		startExperimentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				updateConfiguration();
				tabbedPaneFrame.setVisible(false);
				ConfigurationDialog.this.facilitator.sendBeginExperimentRequest();
			}
		});
		
		return startExperimentButton;
	}
	
	private JButton createOkButton() {
		JButton okButton = new JButton("Ok");		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!readOnly) {					
					updateConfiguration();
				}
				tabbedPaneFrame.setVisible(false);
			}
		});
		
		return okButton;
	}
	
	private JButton createCancelButton() {
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setEnabled(!readOnly);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				tabbedPaneFrame.setVisible(false);				
			}
		});
		
		return cancelButton;
	}
	
	private void updateConfiguration() {/*
		List<RoundConfiguration> rounds = new ArrayList<RoundConfiguration>();
		
		ServerConfiguration configuration = facilitator.getServerConfiguration();				
		int roundCount = configuration.getNumberOfRounds();
		for (int roundIndex = 0; roundIndex < roundCount; roundIndex++) {
			RoundConfigurationDialog roundConfigDlg = roundConfigurationDialogs.get(roundIndex);					
			RoundConfiguration roundConfiguration = new RoundConfiguration();					
			roundConfiguration.setProperty("board-width", roundConfigDlg.getBoardWidth().getText());
			roundConfiguration.setProperty("board-height", roundConfigDlg.getBoardHeight().getText());
			roundConfiguration.setProperty("round-time", roundConfigDlg.getRoundTime().getText());
			roundConfiguration.setProperty("clients-per-group", roundConfigDlg.getClientsPerGroup().getText());
			roundConfiguration.setProperty("food-rate", roundConfigDlg.getFoodRate().getText());
			roundConfiguration.setProperty("private-property", Boolean.valueOf(roundConfigDlg.getPrivateProperty().getModel().isSelected()).toString());
			roundConfiguration.setProperty("practice-round", Boolean.valueOf(roundConfigDlg.getPracticeRound().getModel().isSelected()).toString());
//			roundConfiguration.setProperty("show-other-subjects", Boolean.valueOf(roundConfigDlg.getDisplayOtherClients().getModel().isSelected()).toString());
			roundConfiguration.setProperty("display-group-tokens", Boolean.valueOf(roundConfigDlg.getDisplayGroupTokens().getModel().isSelected()).toString());
//			roundConfiguration.setProperty("show-food", Boolean.valueOf(roundConfigDlg.getDisplayFood().getModel().isSelected()).toString());			
			roundConfiguration.setProperty("sanction-penalty", roundConfigDlg.getSanctionPenalty().getText());
			roundConfiguration.setProperty("sanction-cost", roundConfigDlg.getSanctionCost().getText());
			roundConfiguration.setProperty("dollars-per-token", roundConfigDlg.getDollarsPerToken().getText());
			roundConfiguration.setProperty("instructions", roundConfigDlg.getInstructions().getText());
			//dollars-per-token
			rounds.add(roundConfiguration);
			
			System.out.println("Round " + roundIndex + ": display-group-tokens: " + Boolean.valueOf(roundConfigDlg.getDisplayGroupTokens().getModel().isSelected()).toString());
		}
		
		ConfigurationDialog.this.facilitator.setRoundParameters(rounds);
		ConfigurationDialog.this.facilitator.sendSetConfigRequest();*/

	}
	
//	private void updateTreatment(String treatmentType) {
//		//List<RoundConfiguration> rounds = new ArrayList<RoundConfiguration>();
//		//RoundConfiguration roundConfiguration = new RoundConfiguration();
//		currentTreatment = TreatmentType.FULLVISION;
//		if(treatmentType.equals("0")) {
//			currentTreatment = TreatmentType.LIMITEDVISION;
//		}
//		else {
//			currentTreatment = TreatmentType.FULLVISION;
//		}
//		//roundConfiguration.setProperty("tokens-field-of-vision", Boolean.valueOf(flag).toString());
//		//roundConfiguration.setProperty("subjects-field-of-vision", Boolean.valueOf(flag).toString());
//		//rounds.add(roundConfiguration);
//		//ConfigurationDialog.this.facilitator.setRoundParameters(rounds);
//		//ConfigurationDialog.this.facilitator.sendSetConfigRequest();
//		ConfigurationDialog.this.facilitator.sendTreatmentTypeRequest(currentTreatment);
//
//	}
	
//	private void centerOnParentFrame() {
//		Dimension parentFrameDimension = parentFrame.getSize();
//		Dimension windowDimension = tabbedPaneFrame.getSize();
//		int x = (parentFrameDimension.width - windowDimension.width) / 2;
//		int y = (parentFrameDimension.height - windowDimension.height) / 2;
//		tabbedPaneFrame.setLocation(x, y);
//	}
}
