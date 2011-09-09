package edu.asu.commons.foraging.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
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
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.event.EventTypeProcessor;
import edu.asu.commons.net.Identifier;



/**
 * $Id: EmbeddedChatPanel.java 416 2009-12-25 05:17:14Z alllee $
 * 
 * Chat panel used to communicate with other players within the 3D foraging visualization.  
 * 
 * FIXME: randomize mappings from handle (e.g., A -> 1, B -> 2, C -> 3 ...) so that it's
 * not linear.
 * 
 * FIXME: set the layout/bounds on this class properly.
 * 
 * @author alllee
 * @version $Revision: 416 $
 */

@SuppressWarnings("serial")
public class EmbeddedChatPanel extends JPanel {

    private class TextEntryPanel extends JPanel {
        private JLabel targetHandleLabel;
        private JTextField textField;
        private Identifier targetIdentifier;

        public TextEntryPanel() {
            super();
            setLayout(new BorderLayout(3, 3));
            textField = new JTextField();
            textField.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent event) {
                    // System.err.println("event keycode is: " +
                    // event.getKeyCode());
                    // System.err.println("vk_enter: " + KeyEvent.VK_ENTER);
                    if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                        sendMessage(textField);
                    }
                }
            });
            final JButton sendButton = new JButton("Send");
            sendButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    sendMessage(textField);
                }
            });
            JPanel targetHandlePanel = new JPanel();
            targetHandlePanel.setLayout(new BoxLayout(targetHandlePanel,
                    BoxLayout.LINE_AXIS));
            targetHandleLabel = new JLabel(" nobody ");
            targetHandleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            targetHandleLabel.setForeground(new Color(0x0000dd));
            targetHandlePanel.add(new JLabel(" Chatting with: "));
            targetHandlePanel.add(targetHandleLabel);

            add(targetHandlePanel, BorderLayout.NORTH);
            add(textField, BorderLayout.CENTER);
            add(sendButton, BorderLayout.SOUTH);
        }

        private void sendMessage(JTextField textField) {
            String message = textField.getText();
            // System.err.println("message: " + message);
            if (message == null || "".equals(message)) {
                return;
            }
            if (targetIdentifier == null) {
                return;
            }
            //Check for distance between the clients before sending the message
            if (client.getGameWindow3D().getGameView().shouldChat(clientId, targetIdentifier)) {
                client.transmit(new ChatRequest(clientId, message, targetIdentifier));
                System.err.println("Sending a new chat request from " + clientId + " to " + targetIdentifier);
                displayMessage(getChatHandle(clientId) + " -> "
                        + getChatHandle(targetIdentifier), message);
                textField.setText("");
                textField.requestFocusInWindow();
            }
        }

        private void setTargetHandle(Identifier targetIdentifier) {
            this.targetIdentifier = targetIdentifier;
            if (targetIdentifier == null) {
                targetHandleLabel.setText("nobody");
            } else {
                targetHandleLabel.setText(getChatHandle(targetIdentifier));
            }
        }
    }

    private final static String HANDLE_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static String[] HANDLES;

    private ForagingClient client;
    private Identifier clientId;

    private JScrollPane messageScrollPane;

    private JTextPane messageWindow;

    private List<Identifier> participants;

    private TextEntryPanel textEntryPanel;
    
//    private JEditorPane chatInstructionsPane;
    
    private final EventChannel channel;
    
    public EmbeddedChatPanel(ForagingClient client) {
        this.client = client;
        this.clientId = client.getId();
        this.channel = client.getEventChannel();
        channel.add(this, new EventTypeProcessor<ChatEvent>(ChatEvent.class) {
            public void handle(final ChatEvent chatEvent) {
                displayMessage(getChatHandle(chatEvent.getSource()) + " -> "
                        // FIXME: either "all" or "you".
                        + getChatHandle(chatEvent.getTarget()), chatEvent.toString());
            }
        });
        initGuiComponents();
    }
    
    public void stop() {
        channel.remove(this);
    }
    
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
        StyleConstants.setItalic(styledDocument.addStyle("italic", defaultStyle), true);
    }

    public String getChatHandle(Identifier source) {
        if (source == null) {
            return "nobody";
        }
        else {
            int index = participants.indexOf(source);
            if (source.equals(clientId)) {
                return HANDLES[index] + "(you)";
            }
            return "   " + HANDLES[index] + "   ";
        }
    }

    private void initGuiComponents() {
        setLayout(new BorderLayout(4, 4));
        messageWindow = new JTextPane();
        messageWindow.setEditable(false);
        messageScrollPane = new JScrollPane(messageWindow);
        
        Dimension messageWindowSize = new Dimension(getPreferredSize().width, 104);
        messageScrollPane.setPreferredSize(messageWindowSize);
        messageScrollPane.setMinimumSize(messageWindowSize);
        messageScrollPane.setMaximumSize(messageWindowSize);
        addStylesToMessageWindow();

        textEntryPanel = new TextEntryPanel();
        add(messageScrollPane, BorderLayout.CENTER);
        add(textEntryPanel, BorderLayout.SOUTH);
    }

    public void clear() {
        participants.clear();
//        EventChannel.getInstance().unsubscribe(this);
    }
    
    public void setTargetHandle(Identifier id) {
        textEntryPanel.setTargetHandle(id);
        textEntryPanel.textField.requestFocusInWindow();
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
            return;
        }
        this.participants = client.getDataModel().getAllClientIdentifiers();
        HANDLES = new String[participants.size()];
        for (int i = HANDLES.length; --i >= 0;) {
            HANDLES[i] = " " + HANDLE_STRING.charAt(i) + " ";
        }
    }
}
