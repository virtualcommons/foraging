package edu.asu.commons.foraging.client;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.asu.commons.foraging.event.EnforcementRankingRequest;
import edu.asu.commons.foraging.model.EnforcementMechanism;
import edu.asu.commons.net.Identifier;

/**
 * $Id: EnforcementPanel.java 529 2010-08-17 00:08:01Z alllee $
 * 
 * Enforcement panel is used to vote enforcement mechanism
 *  
 * @author dbarge
 * @version $Revision: 529 $
 */

@SuppressWarnings("serial")
public class EnforcementPanel extends JPanel {

    private ForagingClient client;


    private String[] votes = { "1", "2", "3","4"};
    private EnforcementMechanism[] enforcementOptions = EnforcementMechanism.values(); 
    //    private String[] enforcementOptions = {
    //            "No Enforcement - Click here for more info            ",
    //            "Everyone sanctions - Click here for more info        ",
    //            "Randomly picked monitering - Click here for more info",
    //            "Random sanctioning - Click here for more info        "
    //    };

    private String[] enforcementText = {
            "Everybody can harvest. Nobody can subtract tokens<br>" +
            "from others<br>",

            "Each participant can reduce the token amount of<br>" +
            "another participant by two tokens at a cost of <br>" +
            "one by pressing the numeric key that identifies<br>" +
            "the other participant.<br>",

            "Randomly one of the participants is selected to<br>" +
            "be the monitoring participant. This participant<br>" +
            "can not harvest, but can force another particip<br>" +
            "ant to pay a token to the monitoring participan<br>" +
            "by pressing the responding numeric key. At the <br>" +
            "end of the round each participant who could har<br>" +
            "vest pays 25% of the earning to the monitoring <br>" +
            "participant.<br>",

            "Same as two, but now each participant takes turns<br>" +
            "of 48 seconds randomly assigned by the computer.<br>"
    };

    public EnforcementPanel (ForagingClient client) {
        this();
        this.client = client;
        this.clientId = client.getId();
    }

    private Identifier clientId;
    private JPanel votingPanel;
//    private JButton reset;
//    private JButton sendMyVotes;
    private JPanel instructionsPanel;
    private SixChoicePanel[] newPanel;

    private int noOfEnforcements = EnforcementMechanism.values().length;

    private int currentRankingInformation[];

    private JPanel buttonPanel;

    public EnforcementPanel () 
    {
        newPanel = new SixChoicePanel[4];
    }

    public String getVotedEnforcementOptions(int index){
        return this.enforcementText[index];
    }

    private JPanel getInstructionPanel()
    {
        JPanel instructionPanel = new JPanel();

        //instructionPanel.setBackground(color);
        instructionPanel.setLayout(new BoxLayout(instructionPanel, BoxLayout.X_AXIS));   
        instructionPanel.setBorder(BorderFactory.createTitledBorder("Enforcement Instructions"));

        //create Text area and JSCroll pane for it
        String instructions = client.getDataModel().getRoundConfiguration().getVotingInstructions();

        JTextArea instructionText = new JTextArea(instructions,3,50);
        instructionText.setWrapStyleWord(true);
        JScrollPane scrollForRegulationText = new JScrollPane(instructionText);
        instructionPanel.add(scrollForRegulationText);

        return instructionPanel;

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

    // FIXME: this is extremely inefficient, reimplement later
    private void updateVotingPanel(int currentActive){
        int r,c,i;
        SixChoicePanel temp = null;
//        boolean enableSendButton = true;


        for(r = 0; r < noOfEnforcements; r++)
        {

//            if(newPanel[r].currentRanking == -1)enableSendButton = false;

            if((newPanel[currentActive].currentRanking == newPanel[r].currentRanking) && (r != currentActive))
            {
                newPanel[r].currentRanking = -1;
                newPanel[r].group.clearSelection();
            }
        }


        for(r = 0; r < noOfEnforcements-1; r++)
        {
            for(c = 0; c < noOfEnforcements-1; c++)
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
        for(c = 0; c < noOfEnforcements; c++)
        {
            //		  System.out.print(newPanel[c].getCurrentRanking() +" ");
        }

        votingPanel.setVisible(false);
        remove(votingPanel);
        votingPanel = new JPanel();
        votingPanel.setLayout(new BoxLayout(votingPanel, BoxLayout.Y_AXIS));

        votingPanel.add(getInstructionPanel());


        for(i=0; i < noOfEnforcements; i++) {
            votingPanel.add(newPanel[i].enforcementPanel);
        }

        votingPanel.setVisible(true);
        add(votingPanel, BorderLayout.CENTER);

//        if(enableSendButton) {
//            sendMyVotes.setEnabled(true);
//            buttonPanel.setVisible(true);
//            add(buttonPanel, BorderLayout.SOUTH);
//        }
        revalidate();
    }

    private String getVoteString(){

        StringBuilder sb = new StringBuilder();

        for(int c = 0; c < noOfEnforcements; c++)
        {
            sb.append("\nEnforcement "+(newPanel[c].getCurrentRanking()+1));
        }
        return(sb.toString());  	  
    }

    public void sendEnforcementVotes() {
        int i;
        for(i=0; i < noOfEnforcements; i++) {
            if(newPanel[i].currentRanking == -1)
                this.currentRankingInformation[i] = -1;
            else
                this.currentRankingInformation[i] = newPanel[i].getCurrentRanking();
        }
        client.transmit(new EnforcementRankingRequest(clientId, currentRankingInformation));
    }

    public void initGuiComponents(){

        // remove(enforcementInstructionsScrollPane);
        // remove(messageScrollPane);
        this.currentRankingInformation = new int[4];
        this.newPanel = new SixChoicePanel[4];
        setBackground(Color.lightGray);
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        votingPanel = new JPanel();
        votingPanel.setLayout(new BoxLayout(votingPanel, BoxLayout.Y_AXIS));

        //add the instruction panel as the first panel in voting panel.
        instructionsPanel = getInstructionPanel();
        votingPanel.add(instructionsPanel);




        for(int i=0; i<noOfEnforcements; i++) {

            //newPanel[i] = new SixChoicePanel(s, votes, enforcementData.getEnforcementID(), getColor(i));
            //newPanel[i] = new SixChoicePanel(s, votes, client.getEnforcementID(), getColor(i));
            newPanel[i] = new SixChoicePanel(enforcementOptions[i], votes, i, getColor(i));
            votingPanel.add(newPanel[i].getEnforcementPanel(i));
        }

        add(votingPanel, BorderLayout.CENTER);
//        reset = new JButton("Reset All Ranks");
//        reset.setAlignmentX(Component.CENTER_ALIGNMENT);
//        reset.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                for(int i=0; i<noOfEnforcements; i++) {
//                    for(int j=0; j<noOfEnforcements; j++) {
//                        newPanel[i].option[j].setEnabled(true);
//                        newPanel[i].group.clearSelection();
//                        newPanel[i].currentRanking = -1;
//                    }
//                }
//            }
//        });
//        sendMyVotes = new JButton("Send votes");
//        sendMyVotes.setAlignmentX(Component.CENTER_ALIGNMENT);
//        sendMyVotes.setEnabled(false);
//        sendMyVotes.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//
//                int n = JOptionPane.showConfirmDialog(
//                        null, "Are you sure to submit your votes ?"+
//                        "\nBelow is order of your voting" +
//                        getVoteString(),
//                        "Confirm and send votes",
//                        JOptionPane.YES_NO_OPTION);
//
//                if (n == JOptionPane.YES_OPTION) {
//                    GameWindow2D.duration.expire();
//                }
//                if (n == JOptionPane.NO_OPTION) {
//
//                }
//
//            }
//        });
//        buttonPanel = new JPanel();
//        //buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
//        buttonPanel.setLayout(new GridLayout(1,2));
//        buttonPanel.add(reset);        
//        buttonPanel.add(sendMyVotes);
//        buttonPanel.setVisible(true);
//        buttonPanel.repaint();
//        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void initialize() {
        initGuiComponents();
    }
    
    private class SixChoicePanel implements ActionListener{
        String title;
        String description;
        String [] buttonLabels;
        int enforcementID;
        int currentRanking;
        JPanel enforcementPanel;  
        JPanel rankPanel;  
        ButtonGroup group;
        JRadioButton option [];
        Color color;


        public SixChoicePanel(EnforcementMechanism enforcementMechanism, String[] buttonLabels, int enforcementID, Color color ) {
            this.title = enforcementMechanism.getTitle();
            this.description = enforcementMechanism.getDescription();
            this.buttonLabels = buttonLabels;
            this.enforcementID = enforcementID;
            this.color = color;
            this.currentRanking = -1;
            this.option = new JRadioButton[4];
        }
        public int getCurrentRanking(){
            return enforcementID;
        }

        public void actionPerformed(ActionEvent e) {
            String choice = group.getSelection().getActionCommand();
            int buttonNo = Integer.parseInt(choice);
            System.out.println("ACTION Choice Selected: " + choice);
            System.out.println("Bno: " + buttonNo);
            System.out.println("CurrentActive : "+this.enforcementID);
            this.currentRanking = buttonNo;
            updateVotingPanel(this.enforcementID);
        }


        public JPanel getEnforcementPanel(int i){
            enforcementPanel = new JPanel();
            enforcementPanel.setBackground(color);
            enforcementPanel.setLayout(new BoxLayout(enforcementPanel, BoxLayout.Y_AXIS));   
            enforcementPanel.setBorder(BorderFactory.createTitledBorder(title));

            //create Text area and JSCroll pane for it

            JTextArea regulationText = new JTextArea(title,3,50);
            regulationText.setText(description);
            regulationText.setWrapStyleWord(true);
            JScrollPane scrollForRegulationText = new JScrollPane(regulationText);
            enforcementPanel.add(scrollForRegulationText);

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
            enforcementPanel.add(rankPanel);
            return enforcementPanel;
        }

    }

}
