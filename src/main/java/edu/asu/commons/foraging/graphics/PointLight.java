package edu.asu.commons.foraging.graphics;

import javax.media.opengl.GL;


/**
 * The PointLight class represents a point light source in a graphics application.
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 */
public class PointLight {
	
	/**
	 * Id of the point light. Can be of the form GL_LIGHTn
	 */
	protected int id;
	
	/**
	 * Position of the point light
	 */
	protected float[] position = new float[4];
	
	/**
	 * Ambient color of the point light
	 */
	protected float[] ambient = new float[4];
	
	/**
	 * Diffuse color of the point light
	 */
	protected float[] diffuse = new float[4];
	
	/**
	 * Specular color of the point light
	 */
	protected float[] specular = new float[4];
	
	/**
	 * Angle made by light vector with respect to X-axis in the horizontal plane
	 */
	protected float Angle_x;	//angle in radians
	
	/**
	 * Length of the light vector in the horizontal plane
	 */
	protected float Radius_xz;
	
	/**
	 * Step size used while moving this light source in vertical direction
	 */
	private static float VERT_STEP = 2;
	
	/**
	 * Step size used while moving this light along the light vector
	 */
	private static float RADIUS_STEP = 2;
	
	/**
	 * Step size used while moving this light angularly
	 */
	private static float ANGLE_STEP= 0.05f;
	
	/**
	 * Creates a new point light with the specified Id of the form GL_LIGHTn
	 * @param id
	 */
	public PointLight(int id) {
		this.id = id;
	}

	/**
	 * Returns position of this light source in the form of a float vector
	 * @return float vector holding x, y, z and positional/directional components
	 * 1: positional, 0: directional
	 */
	public float[] getPositionfv() {
		return position;
	}

	/**
	 * Returns position of this light source 
	 * @return a 3D point
	 */
	public Point3D getPosition() {
		return new Point3D(position[0], position[1], position[2]);		
	}
	
	/**
	 * Sets position and type of this light source.  
	 * @param position location in 3D space
	 * @param directional true: directional, false: positional
	 */
	public void setPosition(Point3D position, boolean directional) {
		this.position[0] = (float)position.x;
		this.position[1] = (float)position.y;
		this.position[2] = (float)position.z;
		if (directional) this.position[3] = 0.0f; 
	}
	
	/**
	 * Returns ambient color of this light source
	 * @return float vector holding the ambient color components
	 */
	public float[] getAmbient() {
		return ambient;
	}
	
	/**
	 * Sets ambient color of this light source
	 * @param r Red component of the ambient color
	 * @param g Green component of the ambient color
	 * @param b Blue component of the ambient color
	 * @param a Alpha component of the ambient color
	 */
	public void setAmbient(float r, float g, float b, float a) {
		this.ambient[0] = r;
		this.ambient[1] = g;
		this.ambient[2] = b;
		this.ambient[3] = a;
	}
	
	/**
	 * Returns ambient color of this light source
	 * @return float vector holding the diffuse color components
	 */
	public float[] getDiffuse() {
		return diffuse;
	}
	
	/**
	 * Sets diffuse color of this light source
	 * @param r Red component of the diffuse color
	 * @param g Green component of the diffuse color
	 * @param b Blue component of the diffuse color
	 * @param a Alpha component of the diffuse color
	 */
	public void setDiffuse(float r, float g, float b, float a) {
		this.diffuse[0] = r;
		this.diffuse[1] = g;
		this.diffuse[2] = b;
		this.diffuse[3] = a;
	}
	
	/**
	 * Returns specular color of this light source
	 * @return float vector holding the specular color components 
	 */
	public float[] getSpecular() {
		return specular;
	}
	
	/**
	 * Sets specular color of this light source
	 * @param r Red component of the specular color
	 * @param g Green component of the specular color
	 * @param b Blue component of the specular color
	 * @param a Alpha component of the specular color
	 */
	public void setSpeculart(float r, float g, float b, float a) {
		this.specular[0] = r;
		this.specular[1] = g;
		this.specular[2] = b;
		this.specular[3] = a;
	}
	
	/**
	 * Sets this point light source and enables it using OpenGL 
	 * @param gl interface to OpenGL
	 */
	public void init(GL gl) {
		gl.glLightfv(id, GL.GL_POSITION, position, 0);			
		gl.glLightfv(id, GL.GL_AMBIENT, ambient, 0);
		gl.glLightfv(id, GL.GL_DIFFUSE, diffuse, 0);
		gl.glLightfv(id, GL.GL_SPECULAR, specular, 0);

	    //Enable the light source
		gl.glEnable(id);
	}

	/**
	 * Enables this point light source using OpenGL
	 * @param gl interface to OpenGL
	 */
	public void enable(GL gl) {
		gl.glLightfv(id, GL.GL_POSITION, position, 0);		
		gl.glEnable(id);
	}
	
	/**
	 * Rotates this light to its left 
	 */
	public void rotateLeft()
	{
		Angle_x += ANGLE_STEP;
		position[0] = Radius_xz * (float)Math.cos(Angle_x);
		position[2] = Radius_xz * (float)Math.sin(Angle_x);
	}

	/**
	 * Rotates this light to its right
	 */
	public void rotateRight()
	{
		Angle_x -= ANGLE_STEP;
		position[0] = Radius_xz * (float)Math.cos(Angle_x);
		position[2] = Radius_xz * (float)Math.sin(Angle_x);
	}

	/**
	 * Moves this light near the origin
	 */
	public  void moveNear()
	{
		Radius_xz -= RADIUS_STEP;
		position[0] = Radius_xz * (float)Math.cos(Angle_x);
		position[2] = Radius_xz * (float)Math.sin(Angle_x);
	}

	/**
	 * Moves this light away from the origin
	 */
	public void moveAway()
	{
		Radius_xz += RADIUS_STEP;
		position[0] = Radius_xz * (float)Math.cos(Angle_x);
		position[2] = Radius_xz * (float)Math.sin(Angle_x);
	}

	/**
	 * Moves this light upwards
	 */
	public void moveUp()
	{
		position[1] += VERT_STEP;
	}

	/**
	 * Moves this light downwards
	 */
	public void moveDown()
	{
		position[1] -= VERT_STEP;
	}

}
