package edu.asu.commons.foraging.graphics;

import javax.media.opengl.GL;


/**
 * The ViewSettings class encapsulates rendering settings.
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 *
 */
public class ViewSettings {
	/**
	 * Specifies if geometry is rendered as solid or wireframe. Deafult is solid.
	 */
	public static int fillModel = GL.GL_FILL;
	
	/**
	 * Specifies if smooth or flat shading is applied while rendering the geometry. Default is smooth. 
	 */
	public static int shadeModel = GL.GL_SMOOTH;
}

