package edu.asu.commons.foraging.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;

import edu.asu.commons.util.HtmlEditorPane;

/**
 * $Id$
 * 
 * static utility class for common UI methods to set up a consistent look & feel.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public final class ForagingInterface {
    
    public static final Font DEFAULT_PLAIN_FONT = new Font(getDefaultFont().getFamily(), Font.PLAIN, 16);
    public static final Font DEFAULT_BOLD_FONT = new Font(getDefaultFont().getFamily(), Font.BOLD, 16);

       /** A very dark red color. */
    public static final Color VERY_DARK_RED = new Color(0x80, 0x00, 0x00);

    /** A dark red color. */
    public static final Color DARK_RED = new Color(0xc0, 0x00, 0x00);

    /** A light red color. */
    public static final Color LIGHT_RED = new Color(0xFF, 0x40, 0x40);

    /** A very light red color. */
    public static final Color VERY_LIGHT_RED = new Color(0xFF, 0x80, 0x80);

    /** A very dark yellow color. */
    public static final Color VERY_DARK_YELLOW = new Color(0x80, 0x80, 0x00);

    /** A dark yellow color. */
    public static final Color DARK_YELLOW = new Color(0xC0, 0xC0, 0x00);

    /** A light yellow color. */
    public static final Color LIGHT_YELLOW = new Color(0xFF, 0xFF, 0x40);

    /** A very light yellow color. */
    public static final Color VERY_LIGHT_YELLOW = new Color(0xFF, 0xFF, 0x80);

    /** A very dark green color. */
    public static final Color VERY_DARK_GREEN = new Color(0x00, 0x80, 0x00);

    /** A dark green color. */
    public static final Color DARK_GREEN = new Color(0x00, 0xC0, 0x00);

    /** A light green color. */
    public static final Color LIGHT_GREEN = new Color(0x40, 0xFF, 0x40);

    /** A very light green color. */
    public static final Color VERY_LIGHT_GREEN = new Color(0x80, 0xFF, 0x80);

    /** A very dark cyan color. */
    public static final Color VERY_DARK_CYAN = new Color(0x00, 0x80, 0x80);

    /** A dark cyan color. */
    public static final Color DARK_CYAN = new Color(0x00, 0xC0, 0xC0);

    /** A light cyan color. */
    public static final Color LIGHT_CYAN = new Color(0x40, 0xFF, 0xFF);

    /** Aa very light cyan color. */
    public static final Color VERY_LIGHT_CYAN = new Color(0x80, 0xFF, 0xFF);

    /** A very dark blue color. */
    public static final Color VERY_DARK_BLUE = new Color(0x00, 0x00, 0x80);

    /** A dark blue color. */
    public static final Color DARK_BLUE = new Color(0x00, 0x00, 0xC0);

    /** A light blue color. */
    public static final Color LIGHT_BLUE = new Color(0x40, 0x40, 0xFF);

    /** A very light blue color. */
    public static final Color VERY_LIGHT_BLUE = new Color(0x80, 0x80, 0xFF);

    /** A very dark magenta/purple color. */
    public static final Color VERY_DARK_MAGENTA = new Color(0x80, 0x00, 0x80);

    /** A dark magenta color. */
    public static final Color DARK_MAGENTA = new Color(0xC0, 0x00, 0xC0);

    /** A light magenta color. */
    public static final Color LIGHT_MAGENTA = new Color(0xFF, 0x40, 0xFF);

    /** A very light magenta color. */
    public static final Color VERY_LIGHT_MAGENTA = new Color(0xFF, 0x80, 0xFF);
 

    public static Font getDefaultFont() {
        return UIManager.getFont("Label.font");
    }
    
    public static void addStyles(JEditorPane editorPane, int fontSize) {
        editorPane.setContentType("text/html");
        Font font = getDefaultFont();
        String bodyRule = String.format("body { font-family: %s; font-size: %s pt; }", font.getFamily(), fontSize);
        ((HTMLDocument) editorPane.getDocument()).getStyleSheet().addRule(bodyRule); 
    }
    
    public static HtmlEditorPane createInstructionsEditorPane() {
        return createInstructionsEditorPane(false);
    }
    
    public static HtmlEditorPane createInstructionsEditorPane(boolean editable) {
        final HtmlEditorPane htmlPane = new HtmlEditorPane();
        htmlPane.setEditable(editable);
        htmlPane.setDoubleBuffered(true);
        htmlPane.setBackground(Color.WHITE);
        ForagingInterface.addStyles(htmlPane, 16);
        return htmlPane;
    }
}
