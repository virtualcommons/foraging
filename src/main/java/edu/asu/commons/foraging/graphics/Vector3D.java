package edu.asu.commons.foraging.graphics;

import java.io.Serializable;

import edu.asu.commons.foraging.jcal3d.misc.Matrix;
import edu.asu.commons.foraging.jcal3d.misc.Quaternion;


/**
 * The Vector3D class represents a vector in 3D space.
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 *
 */
public class Vector3D extends Point3D implements Serializable{
	private static final long serialVersionUID = -5515595637951026300L;

	/**
	 * Creates a vector with all three components initialized to zero
	 */
	public Vector3D() {
		super();
	}
	
	/**
	 * Creates a vector with specified coordinates
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param z z coordinate
	 */
	public Vector3D(float x, float y, float z) {
		super(x, y, z);
	}
	
	/**
	 * Creates a vector from an existing vector
	 * @param v existing vector 
	 */
	public Vector3D(Vector3D v) {
		super(v.x, v.y, v.z);
	}
	
	/**
	 * Creates a vector from an existing point
	 * @param p existing point
	 */
	public Vector3D(Point3D p) {
		super(p.x, p.y, p.z);
	}
	
	/**
	 * Creates a vector using the formula vector = end point - start point
	 * @param a end point
	 * @param b start point
	 */
	public Vector3D(Point3D a, Point3D b) {
		x = a.x - b.x;
		y = a.y - b.y;
		z = a.z - b.z;		
	}
	
	/**
	 * Returns length of this vector  
	 * @return length
	 */
	public float length() {
		return (float)Math.sqrt( (x*x) + (y*y) + (z*z) );
	}
	
	/**
	 * Normalizes this vector
	 *
	 */
	public void normalize() {
		float length = length();
		x = x / length;
		y = y / length;
		z = z / length;
	}
	
	/**
	 * Returns a dot product of this vector with another vector
	 * @param v second vector in the dot product
	 * @return result of the dot product
	 */
	public float dot(Vector3D v) {
		return this.multiply(v); 
	}
	
	/**
	 * Returns cross product of this vector with another vector
	 * @param v second vector in the cross product
	 * @return result of the cross product
	 */
	public Vector3D cross(Vector3D v) {
		return new Vector3D( (y*v.z - z*v.y), (z*v.x - x*v.z), (x*v.y - y*v.x));		 
	}
	
	/**
	 * Multiply this vector with a quaternion
	 * @param q quaternion
	 * @return resultant vector
	 */
	public Vector3D multiply(Quaternion q) {
		Quaternion temp = new Quaternion(-q.x, -q.y, -q.z, q.w);
		temp = temp.multiply(this);
		temp = temp.multiply(q);

		return new Vector3D(temp.x, temp.y, temp.z);		
	}
	
	/**
	 * Returns cos of angle made by this vector with another vector in the horizontal plane
	 * @param v vector
	 * @return cos of angle between the two vectors
	 */	
	public float cosXZAngle(Vector3D v) {
		return this.dot(v) /(this.length() * v.length());
	}
		
	/**
	 * Returns a new vector whose components are divided by n
	 * @param n divisor
	 * @return new vector
	 */
	public Vector3D average(int n) {
		Vector3D vector = new Vector3D( (x/n), (y/n), (z/n) );
		vector.normalize();
		return vector;
	}	
	
	/**
	 * Adds a vector to this vector
	 * @param v vector to be added
	 * @return resultant vector
	 */
	public Vector3D add(Vector3D v) {
		return new Vector3D( (x+v.x), (y+v.y), (z+v.z) );
	}
	
	/**
	 * Blends this vector with another vector using the specified parameter
	 * @param d blending parameter
	 * @param v another vector
	 * @return resultant vector
	 */
	public Vector3D blend(float d, Vector3D v){
		return new Vector3D(x + d * (v.x - x), y + d * (v.y - y), z + d * (v.z - z));
	}
	
	/**
	 * Multiplies this vector with a float value
	 * @param factor float value with which to multiply
	 * @return resultant vector
	 */
	@Override
	public Vector3D multiply(float factor) {
		return new Vector3D( (x*factor), (y*factor), (z*factor) );
	}
	
	/**
	 * Multiply this vector by a matrix
	 * @param m matrix
	 * @return resultant vector
	 */
	public Vector3D multiply(Matrix m) {		
		return new Vector3D(m.dxdx*x + m.dxdy*y + m.dxdz*z,
				m.dydx*x + m.dydy*y + m.dydz*z,
				m.dzdx*x + m.dzdy*y + m.dzdz*z);
	}

	/**
	 * Sets this vector as a difference of the two specified points
	 * @param p1 end point of the vector
	 * @param p2 start point of the vector
	 */
	public void set(Point3D p1, Point3D p2) {
		x = p1.x - p2.x;
		y = p1.y - p2.y;
		z = p1.z - p2.z;
	}
		
}
