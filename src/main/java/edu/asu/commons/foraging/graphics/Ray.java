package edu.asu.commons.foraging.graphics;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;


/**
 * The Ray class represents a ray with a starting point and a direction. 
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 */
public class Ray {

	/**
	 * Starting point of the ray
	 */
	Point3D origin;
	
	/**
	 * Direction of the ray
	 */
	Vector3D direction;
	
	/**
	 * Constructs a ray using the specified starting point and the direction
	 * @param origin stating point of the ray
	 * @param direction direction of the ray
	 */
	public Ray(Point3D origin, Vector3D direction) {
		this.origin = origin;
		this.direction = direction;
	}
	
	/**
	 * Returns starting point of the ray
	 * @return starting point
	 */
	public Point3D getOrigin() {
		return origin;
	}
	
	/**
	 * Sets starting point of the ray
	 * @param origin starting point 
	 */
	public void setOrigin(Point3D origin) {
		this.origin = origin;
	}
	
	/**
	 * Returns direction if the ray
	 * @return direction
	 */
	public Vector3D getDirection() {
		return direction;
	}
	
	/**
	 * Sets direction of the ray
	 * @param direction ray direction
	 */
	public void setDirection(Vector3D direction) {
		this.direction = direction;
	}
	
	/**
	 * Sets direction of the ray
	 * @param x x coordinate of the direction
	 * @param y y coordinate of the direction
	 * @param z z coordinate of the direction
	 */
	public void setDirection(float x, float y, float z) {
		this.direction.x = x;
		this.direction.y = y;
		this.direction.z = z;
	}
	
	/**
	 * Returns point on the ray at the given parameter value
	 * @param t parameter value
	 * @return point on ray
	 */
	public Point3D pointAtParameter(float t) {
		return new Point3D(origin.add(direction.multiply(t)));
	}
	
	/**
	 * Displays the ray. This can be used for displaying purposes.
	 * @param drawable current rendering context
	 * @param length length of the ray for display purposes.
	 */
	public void display(GLAutoDrawable drawable, float length) {
		GL gl = drawable.getGL();
		
		gl.glEnable(GL.GL_COLOR_MATERIAL);
		gl.glColor3f(1.0f, 1.0f, 0.0f);
		   
		gl.glBegin(GL.GL_LINES);
			gl.glVertex3f(origin.x, origin.y, origin.z);
			Point3D endPoint = pointAtParameter(length);
			gl.glVertex3f(endPoint.x, endPoint.y, endPoint.z);
		gl.glEnd();
		
		gl.glDisable(GL.GL_COLOR_MATERIAL);
	}
}
