package edu.asu.commons.foraging.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
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

    private boolean isInRoundChat = false;
    private ForagingClient client;

    private JScrollPane messageScrollPane;

    private JEditorPane messagesEditorPane;

    private List<Identifier> participants;

    private TextEntryPanel textEntryPanel;
    
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
        if (! isInRoundChat) {
            JEditorPane instructionsEditorPane = UserInterfaceUtils.createInstructionsEditorPane();
            JScrollPane instructionsScrollPane = new JScrollPane(instructionsEditorPane);
            RoundConfiguration roundConfiguration = client.getCurrentRoundConfiguration();

            instructionsEditorPane.setText(roundConfiguration.getChatInstructions());
            instructionsScrollPane.setPreferredSize(new Dimension(300, 300));
            add(instructionsScrollPane, BorderLayout.WEST);
        }
        messagesEditorPane = UserInterfaceUtils.createInstructionsEditorPane();
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

    public void initialize(DataModel<ServerConfiguration, RoundConfiguration> dataModel) {
        participants = dataModel.getAllClientIdentifiers();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Duration chatDuration = Duration.create(dataModel.getRoundConfiguration().getChatDuration()).start();
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

        public TextEntryPanel(ForagingClient client) {
            setLayout(new BorderLayout(3, 3));
            chatLabel = new JLabel("Chat: ");
            chatField = new JTextField();
            chatField.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent event) {
                    if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                        sendMessage();
                    }
                }
            });
            JPanel headerPanel = new JPanel();
            JLabel headerLabel = new JLabel("Time remaining: ");
            headerLabel.setFont(UserInterfaceUtils.DEFAULT_BOLD_FONT);
            headerPanel.add(headerLabel);
            headerPanel.add(timeRemainingLabel);
            add(headerPanel, BorderLayout.NORTH);
            add(chatLabel, BorderLayout.WEST);
            add(chatField, BorderLayout.CENTER);
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
