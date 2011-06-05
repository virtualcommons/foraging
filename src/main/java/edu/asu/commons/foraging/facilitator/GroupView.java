package edu.asu.commons.foraging.facilitator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;

import edu.asu.commons.foraging.client.GridView;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;

/**
 * $Id: GroupView.java 510 2010-04-20 04:11:22Z alllee $
 * 
 * Provides an overview visualization of a particular group and all participants in the group.
 * 
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 510 $
 */
public class GroupView extends GridView {

    private static final long serialVersionUID = -8972140468932621959L;
    
    private GroupDataModel groupDataModel;
    
    public GroupView(Dimension screenSize) {
        super(screenSize);
    }
    
    public GroupView(Dimension screenSize, GroupDataModel groupDataModel) {
        super(screenSize);
        this.groupDataModel = groupDataModel;
    }
    
    @Override
    protected void paintSubjects(Graphics2D graphics2D) {
        graphics2D.setFont(super.font);
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        int characterHeight = fontMetrics.getAscent();
        int verticalCharacterSpacing = (int) ( (dh - characterHeight) / 2);
        int yOffset = characterHeight - verticalCharacterSpacing;
        for (ClientData clientData: groupDataModel.getClientDataMap().values()) {
            Point subjectLocation = clientData.getPosition();
            int scaledX = scaleX(subjectLocation.x);
            int scaledY = scaleY(subjectLocation.y);
            graphics2D.drawImage(scaledOtherSubjectImage, scaledX, scaledY, this);
            // paint subject number
            graphics2D.setColor(Color.WHITE);
            String subjectNumber = String.valueOf( clientData.getAssignedNumber() );
            //Calculate x and y so that the text is roughly center aligned
            int characterWidth = fontMetrics.stringWidth(subjectNumber);
            int x = (int) (scaledX + ( (dw - characterWidth) / 2));
            int y = (int) (scaledY + yOffset);
            graphics2D.drawString(subjectNumber, x, y);
        }
    }

    @Override
    protected void paintTokens(Graphics2D graphics2D) {
        super.paintCollection(groupDataModel.getResourcePositions(), graphics2D, scaledTokenImage);
    }

    public void setGroupDataModel(GroupDataModel groupDataModel) {
        this.groupDataModel = groupDataModel;
    }

}
