package edu.asu.commons.foraging.visualization.forestry.va;

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
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.foraging.visualization.GameView3d;

/**
 * The TestView class is used to test the trees created in this package.
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version $Revision: 4 $
 *
 */
public class TestView extends GameView3d {

	/**
	 * A vector of trees 
	 */
	private Vector<Tree> trees = new Vector<Tree>();
	
	/**
	 * A vector of positions of the trees 
	 */
	private Vector<Point> treePositions = new Vector<Point>();
	
	/**
	 * No. of trees to instantiate
	 */
	private int nTrees = 1;
	
	/**
	 * Initializes this test view by setting the camera position and lookat point
	 * @param drawable current rendering context
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		camera.setPoints(new Point3D(0, 12, 50), new Point3D(0, 12, 0));
	}
		
	/**
	 * Initializes this test view by initializing the tree geometry and the first nTrees trees representing the resources in the common pool.  
	 */
	@Override
	public void initialize() {		
		Tree.init(client.getDataModel().getRoundConfiguration().getMaximumResourceAge());
		TreeView.init();
		int treeCount = 0;
		for (Resource resource : getDataModel().getResourceDistribution().values()) {
			//Create a tree
			Tree tree = new Tree(resource, new Point3D(treeCount*10, 0, 0), this);			
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

	/**
	 * Renders the test view by rendering the branches, leaves and fruits of the trees
	 * @param drawable current rendering context
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		super.display(drawable);
		
		GL gl = drawable.getGL();
		
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
		
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, Tree.branchAmbient, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, Tree.branchDiffuse, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, Tree.branchSpecular, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, Tree.branchShininess);
		
		////////////// BRANCHES //////////////////////
		for (int index = 0; index < trees.size(); index++)
			trees.get(index).displayBranches(drawable);
		
		////////////// FRUITS //////////////////////
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, Tree.fruitAmbient, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, Tree.fruitDiffuse, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, Tree.fruitSpecular, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, Tree.branchShininess);
		for (int index = 0; index < trees.size(); index++)
			trees.get(index).displayFruits(drawable);
			
		///////////// LEAVES ///////////////////////
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, Tree.leafAmbient, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, Tree.leafDiffuse, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, Tree.leafSpecular, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, Tree.branchShininess);
		
		for (int index = 0; index < trees.size(); index++)
			trees.get(index).displayLeaves(drawable);
		
		gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
	}

	/**
	 * Updates the tree ages according to the resource ages in the common pool resource  
	 */
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
					update();
				}
				treeCount++;
			}			
			if (treeCount == nTrees)
				break;
        }
        getDataModel().clearDiffLists();

	}

	/**
	 * Handles the key pressed event. The mapping of keys and actions is as follows
	 * <table>
	 * <tr><th>Key</th><th>Action</th></tr>
	 * <tr><td>Q</td><td>Select a tree</td></tr>
	 * <tr><td>E</td><td>Hit the selected tree</td></tr>
	 * <tr><td>X</td><td>Harvest fruits of the selected tree</td></tr>
	 * </table>
	 * @param e key event
	 */
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);		
		int keyCode = e.getKeyCode();		
		switch(keyCode) {
		case KeyEvent.VK_Q:
			requestTreeSelection();
			break;
		case KeyEvent.VK_E:			
			hitTree();			
			break;
		case KeyEvent.VK_X:
			harvestFruits(trees.get(0));
			break;
		}
		update();
	}
	
	/**
	 * Sends a request of selecting a tree to the server.
	 */
	private void requestTreeSelection() {			        	
		Tree tree = trees.get(0);
    	client.transmit(new LockResourceRequest(client.getId(), tree.getResource()));
    	System.out.println("Sent a LockResourceRequest to lock the tree " + tree);
    }
	
	/**
	 * Hits the selected tree with an axe. If no. of hits are equal to the tree age, then the tree is harvested
	 */
	public void hitTree() {
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
	
	/**
	 * Harvests fruits of the selected tree.
	 * @param tree
	 */
	public void harvestFruits(Tree tree) {
    	//Stop the fruit fall timer if not stopped already
        if (fruitTimer != null) {
            fruitTimer.stop();
        }
        
        long duration = 10000L;
        int interval = 200;        
        fruitTimer = createFruitFallTimer(tree, duration, interval);
        fruitTimer.start();
    }
    
	/**
     * Timer used to create fruit falling animation
     */
    private FruitTimer fruitTimer;
    
    /**
     * The FruitTimer class is used to create fruit falling animation depicting fruit harvest 
     * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
     *
     */
    private class FruitTimer extends Timer {
    	
    	/**
    	 * The tree whose fruits are being harvested
    	 */
        private final Tree tree;
        
        /**
         * Constructs a new fruit timer for the specified tree with the specified delay and listener
         * @param tree tree whose fruits are being harvested
         * @param delay time interval between timer handler calls
         * @param listener listener listening to the timer calls and creating fruit falling animation
         */
        FruitTimer(Tree tree, int delay, ActionListener listener) {
            super(delay, listener);
            this.tree = tree;
        }
        
        /**
         * Stops the timer and sends the fruits harvested request to the server.
         */
        public void stop() {
            super.stop();    
            
            //Send the fruit harvest event to the server which will decrease the resource age.
            client.transmit(new HarvestFruitRequest(client.getId(), tree.getResource()));
            
            //Remove fruits as they are harvested
            tree.resetFruitHeight();
            
            //Deselect the tree
            tree.setSelected(false);
        }
    }
    
    /**
     * Creates the fruit fall timer. 
     * @param tree tree whose fruits are being harvested
     * @param duration time taken to reach the fruits to the ground
     * @param interval time interval between timer handler calls
     * @return fruit falling timer object
     */
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
                    tree.updateFruitValues();
                }
            }
        });
    }
}
