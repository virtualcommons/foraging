package edu.asu.commons.foraging.visualization.forestry.va;

import java.awt.Color;
import java.awt.event.KeyEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.PointLight;
import edu.asu.commons.foraging.graphics.RGBA;
import edu.asu.commons.foraging.visualization.GameView3d;
import edu.asu.commons.foraging.visualization.forestry.AvatarDesignPanel;

/**
 * The AvatarPreviewView class displays avatar preview while designing the avatar.
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version $Revision$
 */
public class AvatarPreviewView extends GameView3d {

	private static final long serialVersionUID = 7111629153664589603L;
	/**
	 * Avatar to be previewed
	 */
	private Woodcutter avatar = null;
	
	/**
	 * Container panel
	 */
	private AvatarDesignPanel parentPanel;
	
	/**
	 * Creates a new avatar preview
	 * @param parentPanel container panel
	 */
	public AvatarPreviewView(AvatarDesignPanel parentPanel) {
		this.parentPanel = parentPanel;
	}
	
	/**
	 * Initializes the preview 
	 * @param drawable current rendering context 
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
        super.init(drawable);

        // light
        PointLight pointLight0 = lights.elementAt(0);
        pointLight0.setPosition(new Point3D(-50, 80, 10), false);
        pointLight0.setAmbient(0.5f, 0.5f, 0.5f, 1.0f);
        pointLight0.setDiffuse(0.5f, 0.5f, 0.5f, 1.0f);
        pointLight0.setSpeculart(1.0f, 1.0f, 1.0f, 1.0f);
        pointLight0.init(drawable.getGL());
        
		//camera
        camera.setPosition(new Point3D(0, 10, 20));
        camera.setLookAtPoint(new Point3D(0, 8, 0));

    }
	
	/**
	 * Displays the avatar preview
	 * @param drawable current rendering context
	 */
	@Override
    public void display(GLAutoDrawable drawable) {
        super.display(drawable);
        GL gl = drawable.getGL();        
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glCullFace(GL.GL_BACK);         
        avatar.display(drawable);        
    }
	
	/**
	 * Handles the key pressed events. Since this is a preview, the method does not respond to any keys.
	 * @param e key event
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		//Do nothing
	}
	
	/**
	 * Initializes the avatar to be previewed using values selected on the avatar design panel
	 */
	@Override
	public void initialize() {
		avatar = new Woodcutter(new Point3D(), -90, 0.5f, this);
		avatar.init(ForestryView.AVATAR_FILE, parentPanel.getHairColor(), parentPanel.getSkinColor(), parentPanel.getShirtColor(), 
				parentPanel.getTrouserColor(), parentPanel.getShoesColor() );
		
		avatar.setMaterials(new RGBA(1.0f, 0.0f, 1.0f, 0.0f), new RGBA(1.0f, 0.0f, 1.0f, 0.0f), new RGBA(1.0f, 1.0f, 1.0f, 0.0f), 128.0f);
	}

	/**
	 * No-op
	 */
	@Override
	public void updateResources() {
		// TODO Auto-generated method stub
	}

	/**
	 * Sets the skin color to the specified color
	 * @param color new skin color
	 */
	public void setSkinColor(Color color) {
		avatar.setSkinColor(color);
	}

	/**
	 * Sets the shoes color to the specified color
	 * @param color new shoes color
	 */
	public void setShoesColor(Color color) {
		avatar.setShoesColor(color);
	}
	
	/**
	 * Sets the trouser color to the specified color
	 * @param color new trouser color
	 */
	public void setTrouserColor(Color color) {
		avatar.setTrouserColor(color);
	}
	
	/**
	 * Sets the shirt color to the specified color
	 * @param color new shirt color
	 */
	public void setShirtColor(Color color) {
		avatar.setShirtColor(color);
	}
}
