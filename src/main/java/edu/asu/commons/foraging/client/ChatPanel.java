package edu.asu.commons.foraging.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
import javax.swing.text.html.HTMLEditorKit;

import edu.asu.commons.event.ChatEvent;
import edu.asu.commons.event.ChatRequest;
import edu.asu.commons.event.EventTypeProcessor;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.CensoredChatRequest;
import edu.asu.commons.net.Identifier;




/**
 * $Id: ChatPanel.java 516 2010-05-10 23:31:53Z alllee $
 * 
 * Chat panel used to communicate with other players.  
 * 
 * FIXME: randomize handle mappings (e.g., A -> 3, B -> 1, C -> 4, D -> 2 ...) so that it's
 * not linear.
 * 
 * @author alllee
 * @version $Revision: 516 $
 */
@SuppressWarnings("serial")
public class ChatPanel extends JPanel {

    private ForagingClient client;
    
    public ChatPanel(ForagingClient client) {
        this.client = client;
        this.clientId = client.getId();
        client.getEventChannel().add(this, new EventTypeProcessor<ChatEvent>(ChatEvent.class) {
            public void handle(final ChatEvent chatEvent) {
                displayMessage(getChatHandle(chatEvent.getSource())  
//                         FIXME: either "all" or "you".
//                        + " -> " + getChatHandle(chatEvent.getTarget())
                        ,chatEvent.toString());
            }
        });
    }
    private class TextEntryPanel extends JPanel {
        private JLabel targetHandleLabel;

        private Identifier targetIdentifier = Identifier.ALL;
        private JTextField chatField;

        public TextEntryPanel() {
            super();
            setLayout(new BorderLayout(3, 3));
            chatField = new JTextField();
            chatField.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent event) {
                    // System.err.println("event keycode is: " +
                    // event.getKeyCode());
                    // System.err.println("vk_enter: " + KeyEvent.VK_ENTER);
                    if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                        sendMessage();
                    }
                }
            });
            final JButton sendButton = new JButton("Send");
            sendButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    sendMessage();
                }
            });
            JPanel targetHandlePanel = new JPanel();
            targetHandlePanel.setLayout(new BoxLayout(targetHandlePanel,
                    BoxLayout.LINE_AXIS));
            targetHandleLabel = new JLabel("everyone");
            targetHandleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            targetHandleLabel.setForeground(new Color(0x0000dd));
            targetHandlePanel.add(new JLabel(" Chatting with: "));
            targetHandlePanel.add(targetHandleLabel);

            add(targetHandlePanel, BorderLayout.NORTH);
            add(chatField, BorderLayout.CENTER);
//            add(sendButton, BorderLayout.SOUTH);
            setChatFieldFocus();
        }

        private void setChatFieldFocus() {
            chatField.requestFocusInWindow();
        }

        private void sendMessage() {
            String message = chatField.getText();
            // System.err.println("message: " + message);
            if (message == null || "".equals(message) || targetIdentifier == null) {
                return;
            }
            RoundConfiguration roundConfiguration = client.getCurrentRoundConfiguration();
            if (roundConfiguration.isCensoredChat()) {
                // FIXME: perform extra checks to see if we are able to send this message 
                client.transmit(new CensoredChatRequest(clientId, message, targetIdentifier));
            }
            else {
                client.transmit(new ChatRequest(clientId, message, targetIdentifier));
//                displayMessage(getChatHandle(clientId) 
////                      + " -> " + getChatHandle(targetIdentifier)
//                      , message);
            }
            chatField.requestFocusInWindow();
            chatField.setText("");
        }

        private void setTargetHandle(Identifier targetIdentifier) {
            this.targetIdentifier = targetIdentifier;
            if (targetIdentifier == Identifier.ALL) {
                targetHandleLabel.setText("everyone");
            } else {
                targetHandleLabel.setText(getChatHandle(targetIdentifier));
            }
            setChatFieldFocus();
        }
    }

    private final static String HANDLE_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    private static String[] HANDLES;

//    public static void main(String[] args) {
//        JFrame frame = new JFrame();
//        ChatPanel chatPanel = new ChatPanel();
//        Identifier selfId = new Identifier.Base();
//        chatPanel.setClientId(selfId);
//        chatPanel.initialize(Arrays.asList(new Identifier[] {
//                new Identifier.Base(), new Identifier.Base(),
//                new Identifier.Base(), selfId }));
//        frame.add(chatPanel);
//        frame.setSize(new Dimension(400, 400));
//        frame.setVisible(true);
//    }

    private Identifier clientId;

    private JScrollPane messageScrollPane;

    private JTextPane messageWindow;

    // used by the participant to select which participant to send a message to.
    private JPanel participantButtonPanel;

    private List<Identifier> participants;

    private TextEntryPanel textEntryPanel;
    
    private JEditorPane chatInstructionsPane;

    private void addStylesToMessageWindow() {
        StyledDocument styledDocument = messageWindow.getStyledDocument();
        // and why not have something like... StyleContext.getDefaultStyle() to
        // replace this junk
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(
                StyleContext.DEFAULT_STYLE);
        // Style regularStyle = styledDocument.addStyle("regular",
        // defaultStyle);
        StyleConstants.setFontFamily(defaultStyle, "Helvetica");
        StyleConstants.setBold(styledDocument.addStyle("bold", defaultStyle),
                true);
        StyleConstants.setItalic(styledDocument
                .addStyle("italic", defaultStyle), true);
    }

    private String getChatHandle(Identifier source) {
        if (source.equals(Identifier.ALL)) {
            return "all";
        }
        else {
            String chatHandle = HANDLES[participants.indexOf(source)];
            if (source.equals(clientId)) {
                return chatHandle.concat(" (you)");
            }
            return chatHandle;
        }
    }
    
    private void initGuiComponents() {
        setLayout(new BorderLayout(4, 4));
        setName("Chat panel");
        messageWindow = new JTextPane();
        messageWindow.setEditable(false);
        messageWindow.setBackground(Color.WHITE);
        messageScrollPane = new JScrollPane(messageWindow);
        addStylesToMessageWindow();

        // set up the participant panel
        participantButtonPanel = new JPanel();
        // participantButtonPanel.setLayout(new
        // BoxLayout(participantButtonPanel,
        // BoxLayout.PAGE_AXIS));
        participantButtonPanel.setLayout(new GridLayout(0, 1));
        participantButtonPanel.setBackground(Color.GRAY);
        // JLabel selfLabel = new JLabel(getChatHandle(clientId));
        // selfLabel.setForeground(Color.ORANGE);
        // selfLabel.setBackground(Color.ORANGE);
        // selfLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton selfButton = new JButton(getChatHandle(clientId));
        selfButton.setEnabled(false);
        selfButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        participantButtonPanel.add(selfButton);
        participantButtonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        for (final Identifier targetId: participants) {
            if (targetId.equals(clientId)) {
                continue;
            }
            JButton button = new JButton(getChatHandle(targetId));
            button.setAlignmentX(JButton.CENTER_ALIGNMENT);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // change stuff in the messageEntryPanel
                    textEntryPanel.setTargetHandle(targetId);
                }
            });
            participantButtonPanel.add(button);
        }
        // special case to send a message to everyone
        JButton sendAllButton = new JButton(" all ");
        sendAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        sendAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textEntryPanel.setTargetHandle(Identifier.ALL);
            }
        });
        participantButtonPanel.add(sendAllButton);

        textEntryPanel = new TextEntryPanel();
        // orient the components in true lazyman fashion.
        
        chatInstructionsPane = new JEditorPane();
        chatInstructionsPane.setContentType("text/html");
        chatInstructionsPane.setEditorKit(new HTMLEditorKit());
        chatInstructionsPane.setEditable(false);
        chatInstructionsPane.setBackground(Color.WHITE);
        JScrollPane chatInstructionsScrollPane = new JScrollPane(chatInstructionsPane);
        chatInstructionsPane.setText(client.getCurrentRoundConfiguration().getChatInstructions());

        add(chatInstructionsScrollPane, BorderLayout.NORTH);
        add(messageScrollPane, BorderLayout.CENTER);
        add(textEntryPanel, BorderLayout.SOUTH);
        textEntryPanel.setChatFieldFocus();
    }

    public void clear() {
        participants.clear();
    }

    private void displayMessage(String chatHandle, String message) {
        //		String chatHandle = getChatHandle(source);
        StyledDocument document = messageWindow.getStyledDocument();
        try {
            document.insertString(document.getLength(), chatHandle + " : ",
                    document.getStyle("bold"));
            document.insertString(document.getLength(), message + "\n", null);
            messageWindow.setCaretPosition(document.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void initialize() {
        if (HANDLES != null) {
            displayMessage("System message", " --- Round ended --- ");
            return;
        }
        this.participants = client.getDataModel().getAllClientIdentifiers();
        HANDLES = new String[participants.size()];
        List<String> handles = new ArrayList<String>();
        if (client.getDataModel().getRoundConfiguration().isChatAnonymized()) {
            for (int i = HANDLES.length; --i >= 0;) {
                handles.add(HANDLE_STRING.charAt(i) + "");
            }
            Collections.shuffle(handles);
            for (int i = 0; i < HANDLES.length; i++) {
                HANDLES[i] = handles.get(i);
            }
        }
        else {
            for (int i = 0; i < HANDLES.length; i++) {
                HANDLES[i] = client.getDataModel().getAssignedNumber(participants.get(i)) + "";
            }
        }
        
//        Collections.shuffle(Arrays.asList(HANDLES));
//        System.err.println("handles: " + HANDLES);
        initGuiComponents();
    }

}
