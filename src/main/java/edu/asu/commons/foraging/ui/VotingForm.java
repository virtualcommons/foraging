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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
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
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import edu.asu.commons.foraging.client.ForagingClient;
import edu.asu.commons.foraging.rules.Strategy;
import edu.asu.commons.foraging.rules.iu.ForagingStrategy;
import edu.asu.commons.ui.UserInterfaceUtils;

/**
 * $Id$
 * 
 * Interface nominate a given ForagingStrategy
 * 
 * @author Allen Lee
 */
public class VotingForm extends JPanel {
    
    private static final long serialVersionUID = 3871660663519284024L;

    public final static String NAME = "Strategy voting form";
    
    private ForagingClient client;

    public VotingForm(ForagingClient client) {
        this(client, new HashMap<Strategy, Integer>());
    }
    
    public VotingForm(ForagingClient client, Map<Strategy, Integer> votingResults) {
        this.client = client;
        initComponents();
        initForm(votingResults);
        setName(NAME);
    }
    
    private void initForm(Map<Strategy, Integer> votingResults) {
        ForagingStrategy[] strategies = ForagingStrategy.values();
        JPanel panel = new JPanel();
        GroupLayout groupLayout = new GroupLayout(panel);
        panel.setLayout(groupLayout);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setAutoCreateContainerGaps(true);
        GroupLayout.SequentialGroup horizontalGroup = groupLayout.createSequentialGroup();
        // horizontal parallel group
        GroupLayout.ParallelGroup horizontalLabelParallelGroup = groupLayout.createParallelGroup();
        horizontalGroup.addGroup(horizontalLabelParallelGroup);
        GroupLayout.ParallelGroup horizontalButtonParallelGroup = groupLayout.createParallelGroup();
        horizontalGroup.addGroup(horizontalButtonParallelGroup);
        
        GroupLayout.SequentialGroup verticalGroup = groupLayout.createSequentialGroup();
        boolean imposedStrategyEnabled = client.getCurrentRoundConfiguration().isImposedStrategyEnabled();
        // XXX: this is certainly what Rawlins was warning against
        String rightColumnHeader = votingResults.isEmpty() 
                ? (imposedStrategyEnabled) ? "" : "Select" 
                    : "Nominations";
        JLabel rightHeaderLabel = new JLabel(rightColumnHeader);
        rightHeaderLabel.setFont(UserInterfaceUtils.DEFAULT_BOLD_FONT);
        horizontalButtonParallelGroup.addComponent(rightHeaderLabel);
        
        JLabel strategyHeaderLabel = new JLabel("Strategy");
        strategyHeaderLabel.setFont(UserInterfaceUtils.DEFAULT_BOLD_FONT);
        horizontalLabelParallelGroup.addComponent(strategyHeaderLabel);
        
        verticalGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(strategyHeaderLabel).addGap(20).addComponent(rightHeaderLabel));
        Dimension labelDimension = new Dimension(800, 100);
 
        for (ForagingStrategy strategy: strategies) {
            JLabel ruleLabel = new JLabel("<html>" + strategy.getDescription() + "</html>");
            ruleLabel.setFont(UserInterfaceUtils.DEFAULT_PLAIN_FONT);
            ruleLabel.setMaximumSize(labelDimension);
            horizontalLabelParallelGroup.addComponent(ruleLabel);
            JComponent component = null;
            if (imposedStrategyEnabled) {
                component = new JLabel("");
            }
            else if (votingResults.isEmpty()) {
                JRadioButton radioButton = new JRadioButton();                        
                radioButton.setActionCommand(strategy.name());
                buttonGroup.add(radioButton);
                component = radioButton;
//                horizontalButtonParallelGroup.addComponent(radioButton);
//                verticalGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(ruleLabel).addComponent(radioButton));
            }
            else {
                Integer numberOfVotes = votingResults.get(strategy);
                component = new JLabel(String.valueOf(numberOfVotes == null ? 0 : numberOfVotes));
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
        JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JButton getSubmitButton() {
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (client.getCurrentRoundConfiguration().isImposedStrategyEnabled()) {
                    client.sendRuleVoteRequest(null);
                    return;
                }
                ButtonModel model = buttonGroup.getSelection();
                if (model == null) {
                    JOptionPane.showMessageDialog(VotingForm.this, "Please select a strategy.");
                    return;
                }
                ForagingStrategy selectedRule = ForagingStrategy.valueOf(model.getActionCommand());
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
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
    }
}
