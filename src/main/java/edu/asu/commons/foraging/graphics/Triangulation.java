package edu.asu.commons.foraging.graphics;

import java.nio.FloatBuffer;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.BufferUtil;

import edu.asu.commons.foraging.util.Tuple2f;
import edu.asu.commons.foraging.util.Tuple3i;

/**
 * The Triangulation class represents a triangle mesh used in graphics applications
 * 
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 *
 */
public class Triangulation extends GraphicsObject{
		
	private static final long serialVersionUID = -8566547688852235602L;
	//Geometry related data structures
	/**
	 * Vector to store vertices of this triangulation
	 */
	protected Vector<Point3D>	vertices = new Vector<Point3D>();		//v in obj file
	
	/**
	 * Vector to store vertex normals of this triangulation
	 */
	protected Vector<Vector3D>	vertexNormals = new Vector<Vector3D>();	//vn in obj file
	
	/**
	 * Vector to store faces of this triangulation
	 */
	protected Vector<Tuple3i>	faces = new Vector<Tuple3i>();			//f in obj file
	
	/**
	 * Vector to store face normals of this triangulation
	 */
	protected Vector<Vector3D>  faceNormals = new Vector<Vector3D>();
	
	/**
	 * Vector to store no. of times a vertex has occured in this triangulation
	 */
	protected Vector<Integer>	vertexOccurances = new Vector<Integer>();
	
	/**
	 * Vector to store texture coordinates at each vertex
	 */
	protected Vector<Tuple2f> textureCoordinates = new Vector<Tuple2f>();
	
	/**
	 * Scale factor in x, y and z direction
	 */
	protected Point3D scale;	
	
	/**
	 * Geometry dirty flag
	 */
	private boolean updateGeometry = true;
	
	/**
	 * VBO ids used for rendering
	 */
	private int[] VBOIds = new int[3];
	
	/**
	 * no. of vertices to render
	 */
	private int nVertices = 0;
	
	/**
	 * Creates a new triangulation
	 *
	 */
	public Triangulation() {
		
	}
	
	/**
	 * Checks if a given vertex is present in this triangulation
	 * @param p vertex to be checked
	 * @return if present 1-based index of the vertex, else -1
	 */
	public int isVertexPresent(Point3D p) {
		for (int vertexIndex = 0; vertexIndex < vertices.size(); vertexIndex++) {
			if (vertices.get(vertexIndex).equals(p)) {
				return vertexIndex+1;
			}
		}
		return -1;
	}
	
	/**
	 * Adds the given vertex in the triangulation
	 * @param p vertex to be added
	 * @return 1-based index of the newly added vertex
	 */
	public int addVertex(Point3D p) {
		vertices.add(p);
		return vertices.size();
	}

	/**
	 * Adds a vertex normal at the specified index
	 * @param n vertex normal
	 * @param vertexIndex vertex index
	 * @return 1-based index of the newly added normal
	 */
	public int addVertexNormal(Vector3D n, int vertexIndex)	{
		// Index of vertices is '1' based, but we use '0' based indexing in Java		
		if( vertexNormals.size() < vertexIndex ) {	
			vertexNormals.setSize(vertexIndex);
			vertexNormals.setElementAt(n, vertexIndex-1);
		}
		else {
			Vector3D vertexNormal = vertexNormals.get(vertexIndex-1);
			if (vertexNormal == null) vertexNormals.setElementAt(n, vertexIndex-1);
			else vertexNormals.setElementAt(vertexNormal.add(n), (vertexIndex-1));			
			//We add all the normals at this vertex and later calculate average normal		
		}
		// this is also a 1-based value
		return vertexNormals.size();
	}
		
	/**
	 * Adds a face to this triangulation
	 * @param face face made up of three vertex indices
	 */
	public void addFace(Tuple3i face) {
		faces.add(face);		
		updateVertexOccurance(face.a);
		updateVertexOccurance(face.b);
		updateVertexOccurance(face.c);
	}
	
	/**
	 * Adds a face normal to this triangulation
	 * @param normal face normal
	 */
	public void addFaceNormal(Vector3D normal) {
		faceNormals.add(normal);
	}
	
	/**
	 * Updates the no. of times a vertex has occured in this triangulation. This is used to calculate vertex normals
	 * @param vertexIndex vertex index
	 */
	private void updateVertexOccurance(int vertexIndex) {
		//vertexIndex is 1-based, but we use 0-based indexing in Java			
		if (vertexOccurances.size() < vertexIndex) {
			vertexOccurances.setSize(vertexIndex);
			vertexOccurances.setElementAt(1, vertexIndex-1);				
		}
		else {
			Integer occurances = vertexOccurances.get(vertexIndex-1);
			if (occurances == null) vertexOccurances.setElementAt(1, vertexIndex-1);
			else vertexOccurances.setElementAt(occurances + 1, vertexIndex-1);
		}
	}
	
	//################# Get methods ########################
	/**
	 * Returns vertex at the specified index
	 * @param index 0-based index
	 * @return vertex
	 */
	public Point3D getVertex(int index) {
		return vertices.get(index);
	}
	
	/**
	 * Returns no. of vertices present in this triangulation
	 * @return no. of vertices
	 */
	public int getNoVertices() {
		return vertices.size();
	}
	
	/**
	 * Returns no. of faces present in this triangulation
	 * @return no. of faces
	 */
	public int getNoFaces() {
		return faces.size();
	}
	
	/**
	 * Returns no. of vertex normals present in this triangulation
	 * @return no. of vertex normals
	 */
	public int getNoVertexNormals() {
		return vertexNormals.size();
	}
	
	/**
	 * Returns face at the specified index 
	 * @param index 0-based index of the face
	 * @return tuple of vertex indices forming a face
	 */
	public Tuple3i getFace(int index) {
		return faces.get(index);
	}
	
	/**
	 * Returns vertex normal at the specified index
	 * @param index 0-based index
	 * @return vertex normal
	 */
	public Vector3D getNormal(int index) {
		return vertexNormals.get(index);
	}
	
	/**
	 * Clears all the data of this triangulation
	 *
	 */
	public void clearAll() {
		vertices.clear();
		vertexNormals.clear();		
		faces.clear();
		faceNormals.clear(); 	
		vertexOccurances.clear();
		textureCoordinates.clear();
	}
	
	/**
	 * Configures this triangulation by finding the minimum and maximum extents and calculating face and vertex normals.
	 */
	@Override
	public void configure() {
		findExtents();
		calculateNormals();
	}
	
	/**
	 * Finds minimum and maximum extents of this triangulation
	 *
	 */
	private void findExtents() {
		for (int vertexIndex = 0; vertexIndex < vertices.size(); vertexIndex++) {
			updateExtents(vertices.get(vertexIndex));
		}
	}
		
	/**
	 * Calculates face normals and then vertex normals using the face normals and vertex occurances.
	 *
	 */
	public void calculateNormals()	{
		boolean populateFaceNormals = faceNormals.size() == 0;
		boolean populateVertexNormals = vertexNormals.size() == 0;		
				
		if (populateFaceNormals || populateVertexNormals) {
			for (int faceIndex = 0; faceIndex < faces.size(); faceIndex++) {
				Tuple3i face = faces.get(faceIndex);
				Triangle triangle = new Triangle(vertices.get(face.a-1), vertices.get(face.b-1), vertices.get(face.c-1));
				Vector3D faceNormal = triangle.faceNormal(false);
				
				// FaceNormal array is 0-based 
				if (populateFaceNormals)
					faceNormals.add(faceNormal);

				if (populateVertexNormals)
				{
					// Assign normal to the vertices of the faces
					addVertexNormal(faceNormal, face.a);
					addVertexNormal(faceNormal, face.b);
					addVertexNormal(faceNormal, face.c);
				}
			}	
						
			// Find average of the accumulated vertex normals 
			for(int vertexIndex = 0; vertexIndex < vertexNormals.size(); ++vertexIndex)	{	
				Integer vertexOccurance = vertexOccurances.get(vertexIndex);
				if (vertexOccurance == null) return;
				vertexNormals.setElementAt(vertexNormals.get(vertexIndex).average(vertexOccurance), vertexIndex);				
			}
		}
	}
	
	/**
	 * Adds texture coordinates at a vertex to this triangulation
	 * @param t vertex coordinates
	 */
	public void addTextureCoordinate(Tuple2f t) {
		textureCoordinates.add(t);
	}
	
	/**
	 * Returns true if texture should be applied, else false
	 * @return true if texture should be applied, else false
	 */
	public boolean shouldApplyTexture() {
		return textureCoordinates.size() != 0;
	}

	/**
	 * Renders this triangulation using VBO
	 * @param drawable current rendering context
	 */	
	public void displayUsingVBO(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		
		//Enable arrays
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
//		gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
		
		if (shouldApplyTexture()) {		
			if (updateGeometry) {
				texture.create(drawable);
				createVBOForTexture(gl);
				updateGeometry = false;
			}
			texture.load(gl);
			gl.glEnable(GL.GL_TEXTURE_2D);
			sendVBOForTexture(gl);
			gl.glDisable(GL.GL_TEXTURE_2D);
		}
		else {
			if (updateGeometry) {
				createVBO(gl);
				updateGeometry = false;
			}		
			sendVBO(gl);
		}
		
		// Disable Vertex Arrays
		gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
//		gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
	}
	
	/**
	 * Creates Vertex Buffer Objects for vertices, normals and texture coordinates
	 * @param gl OpenGL interface
	 */
	private void createVBOForTexture(GL gl) {
		int nFaces = faces.size();
		nVertices = nFaces * 3;
		
		FloatBuffer vertexBuffer = BufferUtil.newFloatBuffer(nVertices * 3);
		FloatBuffer normalBuffer = BufferUtil.newFloatBuffer(nVertices * 3);
		FloatBuffer textureBuffer = BufferUtil.newFloatBuffer(nVertices * 2);
		
		Tuple3i face;
		int v1, v2, v3;
		for (int faceIndex = 0; faceIndex < nFaces; faceIndex++) {
			face = faces.get(faceIndex);
			
			v1 = face.a - 1;
			v2 = face.b - 1;
			v3 = face.c - 1;
			
			populateVBOWithVertexInfo(vertexBuffer, vertices.get(v1), normalBuffer, vertexNormals.get(v1), textureBuffer, textureCoordinates.get(v1));
			populateVBOWithVertexInfo(vertexBuffer, vertices.get(v2), normalBuffer, vertexNormals.get(v2), textureBuffer, textureCoordinates.get(v2));
			populateVBOWithVertexInfo(vertexBuffer, vertices.get(v3), normalBuffer, vertexNormals.get(v3), textureBuffer, textureCoordinates.get(v3));
		}

		vertexBuffer.flip();
		normalBuffer.flip();
		textureBuffer.flip();
		
		//Create VBO Id
		gl.glGenBuffersARB(3, VBOIds, 0);
        
		//Load buffer into graphics card memory
		//Vertex coordinates
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[0]);  // Bind The Buffer
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nVertices * 3 * BufferUtil.SIZEOF_FLOAT, vertexBuffer, GL.GL_STATIC_DRAW_ARB);
        
        //Normal coordinates
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[1]);
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nVertices * 3 * BufferUtil.SIZEOF_FLOAT, normalBuffer, GL.GL_STATIC_DRAW_ARB);
        
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[2]);
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nVertices * 2 * BufferUtil.SIZEOF_FLOAT, textureBuffer, GL.GL_STATIC_DRAW_ARB);
                
        vertexBuffer = null;
        normalBuffer = null;
        textureBuffer = null;

	}
	
	/**
	 * Creates Vertex Buffer Objects for vertices and normals
	 * @param gl OpenGL interface
	 */	
	private void createVBO(GL gl) {
		int nFaces = faces.size();
		nVertices = nFaces * 3;
		
		FloatBuffer vertexBuffer = BufferUtil.newFloatBuffer(nVertices * 3);//3 vertices per face, 3 floats per vertex
		FloatBuffer normalBuffer = BufferUtil.newFloatBuffer(nVertices * 3);
		
		Tuple3i face;
		int v1, v2, v3;
		for (int faceIndex = 0; faceIndex < nFaces; faceIndex++) {
			face = faces.get(faceIndex);
			
			v1 = face.a - 1;
			v2 = face.b - 1;
			v3 = face.c - 1;
				
			putVBO(vertexBuffer, vertices.get(v1), normalBuffer, vertexNormals.get(v1));
			putVBO(vertexBuffer, vertices.get(v2), normalBuffer, vertexNormals.get(v2));
			putVBO(vertexBuffer, vertices.get(v3), normalBuffer, vertexNormals.get(v3));
		}

		vertexBuffer.flip();
		normalBuffer.flip();
		
		//Create VBO Id
		gl.glGenBuffersARB(2, VBOIds, 0);
        
		//Load buffer into graphics card memory
		//Vertex coordinates
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[0]);  // Bind The Buffer
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nVertices * 3 * BufferUtil.SIZEOF_FLOAT, vertexBuffer, GL.GL_STATIC_DRAW_ARB);
        
        //Normal coordinates
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[1]);
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nVertices * 3 * BufferUtil.SIZEOF_FLOAT, normalBuffer, GL.GL_STATIC_DRAW_ARB);
        
        vertexBuffer = null;
        normalBuffer = null;
	}
	
	/**
	 * Sends the geometry information including vertices, normals and texture coordinates for rendering 
	 * @param gl OpenGL interface
	 */
	private void sendVBOForTexture(GL gl) {
		//Vertex coordinates
		gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[0]);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0); //specifies the location and data of an array of vertex coordinates to use when rendering    
        
        //Normal coordinates
		gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[1]);
        gl.glNormalPointer(3, GL.GL_FLOAT, 0);
        
        //Texture coordinates
		gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[2]);
        gl.glTexCoordPointer(3, GL.GL_FLOAT, 0, 0);
                
        //Render
        // Draw All Of The Triangles At Once
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, nVertices);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER,0);
	}
	
	/**
	 * Sends vertex information for rendering
	 * @param gl OpenGL interface
	 */
	private void sendVBO(GL gl) {
        //Vertex coordinates
		gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[0]);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0); //specifies the location and data of an array of vertex coordinates to use when rendering    
       
        //Render
        // Draw All Of The Triangles At Once
        //System.out.println("Drawing triangles " + nVertices);
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, nVertices);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER,0);
	}
	
	/**
	 * Populates buffers with vertex, normal and texture coordinate information
	 * @param vertexBuffer buffer holding vertices
	 * @param vertex vertex to be buffered
	 * @param normalBuffer buffer holding normals
	 * @param normal normal to be buffered
	 * @param textureBuffer buffer holding texture coordinates
	 * @param textureCoordinate texture coordinate to be buffered
	 */
	private void populateVBOWithVertexInfo(FloatBuffer vertexBuffer, Point3D vertex, FloatBuffer normalBuffer, Vector3D normal, FloatBuffer textureBuffer, Tuple2f textureCoordinate) {
		putVBO(vertexBuffer, vertex, normalBuffer, normal);
		
		//Texture
		textureBuffer.put(textureCoordinate.a);
		textureBuffer.put(textureCoordinate.b);
	}
	
	/**
	 * Populates buffers with vertex and normal information
	 * @param vertexBuffer buffer holding vertices
	 * @param vertex vertex to be buffered
	 * @param normalBuffer buffer holding normals
	 * @param normal normal to be buffered
	 */
	private void putVBO(FloatBuffer vertexBuffer, Point3D vertex, FloatBuffer normalBuffer, Vector3D normal) {
		//Vertex
		vertexBuffer.put(vertex.x);
		vertexBuffer.put(vertex.y);
		vertexBuffer.put(vertex.z);
		
		//Normal
		normalBuffer.put(normal.x);
		normalBuffer.put(normal.y);
		normalBuffer.put(normal.z);		
	}

	/**
	 * Renders this triangulation using immediate mode 
	 * @param drawable current rendering context
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
				
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, ViewSettings.fillModel);
		gl.glShadeModel(ViewSettings.shadeModel);
	
		//if( displayListID == -1 ) {		
			Tuple3i	face;			
			Tuple2f	texCoords;
			
			//Load texture, if applicable
			// Reasons for putting it here
			//  1. Loading texture needs drawable object
			//  2. As we only need to load it once, not including it in the display list			 
			
			boolean	isTextureApplied = shouldApplyTexture();
			if (isTextureApplied) {				
				texture.create(drawable);				
			}
						
			//displayListID = gl.glGenLists(1);
			//gl.glNewList(displayListID, GL.GL_COMPILE_AND_EXECUTE);
			
			//Apply material
			/*gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, ambient, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, diffuse, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, specular, 0);
			gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess);*/
			if (isTextureApplied) {
				texture.load(gl);
				gl.glEnable(GL.GL_TEXTURE_2D);
			}
			else {
				gl.glDisable(GL.GL_TEXTURE_2D);
			}
			
			//Draw the geometry
			gl.glBegin(GL.GL_TRIANGLES);			
				for(int faceIndex = 0; faceIndex < faces.size(); faceIndex++) {				
					face = faces.get(faceIndex);
					//Converting from 1-based to 0-based
					int a = face.a - 1;
					int b = face.b - 1;
					int c = face.c - 1;
					
					if (isTextureApplied) {
						//Vertex a
						//FIXME: Make the commented line work. 
						//Right now JOGL is crashing because of it w/o any exception in our code!!
						//texCoords = textureCoordinates.get(faceTextureIndices.get(faceIndex).a);
						texCoords = textureCoordinates.get(a);
						gl.glTexCoord2d(texCoords.a,texCoords.b);
					}
					gl.glNormal3d(vertexNormals.get(a).x, vertexNormals.get(a).y, vertexNormals.get(a).z);
					gl.glVertex3d(vertices.get(a).x, vertices.get(a).y, vertices.get(a).z);
					
					//Vertex b
					if (isTextureApplied) {
						texCoords = textureCoordinates.get(b);
						gl.glTexCoord2d(texCoords.a,texCoords.b);
					}
					gl.glNormal3d(vertexNormals.get(b).x, vertexNormals.get(b).y, vertexNormals.get(b).z);
					gl.glVertex3d(vertices.get(b).x, vertices.get(b).y, vertices.get(b).z);
					
					if (isTextureApplied) {
						texCoords = textureCoordinates.get(c);
						gl.glTexCoord2d(texCoords.a,texCoords.b);
					}
					gl.glNormal3d(vertexNormals.get(c).x, vertexNormals.get(c).y, vertexNormals.get(c).z);
					gl.glVertex3d(vertices.get(c).x, vertices.get(c).y, vertices.get(c).z);				
				}
			gl.glEnd();
		}
}
