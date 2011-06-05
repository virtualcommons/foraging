package edu.asu.commons.foraging.visualization.conceptual;

import java.awt.event.KeyEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.PointLight;
import edu.asu.commons.foraging.graphics.RGBA;
import edu.asu.commons.foraging.visualization.GameView3d;

public class AgentPreviewView extends GameView3d {
	
	private Agent agent = null;
	private AgentDesignPanel parentPanel;
	
	public AgentPreviewView(AgentDesignPanel parentPanel) {
		this.parentPanel = parentPanel;
	}
	
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
    }
	
	@Override
    public void display(GLAutoDrawable drawable) {
        super.display(drawable);
        GL gl = drawable.getGL();        
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glCullFace(GL.GL_BACK);         
        agent.display(drawable);        
    }
	
	@Override
	public void keyPressed(KeyEvent e) {
		//Do nothing
	}
	
	@Override
	public void initialize() {
		Agent.radius = 3.0f;
        Agent.specular = new RGBA(0.15f, 0.15f, 0.15f, 1.0f);
        agent = new Agent(new Point3D(), 0, new RGBA(parentPanel.getColor()), Agent.textureFile, 0, this);
        
        //camera
        camera.setPosition(new Point3D(0, 12, 3));
	}

	@Override
	public void updateResources() {
		// TODO Auto-generated method stub

	}
	
	public void setAgentColor(RGBA color) {
		agent.setColor(color);
	}

}
