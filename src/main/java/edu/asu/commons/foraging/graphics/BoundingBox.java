package edu.asu.commons.foraging.graphics;

import java.nio.FloatBuffer;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import edu.asu.commons.foraging.util.Tuple2f;
import edu.asu.commons.foraging.util.Tuple3i;

/**
 * The BoundingBox class represents a bounding box formed by eight vertices and 
 * eight faces if it is a open bounding box or twelve faces if it is a closed bounding box.
 *	
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version 
 *
 *     
 */
//TOOD: add a figure pointing out all the vertices and faces
public class BoundingBox {
	
	/**
	 * Vector holding vertices of this bounding box
	 * The order of vertices is
	 * 	1. bottom, left, front vertex
		2. bottom, right, front vertex
		3. top, right, front vertex
		4. top, left, front vertex
		5. bottom, left, back vertex
		6. bottom, right, back vertex
		7. top, right, back vertex
		8. top, left, back vertex
	 */
	protected Vector<Point3D>	vertices = new Vector<Point3D>();
	
	/**
	 * Vector holding 1-based vertex indices that form faces of this bounding box
	 */
	protected Vector<Tuple3i>	faces = new Vector<Tuple3i>(); //Contains 1-based indices
	
	/**
	 * Vector holding texture co-ordinates at the vertices of this bounding box 
	 */
	protected Vector<Tuple2f>	texCoords = new Vector<Tuple2f>(); 
	
	/**
	 * Member specifying if the bounding box is open or closed
	 * A closed bounding box has faces at the top and bottom whereas an open bounding box does not 
	 */
	private boolean isClosed = false;
	
	/**
	 * The bottom, left, front vertex of this bounding box
	 * The first member in the vertices vector also points to this vertex
	 */
	private Point3D bottomLeftFront = new Point3D();
	
	/**
	 * The bottom, right, front vertex of this bounding box
	 * The second member in the vertices vector also points to this vertex 
	 */
	private Point3D bottomRightFront = new Point3D();
	
	/**
	 * The top, right, front vertex of this bounding box 
	 * The third member in the vertices vector also points to this vertex
	 */
	private Point3D topRightFront = new Point3D();
	
	/**
	 * The top, left, front vertex of this bounding box
	 * The fourth member in the vertices vector also points to this vertex 
	 */
	private Point3D topLeftFront = new Point3D();
	
	/**
	 * The bottom, left, back vertex of this bounding box
	 * The fifth member in the vertices vector also points to this vertex 
	 */
	private Point3D bottomLeftBack = new Point3D();
	
	/**
	 * The bottom, right, back vertex of this bounding box
	 * The sixth member in the vertices vector also points to this vertex 
	 */
	private Point3D bottomRightBack = new Point3D();
	
	/**
	 * The top, right, back vertex of this bounding box
	 * The seventh member in the vertices vector also points to this vertex 
	 */
	private Point3D topRightBack = new Point3D();
	
	/**
	 * The top, left, back vertex of this bounding box
	 * The eighth member in the vertices vector also points to this vertex 
	 */
	private Point3D topLeftBack = new Point3D();
	
	/**
	 * The texture co-ordinate of the bottom, left, front vertex of this bounding box
	 * The first member in the texCoords vector also points to this texture co-ordinate
	 */
	private Tuple2f texCoordBottomLeftFront = new Tuple2f(0.0f, 0.0f);
	
	/**
	 * The texture co-ordinate of the bottom, right, front vertex of this bounding box
	 * The second member in the texCoords vector also points to this texture co-ordinate
	 */
	private Tuple2f texCoordBottomRightFront = new Tuple2f(1.0f, 0.0f);
	
	/**
	 * The texture co-ordinate of the top, right, front vertex of this bounding box
	 * The third member in the texCoords vector also points to this texture co-ordinate
	 */
	private Tuple2f texCoordTopRightFront = new Tuple2f(1.0f, 1.0f);
	
	/**
	 * The texture co-ordinate of the top, left, front vertex of this bounding box
	 * The fourth member in the texCoords vector also points to this texture co-ordinate
	 */
	private Tuple2f texCoordTopLeftFront = new Tuple2f(0.0f, 1.0f);
	
	/**
	 * The texture co-ordinate of the bottom, left, back vertex of this bounding box
	 * The fifth member in the texCoords vector also points to this texture co-ordinate
	 */
	private Tuple2f texCoordBottomLeftBack = new Tuple2f(0.33f, 0.0f);
	
	/**
	 * The texture co-ordinate of the bottom, right, back vertex of this bounding box
	 * The sixth member in the texCoords vector also points to this texture co-ordinate
	 */
	private Tuple2f texCoordBottomRightBack = new Tuple2f(0.66f, 0.0f);
	
	/**
	 * The texture co-ordinate of the top, right, back vertex of this bounding box
	 * The seventh member in the texCoords vector also points to this texture co-ordinate
	 */
	private Tuple2f texCoordTopRightBack = new Tuple2f(0.66f, 1.0f);
	
	/**
	 * The texture co-ordinate of the top, left, back vertex of this bounding box
	 * The eighth member in the texCoords vector also points to this texture co-ordinate
	 */
	private Tuple2f texCoordTopLeftBack = new Tuple2f(0.33f, 1.0f);
	
	/**
	 * Member variable that helps to avoid continuous local variable instancing 
	 */
	private Triangle faceTriangle = new Triangle();
	
	/**
	 * Creates a new bounding box that is either closed or open
	 * A closed bounding box has faces at the top and bottom whereas an open bounding box does not.
	 * @param isClosed specifies if the bounding box is closed or open
	 */
	public BoundingBox(boolean isClosed) {
		this.isClosed = isClosed;
		configure();
	}
	
	/**
	 * Returns the bottom, left, front vertex of this bounding box
	 * @return bottom, left, front vertex of this bounding box
	 */
	public Point3D getBLF() {
		return bottomLeftFront;
	}
	
	/**
	 * Returns the bottom, right, front vertex of this bounding box
	 * @return bottom, right, front vertex of this bounding box
	 */
	public Point3D getBRF() {
		return bottomRightFront;
	}
	
	/**
	 * Returns the bottom, right, back vertex of this bounding box
	 * @return bottom, right, back vertex of this bounding box
	 */
	public Point3D getBRB() {
		return bottomRightBack;
	}
	
	/**
	 * Returns the bottom, left, back vertex of this bounding box
	 * @return bottom, left, back vertex of this bounding box
	 */
	public Point3D getBLB() {
		return bottomLeftBack;
	}
	
	/**
	 * Returns the top, left, front vertex of this bounding box
	 * @return top, left, front vertex of this bounding box
	 */
	public Point3D getTLF() {
		return topLeftFront;
	}
	
	/**
	 * Returns the top, right, front vertex of this bounding box
	 * @return top, right, front vertex of this bounding box
	 */
	public Point3D getTRF() {
		return topRightFront;
	}
	
	/**
	 * Returns the top, left, back vertex of this bounding box
	 * @return top, left, back vertex of this bounding box
	 */	
	public Point3D getTLB() {
		return topLeftBack;
	}
	
	/**
	 * Returns the top, right, back vertex of this bounding box
	 * @return top, right, back vertex of this bounding box
	 */
	public Point3D getTRB() {
		return topRightBack;
	}

	/**
	 * Sets co-ordinates of the bottom, left, front vertex of this bounding box 
	 * @param x x co-ordinate of the vertex
	 * @param y y co-ordinate of the vertex
	 * @param z z co-ordinate of the vertex
	 */
	public void setBLF(float x, float y, float z) {
		bottomLeftFront.x = x;
		bottomLeftFront.y = y;
		bottomLeftFront.z = z;
	}

	/**
	 * Sets co-ordinates of the bottom, right, front vertex of this bounding box 
	 * @param x x co-ordinate of the vertex
	 * @param y y co-ordinate of the vertex
	 * @param z z co-ordinate of the vertex
	 */
	public void setBRF(float x, float y, float z) {
		bottomRightFront.x = x;
		bottomRightFront.y = y;
		bottomRightFront.z = z;
	}
	
	/**
	 * Sets co-ordinates of the bottom, right, back vertex of this bounding box 
	 * @param x x co-ordinate of the vertex
	 * @param y y co-ordinate of the vertex
	 * @param z z co-ordinate of the vertex
	 */
	public void setBRB(float x, float y, float z) {
		bottomRightBack.x = x;
		bottomRightBack.y = y;
		bottomRightBack.z = z;
	}
	
	/**
	 * Sets co-ordinates of the bottom, left, back vertex of this bounding box 
	 * @param x x co-ordinate of the vertex
	 * @param y y co-ordinate of the vertex
	 * @param z z co-ordinate of the vertex
	 */
	public void setBLB(float x, float y, float z) {
		bottomLeftBack.x = x;
		bottomLeftBack.y = y;
		bottomLeftBack.z = z;
	}
	
	/**
	 * Sets co-ordinates of the top, left, front vertex of this bounding box 
	 * @param x x co-ordinate of the vertex
	 * @param y y co-ordinate of the vertex
	 * @param z z co-ordinate of the vertex
	 */
	public void setTLF(float x, float y, float z) {
		topLeftFront.x = x;
		topLeftFront.y = y;
		topLeftFront.z = z;
	}
	
	/**
	 * Sets co-ordinates of the top, right, front vertex of this bounding box 
	 * @param x x co-ordinate of the vertex
	 * @param y y co-ordinate of the vertex
	 * @param z z co-ordinate of the vertex
	 */
	public void setTRF(float x, float y, float z) {
		topRightFront.x = x;
		topRightFront.y = y;
		topRightFront.z = z;
	}
	
	/**
	 * Sets co-ordinates of the top, right, back vertex of this bounding box 
	 * @param x x co-ordinate of the vertex
	 * @param y y co-ordinate of the vertex
	 * @param z z co-ordinate of the vertex
	 */
	public void setTRB(float x, float y, float z) {
		topRightBack.x = x;
		topRightBack.y = y;
		topRightBack.z = z;
	}
	
	/**
	 * Sets co-ordinates of the top, left, back vertex of this bounding box 
	 * @param x x co-ordinate of the vertex
	 * @param y y co-ordinate of the vertex
	 * @param z z co-ordinate of the vertex
	 */
	public void setTLB(float x, float y, float z) {
		topLeftBack.x = x;
		topLeftBack.y = y;
		topLeftBack.z = z;
	}
	
	/**
	 * Returns the texture co-ordinates of the bottom, left, front vertex of this bounding box
	 * @return texture co-ordinates of the bottom, left, front vertex of this bounding box
	 */
	public Tuple2f getTexCoordBLF() {
		return texCoordBottomLeftFront;
	}
	
	/**
	 * Returns the texture co-ordinates of the bottom, right, front vertex of this bounding box
	 * @return texture co-ordinates of the bottom, right, front vertex of this bounding box
	 */
	public Tuple2f getTexCoordBRF() {
		return texCoordBottomRightFront;
	}
	
	/**
	 * Returns the texture co-ordinates of the bottom, right, back vertex of this bounding box
	 * @return texture co-ordinates of the bottom, right, back vertex of this bounding box
	 */
	public Tuple2f getTexCoordBRB() {
		return texCoordBottomRightBack;
	}
	
	/**
	 * Returns the texture co-ordinates of the bottom, left, back vertex of this bounding box
	 * @return texture co-ordinates of the bottom, left, back vertex of this bounding box
	 */
	public Tuple2f getTexCoordBLB() {
		return texCoordBottomLeftBack;
	}
	
	/**
	 * Returns the texture co-ordinates of the top, left, front vertex of this bounding box
	 * @return texture co-ordinates of the top, left, front vertex of this bounding box
	 */
	public Tuple2f getTexCoordTLF() {
		return texCoordTopLeftFront;
	}
	
	/**
	 * Returns the texture co-ordinates of the top, right, front vertex of this bounding box
	 * @return texture co-ordinates of the top, right, front vertex of this bounding box
	 */
	public Tuple2f getTexCoordTRF() {
		return texCoordTopRightFront;
	}
	
	/**
	 * Returns the texture co-ordinates of the top, left, back vertex of this bounding box
	 * @return texture co-ordinates of the top, left, back vertex of this bounding box
	 */
	public Tuple2f getTexCoordTLB() {
		return texCoordTopLeftBack;
	}
	
	/**
	 * Returns the texture co-ordinates of the top, right, back vertex of this bounding box
	 * @return texture co-ordinates of the top, right, back vertex of this bounding box
	 */
	public Tuple2f getTexCoordTRB() {
		return texCoordTopRightBack;
	}

	/**
	 * Sets the texture co-ordinates of the bottom, left, front vertex of this bounding box
	 * @param u u component of the texture co-ordinate
	 * @param v v component of the texture co-ordinate
	 */
	public void setTexCoordBLF(float u, float v) {
		texCoordBottomLeftFront.a = u;
		texCoordBottomLeftFront.b = v;
	}

	/**
	 * Sets the texture co-ordinates of the bottom, right, front vertex of this bounding box
	 * @param u u component of the texture co-ordinate
	 * @param v v component of the texture co-ordinate
	 */
	public void setTexCoordBRF(float u, float v) {
		texCoordBottomRightFront.a = u;
		texCoordBottomRightFront.b = v;
	}
	
	/**
	 * Sets the texture co-ordinates of the bottom, right, back vertex of this bounding box
	 * @param u u component of the texture co-ordinate
	 * @param v v component of the texture co-ordinate
	 */
	public void setTexCoordBRB(float u, float v) {
		texCoordBottomRightBack.a = u;
		texCoordBottomRightBack.b = v;
	}
	
	/**
	 * Sets the texture co-ordinates of the bottom, left, back vertex of this bounding box
	 * @param u u component of the texture co-ordinate
	 * @param v v component of the texture co-ordinate
	 */
	public void setTexCoordBLB(float u, float v) {
		texCoordBottomLeftBack.a = u;
		texCoordBottomLeftBack.b = v;
	}
	
	/**
	 * Sets the texture co-ordinates of the top, left, front vertex of this bounding box
	 * @param u u component of the texture co-ordinate
	 * @param v v component of the texture co-ordinate
	 */
	public void setTexCoordTLF(float u, float v) {
		texCoordTopLeftFront.a = u;
		texCoordTopLeftFront.b = v;
	}
	
	/**
	 * Sets the texture co-ordinates of the top, right, front vertex of this bounding box
	 * @param u u component of the texture co-ordinate
	 * @param v v component of the texture co-ordinate
	 */
	public void setTexCoordTRF(float u, float v) {
		texCoordTopRightFront.a = u;
		texCoordTopRightFront.b = v;
	}
	
	/**
	 * Sets the texture co-ordinates of the top, right, back vertex of this bounding box
	 * @param u u component of the texture co-ordinate
	 * @param v v component of the texture co-ordinate
	 */
	public void setTexCoordTRB(float u, float v) {
		texCoordTopRightBack.a = u;
		texCoordTopRightBack.b = v;
	}
	
	/**
	 * Sets the texture co-ordinates of the top, left, back vertex of this bounding box
	 * @param u u component of the texture co-ordinate
	 * @param v v component of the texture co-ordinate
	 */
	public void setTexCoordTLB(float u, float v) {
		texCoordTopLeftBack.a = u;
		texCoordTopLeftBack.b = v;
	}
	
	/**
	 * Configures the bouding box by setting up its faces, vertices and their texture co-ordinates.
	 * A closed bounding box has additional faces at the top and bottom
	 *
	 */
	public void configure() {
		vertices.clear();
		faces.clear();
		texCoords.clear();
		
		//Add vertices and texture coordinates in the corresponding vectors
		vertices.add(bottomLeftFront);
		texCoords.add(texCoordBottomLeftFront);		
		vertices.add(bottomRightFront);
		texCoords.add(texCoordBottomRightFront);		
		vertices.add(topRightFront);
		texCoords.add(texCoordTopRightFront);
		vertices.add(topLeftFront);
		texCoords.add(texCoordTopLeftFront);
		vertices.add(bottomLeftBack);
		texCoords.add(texCoordBottomLeftBack);
		vertices.add(bottomRightBack);
		texCoords.add(texCoordBottomRightBack);
		vertices.add(topRightBack);
		texCoords.add(texCoordTopRightBack);
		vertices.add(topLeftBack);
		texCoords.add(texCoordTopLeftBack);
		
		//Create faces (1-based index)		
		faces.add(new Tuple3i(1, 2, 3));
		faces.add(new Tuple3i(1, 3, 4));
		faces.add(new Tuple3i(5, 1, 4));
		faces.add(new Tuple3i(5, 4, 8));
		faces.add(new Tuple3i(6, 5, 8));
		faces.add(new Tuple3i(6, 8, 7));
		faces.add(new Tuple3i(2, 6, 7));
		faces.add(new Tuple3i(2, 7, 3));
		
		if (isClosed) {
			//Upper faces
			faces.add(new Tuple3i(4, 3, 7));
			faces.add(new Tuple3i(4, 7, 8));
			//Lower faces
			faces.add(new Tuple3i(1, 6, 2));
			faces.add(new Tuple3i(1, 5, 6));
		}
	}

	/**
	 * Transforms the vertices of this bounding box by the specified transformation
	 * @param transformation transformation matrix by which this bounding box should be transformed
	 */
	public void transform(Matrix4 transformation) {
		Point3D newPoint = new Point3D();
		transformation.multiply(bottomLeftFront, newPoint);
		bottomLeftFront.set(newPoint);
		transformation.multiply(bottomRightFront, newPoint);
		bottomRightFront.set(newPoint);
		transformation.multiply(topRightFront, newPoint);
		topRightFront.set(newPoint);
		transformation.multiply(topLeftFront, newPoint);
		topLeftFront.set(newPoint);
		transformation.multiply(bottomLeftBack, newPoint);
		bottomLeftBack.set(newPoint);
		transformation.multiply(bottomRightBack, newPoint);
		bottomRightBack.set(newPoint);
		transformation.multiply(topRightBack, newPoint);
		topRightBack.set(newPoint);
		transformation.multiply(topLeftBack, newPoint);
		topLeftBack.set(newPoint);
	}
	
	/**
	 * Checks if this bounding box intersects the specified ray within the specified length of the ray
	 * @param ray with which the intersection is tested
	 * @param length of the ray 
	 * @return true if the bounding box intersects the ray within the specified length
	 * false otherwise
	 */
	public boolean isIntersecting(Ray ray, float length) {
		Tuple3i face;
		float param;
		for (int faceIndex = 0; faceIndex < faces.size(); faceIndex++) {
			face = faces.get(faceIndex);
			faceTriangle.set(vertices.get(face.a-1), vertices.get(face.b-1), vertices.get(face.c-1));
			param = faceTriangle.getIntersectionParam(ray);
			if (param >= 0 && param <= length) {
				return true;
			}			
		}
		return false;
	}

	/**
	 * Displays this bounding box in immediate mode. Uses purple as a material color
	 * @param drawable
	 */
	public void display(GLAutoDrawable drawable) {
		Tuple3i face;
		Point3D p1;
		Point3D p2;
		Point3D p3;
		GL gl = drawable.getGL();
		
		//gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
		float[] color = {1.0f, 0.0f, 1.0f, 1.0f};		
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, color, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, color, 0);
		
		gl.glBegin(GL.GL_TRIANGLES);
		for (int faceIndex = 0; faceIndex < faces.size(); faceIndex++) {
			face = faces.get(faceIndex);
			p1 = vertices.get(face.a-1);
			p2 = vertices.get(face.b-1);
			p3 = vertices.get(face.c-1);
			
			gl.glVertex3f(p1.x, p1.y, p1.z);
			gl.glVertex3f(p2.x, p2.y, p2.z);
			gl.glVertex3f(p3.x, p3.y, p3.z);
		}
		gl.glEnd();
//		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
	}
	
	/**
	 * Returns reference to the vector that stores vertices of this bounding box
	 * @return reference to the vector that stores vertices of this bounding box
	 */
	public Vector<Point3D> getVertices() {
		return vertices;
	}
	
	/**
	 * Adds vertices, normals and texture co-ordinates of this bounding box to the vertex buffer objects (VBO)
	 * @param vertexBuffer reference to the VBO holding vertices
	 * @param normalBuffer reference to the VBO holding vertex normals
	 * @param texCoordBuffer reference to the VBO holding texture co-ordinates of the vertices
	 * @return no. of vertices buffered
	 */
	public int populateVBOWithVerticesInfo(FloatBuffer vertexBuffer, FloatBuffer normalBuffer, FloatBuffer texCoordBuffer) {
		
		int nFaces = faces.size();
		int nVertices = nFaces * 3;
		int vertexIndex = 0;
		Vector3D fakeNormal = new Vector3D(1.0f, 0.0f, 0.0f);
		
		Tuple3i face;
		for (int faceIndex = 0; faceIndex < nFaces; faceIndex++) {
			face = faces.get(faceIndex);
			
			//Vertex 1
			vertexIndex = face.a-1;
			addVertexInfo2VBO(vertices.get(vertexIndex), vertexBuffer, fakeNormal, normalBuffer, texCoords.get(vertexIndex), texCoordBuffer);
			
			//Vertex 2
			vertexIndex = face.b-1;
			addVertexInfo2VBO(vertices.get(vertexIndex), vertexBuffer, fakeNormal, normalBuffer, texCoords.get(vertexIndex), texCoordBuffer);
			
			//Vertex 3
			vertexIndex = face.c-1;
			addVertexInfo2VBO(vertices.get(vertexIndex), vertexBuffer, fakeNormal, normalBuffer, texCoords.get(vertexIndex), texCoordBuffer);
		}
		
		return nVertices;
	}
	
	/**
	 * Adds information of a single vertex to vertex buffer objects (VBO)
	 * @param vertex co-ordinates of the vertex
	 * @param vertexBuffer reference to the VBO holding vertices
	 * @param normal vertex normal
	 * @param normalBuffer reference to the VBO holding normals
	 * @param texCoord texture co-ordinates of the vertex
	 * @param texCoordBuffer reference to the VBO holding texture co-ordinates of the vertices 
	 */
	private void addVertexInfo2VBO(Point3D vertex, FloatBuffer vertexBuffer, Vector3D normal, FloatBuffer normalBuffer, 
			Tuple2f texCoord, FloatBuffer texCoordBuffer) {
		vertexBuffer.put(vertex.x);
		vertexBuffer.put(vertex.y);
		vertexBuffer.put(vertex.z);
		
		normalBuffer.put(normal.x);
		normalBuffer.put(normal.y);
		normalBuffer.put(normal.z);
		
		texCoordBuffer.put(texCoord.a);
		texCoordBuffer.put(texCoord.b);
	}
	
	/**
	 * Adds vertices of this bounding box to the vertex buffer objects (VBO)
	 * @param vertexBuffer reference to the VBO holding vertices
	 */
	public int populateVBOWithVertices(FloatBuffer vertexBuffer) {		
		int nFaces = faces.size();
		int nVertices = nFaces * 3;
		int vertexIndex = 0;
		
		Tuple3i face;
		for (int faceIndex = 0; faceIndex < nFaces; faceIndex++) {
			face = faces.get(faceIndex);
			
			//Vertex 1
			vertexIndex = face.a-1;
			addVertex2VBO(vertices.get(vertexIndex), vertexBuffer);
			
			//Vertex 2
			vertexIndex = face.b-1;
			addVertex2VBO(vertices.get(vertexIndex), vertexBuffer);
			
			//Vertex 3
			vertexIndex = face.c-1;
			addVertex2VBO(vertices.get(vertexIndex), vertexBuffer);
		}
		
		return nVertices;
	}
	
	/**
	 * Adds vertex to the vertex buffer object (VBO) 
	 * @param vertex to be buffered
	 * @param vertexBuffer reference to the VBO holding vertices
	 */
	private void addVertex2VBO(Point3D vertex, FloatBuffer vertexBuffer) {
		vertexBuffer.put(vertex.x);
		vertexBuffer.put(vertex.y);
		vertexBuffer.put(vertex.z);
	}
	
	/**
	 * Adds vertices of this bounding box to the vertex buffer objcet (VBO)
	 * @param vertexArray reference to the VBO holding vertices
	 * @return no. of vertices buffered
	 */
	public int addVertices2VA(FloatBuffer vertexArray) {
		int nFaces = faces.size();
		int nVertices = nFaces * 3;
		int vertexIndex = 0;
		
		Tuple3i face;
		for (int faceIndex = 0; faceIndex < nFaces; faceIndex++) {
			face = faces.get(faceIndex);
			
			//Vertex 1
			vertexIndex = face.a-1;
			addVertex2VA(vertices.get(vertexIndex), vertexArray);
			
			//Vertex 2
			vertexIndex = face.b-1;
			addVertex2VA(vertices.get(vertexIndex), vertexArray);
			
			//Vertex 3
			vertexIndex = face.c-1;
			addVertex2VA(vertices.get(vertexIndex), vertexArray);
		}
		
		return nVertices;
	}
	
	/**
	 * Adds top, right, front vertex, top, right, back vertex, top, left, back vertex and 
	 * top, left, front vertex to the vertex array object (VA)  
	 * @param vertexArray reference to the VA
	 */
	public void populateVAWithTopVertices(FloatBuffer vertexArray) {
		addVertex2VA(topRightFront, vertexArray);
		addVertex2VA(topRightBack, vertexArray);
		addVertex2VA(topLeftBack, vertexArray);
		addVertex2VA(topLeftFront, vertexArray);		
	}
	
	/**
	 * Adds bottom, right, front vertex, bottom, right, back vertex, bottom, left, back vertex and
	 * bottom, left front vertex to the vertex array object (VA)
	 * @param vertexArray reference to the VA
	 */
	public void populateVAWithBottomVertices(FloatBuffer vertexArray) {
		addVertex2VA(bottomRightFront, vertexArray);
		addVertex2VA(bottomRightBack, vertexArray);
		addVertex2VA(bottomLeftBack, vertexArray);
		addVertex2VA(bottomLeftFront, vertexArray);
	}
	
	/**
	 * Adds a single vertex to the vertex array object (VA)
	 * @param vertex to be buffered
	 * @param vertexArray reference to VA
	 */
	public static void addVertex2VA(Point3D vertex, FloatBuffer vertexArray) {
		vertexArray.put(vertex.x);
		vertexArray.put(vertex.y);
		vertexArray.put(vertex.z);
	}
	
	/**
	 * Converts this bounding box object to a string representation listing down all the vertices of the bounding box 
	 */
	public String toString() {
		return "BRF" + bottomRightFront + " BRB" + bottomRightBack + " BLB" + bottomLeftBack + " BLF" + bottomLeftFront + 
			" TRF" + topRightFront + " TRB" + topRightBack + " TLB" + topLeftBack + " TLF" + topLeftFront;
	}
}
