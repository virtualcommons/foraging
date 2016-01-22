package edu.asu.commons.foraging.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.Image;
import java.util.HashMap;
import java.util.HashSet;
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
    
    private boolean tokenFieldOfVisionEnabled;
    private boolean subjectFieldOfVisionEnabled;
    private boolean shouldNumberPlayers;   
    private boolean useTokenImage;
    
    public final static Color FIELD_OF_VISION_COLOR = new Color(255, 255, 255, 150);

	private final static Color COLLECTED_TOKEN_COLOR = Color.YELLOW;
	private final static Color TOKEN_COLOR = Color.GREEN;

    // associates a Duration with a piece of token consumed at the given Point -
    // the duration is used to render the token as shrinking.
    private Map<Point, Duration> collectedTokens = new HashMap<Point, Duration>();

    private int viewSubjectsRadius;

    private double viewTokensRadius;

    private Circle viewTokensField;

    private Circle viewSubjectsField;

    private double fieldOfVisionOffset;

    // Set from the the show-resource-zones parameter
    private boolean showResourceZones;

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
        showResourceZones = configuration.showResourceZones();
        synchronized (collectedTokens) {
            collectedTokens.clear();
            tokenFieldOfVisionEnabled = configuration.isTokensFieldOfVisionEnabled();
            if (tokenFieldOfVisionEnabled) {
                viewTokensRadius = configuration.getViewTokensRadius();
                Point location = dataModel.getCurrentPosition();
                viewTokensField = new Circle(location, viewTokensRadius);
            }
            subjectFieldOfVisionEnabled = configuration.isSubjectsFieldOfVisionEnabled();
            if (subjectFieldOfVisionEnabled) {
                viewSubjectsRadius = configuration.getViewSubjectsRadius();
                viewSubjectsField = new Circle(dataModel.getCurrentPosition(), viewSubjectsRadius);
            }
        }
        
        shouldNumberPlayers = configuration.getClientsPerGroup() > 2;
        useTokenImage = configuration.isTokenImageEnabled();
        
        super.setup(configuration);
        if (tokenFieldOfVisionEnabled || subjectFieldOfVisionEnabled) {
            fieldOfVisionOffset = (dw * 0.3d);
            System.err.println("field of vision offset: " + fieldOfVisionOffset);
        }
    }

    public void collectTokens(Point ... positions) {
    	if (dataModel.getRoundConfiguration().showTokenAnimation()) {
    		synchronized (collectedTokens) {
    			for (Point position: positions) {
    				collectedTokens.put(position, Duration.create(COLLECTED_TOKEN_ANIMATION_DURATION));
    			}
    		}
    	}
    }

    protected void paintTokens(Graphics2D graphics2D) {

        // When showing the resource zones, paint top and bottom tokens using different images,
        // and draw a line between the zones.
        HashSet<Point> resourcePositionsA = new HashSet<Point>();
        HashSet<Point> resourcePositionsB = new HashSet<Point>();
        int midHeight = (int) boardSize.getHeight() / 2;
        if (showResourceZones) {
            for (Point point : dataModel.getResourcePositions()) {
                if (point.y < midHeight) {
                    resourcePositionsA.add(point);
                } else {
                    resourcePositionsB.add(point);
                }
            }
            double lineY = scaleYDouble((double) midHeight);
            graphics2D.setColor(Color.WHITE);
            graphics2D.draw(new Line2D.Double(0, lineY, scaleXDouble(boardSize.getWidth()), lineY));
        }

        // three cases - show all food on the game board, show all food within
        // visible radius of the current player, or don't show any food.
        if (tokenFieldOfVisionEnabled) {
            viewTokensField.setCenter(dataModel.getCurrentPosition());
            if (showResourceZones) {
                paintCollection(resourcePositionsA, graphics2D, scaledTokenImage, this, viewTokensField);
                paintCollection(resourcePositionsB, graphics2D, scaledTokenImageB, this, viewTokensField);
            } else {
                paintCollection(dataModel.getResourcePositions(), graphics2D, scaledTokenImage, this, viewTokensField);
            }
        }
        else if (showResourceZones) {
        	paintCollection(resourcePositionsA, graphics2D, scaledTokenImage);
        	paintCollection(resourcePositionsB, graphics2D, scaledTokenImageB);
        } 
        else if (useTokenImage) {       
        	paintCollection(dataModel.getResourcePositions(), graphics2D, scaledTokenImage);         
        }
        else {
        	paintCollection(dataModel.getResourcePositions(), graphics2D, TOKEN_COLOR);
        }
        // display animation for food that has been eaten.
        long elapsedTime = 0;
        int width = (int) dw;
        int height = (int) dh;       
        // paint shrinking tokens that this client has consumed.
        if (dataModel.getRoundConfiguration().showTokenAnimation()) {
        	
        	synchronized (collectedTokens) {
        		Paint originalPaint = graphics2D.getPaint();
        		
        		for (Iterator<Map.Entry<Point, Duration>> iter = collectedTokens.entrySet().iterator(); iter.hasNext();) {
        			Map.Entry<Point, Duration> entry = iter.next();
        			Point point = entry.getKey();
        			Duration duration = entry.getValue();
        			elapsedTime = duration.getElapsedTime();
        			// FIXME: offset should be proportional to the actual size.
        			if (elapsedTime < 100L) {
        				graphics2D.setPaint(COLLECTED_TOKEN_COLOR);        				
        			} else if (elapsedTime < 200L) {
        				graphics2D.setPaint(TOKEN_COLOR);
        			} else if (elapsedTime < 300L) {
        				graphics2D.setPaint(COLLECTED_TOKEN_COLOR);
        			} else {
        				// After the time threshold has been exceeded, prune old food 
        				// that shouldn't be displayed.
        				iter.remove();
        				continue;
        			}
        			// highlight square behind the dot green
        			paintCollectedToken(point, graphics2D, width, height);        			
        		}
        		graphics2D.setPaint(originalPaint);
        	}
        }
    }

    private void paintCollectedToken(Point point, Graphics2D graphics2D, int width, int height) {
        int x = scaleX(point.x);
        int y = scaleY(point.y);
        
        if (useTokenImage) {
        	Image image;
        	if (showResourceZones) {
        		// Use token image for zone A or zone B depending on board position
        		image = (point.y < (int) boardSize.getHeight() / 2) ? scaledTokenImage : scaledTokenImageB;
        	} 
        	else {
        		image = scaledTokenImage;
        	}
        	graphics2D.drawImage(image, x, y, width, height, null);
        }
        else {
        	graphics2D.fillRect(x, y, width, height);
        }
    }

    protected void paintSubjects(Graphics2D graphics2D) {
        graphics2D.setFont(font);
        FontMetrics fontMetrics = graphics2D.getFontMetrics(font); 
        int characterHeight = fontMetrics.getAscent();
        int verticalCharacterSpacing = (int) ((dh - characterHeight) * 0.5d);
        Point currentPosition = dataModel.getCurrentPosition();
        if (subjectFieldOfVisionEnabled) {
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
                // Paint the subject's number
                if (shouldNumberPlayers) {
                	String subjectNumber = String.valueOf( dataModel.getAssignedNumber(id) );
                	//Calculate x and y so that the text is center aligned
                	int characterWidth = fontMetrics.stringWidth(subjectNumber);
                	int x = (int) (scaledX + ( (dw - characterWidth) * 0.5d));
                	int y = (int) (scaledY + characterHeight - verticalCharacterSpacing);
                	graphics2D.drawString(subjectNumber, x, y);
                }
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
        // The image to use is determined based on the client's assigned zone.
        Image image;
        if (dataModel.isBeingSanctioned(id)) {
            graphics2D.setColor(Color.CYAN);
            graphics2D.fillRect(x, y, getCellWidth(), getCellHeight());
            image = dataModel.getClientZone(id) == 1 ? scaledBeingSanctionedImageB : scaledBeingSanctionedImage;
            graphics2D.drawImage(image, x, y, this);
        }
        else if (dataModel.isSanctioning(id)) {
            graphics2D.setColor(Color.WHITE);
            graphics2D.fillRect(x, y, getCellWidth(), getCellHeight());
            image = dataModel.getClientZone(id) == 1 ? scaledSanctioningImageB : scaledSanctioningImage;
            graphics2D.drawImage(image, x, y, this);   
        }
//        else if (id.equals(dataModel.getMonitorId())) {
//            graphics2D.drawImage(scaledMonitorImage, x, y, this);
//        }
        else if (id.equals(dataModel.getId())) {
            if (dataModel.isExplicitCollectionMode()) {
                image = dataModel.getClientZone(id) == 1 ? scaledSelfExplicitCollectionModeImageB : scaledSelfExplicitCollectionModeImage;
                graphics2D.drawImage(image, x, y, this);
            }
            else {
        		//System.out.println("Is a self image");
                image = dataModel.getClientZone(id) == 1 ? scaledSelfImageB : scaledSelfImage;
                graphics2D.drawImage(image, x, y, this);
            }
        }
        else {
            image = dataModel.getClientZone(id) == 1? scaledOtherSubjectImageB : scaledOtherSubjectImage;
        	graphics2D.drawImage(image, x, y, this);
        }
    }
    
}
