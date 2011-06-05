package edu.asu.commons.foraging.client;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLEditorKit;

import edu.asu.commons.foraging.event.RegulationRankingRequest;
import edu.asu.commons.foraging.event.SubmitRegulationRequest;
import edu.asu.commons.foraging.model.RegulationData;
import edu.asu.commons.net.Identifier;



/**
 * $Id: RegulationPanel.java 522 2010-06-30 19:17:48Z alllee $
 * 
 * Sanctioning panel is used to create regulations and 
 * enforcement mechanism
 * 
 * FIXME: split this functionality out into two different panels, one for submission and the other for voting.
 *  
 * @author dbarge
 * @version $Revision: 522 $
 */

@SuppressWarnings("serial")
public class RegulationPanel extends JPanel {

	private ForagingClient client;

	public RegulationPanel (ForagingClient client) {
	    this();
		this.client = client;
//		client.getEventChannel().add(this, new EventTypeProcessor<RegulationEvent>(RegulationEvent.class) {
//			public void handle(final RegulationEvent regulationEvent) {
//				boolean votingFlag=false;
//				RegulationData regulationData = null;
//				regulations = regulationEvent.getAllRegulations();
//				//	System.out.println("Regulation received : "+regulations.size());
//				noOfRegulations = regulations.size();
//				for (Identifier targetId : regulations.keySet()) {
//					regulationData = regulations.get(targetId);
//					if(regulationData.isVoting())votingFlag = true;
//					break;
//				}
//				if(votingFlag)
//				{
//					votedRegulation = regulationData; 
//					Utils.notify(GameWindow2D.regulationVotesSignal);
//				}
//				else
//				{
//					//System.out.println("Finding my ID");
//					findAndSetMyRegulationID();
//					Utils.notify(GameWindow2D.regulationSignal);
//				}
//			}
//		});
	}

	public RegulationPanel() {
//		regulations = new ArrayList<RegulationData>();
	    // FIXME: get rid of hardcoded constants, this should be dynamic based on the size of the group.
		newPanel = new SixChoicePanel[5];
		noOfRegulations = 5;
	}

	private int noOfRegulations;

	private int[] currentRankingInformation;

	private SixChoicePanel[] newPanel;

	private JPanel votingPanel;
	private JPanel instructionsPanel;
//	private JPanel buttonPanel;
//	private JButton sendMyVotes;
//	private JButton reset;

	private String[] votes = { "1", "2", "3","4", "5"};

	private JScrollPane messageScrollPane;

	private JScrollPane regulationsInstructionsScrollPane;

	private JTextPane messageWindow;

	private List<Identifier> participants;

	private JEditorPane regulationsInstructionsPane;

//	private void findAndSetMyRegulationID()
//	{
//		for (Identifier targetId : regulations.keySet()) {
//			if(regulations.get(targetId).getRegulationText().compareTo(message) == 0){
//				client.setRegulationID(regulations.get(targetId).getRegulationID());
//				//client.setEnforcementID(regulations.get(targetId).getToken());
//				client.setToken(regulations.get(targetId).getToken());
//				//System.out.println("My RegID:"+client.getRegulationID());
//				//System.out.println("Token:"+client.getEnforcementID());
//				return;
//			}
//		}    		
//	}
	private void addStylesToMessageWindow() {
		StyledDocument styledDocument = messageWindow.getStyledDocument();
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

	// FIXME: messy, refactor after experiment.
	private void updateVotingPanel(int currentActive){
		int r,c,i;
		SixChoicePanel temp = null;
//		boolean enableSendButton = true;

		//  	  System.out.println("Active panel: "+SixChoicePanel.currentActive);
        // The below for loop is used to clear off radio button of the panel whose ranking conflicts
		// with the new panel's radio button
		
		
		for(r = 0; r < noOfRegulations; r++)
		{
			System.out.print(newPanel[r].currentRanking+" ");
//			if(newPanel[r].currentRanking == -1) enableSendButton = false;

			if((newPanel[currentActive].currentRanking == newPanel[r].currentRanking) && (r != currentActive))
			{
				newPanel[r].currentRanking = -1;
				newPanel[r].group.clearSelection();
			}
		}

		//The below for loops are used for sorting the panels when the ranks are 
		//changed

		for(r = 0; r < noOfRegulations-1; r++)
		{
			for(c = 0; c < noOfRegulations-1; c++)
			{
				if((newPanel[c].currentRanking > newPanel[c+1].currentRanking)&&(newPanel[c+1].currentRanking != -1))
				{
					temp = newPanel[c];
					newPanel[c] = newPanel[c+1];
					newPanel[c+1] = temp;
				}
				if((newPanel[c].currentRanking < newPanel[c+1].currentRanking)&&(newPanel[c].currentRanking == -1))
				{
					temp = newPanel[c];
					newPanel[c] = newPanel[c+1];
					newPanel[c+1] = temp;				  
				}
			}
		}
		
		votingPanel.setVisible(false);
		remove(votingPanel);
		votingPanel = new JPanel();
		votingPanel.setLayout(new BoxLayout(votingPanel, BoxLayout.Y_AXIS));

		votingPanel.add(getInstructionPanel());


		for(i=0; i < noOfRegulations; i++) {
			votingPanel.add(newPanel[i].regulationPanel);
		}

		votingPanel.setVisible(true);
		add(votingPanel, BorderLayout.CENTER);

//		if(enableSendButton) {
//			sendMyVotes.setEnabled(true);
//			buttonPanel.setVisible(true);
//			add(buttonPanel, BorderLayout.SOUTH);
//		}
		revalidate();
	}

	public void sendRegulation() {
		client.transmit(new SubmitRegulationRequest(client.getId(), messageWindow.getText()));
	}

	public void sendRegulationVotes() {
		System.out.println("Regulation votes ready to be sent");
		// System.err.println("message: " + message);
		int i;
		for(i=0; i < noOfRegulations; i++) {
			if(newPanel[i].currentRanking == -1)
				this.currentRankingInformation[i] = -1;
			else
				this.currentRankingInformation[i] = newPanel[i].getCurrentRanking();
		}

		client.transmit(new RegulationRankingRequest(client.getId(), currentRankingInformation));
	}

	private Color getColor(int i)
	{
		Color color = null;
		if(i==0) color = new Color(153,153,204);
		if(i==1) color = new Color(204,153,153);
		if(i==2) color = new Color(153,204,102);
		if(i==3) color = new Color(204,204,102);
		if(i==4) color = new Color(255,255,153); 	  
		return color;
	}

	private String getVoteString(){

		StringBuilder sb = new StringBuilder();

		for(int c = 0; c < noOfRegulations; c++)
		{
			sb.append("\nRegulation "+(newPanel[c].getCurrentRanking()+1));
		}
		return(sb.toString());  	  
	}

	public void initRegulationVotingComponents(){

		if (regulationsInstructionsScrollPane != null) {
			remove(regulationsInstructionsScrollPane);
		}
		if (messageScrollPane != null) {
			remove(messageScrollPane);
		}
		this.currentRankingInformation = new int[5];
		this.newPanel = new SixChoicePanel[5];
		setBackground(Color.lightGray);
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		votingPanel = new JPanel();
		votingPanel.setLayout(new BoxLayout(votingPanel, BoxLayout.Y_AXIS));

		//add the instruction panel as the first panel in voting panel.
		instructionsPanel = getInstructionPanel();
		votingPanel.add(instructionsPanel);

		// this is for dummy regulation data for testing
		// start 
//		for(int i=0; i<5; i++) {
//			RegulationData regulationData1 = new RegulationData();
//			regulationData1.setRegulationID(i);        
//			regulationData1.setRegulationText("Test Regulation " + i);
//			Identifier temp = new Identifier.Base (){};
//			System.out.println("Idn : " + temp);
//			regulations.put(temp, regulationData1);
//		}
//		System.out.println(regulations.size());
		// end

		//for(int i=0; i<5; i++) {
		List<RegulationData> submittedRegulations = client.getDataModel().getSubmittedRegulations();
//		List<RegulationData> submittedRegulations = 
//		    Arrays.asList(new RegulationData(new Identifier.Base(), "Regulation 1", 0),
//		            new RegulationData(new Identifier.Base(), "Regulation 2", 1),
//		            new RegulationData(new Identifier.Base(), "Regulation 3", 2),
//		            new RegulationData(new Identifier.Base(), "Regulation 4", 3),
//		            new RegulationData(new Identifier.Base(), "Regulation 5", 4));

		for (RegulationData regulationData : submittedRegulations) {
		    System.err.println("creating six choice panel from regulation data: " + regulationData);
			// FIXME: are you aware that this code is completely unnecessary?  You're creating a StringBuilder for no reason at all -
			// regulationData.getText() is already a String!
//			StringBuilder sb = new StringBuilder();
//			sb.append(regulationData.getText());
//			String s = sb.toString();
		    int index = regulationData.getIndex();
		    
			newPanel[index] = new SixChoicePanel(votes, index, getColor(index));
			//  votingPanel.add(newPanel[i].getRegulationPanel(i,client.getDataModel().getRoundConfiguration().getRegulationInstructions()));
			votingPanel.add(newPanel[index].getRegulationPanel(index,regulationData.getText()));			
		}

		add(votingPanel, BorderLayout.CENTER);
//		reset = new JButton("Reset All Ranks");
//		reset.setAlignmentX(Component.CENTER_ALIGNMENT);
//		reset.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				for(int i=0; i<noOfRegulations; i++) {
//					for(int j=0; j<noOfRegulations; j++) {
//						newPanel[i].option[j].setEnabled(true);
//						newPanel[i].group.clearSelection();
//						newPanel[i].currentRanking = -1;
//					}
//				}
//			}
//		});
//		sendMyVotes = new JButton("Send votes");
//		sendMyVotes.setAlignmentX(Component.CENTER_ALIGNMENT);
//		sendMyVotes.setEnabled(false);
//		sendMyVotes.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//
//				int n = JOptionPane.showConfirmDialog(
//						null, "Are you sure to submit your votes ?"+
//						"\nBelow is order of your voting" +
//						getVoteString(),
//						"Confirm and send votes",
//						JOptionPane.YES_NO_OPTION);
//
//				if (n == JOptionPane.YES_OPTION) {
//					GameWindow2D.duration.expire();
//				}
//				if (n == JOptionPane.NO_OPTION) {
//
//				}
//
//			}
//		});
//		buttonPanel = new JPanel();
//		buttonPanel.setLayout(new GridLayout(1,2));
//		buttonPanel.add(reset);        
//		buttonPanel.add(sendMyVotes);

//		add(buttonPanel, BorderLayout.SOUTH);
	}

	private JPanel getInstructionPanel()
	{
		JPanel instructionPanel = new JPanel();
		//instructionPanel.setBackground(color);
		instructionPanel.setLayout(new BoxLayout(instructionPanel, BoxLayout.X_AXIS));   
		instructionPanel.setBorder(BorderFactory.createTitledBorder("Regulation Instruction"));
		
//		String instructions = client.getDataModel().getRoundConfiguration().getVotingInstructions();
		String instructions = "Voting instructions";
		JTextArea instructionText = new JTextArea(instructions, 3, 50);
		instructionText.setWrapStyleWord(true);
		instructionText.setEditable(false);
		JScrollPane scrollForRegulationText = new JScrollPane(instructionText,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		instructionPanel.add(scrollForRegulationText);
		
		return instructionPanel;

	}

	public void initialize() {
		setLayout(new BorderLayout(4, 4));
		messageWindow = new JTextPane();
		messageWindow.setBorder(BorderFactory.createTitledBorder("Type your regulation here"));
		messageWindow.requestFocusInWindow();
		messageScrollPane = new JScrollPane(messageWindow);
		addStylesToMessageWindow();

		// orient the components in true lazyman fashion.

		regulationsInstructionsPane = new JEditorPane();
		regulationsInstructionsPane.setContentType("text/html");
		regulationsInstructionsPane.setEditorKit(new HTMLEditorKit());
		regulationsInstructionsPane.setEditable(false);
		regulationsInstructionsPane.setBorder(BorderFactory.createTitledBorder("Regulation details"));
		regulationsInstructionsScrollPane = new JScrollPane(regulationsInstructionsPane);

		//FIXME: Need to fetch the regulation instructions over here
		//dummy regulation instructions are

		//regulationsInstructionsPane.setText(client.getDataModel().getRoundConfiguration().getRegulationInstructions());
//		regulationsInstructionsPane.setText(client.getDataModel().getRoundConfiguration().getRegulationInstructions());

		setInstructions("Regulation instructions");
		add(regulationsInstructionsScrollPane, BorderLayout.NORTH);
		add(messageScrollPane, BorderLayout.CENTER);

	}
	
	public void setInstructions(String instructions) {
        regulationsInstructionsPane.setText(instructions);	    
	}

	public void clear() {
		participants.clear();
	}


	private class SixChoicePanel implements ActionListener{
		//String title;
		String [] buttonLabels;
		// the index of the regulation that is being rendered in this panel
		int regulationID;
		int currentRanking;
		JPanel regulationPanel;  
		JPanel rankPanel;  
		ButtonGroup group;
		JRadioButton option [];
		Color color;

		public SixChoicePanel(String[] buttonLabels, int regulationID, Color color ) {
			//this.title = title;
			this.buttonLabels = buttonLabels;
			this.regulationID = regulationID;
			this.color = color;
			this.currentRanking = -1;
			this.option = new JRadioButton[5];
		}

		public int getCurrentRanking(){
			return regulationID;
		}

		public void actionPerformed(ActionEvent e) {
			String choice = group.getSelection().getActionCommand();
			int buttonNo = Integer.parseInt(choice);
			System.out.println("ACTION Choice Selected: " + choice);
			System.out.println("Bno: " + buttonNo);
			System.out.println("CurrentActive : "+this.regulationID);
			this.currentRanking = buttonNo;
			updateVotingPanel(this.regulationID);
		}

		
		public JPanel getRegulationPanel(int i, String information){
			regulationPanel = new JPanel();
			regulationPanel.setBackground(color);
			regulationPanel.setLayout(new BoxLayout(regulationPanel, BoxLayout.Y_AXIS));   
			regulationPanel.setBorder(BorderFactory.createTitledBorder("Regulation "+(i+1)));

			//create Text area and JSCroll pane for it

			JTextArea regulationText = new JTextArea(information, 3, 50);
			regulationText.setWrapStyleWord(true);
			JScrollPane scrollForRegulationText = new JScrollPane(regulationText);
			regulationPanel.add(scrollForRegulationText);

			rankPanel = new JPanel();  
			rankPanel.setBackground(color);
			rankPanel.setLayout(new BoxLayout(rankPanel, BoxLayout.X_AXIS));   
			rankPanel.setBorder(BorderFactory.createTitledBorder("Rank"));
			group = new ButtonGroup();
			int length = buttonLabels.length;  // Assumes even length

			for(int j=0; j<length; j++) {
				option[j] = new JRadioButton(buttonLabels[j]);
				option[j].setActionCommand(buttonLabels[j]);
				group.add(option[j]);
				option[j].addActionListener(this);
				rankPanel.add(option[j]);
			}
			regulationPanel.add(rankPanel);
			return regulationPanel;
		}

	}

}
