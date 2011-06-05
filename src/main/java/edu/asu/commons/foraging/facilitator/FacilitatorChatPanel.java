package edu.asu.commons.foraging.facilitator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;

import edu.asu.commons.event.EventTypeProcessor;
import edu.asu.commons.foraging.event.CensoredChatRequest;
import edu.asu.commons.foraging.event.FacilitatorCensoredChatRequest;
import edu.asu.commons.util.ResourceLoader;

/**
 * $Id: FacilitatorChatPanel.java 533 2010-11-23 19:31:57Z alllee $
 * 
 * Exposes a JPanel for approving chat messages and tied to a specific Facilitator instance.
 * 
 * FIXME: should get rid of Facilitator dependency and use EventChannel instead
 * 
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 533 $
 */
public class FacilitatorChatPanel {
    
    private Facilitator facilitator;
    
    private JPanel chatRequestsPanel;
    private JScrollPane chatRequestsScrollPane;
    
    private GroupLayout groupLayout;
    private GroupLayout.ParallelGroup chatTextParallelGroup;
    private GroupLayout.ParallelGroup approveParallelGroup;
    private GroupLayout.ParallelGroup statusLabelParallelGroup;
    private GroupLayout.ParallelGroup denyParallelGroup;
    
    private GroupLayout.SequentialGroup verticalGroup;
    
    public FacilitatorChatPanel(Facilitator facilitator) {
        this.facilitator = facilitator;
        initGuiComponents();
        facilitator.addEventProcessor(new EventTypeProcessor<FacilitatorCensoredChatRequest>(FacilitatorCensoredChatRequest.class) {
            public void handle(final FacilitatorCensoredChatRequest request) {
                // add formatted message + approve / deny button to component 
                handleCensoredChatRequest(request);
            }
        });
    }
    
    private void handleCensoredChatRequest(final FacilitatorCensoredChatRequest request) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                CensoredChatRequestView censoredChatRequestView = new CensoredChatRequestView(request.getCensoredChatRequest());
                censoredChatRequestView.addToGroupLayout();
                JScrollBar verticalScrollBar = chatRequestsScrollPane.getVerticalScrollBar();
                verticalScrollBar.setValue(verticalScrollBar.getMaximum());
                chatRequestsPanel.revalidate();
            }
        });
    }
    
    private void initGuiComponents() {
        JPanel scrollablePanel = new JPanel(new BorderLayout());
        scrollablePanel.add(new JLabel("Chat requests"), BorderLayout.NORTH);
        chatRequestsPanel = new JPanel();
        groupLayout = new GroupLayout(chatRequestsPanel);
        chatRequestsPanel.setLayout(groupLayout);
        groupLayout.setAutoCreateGaps(true);
        GroupLayout.SequentialGroup horizontalGroup = groupLayout.createSequentialGroup();
        JLabel messagesLabel = new JLabel("Messages");
        JLabel approveLabel = new JLabel("Approve");
        JLabel statusLabel = new JLabel("Status");
        JLabel denyLabel = new JLabel("Deny");
        chatTextParallelGroup = groupLayout.createParallelGroup().addComponent(messagesLabel);
        approveParallelGroup = groupLayout.createParallelGroup().addComponent(approveLabel);
        statusLabelParallelGroup = groupLayout.createParallelGroup().addComponent(statusLabel);
        denyParallelGroup = groupLayout.createParallelGroup().addComponent(denyLabel);
        horizontalGroup.addGroup(chatTextParallelGroup).addGroup(approveParallelGroup).addGroup(statusLabelParallelGroup).addGroup(denyParallelGroup);        
        groupLayout.setHorizontalGroup(horizontalGroup);
        
        verticalGroup = groupLayout.createSequentialGroup();
        GroupLayout.ParallelGroup rowParallelGroup = groupLayout.createParallelGroup(Alignment.BASELINE);
        rowParallelGroup.addComponent(messagesLabel).addComponent(approveLabel).addComponent(statusLabel).addComponent(denyLabel);
        verticalGroup.addGroup(rowParallelGroup);
        groupLayout.setVerticalGroup(verticalGroup);
        
        scrollablePanel.add(chatRequestsPanel, BorderLayout.CENTER);
        chatRequestsScrollPane = new JScrollPane(scrollablePanel);
        chatRequestsScrollPane.setPreferredSize(new Dimension(300, 300));
    }
    
    public Component getComponent() {
        return chatRequestsScrollPane;
    }
    
    /**
     * 
     * Mini component for a chat request that can be approved or denied.  
     * Contains a TextArea for the message, approve and deny buttons, 
     * and a label to be shown once the request has been approved or denied.
     *
     */
    public class CensoredChatRequestView {

        private static final long serialVersionUID = -5819416143717776775L;
        
        private JButton approveButton;
        private JButton denyButton;
        private JLabel statusLabel;
        
        private JTextArea chatMessageTextArea;
        
        public JTextArea getChatMessageTextArea() {
            return chatMessageTextArea;
        }
        

        public CensoredChatRequestView(final CensoredChatRequest request) {
            chatMessageTextArea = createTextArea(request);

            approveButton = new JButton();
            denyButton = new JButton();
            approveButton.setIcon(new ImageIcon(ResourceLoader.getResourceAsUrl("images/checked.gif")));
            denyButton.setIcon(new ImageIcon(ResourceLoader.getResourceAsUrl("images/unchecked.gif")));
            statusLabel = new JLabel("Pending");

            approveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    facilitator.transmit(request.toApprovedChatRequest());
                    updateChatStatus(" approved ");

                }
            });
            denyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    facilitator.transmit(request.toDeniedChatRequest());
                    updateChatStatus(" denied ");
                }
            });
        }
        
        private void addToGroupLayout() {
            chatTextParallelGroup.addComponent(chatMessageTextArea);
            approveParallelGroup.addComponent(approveButton);
            statusLabelParallelGroup.addComponent(statusLabel);
            denyParallelGroup.addComponent(denyButton);
            
            GroupLayout.ParallelGroup rowParallelGroup = groupLayout.createParallelGroup(Alignment.BASELINE);
            rowParallelGroup.addComponent(chatMessageTextArea).addComponent(approveButton).addComponent(statusLabel).addComponent(denyButton);
            verticalGroup.addGroup(rowParallelGroup);
        }
        
        private void updateChatStatus(String message) {
            // move this component to the sidebar.
            approveButton.setEnabled(false);
            denyButton.setEnabled(false);
            statusLabel.setText(message);
        }
        
        private JTextArea createTextArea(CensoredChatRequest request) {
            JTextArea textArea = new JTextArea( toString( request ) );
            textArea.setLineWrap(true);
            textArea.setEditable(false);
            return textArea;
        }
        
        private String toString(CensoredChatRequest request) {
            return String.format("%s: %s", request.getId(), request.getMessage());
        }

    }
}
