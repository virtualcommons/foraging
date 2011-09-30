/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * VotingForm.java
 *
 * Created on Sep 29, 2011, 3:52:52 AM
 */
package edu.asu.commons.foraging.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ButtonModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import edu.asu.commons.foraging.client.ForagingClient;
import edu.asu.commons.foraging.event.VoteRuleRequest;

/**
 *
 * @author alllee
 */
public class VotingForm extends javax.swing.JPanel {
    
    public final static String NAME = "Voting form";
    
    private ForagingClient client;
    
    private List<JRadioButton> radioButtons = new ArrayList<JRadioButton>();
    private List<JLabel> labels = new ArrayList<JLabel>();
    private List<String> rules = new ArrayList<String>();
    /** Creates new form VotingForm */
    public VotingForm() {
        this(Arrays.asList("Rule 1", "Rule 2", "Rule 3", "Rule 4"));
    }
    
    public VotingForm(ForagingClient client) {
        this(client.getCurrentRoundConfiguration().getFixedRules());
        this.client = client;
    }

    public VotingForm(List<String> rules) {
        initComponents();
        this.rules.addAll(rules);
        initForm(rules);
        setName(NAME);
    }
    
    private void initForm(List<String> rules) {
        GroupLayout groupLayout = new GroupLayout(this);
        setLayout(groupLayout);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setAutoCreateContainerGaps(true);
        GroupLayout.SequentialGroup horizontalGroup = groupLayout.createSequentialGroup();
        // horizontal parallel group
        GroupLayout.ParallelGroup horizontalLabelParallelGroup = groupLayout.createParallelGroup();
        horizontalGroup.addGroup(horizontalLabelParallelGroup);
        GroupLayout.ParallelGroup horizontalButtonParallelGroup = groupLayout.createParallelGroup();
        horizontalGroup.addGroup(horizontalButtonParallelGroup);
        
        GroupLayout.SequentialGroup verticalGroup = groupLayout.createSequentialGroup();
        JLabel buttonHeaderLabel = new JLabel("Select one");
        buttonHeaderLabel.setFont(ForagingInterface.DEFAULT_BOLD_FONT);
        horizontalButtonParallelGroup.addComponent(buttonHeaderLabel);
        
        JLabel ruleHeaderLabel = new JLabel("Rule");
        ruleHeaderLabel.setFont(ForagingInterface.DEFAULT_BOLD_FONT);
        horizontalLabelParallelGroup.addComponent(ruleHeaderLabel);
        
        verticalGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(ruleHeaderLabel).addGap(10).addComponent(buttonHeaderLabel));
        
        for (String rule: rules) {
            JRadioButton radioButton = new JRadioButton();                        
            radioButton.setActionCommand(String.valueOf(radioButtons.size()));
            radioButtons.add(radioButton);
            horizontalButtonParallelGroup.addComponent(radioButton);
            JLabel ruleLabel = new JLabel(String.format("%d. %s", radioButtons.size(), rule));
            ruleLabel.setFont(ForagingInterface.DEFAULT_PLAIN_FONT);
            labels.add(ruleLabel);
            buttonGroup.add(radioButton);
            horizontalLabelParallelGroup.addComponent(ruleLabel);
            verticalGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(ruleLabel).addComponent(radioButton));
        }
        JButton submitButton = getSubmitButton();
        horizontalLabelParallelGroup.addComponent(submitButton);
        verticalGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(submitButton).addGap(80));
        groupLayout.setHorizontalGroup(horizontalGroup);
        groupLayout.setVerticalGroup(verticalGroup);
    }
    
    private JButton getSubmitButton() {
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ButtonModel model = buttonGroup.getSelection();
                if (model == null) {
                    JOptionPane.showMessageDialog(VotingForm.this, "Please select a rule.");
                    return;
                }
                String selectedRule = model.getActionCommand();
                int selectedRuleIndex = Integer.parseInt(selectedRule);
                client.transmit(new VoteRuleRequest(client.getId(), selectedRuleIndex, rules.get(selectedRuleIndex)));
            }
        });
        return submitButton;
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonGroup = new javax.swing.ButtonGroup();
    }// </editor-fold>//GEN-END:initComponents

    private javax.swing.ButtonGroup buttonGroup;
    
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.add(new VotingForm());
        frame.pack();
        frame.setVisible(true);
        
    }
}
