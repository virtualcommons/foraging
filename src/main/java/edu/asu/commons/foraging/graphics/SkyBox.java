package edu.asu.commons.foraging.graphics;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.BufferUtil;

import edu.asu.commons.foraging.util.Tuple3i;

/**
 * The skybox class represents a skybox to create an illusion of 3D environment.
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 *
 */
public class SkyBox extends BoundingBox {

	/**
	 * The texture atlas used to texture the skybox 
	 */
	private static Texture texture;
	
	/**
	 * no. of vertices present in the skybox geometry
	 */
	private int nVertices = 0;
	
	/**
	 * Vertex Buffer Object Ids used for rendering
	 */
	private int[] VBOIds = new int[2];
	
	/**
	 * Create a new closed skybox
	 *
	 */
	public SkyBox() {
		super(true);	//We want a closed box
	}
	
	/**
	 * Initializes the skybox by loading the specified texture
	 * @param drawable current rendering context
	 */
	public static void init(GLAutoDrawable drawable, String textureFile) {
		TextureLoader texLoader = new TextureLoader();
		texture = texLoader.getTexture(textureFile, true);
		texture.create(drawable);
	}
	
	/**
	 * Populates texture coordinate buffer with texture coordinates of this skybox 
	 * @param textureBuffer
	 */
	private void populateVBOWithTextureCoords(FloatBuffer textureBuffer) {		
		//back face
		textureBuffer.put(1.00f); textureBuffer.put(0.50f);
		textureBuffer.put(0.75f); textureBuffer.put(0.50f);
		textureBuffer.put(0.75f); textureBuffer.put(0.75f);
		
		textureBuffer.put(1.00f); textureBuffer.put(0.50f);
		textureBuffer.put(0.75f); textureBuffer.put(0.75f);
		textureBuffer.put(1.00f); textureBuffer.put(0.75f);
		
		//right face
		textureBuffer.put(0.25f); textureBuffer.put(0.50f);
		textureBuffer.put(0.00f); textureBuffer.put(0.50f);
		textureBuffer.put(0.00f); textureBuffer.put(0.75f);
		
		textureBuffer.put(0.25f); textureBuffer.put(0.50f);
		textureBuffer.put(0.00f); textureBuffer.put(0.75f);
		textureBuffer.put(0.25f); textureBuffer.put(0.75f);
		
		//front face
		textureBuffer.put(0.50f); textureBuffer.put(0.50f);
		textureBuffer.put(0.25f); textureBuffer.put(0.50f);
		textureBuffer.put(0.25f); textureBuffer.put(0.75f);
		
		textureBuffer.put(0.50f); textureBuffer.put(0.50f);
		textureBuffer.put(0.25f); textureBuffer.put(0.75f);
		textureBuffer.put(0.50f); textureBuffer.put(0.75f);
		
		//left face
		textureBuffer.put(0.75f); textureBuffer.put(0.50f);
		textureBuffer.put(0.50f); textureBuffer.put(0.50f);
		textureBuffer.put(0.50f); textureBuffer.put(0.75f);
		
		textureBuffer.put(0.75f); textureBuffer.put(0.50f);
		textureBuffer.put(0.50f); textureBuffer.put(0.75f);
		textureBuffer.put(0.75f); textureBuffer.put(0.75f);
		
		//top face
		textureBuffer.put(0.25f); textureBuffer.put(1.0f);
		textureBuffer.put(0.50f); textureBuffer.put(1.0f);
		textureBuffer.put(0.50f); textureBuffer.put(0.75f);
		
		textureBuffer.put(0.25f); textureBuffer.put(1.00f);
		textureBuffer.put(0.50f); textureBuffer.put(0.75f);
		textureBuffer.put(0.25f); textureBuffer.put(0.75f);
		
		//bottom face
		textureBuffer.put(0.25f); textureBuffer.put(0.25f);
		textureBuffer.put(0.50f); textureBuffer.put(0.50f);
		textureBuffer.put(0.50f); textureBuffer.put(0.25f);
		
		textureBuffer.put(0.25f); textureBuffer.put(0.25f);
		textureBuffer.put(0.25f); textureBuffer.put(0.50f);
		textureBuffer.put(0.50f); textureBuffer.put(0.50f);
		
	}
	
	/**
	 * Creates vertex buffer objects to render this skybox.
	 * @param gl OpenGL interface
	 */
	private void createVBO(GL gl) {
		FloatBuffer vertexBuffer = BufferUtil.newFloatBuffer(324); //36 faces, 3 vertices per face, 3 co-ordinates per vertex
		FloatBuffer textureBuffer = BufferUtil.newFloatBuffer(216); //36 faces, 3 vertices per face, 2 co-ordinates per vertex
		nVertices = populateVBOWithVertices(vertexBuffer);
		vertexBuffer.flip();
		populateVBOWithTextureCoords(textureBuffer);
		textureBuffer.flip();
		
		gl.glGenBuffersARB(2, VBOIds, 0);
		
		//Load buffer into graphics card memory
		//Vertex coordinates
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[0]);  // Bind The Buffer
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nVertices * 3 * BufferUtil.SIZEOF_FLOAT, vertexBuffer, GL.GL_STATIC_DRAW_ARB);
        
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[1]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nVertices * 2 * BufferUtil.SIZEOF_FLOAT, textureBuffer, GL.GL_STATIC_DRAW_ARB);
        
        vertexBuffer = null;
        textureBuffer = null;		
	}
	
	/**
	 * Populates vertex buffer with vertices of this skybox
	 * @param vertexBuffer vertex buffer to be populated
	 * @return no. of vertices added to the buffer
	 */
	@Override
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
	 * Adds vertex to the vertex buffer
	 * @param vertex vertex to be added
	 * @param vertexBuffer buffer holding vertex information
	 */
	private void addVertex2VBO(Point3D vertex, FloatBuffer vertexBuffer) {
		vertexBuffer.put(vertex.x);
		vertexBuffer.put(vertex.y);
		vertexBuffer.put(vertex.z);
	}
	
	/**
	 * Renders the skybox using VBO
	 * @param drawable current rendering context
	 */
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
    	
		//Enable arrays
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
    	gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
    	
		if (nVertices == 0) {
			createVBO(gl);
		}
		
		//gl.glDisable(GL.GL_CULL_FACE);
		gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
		gl.glEnable(GL.GL_TEXTURE_2D);				
		texture.load(gl);
		
		//Vertex coordinates
		gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[0]);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);     
        
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[1]);
        gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0);     
                
        //Render
        // Draw All Of The Triangles At Once
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, nVertices);
        
		// Disable Vertex Arrays
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        
        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
        //gl.glEnable(GL.GL_CULL_FACE);
	}
}
