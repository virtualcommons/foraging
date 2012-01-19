package edu.asu.commons.foraging.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.asu.commons.foraging.client.ClientDataModel;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.util.Duration;



/**
 * $Id$
 * <p>
 * This class encapsulates the client's view of the game board. Used by the
 * ForagerGameWindow to render the current state of the game.
 * 
 * <br/>
 * FIXME: refactor field of vision
 * </p>
 * 
 * 
 * @author Allen Lee
 * @version $Revision$
 * 
 */
public class SubjectView extends GridView {

    private static final long COLLECTED_TOKEN_ANIMATION_DURATION = 2000L;

    private static final long serialVersionUID = 8215577330876498459L;

    private final ClientDataModel dataModel;
    
    private boolean tokenFieldOfVision;
    private boolean subjectFieldOfVision;
    
    public final static Color FIELD_OF_VISION_COLOR = new Color(255, 255, 255, 150);

    // associates a Duration with a piece of token consumed at the given Point -
    // the duration is used to render the token as shrinking.
    private Map<Point, Duration> collectedTokens = new HashMap<Point, Duration>();

    private int viewSubjectsRadius;

    private double viewTokensRadius;

    private Circle viewTokensField;

    private Circle viewSubjectsField;

    private double fieldOfVisionOffset;

    public SubjectView(Dimension screenSize, ClientDataModel dataModel) {
        super(screenSize);
        this.dataModel = dataModel;
    }

    /**
     * FIXME: use dataModel.getRoundConfiguration() instead, but need to ensure it is updated properly 
     * prior to invocation of this method.
     */
    public void setup(RoundConfiguration configuration) {
        viewSubjectsField = null;
        viewTokensField = null;
        synchronized (collectedTokens) {
            collectedTokens.clear();
            tokenFieldOfVision = configuration.isTokensFieldOfVisionEnabled();
            if (tokenFieldOfVision) {
                viewTokensRadius = configuration.getViewTokensRadius();
                Point location = dataModel.getCurrentPosition();
                viewTokensField = new Circle(location, viewTokensRadius);
            }
            subjectFieldOfVision = configuration.isSubjectsFieldOfVisionEnabled();
            if (subjectFieldOfVision) {
                viewSubjectsRadius = configuration.getViewSubjectsRadius();
                viewSubjectsField = new Circle(dataModel.getCurrentPosition(), viewSubjectsRadius);
            }
        }
        super.setup(configuration);
        if (tokenFieldOfVision || subjectFieldOfVision) {
            fieldOfVisionOffset = (dw / 2.0d);
            System.err.println("field of vision offset: " + fieldOfVisionOffset);
        }
    }

    public void collectTokens(Point ... positions) {
        synchronized (collectedTokens) {
            for (Point position: positions) {
                collectedTokens.put(position, Duration.create(COLLECTED_TOKEN_ANIMATION_DURATION));
            }
        }
    }

    protected void paintTokens(Graphics2D graphics2D) {
        // three cases - show all food on the game board, show all food within
        // visible radius of the current player, or don't show any food.
        if (tokenFieldOfVision) {
            viewTokensField.setCenter(dataModel.getCurrentPosition());
            paintCollection(dataModel.getResourcePositions(), graphics2D, scaledTokenImage, this, viewTokensField);
        }
        else {
            paintCollection(dataModel.getResourcePositions(), graphics2D, scaledTokenImage);
        }
        // display animation for food that has been eaten.
        long elapsedTime = 0;
        int width = (int) dw;
        int height = (int) dh;
        int offset = 1;
        // paint shrinking tokens that this client has consumed.
        synchronized (collectedTokens) {
            for (Iterator<Map.Entry<Point, Duration>> iter = collectedTokens.entrySet().iterator(); iter.hasNext();) {
                Map.Entry<Point, Duration> entry = iter.next();
                Point point = entry.getKey();
                Duration duration = entry.getValue();
                elapsedTime = duration.getElapsedTime();
                // FIXME: offset should be proportional to the actual size.
                if (elapsedTime < 333L) {
                    offset = 1;
                } else if (elapsedTime < 666L) {
                    offset = 2;
                } else if (elapsedTime < 1000L) {
                    offset = 3;
                } else {
                    // After the time threshold has been exceeded, prune old food 
                    // that shouldn't be displayed.
                    iter.remove();
                    continue;
                }
                // food pellets shrink over time
                paintToken(point, graphics2D, width/offset, height/offset);
            }
        }
    }

    private void paintToken(Point point, Graphics2D graphics2D, int width, int height) {
        int x = scaleX(point.x);
        int y = scaleY(point.y);
        graphics2D.drawImage(scaledTokenImage, x, y, width, height, null);
    }

    protected void paintSubjects(Graphics2D graphics2D) {
        graphics2D.setFont(font);
        FontMetrics fontMetrics = graphics2D.getFontMetrics(font); 
        int characterHeight = fontMetrics.getAscent();
        int verticalCharacterSpacing = (int) ( (dh - characterHeight) / 2);
        Point currentPosition = dataModel.getCurrentPosition();
        if (subjectFieldOfVision) {
            // paint a transparent circle centered on the current position of the subject.
            int radius = viewSubjectsRadius;
            viewSubjectsField.setCenter(currentPosition);
            Point topLeftCorner = new Point(currentPosition.x - radius, currentPosition.y - radius);
            double x = scaleXDouble(topLeftCorner.x) + fieldOfVisionOffset;
            double y = scaleYDouble(topLeftCorner.y) + fieldOfVisionOffset;
            double diameter = (dw * radius * 2.0d) + fieldOfVisionOffset;
            Ellipse2D.Double circle = new Ellipse2D.Double(x, y, diameter, diameter);
            // clip the rendered part of the Field of vision circle that crosses the playing boundary 
            graphics2D.setClip(circle);
            Rectangle bounds = new Rectangle(getPreferredSize());
            graphics2D.clip(bounds);
            Paint originalPaint = graphics2D.getPaint();
            graphics2D.setPaint(FIELD_OF_VISION_COLOR);
            graphics2D.fill(circle);
            graphics2D.setPaint(originalPaint);
        }
        for (Map.Entry<Identifier, Point> entry : dataModel.getClientPositions().entrySet()) {
            Identifier id = entry.getKey();
            Point subjectLocation = entry.getValue();
            // optimized conditional
            if (viewSubjectsField == null || id.equals(dataModel.getId()) || viewSubjectsField.contains(subjectLocation)) {
                // only draw if:
                // 1. field of vision is not enabled for subjects
                // 2. subject being drawn is this participant (i.e., id == dataModel.id)
                // 3. field of vision contains subject location
                int scaledX = scaleX(subjectLocation.x);
                int scaledY = scaleY(subjectLocation.y);
                drawParticipant( graphics2D, id, scaledX, scaledY );
                //            graphics2D.drawImage( getImage(id), scaledX, scaledY, null);
                graphics2D.setColor( getSubjectNumberColor(id) );
                //          Paint the subject's number
                String subjectNumber = String.valueOf( dataModel.getAssignedNumber(id) );
                //Calculate x and y so that the text is center aligned
                int characterWidth = fontMetrics.stringWidth(subjectNumber);
                int x = (int) (scaledX + ( (dw - characterWidth) / 2));
                int y = (int) (scaledY + characterHeight - verticalCharacterSpacing);
                graphics2D.drawString(subjectNumber, x, y);
            }
        }
    }
    
    private Color getSubjectNumberColor(Identifier id) {
        if (id.equals(dataModel.getId())) {
            return Color.BLUE;
        }
        else {
            return Color.WHITE;
        }
    }
    
    private void drawParticipant(Graphics2D graphics2D, Identifier id, int x, int y) {
        if (dataModel.isBeingSanctioned(id)) {
            graphics2D.setColor(Color.CYAN);
            graphics2D.fillRect(x, y, getCellWidth(), getCellHeight());
            graphics2D.drawImage(scaledBeingSanctionedImage, x, y, this);
        }
        else if (dataModel.isSanctioning(id)) {
            graphics2D.setColor(Color.WHITE);
            graphics2D.fillRect(x, y, getCellWidth(), getCellHeight());
            graphics2D.drawImage(scaledSanctioningImage, x, y, this);   
        }
//        else if (id.equals(dataModel.getMonitorId())) {
//            graphics2D.drawImage(scaledMonitorImage, x, y, this);
//        }
        else if (id.equals(dataModel.getId())) {
            if (dataModel.isExplicitCollectionMode()) {
                graphics2D.drawImage(scaledSelfExplicitCollectionModeImage, x, y, this);
            }
            else {
        		//System.out.println("Is a self image");
                graphics2D.drawImage(scaledSelfImage, x, y, this);
            }
        }
        else {
        	graphics2D.drawImage(scaledOtherSubjectImage, x, y, this);
        }
    }
    
}
