package edu.asu.commons.foraging.graphics;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;


/**
 * The Camera class encapsulates the functionality of a camera in a graphics application.
 * It also encapsulates camera navigation.
 * 
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version 
 */
public class Camera {
	/**
	 * Specifies camera location
	 */
	private Point3D position;
	
	/**
	 * Specifies the point at which camera is looking
	 */
	private Point3D lookAtPoint;
	
	/**
	 * Specifies the vector in the up direction which is Y-axis
	 */
	private Vector3D upVector = new Vector3D(0, 1, 0);
	
	/**
	 * Holds length of the camera vector in the XZ plane
	 */
	protected float length;
	
	/**
	 * Holds angle made by the camera vector with positive X-axis in radians 
	 */
	protected float angle = 0;
	
	/**
	 * Constant specifying step size of radius. This is used when moving the camera in forward or backward direction.
	 */
	public static float RADIUS_STEP = 2.0f;
	
	/**
	 * Constant specifying step size of angle in radians. This is used when moving the camera in left or right direction.
	 */
	public static float ANGLE_STEP = 0.03f;	
	
	/**
	 * Constant specifying step size in the up direction. This is used when moving the camera upwards or downwards.
	 */
	public static float ELEVATION_STEP = 2.0f;
	
	/**
	 * Creates a new camera placed at the specified position. Look at point is set to origin.
	 * @param position camera location
	 */
	public Camera(Point3D position) {
		this.position = position;
		this.lookAtPoint = new Point3D(0, 0, 0);		
		calculateLength();
		calculateAngle();
	}
	
	/**
	 * Creates a new camera placed at the specified position and looking at the specified point
	 * @param position camera location
	 * @param lookAtPoint camera look at point
	 */
	public Camera(Point3D position, Point3D lookAtPoint) {
		this.position = position;
		this.lookAtPoint = lookAtPoint;
		calculateLength();
		calculateAngle();	
	}
	
	/**
	 * Returns the current position of the camera
	 * @return camera position
	 */
	public Point3D getPosition() {
		return position;
	}
	
	/**
	 * Returns the current look at point of the camera
	 * @return look at point
	 */
	public Point3D getLookAtPoint() {
		return lookAtPoint;
	}
	
	/**
	 * Returns the angle made by the camera vector with the positive X-axis
	 * @return angle in radians
	 */
	public float getAngle() {
		return angle;
	}
	
	/**
	 * Retusn the length of the camera vector in the XZ plane
	 * @return length of the camera vector
	 */
	public float getRadius() {
		return length;
	}
	
	/**
	 * Returns the current height of the camera
	 * @return camera height
	 */
	public float getElevation() {
		return position.y;
	}
	
	/**
	 * Sets new angle made by the camera vector with positive X-axis. Updates the camera position accordingly.
	 * @param angle new angle made by the camera vector with positive X-axis
	 */
	public void setAngle(float angle) {
		this.angle = angle;
		updatePosition();
	}

	/**
	 * Sets new length of the camera vector in the XZ plane. Updates the camera position accordingly 
	 * @param length new length of the camera vector
	 */
	public void setLength(float length) {
		this.length = length;
		updatePosition();
	}
	
	/**
	 * Sets new look at point.
	 * @param lookAtPoint new point to look at
	 */
	public void setLookAtPoint(Point3D lookAtPoint) {
		this.lookAtPoint = lookAtPoint;		
	}
	
	/**
	 * Sets new position of the camera
	 * @param position new position 
	 */
	public void setPosition(Point3D position) {
		this.position = position;
	}
	
	/**
	 * Sets new elevation of the camera
	 * @param height new elevation
	 */
	public void setElevation(float height) {
		position.y = height;		
	}
	
	/**
	 * Calculates length of the camera vector in the XZ plane
	 */
	private void calculateLength() {
		length = (float)Math.sqrt( Math.pow( (position.x - lookAtPoint.x), 2) + Math.pow( (position.z - lookAtPoint.z), 2));
	}
	
	/**
	 * Calculates angle made by the camera vector with the positive X-axis
	 */
	private void calculateAngle() {
		angle = (float)Math.acos( (position.x - lookAtPoint.x) / length );
		//angle = (float)Math.acos( new Vector3D(new Point3D(position.x, 0, position.z), new Point3D(lookAtPoint.x, 0, lookAtPoint.z)).dot(new Vector3D(1, 0, 0)) );
	}
	
	/**
	 * Calculates camera position according to the camera vector length, angle made by the camera vector with the positive X-axis
	 * and the look at point
	 *
	 */
	private void updatePosition() {
		position.x = (float)Math.cos(angle) * length + lookAtPoint.x;
		position.z = (float)Math.sin(angle) * length + lookAtPoint.z;
	}
	
	/**
	 * Sets new position and new look at point of this camera
	 * @param position new position
	 * @param lookAtPoint new look at point
	 */
	public void setPoints(Point3D position, Point3D lookAtPoint) {
		this.position = position;
		this.lookAtPoint = lookAtPoint;
		calculateLength();
		calculateAngle();
	}
	
	/**
	 * Loads OpenGL model view matrix according to the camera values  
	 * @param drawable represnts primary rendering context
	 */
	public void loadModelViewMatrix(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		GLU glu = new GLU();
		
		gl.glMatrixMode(GL.GL_MODELVIEW);
    	gl.glLoadIdentity();    	
    	glu.gluLookAt(position.x, position.y, position.z, lookAtPoint.x, lookAtPoint.y, lookAtPoint.z, upVector.x, upVector.y, upVector.z);
	}

	//TODO: Implement this method
	public void lookUp() {
		//Calculate current position-lookat vector
		//Vector3D currentLookAtVector = new Vector3D(position, lookAtPoint);
		//float length = currentLookAtVector.length();
				
		//Calculate new lookat point that makes angle x with the current position
				
		//Set this new lookat point as the current lookat point
	}
}
