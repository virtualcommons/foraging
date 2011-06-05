package edu.asu.commons.foraging.visualization.forestry.shader;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.BufferUtil;


public class TreeView {	
	
	private Tree tree;
    private int[] branchVBOIds = new int[7];
    private int nBranchVertices = 0;
    private int[] leafVBOIds = new int[2];
    private int nLeafVertices = 0;
    private int[] fruitVBOIds = new int[7];
    private int nFruitVertices = 0;
    
    public TreeView(Tree tree) {
    	this.tree = tree;
    }
    
    //################ Fruits display methods ##################################
    public void displayFruits(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		
		//Enable arrays
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		
		if (tree.isFreshlyFruited()) {
			buildFruitVBO(gl);	
		}
		else if (tree.needsFruitGeometryUpdate()) {
			updateFruitVBO(gl);
		}

		sendBoundingBoxInfo(gl, 1);
		
		// Disable Vertex Arrays
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
	}

    private void buildFruitVBO(GL gl) {
		//Allocate buffer memory
		FloatBuffer vertexBuffer = BufferUtil.newFloatBuffer(40 * 36 * 3); //40 fruits, 36 vertices per fruit, 3 coordinates per vertex
		FloatBuffer attributeBuffer = BufferUtil.newFloatBuffer(40 * 36 * 4); 
	    FloatBuffer baseCenterBuffer = BufferUtil.newFloatBuffer(40 * 36 * 3); 
	    FloatBuffer transformationRow1Buffer = BufferUtil.newFloatBuffer(40 * 36 * 4);
	    FloatBuffer transformationRow2Buffer = BufferUtil.newFloatBuffer(40 * 36 * 4);
	    FloatBuffer transformationRow3Buffer = BufferUtil.newFloatBuffer(40 * 36 * 4);
	    FloatBuffer transformationRow4Buffer = BufferUtil.newFloatBuffer(40 * 36 * 4);
	    
	    nFruitVertices = tree.createFruitVBO(vertexBuffer, attributeBuffer, baseCenterBuffer, transformationRow1Buffer, transformationRow2Buffer, transformationRow3Buffer, transformationRow4Buffer);
	    
		vertexBuffer.flip();
    	attributeBuffer.flip();
    	baseCenterBuffer.flip();
    	transformationRow1Buffer.flip();
    	transformationRow2Buffer.flip();
    	transformationRow3Buffer.flip();
    	transformationRow4Buffer.flip();
    	
    	//Create VBO Id
		gl.glGenBuffersARB(7, fruitVBOIds, 0);  //Get Valid Names
        
		//Load buffer into graphics card memory
		//Vertex coordinates
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, fruitVBOIds[0]);  // Bind The Buffer
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nFruitVertices * 3 * BufferUtil.SIZEOF_FLOAT, vertexBuffer, GL.GL_DYNAMIC_DRAW_ARB);
                
        //Vertex attributes
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, fruitVBOIds[1]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nFruitVertices * 4 * BufferUtil.SIZEOF_FLOAT, attributeBuffer, GL.GL_DYNAMIC_DRAW_ARB);
        
        //branch base centers
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, fruitVBOIds[2]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nFruitVertices * 3 * BufferUtil.SIZEOF_FLOAT, baseCenterBuffer, GL.GL_DYNAMIC_DRAW_ARB);
        
        //branch transformations
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, fruitVBOIds[3]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nFruitVertices * 4 * BufferUtil.SIZEOF_FLOAT, transformationRow1Buffer, GL.GL_STATIC_DRAW_ARB);
        
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, fruitVBOIds[4]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nFruitVertices * 4 * BufferUtil.SIZEOF_FLOAT, transformationRow2Buffer, GL.GL_STATIC_DRAW_ARB);
        
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, fruitVBOIds[5]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nFruitVertices * 4 * BufferUtil.SIZEOF_FLOAT, transformationRow3Buffer, GL.GL_STATIC_DRAW_ARB);
        
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, fruitVBOIds[6]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nFruitVertices * 4 * BufferUtil.SIZEOF_FLOAT, transformationRow4Buffer, GL.GL_DYNAMIC_DRAW_ARB);
                
        // Our Copy Of The Data Is No Longer Necessary, It Is Safe In The Graphics Card
        vertexBuffer = null;
        attributeBuffer = null;
        baseCenterBuffer = null;
        transformationRow1Buffer = null;
        transformationRow2Buffer = null;
        transformationRow3Buffer = null;
        transformationRow4Buffer = null;
	}

    private void updateFruitVBO(GL gl) {
		//Allocate buffer memory
		FloatBuffer vertexBuffer = BufferUtil.newFloatBuffer(40 * 36 * 3); //40 fruits, 36 vertices per fruit, 3 coordinates per vertex
		FloatBuffer attributesBuffer = BufferUtil.newFloatBuffer(40 * 36 * 4);
		FloatBuffer baseCenterBuffer = BufferUtil.newFloatBuffer(40 * 36 * 3); 
	    FloatBuffer transformationRow4Buffer = BufferUtil.newFloatBuffer(40 * 36 * 4);
	    
	    nFruitVertices = tree.updateFruitVBO(vertexBuffer, attributesBuffer, baseCenterBuffer, transformationRow4Buffer);
	    
		vertexBuffer.flip();
		attributesBuffer.flip();
    	baseCenterBuffer.flip();
    	transformationRow4Buffer.flip();
    	
    	//Re-load buffer into graphics card memory
		//Vertex coordinates
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, fruitVBOIds[0]);  // Bind The Buffer
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nFruitVertices * 3 * BufferUtil.SIZEOF_FLOAT, vertexBuffer, GL.GL_DYNAMIC_DRAW_ARB);
                
        //attributes 
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, fruitVBOIds[1]);  // Bind The Buffer
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nFruitVertices * 4 * BufferUtil.SIZEOF_FLOAT, attributesBuffer, GL.GL_DYNAMIC_DRAW_ARB);
        
        //branch base centers
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, fruitVBOIds[2]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nFruitVertices * 3 * BufferUtil.SIZEOF_FLOAT, baseCenterBuffer, GL.GL_DYNAMIC_DRAW_ARB);
    
        //transformation row4
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, fruitVBOIds[6]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nFruitVertices * 4 * BufferUtil.SIZEOF_FLOAT, transformationRow4Buffer, GL.GL_DYNAMIC_DRAW_ARB);
                
        // Our Copy Of The Data Is No Longer Necessary, It Is Safe In The Graphics Card
        vertexBuffer = null;
        baseCenterBuffer = null;
        transformationRow4Buffer = null;
	}
    
    //################# Leaves display methods #################################
    public void displayLeaves(GLAutoDrawable drawable) {
    	GL gl = drawable.getGL();
    	
    	//Enable arrays
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
    	gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
    	//Build the VBO if the tree is just harvested
    	if (tree.needsGeometryUpdate()) { 
    		buildLeavesVBO(gl);
    	}
    	
    	sendLeavesInfo(gl);
    	
		// Disable Vertex Arrays
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
    }
    
    private void sendLeavesInfo(GL gl) {
		//Vertex coordinates
		gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, leafVBOIds[0]);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0); //specifies the location and data of an array of vertex coordinates to use when rendering    
        
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, leafVBOIds[1]);
        gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0);     
                
        //Render
        // Draw All Of The Triangles At Once
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, nLeafVertices);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER,0);        
	}
	
	private void buildLeavesVBO(GL gl) {
		//Allocate buffer memory
		FloatBuffer leafVertexBuffer = BufferUtil.newFloatBuffer(81 * 6 * 3);
		FloatBuffer leafTextureBuffer = BufferUtil.newFloatBuffer(81 * 6 * 2);
		
		nLeafVertices = tree.createLeavesVBO(leafVertexBuffer, leafTextureBuffer);
		leafVertexBuffer.flip();
		leafTextureBuffer.flip();
		
		//Create VBO Id
		if (branchVBOIds[0] != 0) {
    		gl.glDeleteBuffersARB(2, leafVBOIds, 0);
    	}
		gl.glGenBuffersARB(2, leafVBOIds, 0);  //Get Valid Names
        
		//Load buffer into graphics card memory
		//Vertex coordinates
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, leafVBOIds[0]);  // Bind The Buffer
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nLeafVertices * 3 * BufferUtil.SIZEOF_FLOAT, leafVertexBuffer, GL.GL_STATIC_DRAW_ARB);
        
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, leafVBOIds[1]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nLeafVertices * 2 * BufferUtil.SIZEOF_FLOAT, leafTextureBuffer, GL.GL_STATIC_DRAW_ARB);
        
        leafVertexBuffer = null;
        leafTextureBuffer = null;
	}
	
    //################ Heap of coins display methods ##############################
    public void displayHeapOfCoins(GLAutoDrawable drawable) {
    	GL gl = drawable.getGL();
    	
    	//Enable arrays
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
    	
    	//Build the VBO if the tree is just harvested
    	if (tree.isFreshlyHarvested()) {    		
    		buildHeapVBO(gl);
    	}
    	
    	sendBoundingBoxInfo(gl, 0);
    	
		// Disable Vertex Arrays
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
    }
    
    private void buildHeapVBO(GL gl) {
		//Allocate buffer memory
		FloatBuffer branchVertices = BufferUtil.newFloatBuffer(24 * 3); //1 bounding box of heap, 24 vertices of the box, 3 coordinates per vertex
		FloatBuffer branchAttributes = BufferUtil.newFloatBuffer(24 * 4); 
	    FloatBuffer branchBaseCenters = BufferUtil.newFloatBuffer(24 * 3); 
	    FloatBuffer branchTransformationsRow1 = BufferUtil.newFloatBuffer(24 * 4);
	    FloatBuffer branchTransformationsRow2 = BufferUtil.newFloatBuffer(24 * 4);
	    FloatBuffer branchTransformationsRow3 = BufferUtil.newFloatBuffer(24 * 4);
	    FloatBuffer branchTransformationsRow4 = BufferUtil.newFloatBuffer(24 * 4);
	    
	    //Create bounding box for the heap
		nBranchVertices = tree.createHeapBoundingBox(branchVertices, branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);
		
		branchVertices.flip();
		branchAttributes.flip();
    	branchBaseCenters.flip();
    	branchTransformationsRow1.flip();
    	branchTransformationsRow2.flip();
    	branchTransformationsRow3.flip();
    	branchTransformationsRow4.flip();

    	if (branchVBOIds[0] != 0) {
    		gl.glDeleteBuffersARB(7, branchVBOIds, 0);
    	}
    	//Create VBO Id
		gl.glGenBuffersARB(7, branchVBOIds, 0);  //Get Valid Names
        
		//Load buffer into graphics card memory
		//Vertex coordinates
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, branchVBOIds[0]);  // Bind The Buffer
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nBranchVertices * 3 * BufferUtil.SIZEOF_FLOAT, branchVertices, GL.GL_STATIC_DRAW_ARB);
        
        //Vertex attributes
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, branchVBOIds[1]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nBranchVertices * 4 * BufferUtil.SIZEOF_FLOAT, branchAttributes, GL.GL_STATIC_DRAW_ARB);
        
        //branch base centers
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, branchVBOIds[2]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nBranchVertices * 3 * BufferUtil.SIZEOF_FLOAT, branchBaseCenters, GL.GL_STATIC_DRAW_ARB);
        
        //branch transformations
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, branchVBOIds[3]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nBranchVertices * 4 * BufferUtil.SIZEOF_FLOAT, branchTransformationsRow1, GL.GL_STATIC_DRAW_ARB);
        
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, branchVBOIds[4]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nBranchVertices * 4 * BufferUtil.SIZEOF_FLOAT, branchTransformationsRow2, GL.GL_STATIC_DRAW_ARB);
        
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, branchVBOIds[5]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nBranchVertices * 4 * BufferUtil.SIZEOF_FLOAT, branchTransformationsRow3, GL.GL_STATIC_DRAW_ARB);
        
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, branchVBOIds[6]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nBranchVertices * 4 * BufferUtil.SIZEOF_FLOAT, branchTransformationsRow4, GL.GL_STATIC_DRAW_ARB);
                
        // Our Copy Of The Data Is No Longer Necessary, It Is Safe In The Graphics Card
        branchVertices = null;
        branchAttributes = null;
        branchBaseCenters = null;
        branchTransformationsRow1 = null;
        branchTransformationsRow2 = null;
        branchTransformationsRow3 = null;
        branchTransformationsRow4 = null;
	}

    //############ Branch display methods ########################	
	public void displayBranches(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		
		//Enable arrays
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		
		if (tree.needsGeometryUpdate()) {
			buildBranchVBO(gl);			
		}

		sendBoundingBoxInfo(gl, 0);
		
		// Disable Vertex Arrays
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
	}
				
	private void buildBranchVBO(GL gl) {
		//Allocate buffer memory
		FloatBuffer branchVertices = BufferUtil.newFloatBuffer(40 * 24 * 3); //40 branches, 24 vertices per branch, 3 coordinates per vertex
		FloatBuffer branchAttributes = BufferUtil.newFloatBuffer(40 * 24 * 4); 
	    FloatBuffer branchBaseCenters = BufferUtil.newFloatBuffer(40 * 24 * 3); 
	    FloatBuffer branchTransformationsRow1 = BufferUtil.newFloatBuffer(40 * 24 * 4);
	    FloatBuffer branchTransformationsRow2 = BufferUtil.newFloatBuffer(40 * 24 * 4);
	    FloatBuffer branchTransformationsRow3 = BufferUtil.newFloatBuffer(40 * 24 * 4);
	    FloatBuffer branchTransformationsRow4 = BufferUtil.newFloatBuffer(40 * 24 * 4);
	    
	    if (tree.needsGeometryCreation()) {
	    	//Create the tree - creates child branches and leaves and adds vertices to the buffer
	    	nBranchVertices = tree.create(branchVertices, branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);	    	
	    }
	    else { //hasFreshlyGrown returns true here
	    	//Grow the tree - updates bounding box of each branch and adds vertices to the buffer
	    	//System.out.println("Calling trunk.grow()");
	    	nBranchVertices = tree.grow(branchVertices, branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);
	    }
		branchVertices.flip();
		branchAttributes.flip();
    	branchBaseCenters.flip();
    	branchTransformationsRow1.flip();
    	branchTransformationsRow2.flip();
    	branchTransformationsRow3.flip();
    	branchTransformationsRow4.flip();
    	
    	//Create VBO Id
    	if (branchVBOIds[0] != 0) {
    		gl.glDeleteBuffersARB(7, branchVBOIds, 0);
    	}
//    	System.out.println("branchVBOIds[2] = " + branchVBOIds[2]);
		gl.glGenBuffersARB(7, branchVBOIds, 0);  //Get Valid Names
        
		//Load buffer into graphics card memory
		//Vertex coordinates
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, branchVBOIds[0]);  // Bind The Buffer
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nBranchVertices * 3 * BufferUtil.SIZEOF_FLOAT, branchVertices, GL.GL_STATIC_DRAW_ARB);
                
        //Vertex attributes
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, branchVBOIds[1]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nBranchVertices * 4 * BufferUtil.SIZEOF_FLOAT, branchAttributes, GL.GL_STATIC_DRAW_ARB);
        
        //branch base centers
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, branchVBOIds[2]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nBranchVertices * 3 * BufferUtil.SIZEOF_FLOAT, branchBaseCenters, GL.GL_STATIC_DRAW_ARB);
        
        //branch transformations
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, branchVBOIds[3]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nBranchVertices * 4 * BufferUtil.SIZEOF_FLOAT, branchTransformationsRow1, GL.GL_STATIC_DRAW_ARB);
        
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, branchVBOIds[4]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nBranchVertices * 4 * BufferUtil.SIZEOF_FLOAT, branchTransformationsRow2, GL.GL_STATIC_DRAW_ARB);
        
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, branchVBOIds[5]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nBranchVertices * 4 * BufferUtil.SIZEOF_FLOAT, branchTransformationsRow3, GL.GL_STATIC_DRAW_ARB);
        
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, branchVBOIds[6]);  
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, nBranchVertices * 4 * BufferUtil.SIZEOF_FLOAT, branchTransformationsRow4, GL.GL_STATIC_DRAW_ARB);
                
        // Our Copy Of The Data Is No Longer Necessary, It Is Safe In The Graphics Card
        branchVertices = null;
        branchAttributes = null;
        branchBaseCenters = null;
        branchTransformationsRow1 = null;
        branchTransformationsRow2 = null;
        branchTransformationsRow3 = null;
        branchTransformationsRow4 = null;
	}
	
	public void sendBoundingBoxInfo(GL gl, int part) {
		int[] VBOIds;
		int nVertices;
		
		if (part == 0) { //branch / heap of coins
			VBOIds = branchVBOIds;
			nVertices = nBranchVertices;
		}
		else {
			VBOIds = fruitVBOIds;
			nVertices = nFruitVertices;
		}
		
		//Vertex coordinates
		gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[0]);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0); //specifies the location and data of an array of vertex coordinates to use when rendering    
        
        //Vertex attributes
        int programObject = tree.getProgramObject();
        
        int attribLocation1 = gl.glGetAttribLocation(programObject, "FrustumValues_attrib");
        gl.glEnableVertexAttribArray(attribLocation1);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[1]);
//        (int index, int size, int type, boolean normalized, int stride, Buffer pointer)
        gl.glVertexAttribPointer(attribLocation1, 4, GL.GL_FLOAT, false, 0, 0); 
        
        //branch base centers
        int attribLocation2 = gl.glGetAttribLocation(programObject, "FrustumBaseCenter_attrib");
        gl.glEnableVertexAttribArray(attribLocation2);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[2]);
        gl.glVertexAttribPointer(attribLocation2, 3, GL.GL_FLOAT, false, 0, 0);
        
        //branch transformations
        int attribLocation3 = gl.glGetAttribLocation(programObject, "Ws2OsXformRow1_attrib");
        gl.glEnableVertexAttribArray(attribLocation3);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[3]);
        gl.glVertexAttribPointer(attribLocation3, 4, GL.GL_FLOAT, false, 0, 0);
        
        int attribLocation4 = gl.glGetAttribLocation(programObject, "Ws2OsXformRow2_attrib");
        gl.glEnableVertexAttribArray(attribLocation4);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[4]);
        gl.glVertexAttribPointer(attribLocation4, 4, GL.GL_FLOAT, false, 0, 0);
        
        int attribLocation5 = gl.glGetAttribLocation(programObject, "Ws2OsXformRow3_attrib");
        gl.glEnableVertexAttribArray(attribLocation5);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[5]);
        gl.glVertexAttribPointer(attribLocation5, 4, GL.GL_FLOAT, false, 0, 0);
        
        int attribLocation6 = gl.glGetAttribLocation(programObject, "Ws2OsXformRow4_attrib");
        gl.glEnableVertexAttribArray(attribLocation6);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOIds[6]);
        gl.glVertexAttribPointer(attribLocation6, 4, GL.GL_FLOAT, false, 0, 0);
                
        //Render
        // Draw All Of The Triangles At Once
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, nVertices);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER,0);
        
        gl.glDisableVertexAttribArray(attribLocation1);
        gl.glDisableVertexAttribArray(attribLocation2);
        gl.glDisableVertexAttribArray(attribLocation3);
        gl.glDisableVertexAttribArray(attribLocation4);
        gl.glDisableVertexAttribArray(attribLocation5);
        gl.glDisableVertexAttribArray(attribLocation6);
	}
}
