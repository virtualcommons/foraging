package edu.asu.commons.foraging.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.util.Duration;



/**
 * $Id: SubjectView.java 484 2010-03-09 00:42:46Z dbarge $
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
 * @version $Revision: 484 $
 * 
 */
public class SubjectView extends GridView {

    private static final long serialVersionUID = 8215577330876498459L;

    private final ClientDataModel dataModel;
    
    public final static Color FIELD_OF_VISION_COLOR = new Color(255, 255, 255, 150);

    // associates a Duration with a piece of token consumed at the given Point -
    // the duration is used to render the token as shrinking.
    private Map<Point, Duration> collectedTokens = new HashMap<Point, Duration>();

    public SubjectView(Dimension screenSize, ClientDataModel dataModel) {
        super(screenSize);
        this.dataModel = dataModel;
    }

    /**
     * FIXME: use dataModel.getRoundConfiguration() instead, but need to ensure it is updated properly 
     * prior to invocation of this method.
     */
    public void setup(RoundConfiguration configuration) {
        synchronized (collectedTokens) {
            collectedTokens.clear();
        }
        super.setup(configuration);
    }

    public void collectToken(Point p) {
        synchronized (collectedTokens) {
            collectedTokens.put(p, Duration.create(1000L));
        }
    }

    protected void paintTokens(Graphics2D graphics2D) {
        // three cases - show all food on the game board, show all food within
        // visible radius of the current player, or don't show any food.
        if (dataModel.getRoundConfiguration().isTokensFieldOfVisionEnabled()) {
            Point location = dataModel.getCurrentPosition();
            Circle viewTokensField = new Circle(location, dataModel.getRoundConfiguration().getViewTokensRadius());
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
        Map<Identifier, ClientData> positions = dataModel.getClientDataMap();
        graphics2D.setFont(font);
        FontMetrics fontMetrics = graphics2D.getFontMetrics(font); 
        int characterHeight = fontMetrics.getAscent();
        int verticalCharacterSpacing = (int) ( (dh - characterHeight) / 2);
        Circle fieldOfVision = null;
        Point currentPosition = dataModel.getCurrentPosition();
        RoundConfiguration roundConfiguration = dataModel.getRoundConfiguration();
        if (roundConfiguration.isSubjectsFieldOfVisionEnabled()) {
            double radius = roundConfiguration.getViewSubjectsRadius();
            fieldOfVision = new Circle(currentPosition, radius);
            // paint field of vision
            Paint originalPaint = graphics2D.getPaint();
            graphics2D.setPaint(FIELD_OF_VISION_COLOR);
            Point topLeftCorner = new Point(currentPosition.x - (int) radius, currentPosition.y - (int) radius);
            int x = scaleX(topLeftCorner.x);
            int y = scaleY(topLeftCorner.y);
            int diameter = (int) radius * 2;
            diameter = Math.min(scaleX(diameter), scaleY(diameter));
            Ellipse2D.Double circle = new Ellipse2D.Double(x, y, diameter, diameter);      
            //graphics2D.fillOval(x, y, diameter, diameter);
            // clip the rendered part of the Field of vision circle that crosses the playing boundary 
            graphics2D.setClip(circle);
            graphics2D.fill(circle);
            graphics2D.setPaint(originalPaint);
        }
        for (Map.Entry<Identifier, ClientData> entry : positions.entrySet()) {
            Identifier id = entry.getKey();
            Point subjectLocation = entry.getValue().getPosition();
            // optimized conditional
            if (fieldOfVision == null || id.equals(dataModel.getId()) || fieldOfVision.contains(subjectLocation)) {
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
        else if (id.equals(dataModel.getMonitorId())) {
            graphics2D.drawImage(scaledMonitorImage, x, y, this);
        }
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
