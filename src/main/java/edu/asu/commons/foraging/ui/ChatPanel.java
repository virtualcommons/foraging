package edu.asu.commons.foraging.ui;

import edu.asu.commons.event.ChatEvent;
import edu.asu.commons.event.ChatRequest;
import edu.asu.commons.event.EventTypeProcessor;
import edu.asu.commons.experiment.DataModel;
import edu.asu.commons.foraging.client.ForagingClient;
import edu.asu.commons.foraging.client.MockForagingClient;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.conf.ServerConfiguration;
import edu.asu.commons.foraging.event.FacilitatorCensoredChatRequest;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.ui.UserInterfaceUtils;
import edu.asu.commons.util.Duration;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
            // dedicated chat round, include chat instructions via two column GridLayout
            JEditorPane instructionsEditorPane = UserInterfaceUtils.createInstructionsEditorPane();
            JPanel gridPanel = new JPanel(new GridLayout(1, 2, 5, 5));
            JScrollPane instructionsScrollPane = new JScrollPane(instructionsEditorPane);
            RoundConfiguration roundConfiguration = client.getCurrentRoundConfiguration();
            instructionsEditorPane.setText(roundConfiguration.getChatInstructions());
            instructionsEditorPane.setCaretPosition(0);
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

    public void enableChat() {
        textEntryPanel.setVisible(true);
    }

    public void disableChat() {
        textEntryPanel.setVisible(false);
        messagesEditorPane.setText("");
    }

    public void displayMessage(String message) {
        try {
            Document document = messagesEditorPane.getDocument();
            if (! message.endsWith("\n")) {
                message += "\n";
            }
            document.insertString(0, message, null);
            messagesEditorPane.setCaretPosition(0);
        } catch (BadLocationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void displayMessage(String message, Color color) {
        Color originalForeground = messagesEditorPane.getForeground();
        messagesEditorPane.setForeground(color);
        displayMessage(message);
        messagesEditorPane.setForeground(originalForeground);
    }

    private void displayMessage(Identifier identifier, String message) {
        // FIXME: pick a better chat handle <-> message delimiter
        String fullMessage = String.format("%s: %s", identifier.getChatHandle(), message);
        displayMessage(fullMessage);
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
        enableChat();
    }

    private class TextEntryPanel extends JPanel {

        private static final long serialVersionUID = -4846486696999203769L;

        private Identifier targetIdentifier = Identifier.ALL;
        private JLabel chatLabel;
        private JTextField chatField;
        private JLabel timeRemainingLabel = new JLabel("");

        public TextEntryPanel(ForagingClient client, boolean isInRoundChat) {
            setBorder(new EmptyBorder(10, 10, 10, 10));
            setLayout(new BorderLayout(5, 5));
            chatLabel = new JLabel("Chat ");
            IconFontSwing.register(FontAwesome.getIconFont());
            Icon icon = IconFontSwing.buildIcon(FontAwesome.COMMENTS_O, 24);
            chatLabel.setIcon(icon);
            Font defaultFont = UserInterfaceUtils.getDefaultFont((24.0f));
            chatLabel.setFont(defaultFont);
            chatField = new JTextField();
            chatField.setFont(defaultFont);
            // FIXME: consider switching to keybinding style
            // https://docs.oracle.com/javase/tutorial/uiswing/misc/keybinding.html
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
            add(chatLabel, BorderLayout.LINE_START);
            add(chatField, BorderLayout.CENTER);
        }

        private void updateTimeRemaining(int timeRemaining) {
            timeRemainingLabel.setText(String.valueOf(timeRemaining) + "s");
        }

        private void sendMessage() {
            String message = chatField.getText();
            System.err.println("sending message: " + message);
            if (message.isEmpty() || targetIdentifier == null) {
                return;
            }
            // FIXME: consider pushing logic into client.sendChatMessage e.g.,
            // client.sendChatMessage(message, targetIdentifier);
            // somewhat complicated by special casing for in round chat focus request after sending the message
            RoundConfiguration configuration = client.getCurrentRoundConfiguration();
            ChatRequest request = new ChatRequest(client.getId(), message, targetIdentifier);
            if (configuration.isCensoredChat()) {
                client.transmit(new FacilitatorCensoredChatRequest(client.getId(), request));
            }
            else {
                client.transmit(request);
            }
            // FIXME: special case for in round chat
            if (isInRoundChat) {
                client.getGameWindow().requestFocusInWindow();
            }
            else {
                chatField.requestFocusInWindow();
            }
            chatField.setText("");
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.add(new ChatPanel(new MockForagingClient(), true));
        UserInterfaceUtils.maximize(frame);
    }

}
