package edu.asu.commons.foraging.graphics;

import java.awt.Point;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.GLUT;

/**
 * The HUD class encapsulates Head-Up Display used to display textual information.
 *  
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version 
 *
 * 
 */
public class HUD {
	
	/**
	 * GLUT interface
	 */
	private GLUT glut = new GLUT();
	
	/**
	 * Font used to display HUD text
	 */
	private int font = GLUT.BITMAP_HELVETICA_18;
	
	/**
	 * Font color used to display HUD text
	 */
	private RGBA fontColor = new RGBA(0.0f, 0.25f, 0.0f, 1.0f);
	
	/**
	 * Font used to display HUD labels
	 */
	private int lableFont = GLUT.BITMAP_HELVETICA_12;
	
	/**
	 * Font color used to display HUD labels
	 */
	private RGBA lableColor = new RGBA(0.0f, 0.0f, 0.0f, 1.0f);
	
	/**
	 * String holding HUD text
	 */
	private String message;
	
	/**
	 * Flag specifying if HUD should be visible or invisible
	 */
	private boolean visible = false;
	
	/**
	 * Constructor 
	 */
	public HUD() {
		
	}
	
	/**
	 * Displays HUD 
	 * @param drawable current rendering context
	 */
	public void display(GLAutoDrawable drawable) {
//		if (visible) {
			//switchToOrthoProjection(drawable);
			displayMessage(drawable);
			//restorePreviousProjection(drawable);		
//		}
	}
	
	/**
	 * Displays HUD text at the bottom center of the game world
	 * @param drawable current rendering context
	 */
	public void displayMessage(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();		
		
	    gl.glColor3f(fontColor.r, fontColor.g, fontColor.b);
	    
	    int leftPosition = (drawable.getWidth() - glut.glutBitmapLength(font, message))/2;
	    int bottomPosition = 20;
        gl.glRasterPos3f(leftPosition, bottomPosition, 0);
        glut.glutBitmapString(font, message);
	}
	
	/**
	 * Displays label at the specified position
	 * @param drawable current rendering context
	 * @param lablePosition position to display the label
	 * @param lable string holding the label text
	 */
	public void displayLabel(GLAutoDrawable drawable, Point lablePosition, String lable) {
		GL gl = drawable.getGL();
		gl.glColor3f(lableColor.r, lableColor.g, lableColor.b);
		gl.glRasterPos3f(lablePosition.x, lablePosition.y, 0);
		glut.glutBitmapString(lableFont, lable);
	}
	
	/**
	 * Switches the projection matrix to orthographic projection and model view matrix to identity 
	 * to display 2D text in the 3D game world
	 * @param drawable current rendering context
	 */
	public void switchToOrthoProjection(GLAutoDrawable drawable)
	{
	    final GL gl = drawable.getGL();	   
	    gl.glMatrixMode(GL.GL_PROJECTION);
	    gl.glPushMatrix();                
	    gl.glLoadIdentity();              
	    gl.glOrtho(0, drawable.getWidth(), 0, drawable.getHeight(), -1, 1);
	    gl.glMatrixMode(GL.GL_MODELVIEW);
	    gl.glPushMatrix();
	    gl.glLoadIdentity();
	}

	/**
	 * Restores the projection and model view matrices 
	 * @param drawable current rendering context
	 */
	public void restorePreviousProjection(GLAutoDrawable drawable)
	{
	    GL gl = drawable.getGL();
	    gl.glMatrixMode(GL.GL_PROJECTION);
	    gl.glPopMatrix();

	    gl.glMatrixMode(GL.GL_MODELVIEW);
	    gl.glPopMatrix();
	}
	
	/**
	 * Sets HUD font the specified one
	 * @param font HUD font
	 */
	public void setFont(int font) {
		this.font = font;
	}
	
	/**
	 * Sets HUD font color to the specified one
	 * @param rgba HUD font color
	 */
	public void setColor(RGBA rgba) {
		this.fontColor = rgba;
	}
	
	/**
	 * Sets the HUD text to the specified message
	 * @param message string specifying HUD text
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * Sets visibility of HUD
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Returns a string corresponding to a no. e. g.
	 * <table>
	 * <tr><td>1</td> <td>once</td></tr>
	 * <tr><td>2</td> <td>twice</td></tr>
	 * <tr><td>3</td> <td>3 times</td></tr>
	 * <tr><td>4</td> <td>4 times</td></tr>
	 * </table>
	 * and so on
	 * @param number number to be converted
	 * @return string corresponding to the number
	 */
	public static String getNoToString(int number) {
		String string;
		switch (number) {
		case 1:
			string = "once";
			break;
		case 2:
			string = "twice";
			break;
		default:
			string = number + " times";
			break;
		}
		return string;
	}
}
