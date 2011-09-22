package edu.asu.commons.foraging.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.asu.commons.event.ChatEvent;
import edu.asu.commons.event.ChatRequest;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.event.EventTypeProcessor;
import edu.asu.commons.experiment.DataModel;
import edu.asu.commons.foraging.client.ForagingClient;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.net.Identifier;

/**
 * $Id: ChatPanel.java 516 2010-05-10 23:31:53Z alllee $
 * 
 * Chat panel used to communicate with other players.
 * 
 * @author alllee
 * @version $Revision: 516 $
 */
@SuppressWarnings("serial")
public class ChatPanel extends JPanel {

    private ForagingClient client;

    private JScrollPane messageScrollPane;

    private JEditorPane messagesEditorPane;

    private List<Identifier> participants;

    private TextEntryPanel textEntryPanel;
    
    public ChatPanel(EventChannel channel) {
        channel.add(this, new EventTypeProcessor<ChatEvent>(ChatEvent.class) {
            public void handle(final ChatEvent chatEvent) {
                displayMessage(chatEvent.getSource(), chatEvent.toString());
            }
        });
        initGuiComponents();   
    }

    public ChatPanel(ForagingClient client) {
        this(client.getEventChannel());
        this.client = client;
    }
    
    private void initGuiComponents() {
        setLayout(new BorderLayout(3, 3));
        setName("Chat panel");
        messagesEditorPane = ForagingInterface.createInstructionsEditorPane();
        messageScrollPane = new JScrollPane(messagesEditorPane);

        textEntryPanel = new TextEntryPanel(client);
        add(textEntryPanel, BorderLayout.NORTH);
        add(messageScrollPane, BorderLayout.CENTER);
    }

    public void setTextFieldFocus() {
        textEntryPanel.chatField.requestFocusInWindow();
    }

    public TextEntryPanel getTextEntryPanel() {
        return textEntryPanel;
    }

    public JScrollPane getMessageScrollPane() {
        return messageScrollPane;
    }

    public void clear() {
        participants.clear();
    }

    private void displayMessage(Identifier identifier, String message) {
        try {
            Document document = messagesEditorPane.getDocument();
            String source = String.format("%s : ", identifier.getChatHandle());
            document.insertString(0, source, null);
            document.insertString(source.length(), String.format("%s\n", message), null);
            messagesEditorPane.setCaretPosition(0);
        } catch (BadLocationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void initialize(DataModel<RoundConfiguration> dataModel) {
        this.participants = dataModel.getAllClientIdentifiers();
    }
    
    private class TextEntryPanel extends JPanel {

        private static final long serialVersionUID = -4846486696999203769L;

        private Identifier targetIdentifier = Identifier.ALL;
        private JTextField chatField;

        public TextEntryPanel(ForagingClient client) {
            setLayout(new BorderLayout(3, 3));
            chatField = new JTextField();
            chatField.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent event) {
                    if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                        sendMessage();
                    }
                }
            });
            chatField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    chatField.setBackground(Color.YELLOW);
                }
                @Override
                public void focusLost(FocusEvent e) {
                    chatField.setBackground(Color.WHITE);
                }
                
            });
            JLabel headerLabel = new JLabel("Chat");
            headerLabel.setFont(ForagingInterface.DEFAULT_BOLD_FONT);
            add(headerLabel, BorderLayout.NORTH);
            add(chatField, BorderLayout.CENTER);
        }

        private void sendMessage() {
            String message = chatField.getText();
            System.err.println("sending message: " + message);
            if (message == null || "".equals(message) || targetIdentifier == null) {
                return;
            }
            client.transmit(new ChatRequest(client.getId(), message, targetIdentifier));
            // special case for in round chat
            if (client.getCurrentRoundConfiguration().isInRoundChatEnabled()) {
                client.getGameWindow().requestFocusInWindow();
            }
            else {
                chatField.requestFocusInWindow();
            }
            chatField.setText("");
        }

    }

}
