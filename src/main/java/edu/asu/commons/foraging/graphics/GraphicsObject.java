package edu.asu.commons.foraging.graphics;

import java.io.Serializable;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

/**
 * The abstract GraphicsObject class encapsulates basic functionality of a graphical object. Any object that has some geometry, materials 
 * and/or a texture can be extended from this class.
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version   
 * 
 */
public abstract class GraphicsObject implements Serializable {
	
	/**
	 * Holds coordinates of a vertex of the object with minimum x, y and z values
	 */
	protected Point3D minExtent = new Point3D(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
	
	/**
	 * Holds coordinates of a vertex of the object with maximum x, y and z values
	 */
	protected Point3D maxExtent = new Point3D(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	
	/**
	 * Holds coordinates of the center of the object
	 */
	protected Point3D center = new Point3D();
	
	//Material related data structures	
	/**
	 * Holds four components of the ambient color of the object
	 */
	protected float[] ambient = new float[4];
	
	/**
	 * Holds four components of the diffuse color of the object
	 */
	protected float[] diffuse = new float[4];
	
	/**
	 * Holds four components of the specular components of the object
	 */
	protected float[] specular = new float[4];
	
	/**
	 * Holds shininess values of the object
	 */
	protected float   shininess;
		
	/**
	 * Holds texture data to be applied to the object
	 */
	protected Texture texture;
		
	/**
	 * Configures this object. Derived classes can override this method to configure the object according to the functionality of the derived
	 * classes.
	 */
	public void configure() {
		
	}
		
	/**
	 * Displays this object. Derived classes can override this method to display the object according to the specific geometry of the derived 
	 * classes.
	 * @param drawable current rendering context
	 */
	public void display(GLAutoDrawable drawable) {
		
	}

	/**
	 * Returns coordinates of a vertex of the object with maximum x, y and z values
	 * @return 3D coordinates
	 */
	public Point3D getMaxExtent() {
		return maxExtent;
	}

	/**
	 * Returns coordinates of a vertex of the object with minimum x, y and z values
	 * @return 3D coordinates
	 */
	public Point3D getMinExtent() {
		return minExtent;
	}
	
	/**
	 * Sets a vertex with minimum x, y and z coordinates as minimum extent of this object
	 * @param minExtent 3D coordinates of a vertex with minimum x, y and z values
	 */
	public void setMinExtent(Point3D minExtent) {
		this.minExtent = minExtent;		
	}

	/**
	 * Sets a vertex with maximum x, y and z coordinates as maximum extent of this object
	 * @param maxExtent 3D coordinates of a vertex with maximum x, y and z values
	 */
	public void setMaxExtent(Point3D maxExtent) {
		this.maxExtent = maxExtent;		
	}
	
	/**
	 * Updates minimum and maximum extents of the object by comparing coordinates of the specified point 
	 * @param point which is compared against current minimum and maximum extents
	 */
	protected void updateExtents(Point3D point) {
		if (point.x < minExtent.x) minExtent.x = point.x;
		if (point.y < minExtent.y) minExtent.y = point.y;
		if (point.z < minExtent.z) minExtent.z = point.z;
		if (point.x > maxExtent.x) maxExtent.x = point.x;
		if (point.y > maxExtent.y) maxExtent.y = point.y;
		if (point.z > maxExtent.z) maxExtent.z = point.z;
	}
		
	/**
	 * Calculates center of this object using minimum and maximum extents
	 */
	public void calculateCenter() {
		this.center = maxExtent.add(minExtent).divide(2.0f);
	}
	
	/**
	 * Returns center of this object
	 * @return center
	 */
	public Point3D getCenter() {
		return this.center;
	}

	/**
	 * Sets center of this object to the specified point
	 * @param center 3D coordinates of a center
	 */
	public void setCenter(Point3D center) {
		this.center = center;
	}
	
	/**
	 * Returns texture applied to this object
	 * @return texture applied to this object
	 */
	public Texture getTexture() {
		return texture;
	}
	
	/**
	 * Sets texture of this object as the specified texture
	 * @param texture to be applied to this object
	 */
	public void setTexture(Texture texture) {
		this.texture = texture;
	}
	
	/**
	 * Sets ambient, diffuse and specular materials and shininess of this object
	 * @param ambient ambient material
	 * @param diffuse diffuse material
	 * @param specular specular material
	 * @param shininess shininess of the object
	 */
	public void setMaterials(RGBA ambient, RGBA diffuse, RGBA specular, float shininess) {
		setAmbient(ambient);
		setDiffuse(diffuse);
		setSpecular(specular);
		setShininess(shininess);
	}
	
	/**
	 * Sets ambient material of this object
	 * @param rgba ambient material
	 */
	public void setAmbient(RGBA rgba) {
		ambient[0] = rgba.r;
		ambient[1] = rgba.g;
		ambient[2] = rgba.b;
		ambient[3] = rgba.a;		
	}
	
	/**
	 * Sets diffuse material of this object
	 * @param rgba diffuse material
	 */
	public void setDiffuse(RGBA rgba) {
		diffuse[0] = rgba.r;
		diffuse[1] = rgba.g;
		diffuse[2] = rgba.b;
		diffuse[3] = rgba.a;		
	}
	
	/**
	 * Sets specular material of this object
	 * @param rgba specular material
	 */
	public void setSpecular(RGBA rgba) {
		specular[0] = rgba.r;
		specular[1] = rgba.g;
		specular[2] = rgba.b;
		specular[3] = rgba.a;		
	}

	/**
	 * Sets shininess of this object
	 * @param shininess shininess of this object
	 */
	public void setShininess(float shininess) {
		this.shininess = shininess;		
	}
	
	/**
	 * Applies materials using OpenGL while drawing this object 
	 * @param gl interface to OpenGL
	 */
	public void applyMaterial(GL gl) {
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, ambient, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, diffuse, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, specular, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess);
	}

}
