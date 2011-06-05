package edu.asu.commons.foraging.graphics;

import javax.media.opengl.GL;


/**
 * The triangle class represents a triangle in 3D space
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 *
 */
public class Triangle {

	/**
	 * First vertex of the triangle
	 */
	private Point3D a = new Point3D();
	
	/**
	 * Second vertex of the triangle
	 */
	private Point3D b = new Point3D();
	
	/**
	 * Third vertex of the triangle
	 */
	private Point3D c = new Point3D();
	
	/**
	 * Normal at the first vertex of the triangle
	 */
	private Vector3D aNormal = null;
	
	/**
	 * Normal at the second vertex of the triangle 
	 */
	private Vector3D bNormal = null;
	
	/**
	 * Normal at the third vertex of the triangle
	 */
	private Vector3D cNormal = null;
	
	/**
	 * Face normal
	 */
	private Vector3D normal = null;
	
	/**
	 * Constructs a triangle
	 *
	 */
	public Triangle() {
		
	}
	
	/**
	 * Constructs a triangle with the specified vertices
	 * @param a first vertex
	 * @param b second vertex
	 * @param c third vertex
	 */
	public Triangle(Point3D a, Point3D b, Point3D c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	/**
	 * Returns the first vertex of the triangle
	 * @return first vertex
	 */
	public Point3D a() {
		return a;
	}
	
	/**
	 * Returns the second vertex of the triangle
	 * @return second vertex
	 */
	public Point3D b() {
		return b;
	}
	
	/**
	 * Returns the third vertex of the triangle
	 * @return third vertex
	 */
	public Point3D c() {
		return c;
	}
	
	/**
	 * Returns the centroid of the triangle
	 * @return centroid
	 */
	public Point3D centroid() {
		return (a.add(b).add(c).divide(3.0f));
	}
	
	/**
	 * Calculates face and vertex normals of this triangle
	 *
	 */
	public void calculateNormals() {
		faceNormal(true);
		aNormal();
		bNormal();
		cNormal();
	}
	
	/**
	 * Calculates face normal
	 * @param shouldNormalize if true, normalizes the calculated face normal 
	 * @return face normal
	 */
	public Vector3D faceNormal(boolean shouldNormalize) {		
		if (normal == null) {
			Vector3D v1 = new Vector3D(b, a);
			Vector3D v2 = new Vector3D(c, a);
			normal = v1.cross(v2);
			if (shouldNormalize) normal.normalize();
		}		
		return normal;
	}
	
	/**
	 * Calculates normal at the first vertex of this triangle
	 * @return normal at the first vertex
	 */
	public Vector3D aNormal() {
		//Calculate normal at vertex a		
		if (aNormal == null) {
			Vector3D v1 = new Vector3D(b, a);
			Vector3D v2 = new Vector3D(c, a);
			aNormal = v1.cross(v2);
			aNormal.normalize();
		}
		return aNormal;
	}
		
	/**
	 * Calculates normal at the second vertex of this triangle
	 * @return normal at the second vertex
	 */
	public Vector3D bNormal() {
		//Calculate normal at vertex b
		if (bNormal == null) {			
			Vector3D v1 = new Vector3D(c, b);
			Vector3D v2 = new Vector3D(a, b);
			bNormal = v1.cross(v2);
			bNormal.normalize();
		}
		return bNormal;
	}
		
	/**
	 * Calculates normal at the third vertex of this triangle
	 * @return normal at the third vertex
	 */
	public Vector3D cNormal() {
		//Calculate normal at vertex c
		if (cNormal == null) {			
			Vector3D v1 = new Vector3D(a, c);
			Vector3D v2 = new Vector3D(b, c);
			cNormal = v1.cross(v2);
			cNormal.normalize();
		}
		return cNormal;
	}
	
	/**
	 * Returns a point of intersection of a ray with this triangle
	 * @param ray Ray with whom intersection needs to be calculated 
	 * @return point of intersection
	 */
	public Point3D getIntersection(Ray ray){		
		float x = a.x - b.x;
		float y = a.y - b.y;
		float z = a.z - b.z;

		float d = a.x - c.x;
		float e = a.y - c.y;
		float f = a.z - c.z;

		Vector3D rayDirection = ray.getDirection();
		float g = rayDirection.x;
		float h = rayDirection.y;
		float i = rayDirection.z;

		Point3D rayOrigin = ray.getOrigin();
		float j = a.x - rayOrigin.x;
		float k = a.y - rayOrigin.y;
		float l = a.z - rayOrigin.z;

		float eihf = e*i - h*f;
		float gfdi = g*f - d*i;
		float dheg = d*h - e*g;
		
		float denominator = x*eihf + y*gfdi + z*dheg;

		float beta = (j*eihf + k*gfdi + l*dheg)/denominator;

		if(beta < 0.0f || beta > 1.0f)	
			return null;

		float xkjy = x*k - j*y;
		float jzxl = j*z - x*l;
		float ylkz = y*l - k*z;

		float gamma = (i*xkjy + h*jzxl + g*ylkz)/denominator;
		if (gamma < 0.0f || (beta + gamma) > 1.0)
			return null;

		float t = -(f*xkjy + e*jzxl + d*ylkz) / denominator;
		
		return ray.pointAtParameter(t);			
	}
	
	/**
	 * Returns the parameter, t at which the ray intersects this triangle
	 * @param ray Ray with whom intersection needs to be checked
	 * @return parameter, t
	 */
	public float getIntersectionParam(Ray ray){		
		float x = a.x - b.x;
		float y = a.y - b.y;
		float z = a.z - b.z;

		float d = a.x - c.x;
		float e = a.y - c.y;
		float f = a.z - c.z;

		Vector3D rayDirection = ray.getDirection();
		float g = rayDirection.x;
		float h = rayDirection.y;
		float i = rayDirection.z;

		Point3D rayOrigin = ray.getOrigin();
		float j = a.x - rayOrigin.x;
		float k = a.y - rayOrigin.y;
		float l = a.z - rayOrigin.z;

		float eihf = e*i - h*f;
		float gfdi = g*f - d*i;
		float dheg = d*h - e*g;
		
		float denominator = x*eihf + y*gfdi + z*dheg;

		float beta = (j*eihf + k*gfdi + l*dheg)/denominator;

		if(beta < 0.0f || beta > 1.0f)	
			return -1;

		float xkjy = x*k - j*y;
		float jzxl = j*z - x*l;
		float ylkz = y*l - k*z;

		float gamma = (i*xkjy + h*jzxl + g*ylkz)/denominator;
		if (gamma < 0.0f || (beta + gamma) > 1.0)
			return -1;

		float t = -(f*xkjy + e*jzxl + d*ylkz) / denominator;
		
		return t;			
	}
	
	/**
	 * Renders this triangle
	 * @param gl OpenGL interface
	 */
	public void display(GL gl) {
		gl.glBegin(GL.GL_TRIANGLES);			
			gl.glNormal3d(aNormal.x, aNormal.y, aNormal.z);
			gl.glVertex3d(a.x, a.y, a.z);
			gl.glNormal3d(bNormal.x, bNormal.y, bNormal.z);
			gl.glVertex3d(b.x, b.y, b.z);
			gl.glNormal3d(cNormal.x, cNormal.y, cNormal.z);
			gl.glVertex3d(c.x, c.y, c.z);
		gl.glEnd();
	}

	/**
	 * Sets the triangle vertices to the specified ones
	 * @param p1 first vertex 
	 * @param p2 second vertex
	 * @param p3 third vertex
	 */
	public void set(Point3D p1, Point3D p2, Point3D p3) {
		a.set(p1);
		b.set(p2);
		c.set(p3);
	}
	
	/**
	 * Sets coordinated of the triangle vertices to the specified ones
	 * @param p1x x coordinate of first vertex
	 * @param p1y y coordinate of second vertex
	 * @param p1z z coordinate of third vertex
	 * @param p2x x coordinate of second vertex
	 * @param p2y y coordinate of second vertex
	 * @param p2z z coordinate of second vertex
	 * @param p3x x coordinate of third vertex
	 * @param p3y y coordinate of third vertex
	 * @param p3z z coordinate of third vertex
	 */
	public void set(float p1x, float p1y, float p1z, float p2x, float p2y, float p2z, float p3x, float p3y, float p3z) {
		a.x = p1x;
		a.y = p1y;
		a.z = p1z;
		
		b.x = p2x;
		b.y = p2y;
		b.z = p2z;
		
		c.x = p3x;
		c.y = p3y;
		c.z = p3z;
	}
	
	/**
	 * Transforms this triangle using the specified transformation
	 * @param transformation matrix specifying the transformation
	 */
	public void transform(Matrix4 transformation) {
		Point3D newPoint = new Point3D();
		transformation.multiply(a, newPoint);
		a.set(newPoint);
		transformation.multiply(b, newPoint);
		b.set(newPoint);
		transformation.multiply(c, newPoint);
		c.set(newPoint);		
	}
}
