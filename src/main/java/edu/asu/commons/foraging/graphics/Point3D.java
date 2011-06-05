package edu.asu.commons.foraging.graphics;

import java.awt.Point;
import java.io.Serializable;

import edu.asu.commons.foraging.jcal3d.misc.Matrix;

/**
 * The Point3D class represents a point in 3D space.
 * 
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 */
public class Point3D implements Serializable {	
	private static final long serialVersionUID = -9084438717922978297L;
	
	/**
	 * X coordinate of this point
	 */
	public float x = 0.0f;
	
	/**
	 * Y coordinate of this point
	 */
	public float y = 0.0f;
	
	/**
	 * Z coordinate of this point
	 */
	public float z = 0.0f;
	
	/**
	 * Constructs a new point object
	 */
	public Point3D() {
		
	}
	
	/**
	 * Constructs a new point object based on the specified coordinate values
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 */
	public Point3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point3D(Point point) {
	    this((float)point.x, (float)point.y, 0);
	}
		
	/**
	 * Constructs a new point based on the specified point
	 * @param point a point whose coordinates should be assigned to this point
	 */
	public Point3D(Point3D point) {
		this.x = point.x;
		this.y = point.y;
		this.z = point.z;
	}
	
	/**
	 * Adds a point to this point
	 * @param p point to be added
	 * @return resultant point
	 */
	public Point3D add(Point3D p) {
		return new Point3D( (x+p.x), (y+p.y), (z+p.z) );
	}
	
	/**
	 * Subtracts a point from this point
	 * @param p point to be subtracted
	 * @return resultant point
	 */
	public Point3D subtract(Point3D p) {
		return new Point3D( (x-p.x), (y-p.y), (z-p.z) );
	}
	
	/**
	 * Divides this point with a number
	 * @param factor number to divide with
	 * @return resultant point
	 */
	public Point3D divide(float factor) {
		return new Point3D(x/factor, y/factor, z/factor);
	}
	
	/**
	 * Multiplies this point with another point
	 * @param p point to multiply with
	 * @return resultant point
	 */
	public float multiply(Point3D p) {
		return ( (x*p.x) + (y*p.y) + (z*p.z) );
	}
	
	/**
	 * Multiplies this point with a number
	 * @param factor number to multiply with
	 * @return resultant point
	 */
	public Point3D multiply(float factor) {
		return new Point3D( (x*factor), (y*factor), (z*factor) );
	}
	
	/**
	 * Multiplies this point with a matrix
	 * @param m matrix to multiply with
	 * @return resultant point
	 */
	public Point3D multiply(Matrix m) {
		return new Point3D(m.dxdx*x + m.dxdy*y + m.dxdz*z,
				m.dydx*x + m.dydy*y + m.dydz*z,
				m.dzdx*x + m.dzdy*y + m.dzdz*z);
	}
	
	/**
	 * Checks if this point is origin (0, 0, 0)
	 * @return true if origin, false otherwise
	 */
	public boolean isOrigin() {
		return (x == 0 && y == 0 && z == 0);
	}
	
	/**
	 * Returns string representation of this point
	 * @return string in the form (x, y, z)
	 */
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}
	
	/**
	 * Checks if the coordinates of this point equals the coordinated of the specified point 
	 * @param p point to compare with
	 * @return true if equal, false otherwise
	 */
	public boolean equals(Point3D p) {
		return (x == p.x && y == p.y && z == p.z);
	}
	
	/**
	 * Checks if integer parts of the coordinates of this point equals the specified point
	 * @param p point to compare with
	 * @return true if equal, false otherwise
	 */
	public boolean equalsIntPart(Point3D p) {
		Point3D p1 = new Point3D( Math.round(x), Math.round(y), Math.round(z) );
		Point3D p2 = new Point3D( Math.round(p.x), Math.round(p.y), Math.round(p.z));
		return (p1.x == p2.x && p1.y == p2.y && p1.z == p2.z);
	}
		
	/**
	 * Clears the x, y and z coordinates and sets them to zero
	 */
	public void clear() {
		x = 0;
		y = 0;
		z = 0;
	}
	
	/**
	 * Sets precision of this point to the specified decimal places.
	 * @param decimalPlaces no. of decimal places 
	 */
	public void setPrecision(int decimalPlaces) {
		float factor = 10 * decimalPlaces;
		x = (Math.round(x * factor))/ factor;
		y = (Math.round(y * factor))/ factor;
		z = (Math.round(z * factor))/ factor;
	}

	/**
	 * Sets coordinates of this point as per the specified point
	 * @param newPoint point used for setting up the coordinates 
	 */
	public void set(Point3D newPoint) {
		x = newPoint.x;
		y = newPoint.y;
		z = newPoint.z;		
	}
	
	/**
	 * Sets coordinated of this point as specified
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 */
	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
