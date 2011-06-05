package edu.asu.commons.foraging.visualization.forestry.va;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.media.opengl.GL;

import com.sun.opengl.util.BufferUtil;

/**
 * The TreeView class initializes vertex arrays with the geometry of trees of different ages. This initialization takes place at the 
 * initialization of the experiment visualization. The arrays are then used while rendering the trees. This is a view type class in the MVC
 * architecture.
 *  
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version $Revision: 4 $
 *
 */
public class TreeView {	
	
	//Branches
	/**
	 * No. of vertices forming the branching structure of the tree
	 */	
	private static int branchVertices = 20;
	
	/**
	 * No. of coordinates forming the branching structure of the tree
	 */
	static int nBranchCoordinates = branchVertices * 3;
	
	/**
	 * Buffer storing geometry information of tree of age 1
	 */
	private static FloatBuffer age1BranchVertexBuffer = BufferUtil.newFloatBuffer(nBranchCoordinates); //n branches, 24 vertices per branch, 3 coordinates per vertex
	
	/**
	 * Buffer storing geometry information of tree of age 2
	 */
	private static FloatBuffer age2BranchVertexBuffer = BufferUtil.newFloatBuffer(nBranchCoordinates);
	
	/**
	 * Buffer storing geometry information of tree of age 3
	 */
	private static FloatBuffer age3BranchVertexBuffer = BufferUtil.newFloatBuffer(nBranchCoordinates);
	
	/**
	 * Buffer storing geometry information of tree of age 4
	 */
	private static FloatBuffer age4BranchVertexBuffer = BufferUtil.newFloatBuffer(nBranchCoordinates);
	
	/**
	 * Buffer storing geometry information of tree of age 5
	 */
	private static FloatBuffer age5BranchVertexBuffer = BufferUtil.newFloatBuffer(nBranchCoordinates);
	
	/**
	 * Buffer storing geometry information of tree of age 6
	 */
	private static FloatBuffer age6BranchVertexBuffer = BufferUtil.newFloatBuffer(nBranchCoordinates);
	
	/**
	 * Buffer storing geometry information of tree of age 7
	 */
	private static FloatBuffer age7BranchVertexBuffer = BufferUtil.newFloatBuffer(nBranchCoordinates);
	
	/**
	 * Buffer storing geometry information of tree of age 8
	 */
	private static FloatBuffer age8BranchVertexBuffer = BufferUtil.newFloatBuffer(nBranchCoordinates);
	
	/**
	 * No. of indices of vertices forming the branch triangles
	 */
	private static int branchIndices = 96;
	
	/**
	 * Buffer storing the vertex indices
	 */
	private static ShortBuffer branchIndexBuffer = BufferUtil.newShortBuffer(branchIndices); //40 branches, 24 indices per branch = 960 indices
	
	/**
	 * Array storing the vertex indices
	 */
	private static short[] branchIndexArray = {
		0, 4, 3, 3, 4, 7, 3, 7, 2, 2, 7, 6, 2, 6, 1, 1, 6, 5, 1, 5, 0, 0, 5, 4,						//trunk
		4,  8, 7, 7,  8, 11, 7, 11, 6, 6, 11, 10, 6, 10, 5, 5, 10,  9, 5,  9, 4, 4,  9,  8, 		//branch 1 level 1 
		5, 13, 12, 5, 6, 13, 6, 14, 13, 6, 7, 14, 7, 15, 14, 7, 4, 15, 4, 5, 12, 4, 12, 15, 		//branch 2 level 1
		4, 16, 7, 7, 16, 19, 7, 19, 6, 6, 19, 18, 6, 18, 5, 5, 18, 17, 5, 17, 4, 4, 17, 16			//branch 3 level 1
	};
	
	/**
	 * Buffer storing the vertex normals of the branching triangulation
	 */
	private static FloatBuffer branchNormalBuffer = BufferUtil.newFloatBuffer(nBranchCoordinates);
	
	//Leaves
	/**
	 * No. of vertices forming the foliage of the tree
	 */
	private static int leafVertices = 21;
	
	/**
	 * No. of coordinates forming the foliage of the tree
	 */
	static int nLeafCoordinates = leafVertices * 3;
	
	/**
	 * Buffer storing geometry information of foliage of tree of age 1
	 */
	private static FloatBuffer age1LeafVertexBuffer = BufferUtil.newFloatBuffer(nLeafCoordinates);
	
	/**
	 * Buffer storing geometry information of foliage of tree of age 2
	 */
	private static FloatBuffer age2LeafVertexBuffer = BufferUtil.newFloatBuffer(nLeafCoordinates);
	
	/**
	 * Buffer storing geometry information of foliage of tree of age 3
	 */
	private static FloatBuffer age3LeafVertexBuffer = BufferUtil.newFloatBuffer(nLeafCoordinates);
	
	/**
	 * Buffer storing geometry information of foliage of tree of age 4
	 */
	private static FloatBuffer age4LeafVertexBuffer = BufferUtil.newFloatBuffer(nLeafCoordinates);
	
	/**
	 * Buffer storing geometry information of foliage of tree of age 5
	 */
	private static FloatBuffer age5LeafVertexBuffer = BufferUtil.newFloatBuffer(nLeafCoordinates);
	
	/**
	 * Buffer storing geometry information of foliage of tree of age 6
	 */
	private static FloatBuffer age6LeafVertexBuffer = BufferUtil.newFloatBuffer(nLeafCoordinates);
	
	/**
	 * Buffer storing geometry information of foliage of tree of age 7
	 */
	private static FloatBuffer age7LeafVertexBuffer = BufferUtil.newFloatBuffer(nLeafCoordinates);
	
	/**
	 * Buffer storing geometry information of foliage of tree of age 8
	 */
	private static FloatBuffer age8LeafVertexBuffer = BufferUtil.newFloatBuffer(nLeafCoordinates);
	
	/**
	 * No. of indices of the vertices forming the foliage triangles
	 */
	private static int leafIndices = 54;
	
	/**
	 * Buffer storing the vertex indices
	 */
	private static ByteBuffer leafIndexBuffer = BufferUtil.newByteBuffer(leafIndices);
	
	/**
	 * Array holding the vertex indices
	 */
	private static byte[] leafIndexArray = {0, 1, 2, 0, 2, 3, 0, 3, 4, 0, 4, 5, 0, 5, 6, 0, 6, 1,
											7, 8, 9, 7, 9, 10, 7, 10, 11, 7, 11, 12, 7, 12, 13, 7, 13, 8,
											14, 15, 16, 14, 16, 17, 14, 17, 18, 14, 18, 19, 14, 19, 20, 14, 20, 15};
	
	/**
	 * Buffer storing the vertex normals of the foliage triangles
	 */
	private static FloatBuffer leafNormalBuffer = BufferUtil.newFloatBuffer(leafVertices * 3);
		
	//Fruits
	/**
	 * No. of vertices forming the fruits of the tree
	 */
	private static int fruitVertices = 12 * 3 * 9;
	
	/**
	 * Buffer storing the vertices of the fruits 
	 */
	private static FloatBuffer fruitVertexBuffer = BufferUtil.newFloatBuffer(fruitVertices * 3);
	
	/**
	 * No. of indices of vertices forming the fruit triangulation 
	 */
	private static int fruitIndices = 20 * 3 * 9;//54;
	
	/**
	 * Buffer storing the vertex indices of the fruit triangulation
	 */
	private static IntBuffer fruitIndexBuffer = BufferUtil.newIntBuffer(fruitIndices);
	
	/**
	 * Buffer holding the vertex normals of the fruit triangulation
	 */
	private static FloatBuffer fruitNormalBuffer = BufferUtil.newFloatBuffer(fruitVertices * 3);

	/**
	 * Initializes the buffers by adding index and vertex information of branches, leaves and fruits 
	 */
	public static void init() {
		//branches
		branchIndexBuffer.put(branchIndexArray);
		branchIndexBuffer.flip();
				
		//leaves
		leafIndexBuffer.put(leafIndexArray);
		leafIndexBuffer.flip();
		
		//fruits
//		fruitIndexBuffer.put(fruitIndexArray);
//		fruitIndexBuffer.flip();
		
		createTreeGeometry();		
	}
	
	/**
	 * Renders fruits using vertex arrays
	 * @param gl OpenGL interface
	 */
	public static void displayFruits(GL gl) {
		gl.glVertexPointer(3, GL.GL_FLOAT, 0, fruitVertexBuffer);
		gl.glNormalPointer(GL.GL_FLOAT, 0, fruitNormalBuffer);
		gl.glDrawElements(GL.GL_TRIANGLES, fruitIndices, GL.GL_UNSIGNED_INT, fruitIndexBuffer);
	}
	
	/**
	 * Renders tree foliage according to its age
	 * @param gl OpenGL interface 
	 * @param age age of the tree
	 */
	public static void displayLeaves(GL gl, int age) {
		
		switch(age)
		{
		case 1:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age1LeafVertexBuffer);	    	
			break;
			
		case 2:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age2LeafVertexBuffer);	    	
			break;
			
		case 3:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age3LeafVertexBuffer);			
			break;
			
		case 4:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age4LeafVertexBuffer);	    	
			break;			
			
		case 5:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age5LeafVertexBuffer);	
			break;			
			
		case 6:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age6LeafVertexBuffer);	    	
			break;
			
		case 7:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age7LeafVertexBuffer);	    	
			break;
			
		case 8:
		case 9:
		case 10:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age8LeafVertexBuffer);
			break;
		}
		gl.glNormalPointer(GL.GL_FLOAT, 0, leafNormalBuffer);
		gl.glDrawElements(GL.GL_TRIANGLES, leafIndices, GL.GL_UNSIGNED_BYTE, leafIndexBuffer);
	}
	
	/**
	 * Renders tree branches according to its age
	 * @param gl OpenGL interface
	 * @param age tree age
	 */
	public static void displayBranches(GL gl, int age) {
		
		switch(age)
		{
		case 1:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age1BranchVertexBuffer);        
			break;
			
		case 2:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age2BranchVertexBuffer);	    	
			break;
			
		case 3:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age3BranchVertexBuffer);			
			break;
			
		case 4:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age4BranchVertexBuffer);	    	
			break;			
			
		case 5:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age5BranchVertexBuffer);	
			break;			
			
		case 6:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age6BranchVertexBuffer);	    	
			break;
			
		case 7:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age7BranchVertexBuffer);	    	
			break;
			
		case 8:
		case 9:
		case 10:
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, age8BranchVertexBuffer);	    	
			break;
		}
		gl.glNormalPointer(GL.GL_FLOAT, 0, branchNormalBuffer);		
		gl.glDrawElements(GL.GL_TRIANGLES, branchIndices, GL.GL_UNSIGNED_SHORT, branchIndexBuffer);  
	}
	
    /**
     * Initializes buffers with branch, leaf and fruit information of trees of ages 1 to 10.
     */	
	private static void createTreeGeometry() {
		//Creates a tree of age 1
		TreeModel tree = new TreeModel();
		
		//Age 1
		tree.setAge(1);
		tree.populateGeometryInfo(age1BranchVertexBuffer, age1LeafVertexBuffer);
				
		//Age 2
		tree.setAge(2);
		tree.populateGeometryInfo(age2BranchVertexBuffer, age2LeafVertexBuffer);
				
		//Age 3
		tree.setAge(3);
		tree.populateGeometryInfo(age3BranchVertexBuffer, age3LeafVertexBuffer);
				
		//Age 4
		tree.setAge(4);
		tree.populateGeometryInfo(age4BranchVertexBuffer, age4LeafVertexBuffer);
				
		//Age 5
		tree.setAge(5);
		tree.populateGeometryInfo(age5BranchVertexBuffer, age5LeafVertexBuffer);
				
		//Age 6
		tree.setAge(6);
		tree.populateGeometryInfo(age6BranchVertexBuffer, age6LeafVertexBuffer);
				
		//Age 7
		tree.setAge(7);
		tree.populateGeometryInfo(age7BranchVertexBuffer, age7LeafVertexBuffer);
				
		//Age 8
		tree.setAge(8);
		tree.populateGeometryInfo(age8BranchVertexBuffer, age8LeafVertexBuffer);
		
		tree.populateBranchNormalInfo(age8BranchVertexBuffer, branchIndexBuffer, branchNormalBuffer);
		tree.populateLeafNormalInfo(age8LeafVertexBuffer, leafIndexBuffer, leafNormalBuffer);
		tree.populateFruitGeometryInfo(fruitVertexBuffer, fruitIndexBuffer, fruitNormalBuffer);
	}
	
	/**
	 * Returns buffer holding vertices of the branch geometry according to the age of the tree
	 * @param age tree age
	 * @return buffer holding the vertices
	 */
	public static FloatBuffer getBranchVertexBuffer(int age) {
		switch (age) {
			case 1:
				return age1BranchVertexBuffer.asReadOnlyBuffer();
			case 2:
				return age2BranchVertexBuffer.asReadOnlyBuffer();
			case 3:
				return age3BranchVertexBuffer.asReadOnlyBuffer();
			case 4:
				return age4BranchVertexBuffer.asReadOnlyBuffer();
			case 5:
				return age5BranchVertexBuffer.asReadOnlyBuffer();
			case 6:
				return age6BranchVertexBuffer.asReadOnlyBuffer();
			case 7:
				return age7BranchVertexBuffer.asReadOnlyBuffer();
			case 8:
			case 9:
			case 10:
				return age8BranchVertexBuffer.asReadOnlyBuffer();
				
		}
		
		return null;
	}
	
	/**
	 * Returns buffer holding vertices of the foliage geometry according to the age of the tree
	 * @param age tree age
	 * @return buffer holding the vertices
	 */
	public static FloatBuffer getLeafVertexBuffer(int age) {
		switch (age) {
			case 1:
				return age1LeafVertexBuffer.asReadOnlyBuffer();
			case 2:
				return age2LeafVertexBuffer.asReadOnlyBuffer();
			case 3:
				return age3LeafVertexBuffer.asReadOnlyBuffer();
			case 4:
				return age4LeafVertexBuffer.asReadOnlyBuffer();
			case 5:
				return age5LeafVertexBuffer.asReadOnlyBuffer();
			case 6:
				return age6LeafVertexBuffer.asReadOnlyBuffer();
			case 7:
				return age7LeafVertexBuffer.asReadOnlyBuffer();
			case 8:
			case 9:
			case 10:
				return age8LeafVertexBuffer.asReadOnlyBuffer();
				
		}
		
		return null;
	}
	
	/**
	 * Returns buffer holding the vertex index information forming the tree foliage triangulation
	 * @return buffer holding the indices
	 */
	public static ByteBuffer getLeafIndexBuffer() {
		return leafIndexBuffer.asReadOnlyBuffer();
	}
	
	
}
