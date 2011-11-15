package edu.asu.commons.foraging.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.util.ResourceLoader;

/**
 * $Id: GridView.java 475 2010-02-24 00:39:44Z alllee $
 * 
 * @author Allen Lee
 * @version $Revision: 475 $
 * 
 * Superclass for experimenter and subject views of the simulation world.
 */
@SuppressWarnings("serial")
public abstract class GridView extends JPanel {

    /**
     * If the parameters call for a background, this object is created so that
     * the scaling calculation does not have to be performed for each paint.
     */
    protected Image tokenImage, otherSubjectImage, selfImage, selfExplicitCollectionModeImage, beingSanctionedImage, sanctioningImage, monitorImage;

    protected Image scaledTokenImage, scaledOtherSubjectImage, scaledSelfImage, 
    scaledSelfExplicitCollectionModeImage, scaledBeingSanctionedImage, scaledSanctioningImage, scaledMonitorImage;

    /**
     * Use these for the dimensions when drawing.
     */
    protected double dw, dh;

    // offsets for scaling.
    protected int xoffset = 0;

    protected int yoffset = 0;

    protected Font font;
    protected int fontSize;

    // how big the entire screen is.
    protected Dimension screenSize;

    // the size of the actual resource board.
    protected Dimension boardSize;

    public GridView(Dimension screenSize) {
        loadImages();
        this.screenSize = screenSize;
        setPreferredSize(screenSize);        
    }
    
    public final static int IMAGE_SCALING_STRATEGY = Image.SCALE_SMOOTH;

    public void setImageSizes() {
        if (boardSize == null) return;
        int availableWidth = (int) screenSize.getWidth();
        int availableHeight = (int) screenSize.getHeight();

        // stretch board to the max
        dw = (availableWidth / boardSize.getWidth());
        dh = (availableHeight / boardSize.getHeight());
        // ensure square proportions
        dw = dh = Math.min(dw, dh);
//        availableWidth = availableHeight = Math.min(availableWidth, availableHeight);

        xoffset = (int) Math.floor((availableWidth - (dw * boardSize.getWidth())) / 2);
        yoffset = (int) Math.floor((availableHeight - (dh * boardSize.getHeight())) / 2);
//        System.err.println("x offset: " + xoffset);
//        System.err.println("y offset: " + yoffset);
//        System.err.println("dw : " + dw);
//        System.err.println("dh: " + dh);

        fontSize = (int)(0.85 * dh);
        font = new Font("sansserif", Font.BOLD, fontSize);
        // make sure we've got enough room
        setPreferredSize(new Dimension(availableWidth, availableHeight));

        //FIXME: reduce code duplication
        // get scaled instances of the originals
        int cellWidth = getCellWidth();
        int cellHeight = getCellHeight();
        scaledTokenImage = tokenImage.getScaledInstance(cellWidth, cellHeight, IMAGE_SCALING_STRATEGY);
        scaledOtherSubjectImage = otherSubjectImage.getScaledInstance(cellWidth, cellHeight, IMAGE_SCALING_STRATEGY);
        scaledSelfImage = selfImage.getScaledInstance(cellWidth, cellHeight, IMAGE_SCALING_STRATEGY);
        scaledSelfExplicitCollectionModeImage = selfExplicitCollectionModeImage.getScaledInstance(cellWidth, cellHeight, IMAGE_SCALING_STRATEGY);
        scaledBeingSanctionedImage = beingSanctionedImage.getScaledInstance(cellWidth, cellHeight, IMAGE_SCALING_STRATEGY);
        scaledSanctioningImage = sanctioningImage.getScaledInstance(cellWidth, cellHeight, IMAGE_SCALING_STRATEGY);
        scaledMonitorImage = monitorImage.getScaledInstance(cellWidth, cellHeight, IMAGE_SCALING_STRATEGY);
    }

    /**
     * Sets dw, and dh, xoffset, and yoffset Uses the screenSize as given in the
     * constructor along with the current board size. Expects the following
     * variables to have been set already: 1. the screenSize 2. the boardSize 3.
     * the images
     */
    public void setup(RoundConfiguration configuration) {
        setBoardSize(configuration.getBoardSize());
        setImageSizes();
    }
    
    public void setScreenSize(Dimension screenSize) {
        this.screenSize = screenSize;
    }

    private void setBoardSize(Dimension boardSize) {
        this.boardSize = boardSize;
        setBackground(Color.BLACK);
//        setForeground(Color.WHITE);
    }

    /**
     * Loads the images
     *         // FIXME: reduce code duplication
     */
    private void loadImages() {
        // XXX: images still aren't fully loaded. When you actually invoke
        // drawImage on these images, make sure to pass this in so that when the
        // imageUpdate callback is invoked a repaint happens.
        if (tokenImage == null) {
            tokenImage = loadImage("images/gem-token.gif");
        }
        if (selfImage == null) {
            selfImage = loadImage("images/gem-self.gif");
        }
        if (otherSubjectImage == null) {
            otherSubjectImage = loadImage("images/gem-other.gif");
        }
        if (selfExplicitCollectionModeImage == null) {
            selfExplicitCollectionModeImage = loadImage("images/gem-self-explicit.gif");
        }
        if (beingSanctionedImage == null) {
            beingSanctionedImage = loadImage("images/gem-red.gif");
        }
        if (sanctioningImage == null) {
            sanctioningImage = loadImage("images/gem-purple.gif");
        }
        // FIXME: generate a new image for the monitor, for now just use the explicit-mode image.
        if (monitorImage == null) {
            monitorImage = loadImage("images/gem-self-explicit.gif");
        }
    }
    
    private Image loadImage(String path) {
        try {
            return ImageIO.read(ResourceLoader.getResourceAsUrl(path));
        }
        catch (IOException exception) {
            return null;
        }
//        return Toolkit.getDefaultToolkit().getImage(ResourceLoader.getResourceAsUrl(path));
    }

    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;
//        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // FIXME: can be made more efficient.  
        // Could just update the parts that have changed (tokens removed, subjects moved) 
        // paint the background
        paintBackground(graphics2D);
        // paint resources
        paintTokens(graphics2D);
        // paint subjects last (covering up tokens as needed)
        paintSubjects(graphics2D);
    }

    protected void paintCollection(Collection<Point> collection, Graphics2D graphics2D, Image image) {
        paintCollection(collection, graphics2D, image, this);
    }

    protected void paintCollection(Collection<Point> collection, Graphics2D graphics2D, Image image, ImageObserver observer) {
        synchronized (collection) {
            for (Point point: collection) {
                int x = scaleX(point.x);
                int y = scaleY(point.y);
                graphics2D.drawImage(image, x, y, observer);                    
            }
        }
    }

    protected void paintCollection(Collection<Point> collection, Graphics2D graphics2D, Image image, ImageObserver observer, Circle fieldOfView) {
        synchronized (collection) {
            for (Point point: collection) {
                if (fieldOfView.contains(point)) {
                    int x = scaleX(point.x);
                    int y = scaleY(point.y);
                    graphics2D.drawImage(image, x, y, observer);                    
                }
            }
        }
    }

    protected int getCellWidth() {
        return (int) dw;
    }
    protected int getCellHeight() {
        return (int) dh;
    }

    // FIXME: profiling shows that both scaleX and scaleY are called a lot at runtime, 
    // should see if we can optimize them further.
    protected int scaleX(int x) {
        return (int) ((dw * x) + xoffset);
    }

    protected double scaleXDouble(double x) {
        return ((dw * x) + xoffset);
    }

    protected int scaleY(int y) {
        return (int) ((dh * y) + yoffset);
    }

    protected double scaleYDouble(double y) {
        return ((dh * y) + yoffset);
    }


    protected abstract void paintTokens(Graphics2D graphics2D);

    protected abstract void paintSubjects(Graphics2D graphics2D);

    /**
     * Called only when running, this method should be overidden for a custom
     * background.
     */
    protected void paintBackground(Graphics2D graphics2D) {
        graphics2D.setColor(Color.BLACK);
        graphics2D.fillRect(xoffset, yoffset,
                (int) (boardSize.getWidth() * dw),
                (int) (boardSize.getHeight() * dh));
    }

}
