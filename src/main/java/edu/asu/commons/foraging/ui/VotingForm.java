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
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import edu.asu.commons.foraging.client.ForagingClient;
import edu.asu.commons.foraging.rules.ForagingRule;

/**
 *
 * @author alllee
 */
public class VotingForm extends javax.swing.JPanel {
    
    private static final long serialVersionUID = 3871660663519284024L;

    public final static String NAME = "Voting form";
    
    private ForagingClient client;

    public VotingForm(ForagingClient client) {
        this(client, new HashMap<ForagingRule, Integer>());
    }
    
    public VotingForm(ForagingClient client, Map<ForagingRule, Integer> votingResults) {
        this.client = client;
        initComponents();
        initForm(votingResults);
        setName(NAME);
    }
    
    private void initForm(Map<ForagingRule, Integer> votingResults) {
        ForagingRule[] rules = ForagingRule.values();
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
        String rightColumnHeader = votingResults.isEmpty() ? "Select one" : "Nominations";
        JLabel rightHeaderLabel = new JLabel(rightColumnHeader);
        rightHeaderLabel.setFont(ForagingInterface.DEFAULT_BOLD_FONT);
        horizontalButtonParallelGroup.addComponent(rightHeaderLabel);
        
        JLabel ruleHeaderLabel = new JLabel("Rule");
        ruleHeaderLabel.setFont(ForagingInterface.DEFAULT_BOLD_FONT);
        horizontalLabelParallelGroup.addComponent(ruleHeaderLabel);
        
        verticalGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(ruleHeaderLabel).addGap(10).addComponent(rightHeaderLabel));
        int ruleIndex = 0;
        for (ForagingRule rule: rules) {
            ruleIndex++;
            JLabel ruleLabel = new JLabel(String.format("Rule %d: %s", ruleIndex, rule));
            ruleLabel.setFont(ForagingInterface.DEFAULT_PLAIN_FONT);
            horizontalLabelParallelGroup.addComponent(ruleLabel);
            JComponent component = null;
            if (votingResults.isEmpty()) {
                JRadioButton radioButton = new JRadioButton();                        
                radioButton.setActionCommand(rule.name());
                buttonGroup.add(radioButton);
                component = radioButton;
                horizontalButtonParallelGroup.addComponent(radioButton);
                verticalGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(ruleLabel).addComponent(radioButton));
            }
            else {
                Integer numberOfVotes = votingResults.get(rule);
                component = new JLabel(String.format("%s votes", numberOfVotes == null ? "0" : numberOfVotes));
            }
            horizontalButtonParallelGroup.addComponent(component);
            verticalGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(ruleLabel).addComponent(component));
        }
        if (votingResults.isEmpty()) {
            JButton submitButton = getSubmitButton();
            horizontalLabelParallelGroup.addComponent(submitButton);
            verticalGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(submitButton).addGap(80));
        }
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
                ForagingRule selectedRule = ForagingRule.valueOf(model.getActionCommand());
                client.sendRuleVoteRequest(selectedRule);
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
        frame.add(new VotingForm(null));
        frame.pack();
        frame.setVisible(true);
        
    }
}
