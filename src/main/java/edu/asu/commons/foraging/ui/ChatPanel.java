package edu.asu.commons.foraging.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.asu.commons.event.ChatEvent;
import edu.asu.commons.event.ChatRequest;
import edu.asu.commons.event.EventTypeProcessor;
import edu.asu.commons.experiment.DataModel;
import edu.asu.commons.foraging.client.ForagingClient;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.conf.ServerConfiguration;
import edu.asu.commons.foraging.event.FacilitatorCensoredChatRequest;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.ui.UserInterfaceUtils;
import edu.asu.commons.util.Duration;

/**
 * Chat panel used to communicate with other players.
 * 
 * @author Allen Lee
 */
@SuppressWarnings("serial")
public class ChatPanel extends JPanel {

    private ForagingClient client;

    private JScrollPane messageScrollPane;

    private JEditorPane messagesEditorPane;

    private List<Identifier> participants;

    private TextEntryPanel textEntryPanel;

    private boolean isInRoundChat;

    public ChatPanel(ForagingClient client) {
        this(client, false);
    }

    
    public ChatPanel(ForagingClient client, boolean isInRoundChat) {
        this.client = client;
        this.isInRoundChat = isInRoundChat;
        client.getEventChannel().add(this, new EventTypeProcessor<ChatEvent>(ChatEvent.class) {
            public void handle(final ChatEvent chatEvent) {
                displayMessage(chatEvent.getSource(), chatEvent.toString());
            }
        });
        initGuiComponents();
    }
    
    private void initGuiComponents() {
        setLayout(new BorderLayout(3, 3));
        setName("Chat panel");
        textEntryPanel = new TextEntryPanel(client, isInRoundChat);
        messagesEditorPane = UserInterfaceUtils.createInstructionsEditorPane();
        messageScrollPane = new JScrollPane(messagesEditorPane);
        add(textEntryPanel, BorderLayout.NORTH);
        if (isInRoundChat) {
            add(messageScrollPane, BorderLayout.CENTER);
        }
        else {
            // not in round chat, include chat instructions on the side
            JEditorPane instructionsEditorPane = UserInterfaceUtils.createInstructionsEditorPane();
            JPanel gridPanel = new JPanel(new GridLayout(1, 2, 5, 5));
            JScrollPane instructionsScrollPane = new JScrollPane(instructionsEditorPane);
            RoundConfiguration roundConfiguration = client.getCurrentRoundConfiguration();
            instructionsEditorPane.setText(roundConfiguration.getChatInstructions());
            gridPanel.add(instructionsScrollPane);
            gridPanel.add(messageScrollPane);
            add(gridPanel, BorderLayout.CENTER);
        }
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
            String source = String.format("%s :  ", identifier.getChatHandle());
            document.insertString(0, source, null);
            document.insertString(source.length(), String.format("%s\n", message), null);
            messagesEditorPane.setCaretPosition(0);
        } catch (BadLocationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void initialize(DataModel<ServerConfiguration, RoundConfiguration> dataModel) {
        // close out any existing scheduled executor
        participants = dataModel.getAllClientIdentifiers();
        Duration chatDuration = Duration.create(dataModel.getRoundConfiguration().getChatDuration()).start();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            if (chatDuration.hasExpired()) {
                executor.shutdown();
            }
            else {
                getTextEntryPanel().updateTimeRemaining(chatDuration.getTimeLeftInSeconds());
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private class TextEntryPanel extends JPanel {

        private static final long serialVersionUID = -4846486696999203769L;

        private Identifier targetIdentifier = Identifier.ALL;
        private JLabel chatLabel;
        private JTextField chatField;
        private int timeRemaining;
        private JLabel timeRemainingLabel = new JLabel("");

        public TextEntryPanel(ForagingClient client, boolean isInRoundChat) {
            setLayout(new BorderLayout(5, 5));
            JPanel gridPanel = new JPanel(new GridLayout(1, 2, 2, 5));
            chatLabel = new JLabel("Chat: ", SwingConstants.RIGHT);
            chatLabel.setFont(UserInterfaceUtils.DEFAULT_BOLD_FONT);
            chatField = new JTextField();
            chatField.setFont(UserInterfaceUtils.getDefaultFont(24.0f));
            chatField.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent event) {
                    if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                        sendMessage();
                    }
                }
            });
            if (! isInRoundChat) {
                JPanel headerPanel = new JPanel();
                JLabel headerLabel = new JLabel("Time remaining: ");
                headerLabel.setFont(UserInterfaceUtils.DEFAULT_BOLD_FONT);
                timeRemainingLabel.setFont(UserInterfaceUtils.DEFAULT_PLAIN_FONT);
                headerPanel.add(headerLabel);
                headerPanel.add(timeRemainingLabel);
                add(headerPanel, BorderLayout.NORTH);
            }
            gridPanel.add(chatLabel);
            gridPanel.add(chatField);
            add(gridPanel, BorderLayout.CENTER);
        }

        private void updateTimeRemaining(int timeRemaining) {
            timeRemainingLabel.setText(String.valueOf(timeRemaining) + "s");
        }

        private void sendMessage() {
            String message = chatField.getText();
            System.err.println("sending message: " + message);
            if (message == null || "".equals(message) || targetIdentifier == null) {
                return;
            }
            RoundConfiguration configuration = client.getCurrentRoundConfiguration();
            ChatRequest request = new ChatRequest(client.getId(), message, targetIdentifier);
            if (configuration.isCensoredChat()) {
                client.transmit(new FacilitatorCensoredChatRequest(client.getId(), request));
            }
            else {
                client.transmit(request);
            }
            // special case for in round chat
            if (isInRoundChat) {
                client.getGameWindow().requestFocusInWindow();
            }
            else {
                chatField.requestFocusInWindow();
            }
            chatField.setText("");
        }

    }

}
