package edu.asu.commons.foraging.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import edu.asu.commons.event.ChatEvent;
import edu.asu.commons.event.ChatRequest;
import edu.asu.commons.event.EventTypeProcessor;
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

    private JTextPane messageWindow;

    private List<Identifier> participants;

    private TextEntryPanel textEntryPanel;

    private JEditorPane chatInstructionsPane;
    
    public ChatPanel(ForagingClient client) {
        this.client = client;
        client.getEventChannel().add(this, new EventTypeProcessor<ChatEvent>(ChatEvent.class) {
            public void handle(final ChatEvent chatEvent) {
                displayMessage(chatEvent.getSource(), chatEvent.toString());
            }
        });
        initGuiComponents();
    }
    
    private void addStylesToMessageWindow() {
        StyledDocument styledDocument = messageWindow.getStyledDocument();
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(
                StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(defaultStyle, "Helvetica");
        StyleConstants.setBold(styledDocument.addStyle("bold", defaultStyle),
                true);
        StyleConstants.setItalic(styledDocument
                .addStyle("italic", defaultStyle), true);
    }

    private void initGuiComponents() {
        setLayout(new BorderLayout(3, 3));
        setName("Chat panel");
        messageWindow = new JTextPane();
        messageWindow.setEditable(false);
        messageWindow.setBackground(Color.WHITE);
        messageScrollPane = new JScrollPane(messageWindow);
        addStylesToMessageWindow();

        textEntryPanel = new TextEntryPanel(client);
        // orient the components in true lazyman fashion.

//        chatInstructionsPane = new JEditorPane();
//        chatInstructionsPane.setContentType("text/html");
//        chatInstructionsPane.setEditorKit(new HTMLEditorKit());
//        chatInstructionsPane.setEditable(false);
//        chatInstructionsPane.setBackground(Color.WHITE);
//        JScrollPane chatInstructionsScrollPane = new JScrollPane(chatInstructionsPane);
//        chatInstructionsPane.setText(client.getCurrentRoundConfiguration().getChatInstructions());
//        add(chatInstructionsScrollPane, BorderLayout.NORTH);
        
        add(textEntryPanel, BorderLayout.NORTH);
        add(messageScrollPane, BorderLayout.CENTER);
    }

    public void setTextFieldFocus() {
        textEntryPanel.setChatFieldFocus();
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
        StyledDocument document = messageWindow.getStyledDocument();
        try {
            String source = identifier.getChatHandle() + " : ";
            document.insertString(0, source, document.getStyle("bold"));
            document.insertString(source.length(), message + "\n", null);
            messageWindow.setCaretPosition(0);
        } catch (BadLocationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void initialize() {
        this.participants = client.getDataModel().getAllClientIdentifiers();
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
            add(new JLabel("In round chat"), BorderLayout.NORTH);
            add(chatField, BorderLayout.CENTER);
        }

        void setChatFieldFocus() {
            chatField.requestFocusInWindow();
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
