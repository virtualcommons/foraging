package edu.asu.commons.foraging.visualization.forestry.shader;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.Timer;

import edu.asu.commons.foraging.event.HarvestFruitRequest;
import edu.asu.commons.foraging.event.HarvestResourceRequest;
import edu.asu.commons.foraging.event.LockResourceRequest;
import edu.asu.commons.foraging.fileplugin.FileLoader;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.foraging.visualization.GameView3d;


public class TestView extends GameView3d {

	private Vector<Tree> trees = new Vector<Tree>();
	private Vector<Point> treePositions = new Vector<Point>();
	private int nTrees = 1;
	
	//Shader related variables
	private int conicalFrustumVertexShader;
	private int conicalFrustumFragmentShader;
	private int conicalFrustumProgramObject;
	
	private int sphereVertexShader;
	private int sphereFragmentShader;
	private int sphereProgramObject;
	
	private int foliageVertexShader;
	private int foliageFragmentShader;
	private int foliageProgramObject;
	
	private void setupShaders(GL gl) {
    	String[] fileContents = new String[1];
    	int[] fileLengths = new int[1];

    	////////////////// Conical frustum shader ////////////////////
    	conicalFrustumProgramObject = gl.glCreateProgramObjectARB();    	
    	conicalFrustumVertexShader = gl.glCreateShaderObjectARB(GL.GL_VERTEX_SHADER_ARB);
    	conicalFrustumFragmentShader = gl.glCreateShaderObjectARB(GL.GL_FRAGMENT_SHADER_ARB);	
    	
    	//Vertex shader
    	fileContents[0] = FileLoader.getString("shaders/conicalFrustum.vert");    	
    	fileLengths[0] = fileContents[0].length();
    	gl.glShaderSourceARB(conicalFrustumVertexShader, 1, fileContents, fileLengths, 0);
    	
    	//fragment shader
    	fileContents[0] = FileLoader.getString("shaders/conicalFrustum.frag");
    	fileLengths[0] = fileContents[0].length();    	
    	gl.glShaderSourceARB(conicalFrustumFragmentShader, 1, fileContents, fileLengths, 0);
    	
    	//Compile, attach and link
    	gl.glCompileShaderARB(conicalFrustumVertexShader);
    	gl.glCompileShaderARB(conicalFrustumFragmentShader);
    	gl.glAttachObjectARB(conicalFrustumProgramObject, conicalFrustumVertexShader); 
    	gl.glAttachObjectARB(conicalFrustumProgramObject, conicalFrustumFragmentShader); 
    	gl.glLinkProgramARB(conicalFrustumProgramObject);
    	
    	///////////////////// sphere shader /////////////////////////
    	sphereProgramObject = gl.glCreateProgramObjectARB();    	
    	sphereVertexShader = gl.glCreateShaderObjectARB(GL.GL_VERTEX_SHADER_ARB);
    	sphereFragmentShader = gl.glCreateShaderObjectARB(GL.GL_FRAGMENT_SHADER_ARB);	
    	
    	//Vertex shader
    	fileContents[0] = FileLoader.getString("shaders/sphere.vert");    	
    	fileLengths[0] = fileContents[0].length();
    	gl.glShaderSourceARB(sphereVertexShader, 1, fileContents, fileLengths, 0);
    	
    	//fragment shader
    	fileContents[0] = FileLoader.getString("shaders/sphere.frag");
    	fileLengths[0] = fileContents[0].length();    	
    	gl.glShaderSourceARB(sphereFragmentShader, 1, fileContents, fileLengths, 0);
    	
    	//Compile, attach and link
    	gl.glCompileShaderARB(sphereVertexShader);
    	gl.glCompileShaderARB(sphereFragmentShader);
    	gl.glAttachObjectARB(sphereProgramObject, sphereVertexShader); 
    	gl.glAttachObjectARB(sphereProgramObject, sphereFragmentShader); 
    	gl.glLinkProgramARB(sphereProgramObject);
    	
    	///////////////////// foliage shader /////////////////////////
    	foliageProgramObject = gl.glCreateProgramObjectARB();    	
    	foliageVertexShader = gl.glCreateShaderObjectARB(GL.GL_VERTEX_SHADER_ARB);
    	foliageFragmentShader = gl.glCreateShaderObjectARB(GL.GL_FRAGMENT_SHADER_ARB);	
    	
    	//Vertex shader
    	fileContents[0] = FileLoader.getString("shaders/foliage.vert");    	
    	fileLengths[0] = fileContents[0].length();
    	gl.glShaderSourceARB(foliageVertexShader, 1, fileContents, fileLengths, 0);
    	
    	//fragment shader
    	fileContents[0] = FileLoader.getString("shaders/foliage.frag");
    	fileLengths[0] = fileContents[0].length();    	
    	gl.glShaderSourceARB(foliageFragmentShader, 1, fileContents, fileLengths, 0);
    	
    	//Compile, attach and link
    	gl.glCompileShaderARB(foliageVertexShader);
    	gl.glCompileShaderARB(foliageFragmentShader);
    	gl.glAttachObjectARB(foliageProgramObject, foliageVertexShader); 
    	gl.glAttachObjectARB(foliageProgramObject, foliageFragmentShader); 
    	gl.glLinkProgramARB(foliageProgramObject);
    }

	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		camera.setPoints(new Point3D(0, 12, 50), new Point3D(0, 12, 0));
		
		Tree.init(client.getDataModel().getRoundConfiguration().getMaximumResourceAge(), drawable);
		
//		GL gl = drawable.getGL();
//		setupShaders(gl);
//		gl.glBindAttribLocation(conicalFrustumProgramObject, 0, "FrustumValues_attrib");
//		gl.glBindAttribLocation(conicalFrustumProgramObject, 1, "FrustumBaseCenter_attrib");
//		gl.glBindAttribLocation(conicalFrustumProgramObject, 2, "Ws2OsXformRow1_attrib");
//		gl.glBindAttribLocation(conicalFrustumProgramObject, 3, "Ws2OsXformRow2_attrib");
//		gl.glBindAttribLocation(conicalFrustumProgramObject, 4, "Ws2OsXformRow3_attrib");
//		gl.glBindAttribLocation(conicalFrustumProgramObject, 5, "Ws2OsXformRow4_attrib");
	}
		
	@Override
	public void initialize() {		
		int treeCount = 0;
		
		for (Resource resource : getDataModel().getResourceDistribution().values()) {
			//Create a tree
			Tree tree = new Tree(resource, new Point3D(treeCount*10, 0, 0), 0, this);			
			trees.add(tree);
			treePositions.add(resource.getPosition());
			treeCount++;
			if (treeCount == nTrees)
				break;
		}		
		//Added this line as each tree was getting added twice as it is present in 
		//the resourceDistribution as well as addedResources list
		getDataModel().clearDiffLists(); 
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		super.display(drawable);
		
		GL gl = drawable.getGL();
		
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glCullFace(GL.GL_BACK);
		
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		
//		gl.glUseProgramObjectARB(conicalFrustumProgramObject);
				
//		//Camera position
//		Point3D position = camera.getPosition();
//		int uniformLoc = gl.glGetUniformLocationARB(conicalFrustumProgramObject, "CameraPosition_ws");
//		gl.glUniform3fARB(uniformLoc, position.x, position.y, position.z);
//		
//		//light position
//		position = lights.get(0).getPosition();
//		uniformLoc = gl.glGetUniformLocationARB(conicalFrustumProgramObject, "LightPosition_ws");
//		gl.glUniform3fARB(uniformLoc, position.x, position.y, position.z);
			
		gl.glEnable(GL.GL_TEXTURE_2D);
		
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, Tree.ambient, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, Tree.diffuse, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, Tree.specular, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, Tree.shininess);
		
		////////////// BRANCHES //////////////////////
		for (int index = 0; index < trees.size(); index++)
			trees.get(index).displayBranches(drawable);
		
		////////////// FRUITS //////////////////////
//		gl.glUseProgramObjectARB(sphereProgramObject);
		
//		position = camera.getPosition();
//		uniformLoc = gl.glGetUniformLocationARB(conicalFrustumProgramObject, "CameraPosition_ws");
//		gl.glUniform3fARB(uniformLoc, position.x, position.y, position.z);
//		
//		//light position
//		position = lights.get(0).getPosition();
//		uniformLoc = gl.glGetUniformLocationARB(conicalFrustumProgramObject, "LightPosition_ws");
//		gl.glUniform3fARB(uniformLoc, position.x, position.y, position.z);

		
		for (int index = 0; index < trees.size(); index++)
			trees.get(index).displayFruits(drawable);

		gl.glDisable(GL.GL_CULL_FACE);
		gl.glUseProgramObjectARB(0);

		///////////// LEAVES ///////////////////////
//		gl.glUseProgramObjectARB(foliageProgramObject);
//		
//		uniformLoc = gl.glGetUniformLocationARB(conicalFrustumProgramObject, "TextureApplied");
//		gl.glUniform1iARB(uniformLoc, 1);
		
		Tree.leafTexture.load(drawable.getGL());
		
		for (int index = 0; index < trees.size(); index++) {
			trees.get(index).displayLeaves(drawable);
		}
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glDisable(GL.GL_BLEND);
//		gl.glUseProgramObjectARB(0);
	}

	@Override
	public void updateResources() {
		int treeCount = 0;
		for (Resource resource : getDataModel().getResourceDistribution().values()) {
			Point treePosition = treePositions.get(treeCount);
			if (resource.getPosition().x == treePosition.x && resource.getPosition().y == treePosition.y) {
				Tree tree = trees.get(treeCount);				
				if (!tree.isHarvested() && tree.getAge() != resource.getAge()) { 	
					System.out.println("Tree age is " + resource.getAge());
					tree.setAge(resource.getAge());
				}
				treeCount++;
			}			
			if (treeCount == nTrees)
				break;
        }
        getDataModel().clearDiffLists();

	}

	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);		
		int keyCode = e.getKeyCode();		
		switch(keyCode) {
		case KeyEvent.VK_Q:
			requestTreeSelection();
			break;
		case KeyEvent.VK_E:			
			harvestTree();			
			break;
		case KeyEvent.VK_X:
			harvestFruits(trees.get(0));
			break;
		}
		update();
	}
	
	private void requestTreeSelection() {			        	
		Tree tree = trees.get(0);
    	client.transmit(new LockResourceRequest(client.getId(), tree.getResource()));
    	System.out.println("Sent a LockResourceRequest to lock the tree " + tree);
    }
	
	public void harvestTree() {
		Tree tree = trees.get(0);
		if (!tree.isHarvested()) {
			boolean harvested = tree.hit();
			if (harvested) {				
				client.transmit(new HarvestResourceRequest(client.getId(), tree.getResource()));
				System.out.println("Sent the HarvestResourceRequest");
			}			
		}
		return;
	}
	
	public void harvestFruits(Tree tree) {
    	//Stop the fruit fall timer if not stopped already
        if (fruitTimer != null) {
            fruitTimer.stop();
        }
        
        long duration = 10000L;
        int interval = 200;
        int steps = 50;//(int)duration / interval;

        //Find the terrain height below each fruit
        //and use to do decide ht translation step
        for (int fruitIndex = 0; fruitIndex < tree.noOfFruits(); fruitIndex++) {
        	//Get elevation of terrain below this fruit
        	float terrainElevation = 0.0f;
        	//calculate translation in the fruit elevation accordingly 
        	tree.calculateFruitElevationStep(fruitIndex, terrainElevation, steps);
        }
        fruitTimer = createFruitFallTimer(tree, duration, interval);
        fruitTimer.start();
    }
    
    private FruitTimer fruitTimer;

    @SuppressWarnings("serial")
    private class FruitTimer extends Timer {
        private final Tree tree;
        
        FruitTimer(Tree tree, int delay, ActionListener listener) {
            super(delay, listener);
            this.tree = tree;
        }
        public void stop() {
            super.stop();    
            
            //Send the fruit harvest event to the server which will decrease the resource age.
            client.transmit(new HarvestFruitRequest(client.getId(), tree.getResource()));
            
            //Remove fruits as they are harvested
            tree.removeFruits();
            
            //Deselect the tree
            tree.setSelected(false);
        }
    }
    
    private FruitTimer createFruitFallTimer(final Tree tree, long duration, int interval) {
        final long endTime = System.currentTimeMillis() + duration;
        return new FruitTimer(tree, interval, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	long timeElapsed = endTime - System.currentTimeMillis();
                if (timeElapsed < 0) {
                    fruitTimer.stop();
                    fruitTimer = null;
                }
                else {
                    tree.updateFruitValues(timeElapsed);
                }
            }
        });
    }

	public int getProgramObject() {
		return conicalFrustumProgramObject;
	}	
}
