package edu.asu.commons.foraging.graphics;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;


/**
 * This class represents a rectangle in 3D space. 
 * @author <a href='deepali.bhagvat@gmail.com'>Deepali Bhagvat</a>
 *
 */
public class Rectangle3D {
	/**
	 * Top left point of this rectangle
	 */
	public Point3D topLeft;
	
	/**
	 * Bottom left vertex of this rectangle
	 */
	public Point3D bottomLeft;
	
	/**
	 * Top right vertex of this rectangle 
	 */
	public Point3D topRight;
	
	/**
	 * Bottom right vertex of this rectangle
	 */
	public Point3D bottomRight;
	
	/**
	 * Texture applied to this rectangle
	 */
	private Texture texture = null;
	
	/**
	 * Material color applied to this rectangle
	 */
	private RGBA materialColor;
	
	/**
	 * Display list ID used for rendering
	 */
	private int displayListID = -1;
	
	/**
	 * Creates a new rectangle with the four vertices specified 
	 * @param topLeft top left vertex
	 * @param bottomLeft bottom left vertex
	 * @param topRight top right vertex
	 * @param bottomRight bottom right vertex
	 */
	public Rectangle3D(Point3D topLeft, Point3D bottomLeft, Point3D topRight, Point3D bottomRight) {
		this.topLeft = topLeft;
		this.bottomLeft = bottomLeft;
		this.topRight = topRight;
		this.bottomRight = bottomRight;
	}
	
	/**
	 * Sets the texture of this rectangle to the specified image
	 * @param textureFile image file
	 */
	public void setTexture(String textureFile) {
		TextureLoader textureLoader = new TextureLoader();
		texture = textureLoader.getTexture(textureFile, true);
	}
	
	/**
	 * Sets the material of this rectangle to the specified colorr
	 * @param rgba material color
	 */
	public void setMaterial(RGBA rgba) {
		materialColor = rgba;
	}
	
	/**
	 * Renders this rectangle using a display list 
	 * @param drawable current rendering context
	 */
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		
		if (displayListID == -1) {
			if (texture != null) texture.create(drawable);
			displayListID = gl.glGenLists(1);
			gl.glNewList(displayListID, GL.GL_COMPILE_AND_EXECUTE);
			if (texture != null) {
				texture.load(gl);
				gl.glEnable(GL.GL_TEXTURE_2D);
			}
		
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, materialColor.getfv(), 0);
			
			gl.glBegin(GL.GL_TRIANGLE_STRIP);
			gl.glTexCoord2f(1.0f, 0.0f);
			gl.glVertex3f(bottomRight.x, bottomRight.y, bottomRight.z);
			gl.glTexCoord2f(1.0f, 1.0f);
			gl.glVertex3f(topRight.x, topRight.y, topRight.z);
			gl.glTexCoord2f(0.0f, 0.0f);
			gl.glVertex3f(bottomLeft.x, bottomLeft.y, bottomLeft.z);
			gl.glTexCoord2f(0.0f, 1.0f);
			gl.glVertex3f(topLeft.x, topLeft.y, topLeft.z);
			gl.glEnd();
		
			if (texture != null) gl.glDisable(GL.GL_TEXTURE_2D);
			
			gl.glEndList();
		}
		else {
			gl.glCallList(displayListID);
		}
	}
}
