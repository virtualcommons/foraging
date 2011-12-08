package edu.asu.commons.foraging.visualization.forestry.va;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.sun.opengl.util.BufferUtil;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.ClientPoseUpdate;
import edu.asu.commons.foraging.event.HarvestFruitRequest;
import edu.asu.commons.foraging.event.HarvestResourceRequest;
import edu.asu.commons.foraging.event.LockResourceRequest;
import edu.asu.commons.foraging.event.UnlockResourceRequest;
import edu.asu.commons.foraging.graphics.Grid;
import edu.asu.commons.foraging.graphics.HUD;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.PointLight;
import edu.asu.commons.foraging.graphics.RGBA;
import edu.asu.commons.foraging.graphics.Ray;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.foraging.util.Tuple2i;
import edu.asu.commons.foraging.visualization.GameView3d;
import edu.asu.commons.net.Identifier;

/**
 * The ForestryView class creates visualization of the forestry experiment. 
 * It represents the common pool resource as a forest and individual resources in it as growing trees. The trees are 
 * arranged in a two dimensional grid with a tree in the center of each grid cell. 
 * ForestryView class represents participants as avatars who can navigate in the forest, select a tree and harvest it or it's fruits.
 * 
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version $Revision $
 */
public class ForestryView extends GameView3d {
	
	private static final long serialVersionUID = -7254423448030862245L;
	
	/**
	 * Forest ground 
	 */
	protected Ground ground = null;
	
	//Tree related member variables
	/**
	 * A grid used to arrange forest trees.
	 */
	protected Grid treeGrid = null;
	
	/**
	 * Dimensions of the tree grid in the form (rows, columns)
	 */
	private Tuple2i treeGridDimension;
	
	/**
	 * Width of a tree cell 
	 */
	private float treeCellWidth;
	
	/**
	 * Depth of a tree cell 
	 */
	private float treeCellDepth;
	
	/**
	 * Coordinates of the center of the top left cell 
	 */
	private Point3D topLeftCellCenter;
	
	/**
	 * Pointer to the selected tree
	 */
	private Tree selectedTree;
			
	/**
	 * Flag specifying if the client is waiting to receive a response on the select request from the server  
	 */
	private boolean waitingForSelectRequest = false; 
	
	//Avatar related member variables
	/**
	 * Map of avatar ids and avatar obejcts
	 */
	protected Map<Identifier, Woodcutter> avatars = new HashMap<Identifier, Woodcutter>();
	
	/**
	 * Distance between the avatar and third person camera of the avatar
	 */
	protected static float AVATAR_CAMERA_DISTANCE = 20;
	
	/**
	 * Height difference of the third person camera and the avatar
	 */
	protected static float AVATAR_CAMERA_HEIGHT = 5;
	
	/**
	 * Vector between camera avatar position and camera position
	 */
	protected Vector3D cameraAvatarVector = new Vector3D(camera.getLookAtPoint(), camera.getPosition());
	
	/**
	 * Field of view of the avatar camera
	 */
	public static double FOV = Math.cos((Math.PI / 180.0f) * 100.0f);
	
	/**
	 * Head-up display used to display messages
	 */
	private HUD hud = new HUD();
	
	/**
	 * Message displaying information about keys for navigation 
	 */
	private static final String NAV_INFO_MSG = "You can move by holding down the 'W', 'A', 'S', 'D' keys or the arrow keys.";
	
	/**
	 * Message displaying that the avatar is going out of the forest boundary  
	 */
	private static final String OUT_OF_TERRAIN_MSG = "You cannot go outside the forest boundary.";
	
	/**
	 * Message displaying information about keys for tree selection
	 */
	private static final String TREE_SELECT_MSG = "You can select the tree by pressing 'Q' "; //You are going to collide with the tree. \n
	
	/**
	 * Message displaying that the chat has started
	 */
	private static final String CHAT_START_MSG = "Started chat with the selected avatar";
	
	/**
	 * Message displaying that the selected avatar is out of chatting range
	 */
	private static final String OUT_OF_CHAT_RANGE = "The selected avatar is out of the chatting range";
	
	/**
	 * Message displaying how many time the tree is hit while axing
	 */
	private static final String TREE_HIT_MSG = "You hit the tree ";
	
	/**
	 * Message displaying that the tree is harvested
	 */
	private static final String TREE_HARVESTED_MSG = "You harvested the tree";
	
	/**
	 * Message displaying that fruits are being harvested
	 */
	private static final String FRUIT_HARVESTED_MSG = "You are harvesting fruits";
	
	/**
	 * Message displaying information about keys for harvesting a tree
	 */
	private static final String TREE_SELECTED_MSG = " Harvest the tree by holding the 'E' key down.";
	
	/**
	 * Message displaying that the tree selected by other avatar 
	 */
	private static final String TREE_SELECTED_BY_OTHERS_MSG = "The tree is locked by another avatar";
	
	/**
	 * Message displaying that the server is checking if you can select the tree.
	 */
	private static final String TREE_SELECTION_WAIT = "Checking if you can select the tree.";
	
	//General member variables
	/**
	 * Constant specifying buffer size for picking avatars on mouse click
	 */
	int BUFSIZE = 10;
	
	/**
	 * Byte buffer used to pick avatars on mouse click
	 */
	ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BufferUtil.SIZEOF_INT*BUFSIZE);
	
	/**
	 * OpenGL utility library interface
	 */
	private static GLU glu = new GLU();
	
	/**
	 * Avatar file path
	 */
	protected final static String AVATAR_FILE = "data/forestry/woodcutter.cfg";
	
	/**
	 * Creates a new visualization of the forestry experiment
	 *
	 */
	public ForestryView() {
		byteBuffer.order(ByteOrder.nativeOrder());
	}
	
	/**
	 * Initializes forestry visualization by setting light, camera and HUD message
	 * @param drawable current rendering context
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);	
		
		GL gl = drawable.getGL();
		gl.glClearColor(0.83f, 0.9f, 1.0f, 1.0f ); //sky blue
		
		//Lights
		lights.get(0).setPosition(new Point3D(0, 180, -100), false);
		
		//Camera
		//camera.setPoints(new Point3D(0, 50, 100), new Point3D(0, 0, 0));
		
		hud.setMessage(NAV_INFO_MSG);
	}
	
	/**
	 * Displays the current state of the forestry experiment by displaying forest ground, trees in the forest and avatars
	 * @param drawable current rendering context
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		super.display(drawable);
		
		GL gl = drawable.getGL();
		
		//displayLight(drawable);
		renderAvatars(drawable);
		if (rasterizationMode == GL.GL_SELECT) 			
				gl.glLoadName(100);			
		
		renderGround(drawable);
		renderTrees(drawable);
		displayHud(drawable);
		
		if (mouseClickedFlag) {
			mouseClickedFlag = false;
			pickAvatars(drawable);			
		}
	}
	
	/**
	 * Handles the key pressed events. The mapping of keys to actions is as follows
	 * <table>
	 * <tr><th>Key</th><th>Action</th></tr>
	 * <tr><td>W</td><td>Forward</td></tr>
	 * <tr><td>A</td><td>Turn Left</td></tr>
	 * <tr><td>S</td><td>Backward</td></tr>
	 * <tr><td>D</td><td>Turn Right</td></tr>
	 * <tr><td>Q</td><td>Select a tree</td></tr>
	 * <tr><td>E</td><td>Hit the selected tree</td></tr>
	 * <tr><td>X</td><td>Harvest fruits of the selected tree</td></tr>
	 * </table>
	 * @param e key event 
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		//Do not permit any actions when fruits are being harvested
		if (fruitTimer != null)
			return;
		
		//For debugging
		//super.keyPressed(e);
				
		cameraAvatarVector.set(camera.getLookAtPoint(), camera.getPosition());
		Woodcutter avatar = (Woodcutter)avatars.get(getDataModel().getId());
		Point3D avatarPosition = avatar.getPosition();
		
		int keyCode = e.getKeyCode();		
		switch(keyCode) {
		case KeyEvent.VK_UP:
		case KeyEvent.VK_W:
			if ( isOutofTerrain(avatar.getForwardPosition()) ) {
				hud.setMessage(ForestryView.OUT_OF_TERRAIN_MSG);
            }
            else {
                Tree tree = isAvatarCollidingWithTrunk(avatarPosition, avatar.getFrontRay(true), avatar.getFrontLeftRay(true), avatar.getFrontRightRay(true));
                if (tree == null) 
                	tree = isAvatarCollidingWithBranches(avatarPosition, avatar.getFrontRay(false), avatar.getFrontLeftRay(false), avatar.getFrontRightRay(false));
//                if (tree == null)
//                	tree = isAvatarCollidingWithLeaves(avatarPosition, avatar.getFrontRay(false), avatar.getFrontLeftRay(false), avatar.getFrontRightRay(false));
                if (tree != null) {
                	hud.setMessage(ForestryView.TREE_SELECT_MSG);
                    break;
                }
                hud.setMessage(NAV_INFO_MSG);
                avatar.forward();
                processAvatarMove(avatar);
            }			
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_S:
			if ( isOutofTerrain(avatar.getBackwardPosition()) ) {
				hud.setMessage(ForestryView.OUT_OF_TERRAIN_MSG);
            }
            else {
                Tree tree = isAvatarCollidingWithTrunk(avatarPosition, avatar.getBackRay(true), avatar.getBackLeftRay(true), avatar.getBackRightRay(true));
                if (tree == null)
                	tree = isAvatarCollidingWithBranches(avatarPosition, avatar.getBackRay(false), avatar.getBackLeftRay(false), avatar.getBackRightRay(false));
//                if (tree == null)
//                	tree = isAvatarCollidingWithLeaves(avatarPosition, avatar.getFrontRay(false), avatar.getFrontLeftRay(false), avatar.getFrontRightRay(false));
                if (tree != null) {
                	hud.setMessage(ForestryView.TREE_SELECT_MSG);
    				break;
                }
                hud.setMessage(NAV_INFO_MSG);
                avatar.reverse();
                processAvatarMove(avatar);
            }			
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_A:
		{
            Tree tree = isAvatarCollidingWithTrunk(avatarPosition, avatar.getLeftRay(true), avatar.getFrontLeftRay(true), avatar.getBackLeftRay(true));
            if (tree == null)
            	tree = isAvatarCollidingWithBranches(avatarPosition, avatar.getLeftRay(false), avatar.getFrontLeftRay(false), avatar.getBackLeftRay(false));
//            if (tree == null)
//            	tree = isAvatarCollidingWithLeaves(avatarPosition, avatar.getFrontRay(false), avatar.getFrontLeftRay(false), avatar.getFrontRightRay(false));
            if (tree != null) {
            	hud.setMessage(ForestryView.TREE_SELECT_MSG);
//				hud.setVisible(true);
				break;
            }
            hud.setMessage(NAV_INFO_MSG);
            avatar.moveLeft();
            processAvatarMove(avatar);
			break;
		}
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_D:	
		{
            Tree tree = isAvatarCollidingWithTrunk(avatarPosition, avatar.getRightRay(true), avatar.getFrontRightRay(true), avatar.getBackRightRay(true));
            if (tree == null)
            	tree = isAvatarCollidingWithBranches(avatarPosition, avatar.getRightRay(false), avatar.getFrontRightRay(false), avatar.getBackRightRay(false));
//            if (tree == null)
//            	tree = isAvatarCollidingWithLeaves(avatarPosition, avatar.getFrontRay(false), avatar.getFrontLeftRay(false), avatar.getFrontRightRay(false));
            if (tree != null) {
            	hud.setMessage(ForestryView.TREE_SELECT_MSG);
//				hud.setVisible(true);
				break;
            }
            hud.setMessage(NAV_INFO_MSG);
            avatar.moveRight();
            processAvatarMove(avatar);				
			break;
		}
		case KeyEvent.VK_Q:
//			System.out.println("Q key pressed.");
            requestTreeSelection(avatar);
            break;
        case KeyEvent.VK_E:
//        	System.out.println("E key pressed.");
        	avatar.axe();
			hitTree(avatar);
			sendAvatarPose(avatar);
			break;
        case KeyEvent.VK_X:
//        	System.out.println("X key pressed.");
        	harvestFruits(avatar);        	
		}			

		//moveLight(keyCode);
		update();
	}
	
	/**
	 * Handles the key released events. On releasing W, A, S, D and E keys, a message with the updated client pose is sent
	 * to the server
	 * @param e key event 
	 */
	@Override 
	public void keyReleased(KeyEvent e) {
		super.keyReleased(e);
		int keyCode = e.getKeyCode();
		if (avatars.size() > 0) {
			Woodcutter avatar = avatars.get(getDataModel().getId());
			
			switch(keyCode) {
			case KeyEvent.VK_W:	
			case KeyEvent.VK_S:
            case KeyEvent.VK_A:
            case KeyEvent.VK_D:
			case KeyEvent.VK_E:
				client.transmit(new ClientPoseUpdate(client.getId(), avatar.getPosition(), 
						avatar.getHeading(), avatar.getAnimationState(), false));				
				break;
			
			}//end switch			
		}
	}

	/**
	 * Initializes the visualization by creating a forest ground according to the dimensions specified in the configuration
	 * file, trees according to the resource distribution created by the resource generator and avatars.
	 */
    public void initialize() {
        RoundConfiguration configuration = getDataModel().getRoundConfiguration();
        treeGridDimension = new Tuple2i(configuration.getResourceDepth(), configuration.getResourceWidth());
        
        //Create terrain
        float worldWidth = configuration.getWorldWidth();
        float worldDepth = configuration.getWorldDepth();
        float zExtend = worldWidth / 2.0f;
        float xExtend = worldDepth / 2.0f;
        String textureFile = "";
		RGBA ambient = new RGBA(43.0f/255.0f, 35.0f/255.0f, 30.0f/255.0f, 1.0f);
		RGBA diffuse = new RGBA(71.0f/255.0f, 58.0f/255.0f, 50.0f/255.0f, 1.0f);
		RGBA specular = new RGBA(170.0f/255.0f, 147.0f/255.0f, 132.0f/255.0f, 1.0f);
		float shininess = 2.0f;
        createGround(new Point3D(-xExtend, 0, -zExtend), new Point3D(-xExtend, 0, zExtend), new Point3D(xExtend, 0, -zExtend), new Point3D(xExtend, 0, zExtend),
		ambient, diffuse, specular, shininess, textureFile);
        
        //Create trees
        createTrees();
        
        //Create avatars
        Woodcutter avatar;
        synchronized (getDataModel()) {
//        	for (ClientData clientData : getDataModel().getClientDataMap().values()) {
//        		int avatarNo = clientData.getAssignedNumber();
//        		avatar = loadAvatar(AVATAR_FILE, clientData.getHairColor(), clientData.getSkinColor(), 
//        				clientData.getShirtColor(), clientData.getTrouserColor(), clientData.getShoesColor(), 
//        				clientData.getPoint3D(), avatarNo);
//        		avatars.put(clientData.getId(), avatar);
//        	}
        }
        avatar = avatars.get(client.getId());
        setCamera(avatar);
        sendAvatarPose(avatar);        
    }
      
    //  #################### Ground functions #########################
    /**
     * Creates forest ground
     *  
     * @param lbCorner left back vertex of the ground
     * @param lfCorner left front vertex of the ground
     * @param rbCorner right back vertex of the ground
     * @param rfCorner right front vertex of the ground
     * @param ambient ambient color of the ground
     * @param diffuse diffuse color of the ground
     * @param specular specular color of the ground
     * @param shininess shininess of the ground
     * @param textureFile image to be used to texture the ground
     */
    private void createGround(Point3D lbCorner, Point3D lfCorner, Point3D rbCorner, Point3D rfCorner,
    		RGBA ambient, RGBA diffuse, RGBA specular, float shininess, String textureFile) {
    	
    	ground = new Ground(rfCorner, rbCorner, lbCorner, lfCorner, ambient, diffuse, specular, shininess, textureFile);
    }
    
    /**
     * Displays forest ground
     * @param drawable current rendering context
     */
    private void renderGround(GLAutoDrawable drawable) {
    	ground.display(drawable);
    }
	
    //################  Tree functions  ############################
	/**
	 * Renders trees constisting of branches, leaves and fruits 
	 * @param drawable current rendering context
	 */	
	private void renderTrees(GLAutoDrawable drawable) {
		if (treeGrid == null) return;
				
		GL gl = drawable.getGL();
		int rowIndex, colIndex;
		Tree tree = null;
		
//		gl.glEnable(GL.GL_CULL_FACE);
//    	gl.glCullFace(GL.GL_BACK);
		
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL.GL_NORMAL_ARRAY);		
		
		////////// BRANCHES ////////////////////////
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, Tree.branchAmbient, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, Tree.branchDiffuse, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, Tree.branchSpecular, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, Tree.branchShininess);
		
		for (rowIndex = 0; rowIndex < treeGridDimension.a; rowIndex++) {				
			for (colIndex = 0; colIndex < treeGridDimension.b; colIndex++) {
				tree = (Tree) treeGrid.getNode(rowIndex, colIndex);
				if (tree == null) continue;
				if (isTreeVisible(tree.getLocation()))
					tree.displayBranches(drawable);	
			}
		}
		
		////////// FRUITS /////////////////////////		
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, Tree.fruitAmbient, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, Tree.fruitDiffuse, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, Tree.fruitSpecular, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, Tree.fruitShininess);
		
		for (rowIndex = 0; rowIndex < treeGridDimension.a; rowIndex++) {				
			for (colIndex = 0; colIndex < treeGridDimension.b; colIndex++) {
				tree = (Tree) treeGrid.getNode(rowIndex, colIndex);
				if (tree == null) continue;
				if (isTreeVisible(tree.getLocation()))
					tree.displayFruits(drawable);	
			}
		}
		
//		gl.glDisable(GL.GL_CULL_FACE);
		
		////////////// LEAVES ///////////////////
		
//		Tree.leafTexture.load(drawable.getGL());
		
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, Tree.leafAmbient, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, Tree.leafDiffuse, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, Tree.leafSpecular, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, Tree.leafShininess);
		
		for (rowIndex = 0; rowIndex < treeGridDimension.a; rowIndex++) {				
			for (colIndex = 0; colIndex < treeGridDimension.b; colIndex++) {
				tree = (Tree) treeGrid.getNode(rowIndex, colIndex);
				if (tree == null) continue;
				if (isTreeVisible(tree.getLocation()))
					tree.displayLeaves(drawable);	
			}
		}
					
		gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
	}
	
	/**
	 * Creates trees by initializing tree geometry for different ages, creating tree grid, and placing the trees
	 * at the center of cells of the tree grid
	 *
	 */
	public void createTrees() {	
		Tree.init(client.getDataModel().getRoundConfiguration().getMaximumResourceAge());
		TreeView.init();
		
		treeGrid = new Grid();
        treeGrid.addRows((int) treeGridDimension.a, (int) treeGridDimension.b);
		
        treeCellWidth = (ground.getMaxExtent().x - ground.getMinExtent().x) / treeGridDimension.a;
		treeCellDepth = (ground.getMaxExtent().z - ground.getMinExtent().z) / treeGridDimension.b;
		topLeftCellCenter = ground.getLB();
		topLeftCellCenter = new Point3D(topLeftCellCenter.x+treeCellWidth/2.0f, 0, topLeftCellCenter.z+treeCellDepth/2.0f);
		int nTrees = 0;
		for (Resource resource : getDataModel().getResourceDistribution().values()) {
			addTree(resource);	
			nTrees++;
		}
		//Added this line as each tree was getting added twice as it is present in 
		//the resourceDistribution as well as addedResources list
//		getDataModel().clearDiffLists(); 
		
		repaint();
	}
	
	/**
	 * Updates trees according to the resource distribution sent by the server. This includes removing the harvested trees,
	 * adding the newly generated trees and updating the grown trees. 
	 */
	@Override
	public void updateResources() {
	    Map<Point, Resource> resourceDistribution = getDataModel().getResourceDistribution();
	    for (int row = 0; row < treeGrid.getRows(); row++) {
	        for (int col = 0; col < treeGrid.getColumns(); col++) {
	            Point point = new Point(row, col);
	            Tree tree = (Tree) treeGrid.getNode(point);
	            Resource resource = resourceDistribution.get(point);
	            if (resource != null) {
	                if (tree == null) {
	                    addTree(resource);
	                }
	                else if (!tree.isHarvested() && tree.getAge() != resource.getAge())  {
	                    tree.setAge(resource.getAge());
	                }
	            }
	            else if (tree != null) {
	                treeGrid.remove(point);
	            }
	        }
	    }
//        for (Resource resource: getDataModel().getRemovedResources()) {
//            Tree tree = (Tree) treeGrid.getNode(resource.getPosition());
//            if (! tree.harvested) {
//                treeGrid.remove(resource.getPosition());
//            }
//        }
//        for (Resource resource: getDataModel().getAddedResources()) {
//        	addTree(resource);
//        }
//        for (Resource resource : getDataModel().getResourceDistribution().values()) {
//            Tree tree = (Tree) treeGrid.getNode(resource.getPosition());
//
//        }
//        getDataModel().clearDiffLists();
    }
	
	/**
	 * Adds a new tree to represent the specified resource 
	 * @param resource resource from the resource pool
	 */
	private void addTree(Resource resource) {
        Point point = resource.getPosition();        
        Point3D cellCenter = new Point3D(topLeftCellCenter).add(new Point3D(point.x*treeCellWidth, 0, point.y*treeCellDepth));
		
		Tree tree = new Tree(resource, cellCenter, this);  
		treeGrid.setNode(point.y, point.x, tree);		
    }
		
	//#################### Camera - Tree Functions #########################
	/**
	 * Checks if a tree is visible from the given camera position
	 * @param treePosition position of the tree
	 * @return true if the tree is visible, false otherwise
	 */
	private boolean isTreeVisible(Point3D treePosition) {
		Vector3D cameraTreeVector = new Vector3D(treePosition, camera.getPosition());
		
		//Find out angle between cameraParticipantVector and cameraTreeVector
		if (cameraTreeVector.cosXZAngle(cameraAvatarVector) < FOV)
			return false;
		
		return true;
	}
	
//	############### Avatar functions ##################
	/**
	 * Loads the avatar geometry and animation from the specified file, applies the specified materials, initializes it 
	 * with the specified id and places it at the specified location 
	 * 
	 * @param filePath file containing paths of avatar geometry and animation files
	 * @param hairColor hair color of the avatar
	 * @param skinColor skin color of the avatar
	 * @param shirtColor shirt color of the avatar
	 * @param trouserColor trouser color of the avatar
	 * @param shoesColor shoes color of the avatar
	 * @param position location of the avatar in the forest
	 * @param assignedNo no. assigned to the avatar
	 * @return newly created avatar object
	 */
	public Woodcutter loadAvatar(String filePath, Color hairColor, Color skinColor,  Color shirtColor, Color trouserColor, Color shoesColor, Point3D position, int assignedNo) {	//, String dataPath) {	
		System.out.println("Loading avatar #"+ assignedNo);
		Woodcutter avatar = new Woodcutter(position, 0, 0.5f, this);
		avatar.init(filePath, hairColor, skinColor, shirtColor, trouserColor, shoesColor);
		
		//updateAvatarElevation(avatar);
		avatar.setMaterials(new RGBA(1.0f, 0.0f, 1.0f, 0.0f), new RGBA(1.0f, 0.0f, 1.0f, 0.0f), new RGBA(1.0f, 1.0f, 1.0f, 0.0f), 128.0f);
		avatar.setAssignedNumber(assignedNo);
				
		return avatar;
	}
	
	/**
	 * Displays avatars
	 * @param drawable current rendering context
	 */
	private void renderAvatars(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		
		for (Woodcutter avatar: avatars.values()) {
			if (rasterizationMode == GL.GL_SELECT) {
				//Write a unique name for each woodcutter if we are in the selection mode
				int assignedNo = avatar.getAssignedNumber();
				gl.glLoadName(assignedNo);
			}
			avatar.display(drawable);			
        }
	}
	
	/**
	 * Processes the avatar's move by making it walk, updating the camera according to its new position, updating
	 * the server and deselecting any selected trees.
	 * @param avatar avatar whose move is to be processed 
	 */
	private void processAvatarMove(Woodcutter avatar) {
        avatar.walk();
		//updateAvatarElevation(avatar);
		setCamera(avatar);
		sendAvatarPose(avatar);
		resetTreeToHarvest(); 
		waitingForSelectRequest = false;
    }
	
	/**
	 * Sends avatar's position, heading and animation state to the server
	 * @param avatar avatar whose details need to be sent to the server
	 */
	private void sendAvatarPose(Woodcutter avatar) {
		client.transmit(new ClientPoseUpdate(client.getId(), avatar.getPosition(), 
				avatar.getHeading(), avatar.getAnimationState(), true));		
	}
	
	/**
	 * Updates avatar positions, headings and animation states according to the information sent by the server
	 */
	public void updateAgentPositions() {
//        for (ClientData clientData : getDataModel().getClientDataMap().values()) {
//            Identifier id = clientData.getId();
//            if (id.equals(client.getId())) {
//                continue;
//            }
//            Woodcutter avatar = avatars.get(id);
//            avatar.setPosition(clientData.getPoint3D());
//            avatar.setHeading(clientData.getHeading());            
//            if (clientData.isAnimationActive())
//            	avatar.animate( clientData.getAnimationState() );
//        }
    }

	//################### Avatar picking related functions #####################
	/**
	 * Writes avatar information in a buffer so that the avatar clicked with a mouse button can be selected for chatting 
	 * @param drawable current rendering context
	 */
	private void pickAvatars(GLAutoDrawable drawable) {	
		GL gl = drawable.getGL();
				
		int hits;
		int[] viewport = new int[4];	
		
		IntBuffer selectBuffer;		
		byteBuffer.clear();
        selectBuffer = byteBuffer.asIntBuffer();
        	
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		
		gl.glSelectBuffer(BUFSIZE, selectBuffer);
		
		rasterizationMode = GL.GL_SELECT;
		gl.glRenderMode(rasterizationMode);
		
		gl.glInitNames();
		gl.glPushName(0);
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();			//save current projection matrix
		gl.glLoadIdentity();
		
		//Create 5x5 pixel picking region near cursor location		
		glu.gluPickMatrix(mouseClickedLocation.x, viewport[3]-mouseClickedLocation.y, 5.0, 5.0, viewport, 0);
		//gl.glOrtho(0.0, 5.0, 0.0, 5.0, 0.0, 10.0);
		glu.gluPerspective(fovy, aspectRatio, zNear, zFar);
		
		//Draw the avatars in the GL_SELECT mode
		display(drawable);
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		
		rasterizationMode = GL.GL_RENDER;
		hits = gl.glRenderMode(rasterizationMode);
		System.out.println("Hits = " + hits);
		processHits(hits, selectBuffer);		
	}
	
	/**
	 * Gets the avatar selected by a mouse click and starts chatting with it if it is within the chatting range specified
	 * in the configuration file.
	 * @param hits no. of mouse clicks
	 * @param selectBuffer buffer containing avatar information
	 */
	private void processHits(int hits, IntBuffer selectBuffer) {
		int noOfNames, z1, z2, name;
		int[] z1Array = new int[3];
		int[] z2Array = new int[3];
		int[] names = new int[3];
		int probableAvatars = 0;
		int selectedAvatar = 0;
				
		for (int hitCounter = 0; hitCounter < hits; hitCounter++) {
			noOfNames = selectBuffer.get();
			z1 = selectBuffer.get()*Integer.MAX_VALUE;
			z2 = selectBuffer.get()*Integer.MAX_VALUE;
			
			//Don't do anything if
			//1. the terrain or trees are clicked
			//2. the avatar is clicked by himself
			name = selectBuffer.get();
			if ( name == 100 || name == ((Woodcutter)avatars.get(getDataModel().getId())).getAssignedNumber() )
				continue;
			
			z1Array[probableAvatars] = z1;
			z2Array[probableAvatars] = z2;
			names[probableAvatars] = name;
			probableAvatars++;
		}
			
		if (probableAvatars > 0) {
			//We are here as other avatars are clicked.
			//Select the closest one for the chat			
			int minZ = Integer.MAX_VALUE;	
			for (int index = 0; index < probableAvatars; index++) {
				if ( z1Array[index] < minZ) {
					selectedAvatar = names[index];
				}
			}
			//Check if the selected avatar is within the specified distance to start the chat
			Identifier selectedAvatarId = toIdentifier(selectedAvatar); 
			if (shouldChat(getDataModel().getId(), selectedAvatarId)) {			
				//We should never get -1 here
	            initiateChat(selectedAvatarId);
			}	        
		}
		else {
			hud.setMessage(ForestryView.NAV_INFO_MSG);
		}
		/*//############# Debug code
		System.out.println("##############");
		System.out.println("# of hits = " + hits);
		for (hitCounter = 0; hitCounter < hits; hitCounter++) {			
			noOfNames = selectBuffer.get();
			System.out.println("# of names for this hit = " + noOfNames);
			System.out.print("z1 = " + selectBuffer.get()); ///0x7fffffff);
			System.out.println("z2 = " + selectBuffer.get()); ///0x7fffffff);
			System.out.print("Name is ");
			for (nameCounter = 0; nameCounter < noOfNames; nameCounter++) {
				System.out.print(selectBuffer.get() + " ");
			}
			System.out.println();
		}*/
	}
	
	/**
	 * Checks if the two clients are within the chatting distance and should chat or not 
	 * @param clientId1 identifier of the first client
	 * @param clientId2 identifier of the second client
	 * @return true if the clients can chat, false otherwise
	 */
	@Override
	public boolean shouldChat(Identifier clientId1, Identifier clientId2) {
		Point3D avatar1Position = avatars.get(clientId1).getPosition();
		Point3D avatar2Position = avatars.get(clientId2).getPosition();
		float distance = new Vector3D(new Point3D(avatar1Position.x, 0, avatar1Position.z), 
										new Point3D(avatar2Position.x, 0, avatar2Position.z)).length();
		System.out.println("Distance between two avatars is " + distance);
		if (distance <= client.getDataModel().getRoundConfiguration().getChattingRadius()) {
			hud.setMessage(ForestryView.CHAT_START_MSG);
			return true;
		}
		else {
			hud.setMessage(ForestryView.OUT_OF_CHAT_RANGE);
		}
		return false;
	}
    
	/**
	 * Starts chat with the specified client id
	 * @param id client id with whom the chat should be started
	 */
    private void initiateChat(Identifier id) {
        client.getGameWindow3D().getChatPanel().setTargetHandle(id);
    }
	
	//###############  Avatar-Terrain functions  ########################
	/**
	 * Checks if the specified position is out of the terrain boundary
	 * @param potentialPosition potential position of the avatar
	 * @return true if the avatar is going out of the terain boundary, false otherwise 
	 */
	public boolean isOutofTerrain(Point3D potentialPosition) {
		Point3D minExtent = ground.getMinExtent();
		Point3D maxExtent = ground.getMaxExtent();
		
		if (potentialPosition.x < minExtent.x || potentialPosition.x > maxExtent.x || 
			potentialPosition.z < minExtent.z || potentialPosition.z > maxExtent.z)
			return true;
		
		return false;
	}
	
	//################# Avatar-Tree functions ###################
	/**
	 * Gets the avatar's tree grid cell index based on its position 
	 * @param avatarPosition avatar's position
	 * @return cell index in the form (row index, column index)
	 */
	public Tuple2i getAvatarCellIndex(Point3D avatarPosition) {
	    //Point3D topLeft = ((Tree) treeGrid.getNode(0, 0)).getLocation();
		//
        return new Tuple2i(
                (int)( (avatarPosition.z - topLeftCellCenter.z) / treeCellDepth ),
                Math.round( (avatarPosition.x - topLeftCellCenter.x) / treeCellWidth ) );
	}
	
	/**
	 * Checks if the avatar is going to collide with branches of a tree
	 * @param avatarPosition avatar position
	 * @param ray1 first ray used for collision detection
	 * @param ray2 second ray used for collision detection
	 * @param ray3 third ray used for collision detection
	 * @return tree with which the avatar is going to collide or null 
	 */
	public Tree isAvatarCollidingWithBranches(Point3D avatarPosition, Ray ray1, Ray ray2, Ray ray3)
	{
		Tuple2i avatarCellIndex = getAvatarCellIndex(avatarPosition);
		
		// Get food in that cell and 4 neighboring cells
		//FIXME: Only check the tree in the direction of the avatar's heading
        int minRowIndex = avatarCellIndex.a - 1;
        if (minRowIndex < 0)
            minRowIndex = 0;
        int minColIndex = avatarCellIndex.b - 1;
        if (minColIndex < 0)
            minColIndex = 0;
        int maxRowIndex = avatarCellIndex.a + 2;
        if (treeGrid.getRows() < maxRowIndex)
            maxRowIndex = treeGrid.getRows();
        int maxColIndex =avatarCellIndex.b + 2;
        if (treeGrid.getColumns() < maxColIndex)
            maxColIndex = treeGrid.getColumns();

        for (int rowIndex = minRowIndex; rowIndex < maxRowIndex; rowIndex++) {
            for (int colIndex = minColIndex; colIndex < maxColIndex; colIndex++) {
                Tree tree = (Tree) treeGrid.getNode(rowIndex, colIndex);
                if (tree != null && !tree.isHarvested() && (
                	tree.isIntersectingWithBranches(ray1, 1.5f) 	
                	|| tree.isIntersectingWithBranches(ray2, 1.5f) 	
                	|| tree.isIntersectingWithBranches(ray3, 1.5f)
				)) {
                	return tree;
                }
            }
        }
	
		return null;
	}
	
	/**
	 * Checks if the avatar is going to collide with leaves of a tree
	 * @param avatarPosition avatar position
	 * @param ray1 first ray used for collision detection
	 * @param ray2 second ray used for collision detection
	 * @param ray3 third ray used for collision detection
	 * @return tree with which the avatar is going to collide or null 
	 */
	public Tree isAvatarCollidingWithLeaves(Point3D avatarPosition, Ray ray1, Ray ray2, Ray ray3) {
		Tuple2i avatarCellIndex = getAvatarCellIndex(avatarPosition);
		
		// Get food in that cell and 4 neighboring cells
		//FIXME: Only check the tree in the direction of the avatar's heading
        int minRowIndex = avatarCellIndex.a - 1;
        if (minRowIndex < 0)
            minRowIndex = 0;
        int minColIndex = avatarCellIndex.b - 1;
        if (minColIndex < 0)
            minColIndex = 0;
        int maxRowIndex = avatarCellIndex.a + 2;
        if (treeGrid.getRows() < maxRowIndex)
            maxRowIndex = treeGrid.getRows();
        int maxColIndex =avatarCellIndex.b + 2;
        if (treeGrid.getColumns() < maxColIndex)
            maxColIndex = treeGrid.getColumns();

        for (int rowIndex = minRowIndex; rowIndex < maxRowIndex; rowIndex++) {
            for (int colIndex = minColIndex; colIndex < maxColIndex; colIndex++) {
                Tree tree = (Tree) treeGrid.getNode(rowIndex, colIndex);
                if (tree != null && !tree.isHarvested() && (
                	tree.isIntersectingWithLeaves(ray1, 1.5f) 	
                	|| tree.isIntersectingWithLeaves(ray2, 1.5f) 	
                	|| tree.isIntersectingWithLeaves(ray3, 1.5f)
				)) {
                	return tree;
                }
            }
        }
	
		return null;
	}

	/**
	 * Checks if the avatar is going to collide with a tree trunk
	 * @param avatarPosition avatar position
	 * @param ray1 first ray used for collision detection
	 * @param ray2 second ray used for collision detection
	 * @param ray3 third ray used for collision detection
	 * @return tree with which the avatar is going to collide or null 
	 */
	public Tree isAvatarCollidingWithTrunk(Point3D avatarPosition, Ray ray1, Ray ray2, Ray ray3)
	{
		Tuple2i avatarCellIndex = getAvatarCellIndex(avatarPosition);
		
        int minRowIndex = avatarCellIndex.a - 1;
        if (minRowIndex < 0)
            minRowIndex = 0;
        int minColIndex = avatarCellIndex.b - 1;
        if (minColIndex < 0)
            minColIndex = 0;
        int maxRowIndex = avatarCellIndex.a + 2;
        if (treeGrid.getRows() < maxRowIndex)
            maxRowIndex = treeGrid.getRows();
        int maxColIndex =avatarCellIndex.b + 2;
        if (treeGrid.getColumns() < maxColIndex)
            maxColIndex = treeGrid.getColumns();

        for (int rowIndex = minRowIndex; rowIndex < maxRowIndex; rowIndex++) {
            for (int colIndex = minColIndex; colIndex < maxColIndex; colIndex++) {
                Tree tree = (Tree) treeGrid.getNode(rowIndex, colIndex);
                if (tree != null && !tree.isHarvested() && (
                	tree.isIntersectingWithTrunk(ray1, 3.5f) 	
                	|| tree.isIntersectingWithTrunk(ray2, 1.5f) 	
                	|| tree.isIntersectingWithTrunk(ray3, 1.5f)
				)) {
                	return tree;
                }
            }
        }
	
		return null;
	}
	
	/**
	 * Returns a tree that the avatar can select. The avatar can select a tree if he is facing it and is at most 5 units away 
	 * @param avatar avatar who wants to select a tree
	 * @return selectable tree if any, else null
	 */
	public Tree getSelectableTree(Woodcutter avatar)
	{			
		Tuple2i avatarCellIndex = getAvatarCellIndex(avatar.getPosition());
		
        int minRowIndex = avatarCellIndex.a - 1;
        if (minRowIndex < 0)
            minRowIndex = 0;
        int minColIndex = avatarCellIndex.b - 1;
        if (minColIndex < 0)
            minColIndex = 0;
        int maxRowIndex = avatarCellIndex.a + 2;
        if (treeGrid.getRows() < maxRowIndex)
            maxRowIndex = treeGrid.getRows();
        int maxColIndex =avatarCellIndex.b + 2;
        if (treeGrid.getColumns() < maxColIndex)
            maxColIndex = treeGrid.getColumns();

        for (int rowIndex = minRowIndex; rowIndex < maxRowIndex; rowIndex++) {
            for (int colIndex = minColIndex; colIndex < maxColIndex; colIndex++) {
                Tree tree = (Tree) treeGrid.getNode(rowIndex, colIndex);
                if (tree != null && !tree.isHarvested() &&(
                		tree.isIntersectingWithTrunk(avatar.getFrontRay(true), 5.0f) || 
                		tree.isIntersectingWithTrunk(avatar.getFrontLeftRay(true), 5.0f) ||
                		tree.isIntersectingWithTrunk(avatar.getFrontRightRay(true), 5.0f)) ) {
                	return tree;
                }
            }
        }
	
		return null;
	}

	/**
	 *  Hits a tree and displays the no. of times the tree is hit or harvesting is complete.
	 * @param avatar avatar hitting the tree
	 */
	public void hitTree(Woodcutter avatar) {    	
		if (selectedTree != null && !selectedTree.isHarvested() && avatar.isNewHit()) {
			boolean harvested = selectedTree.hit();
			if (harvested) {
				hud.setMessage(ForestryView.TREE_HARVESTED_MSG);
				client.transmit(new HarvestResourceRequest(client.getId(), selectedTree.getResource()));
				selectedTree.setSelected(false);
				selectedTree = null;
			}
			else {			
				hud.setMessage(ForestryView.TREE_HIT_MSG + HUD.getNoToString(selectedTree.getHitCounter()));
			}
		}

		return;
	}
	
	/**
	 * Harvests fruits by initializing falling of fruits
	 * @param avatar avatar harvesting the fruits
	 */
	public void harvestFruits(Woodcutter avatar) {
		if (fruitTimer == null && selectedTree != null && !selectedTree.isHarvested() && selectedTree.getAge() == getDataModel().getRoundConfiguration().getMaximumResourceAge()){// && selectedTree.hasFruits()) {
	        long duration = 12000L;
	        int interval = 350;
	        //int steps = (int)duration / interval - 1;

	        //Find the terrain height below each fruit
	        //and use to decide ht translation step
//	        for (int fruitIndex = 0; fruitIndex < selectedTree.noOfFruits(); fruitIndex++) {
//	        	//Get the location of a fruit
//	        	Point3D fruitLocation = selectedTree.getFruitLocation(fruitIndex);
//	        	//Get elevation of terrain below this fruit
//	        	float terrainElevation = 0;//terrain.getElevation(fruitLocation);
//	        	//calculate translation in the fruit elevation accordingly 
//	        	selectedTree.calculateFruitElevationStep(fruitIndex, terrainElevation, steps);
//	        }
	        fruitTimer = createFruitFallTimer(selectedTree, duration, interval);
	        fruitTimer.start();
	        hud.setMessage(ForestryView.FRUIT_HARVESTED_MSG);
		}
		return;
	}
	
	/**
	 * Sends request of selecting a tree to the server
	 * @param avatar avatar who wants to select the tree
	 */
	private synchronized void requestTreeSelection(Woodcutter avatar) {
		//If we have already sent the request to select this tree, wait till the server responds; do not send another request
		if (waitingForSelectRequest) {
			hud.setMessage(TREE_SELECTION_WAIT);
			return;
		}
			
		//Select a tree if no other tree is selected
        if (selectedTree == null) {
	        //Get tree in front of the avatar
	        Tree tree = getSelectableTree(avatar);
	        System.out.println("Trying to select " + tree + " tree");
	        if (tree != null) {        	        	
	        	System.out.println("Sending LockResourceRequest" + tree.getResource());
	        	waitingForSelectRequest = true;
	        	client.transmit(new LockResourceRequest(client.getId(), tree.getResource()));
	        	System.out.println("LockResourceRequest successfully sent.");
	        	
	        }
	        else
	        {
	        	System.out.println("Tree is null. Cannot be selected.");
	        }
        }
        else
        {
        	System.out.println("selectedTree is not null. Is any other tree selected?");
        }
    }
	
	/**
	 * Highlights the successfully selected tree. Displays age of the tree. 
	 * @param resource resource from the resource pool
	 */
	public void highlightResource(Resource resource) {
		//Highlight the resource only if we are waiting for the server to respond to our select request
		if (waitingForSelectRequest) {
			waitingForSelectRequest = false;
			if (selectedTree == null || ! selectedTree.getResource().getPosition().equals(resource.getPosition())) {				
				selectedTree = (Tree) treeGrid.getNode(resource.getPosition());
				System.out.println("Received response on our LockResourceRequest for tree " + selectedTree);
	            if (selectedTree == null) return;
		        selectedTree.setSelected(true); 
		        System.out.println(selectedTree + " is selected. Should be displayed in blue.");
		        int age = selectedTree.getAge();
		        String message = "Age: " + age + ForestryView.TREE_SELECTED_MSG;
		        if (age == getDataModel().getRoundConfiguration().getMaximumResourceAge())
		        	message += " or fruits by pressing 'X'";
		        hud.setMessage(message);
		        SwingUtilities.invokeLater(new Runnable() {
		            public void run() {
		                update();       
		            }
		        });
			}
		}
		else
		{
			System.out.println("We are not waiting for a response on the select request. Why are we here in highlightResource?");
		}
    }
	
	/**
	 * Deselects the selected tree by unhilighting it and informs the server
	 *
	 */
	private void deselectTree() {
        if (selectedTree != null) {
        	System.out.println("Deselecting a previously selected tree");
        	System.out.println("Sending UnlockResourceRequest to server for tree " + selectedTree);
        	client.transmit(new UnlockResourceRequest(client.getId(), selectedTree.getResource()));
        	System.out.println("Unselecting the tree.");
        	selectedTree.setSelected(false);
        	selectedTree = null;
        }
        else
        {
        	System.out.println("selected tree is null. Nothing to deselect.");
        }
    }
	
	/**
	 * Resets a tree by stopping the flash timer and unhilighting it
	 */
	protected void resetTreeToHarvest() {
        if (treeLockedFlashTimer != null) {
            treeLockedFlashTimer.stop();
        }
        
        //Deselect the tree if fruits are finished falling down
        if (fruitTimer == null && selectedTree != null) {
        	deselectTree();
        }
    }
	
	//#################### Timers ####################################
	/**
	 * Timer used to flash a tree if the avatar tries to select it but is already selected by another avatar 
	 */
	private TreeTimer treeLockedFlashTimer;
    
        /**
         * Starts the tree locked flash timer to indicate that the tree is already selected by another avatar
	 */
    public void flashResource(Resource resource) {
    	//Reset the previously selected tree
    	selectedTree = null;
    	
    	//Flash the tree only if we are waiting for the server to respond to our select request
    	if (waitingForSelectRequest) {
    		waitingForSelectRequest = false;
    	
	        Tree tree = (Tree) treeGrid.getNode(resource.getPosition());
	        System.out.println("Flashing the tree " + tree + " The server should not lock this resource");
            if (tree == null) return;
	        hud.setMessage(ForestryView.TREE_SELECTED_BY_OTHERS_MSG);
	        if (treeLockedFlashTimer != null) {
	            treeLockedFlashTimer.stop();
	        }
	        treeLockedFlashTimer = createLockFlashTimer(tree);
	        treeLockedFlashTimer.start();
    	}
    	else
    	{
    		System.out.println("We are not waiting for select request. Why are we here in flashResource?");
    	}
    }
    
    /**
     * The TreeTimer class is used to flash a tree if an avatar tries to select a tree that is already selected by another
     * avatar. 
     * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
     *
     */
    private static class TreeTimer extends Timer {
        private final Tree tree;
        
        /**
         * Constructs a new TreeTimer for the specified tree with the specified interval and listener
         * @param tree tree on which the timer acts
         * @param delay time interval between timer handler calls
         * @param listener listener listening to the timer calls and flashing the tree
         */
        TreeTimer(Tree tree, int delay, ActionListener listener) {
            super(delay, listener);
            this.tree = tree;
        }
        
        /**
         * Stops the timer and unhilights the tree
         */
        public void stop() {
            super.stop();
            tree.setSelectedByOthers(false);
        }
    }
    
    /**
     * Creates the lock flash timer and handles the timer events
     * @param tree tree to be flashed 
     * @return timer object
     */
    private TreeTimer createLockFlashTimer(final Tree tree) {
        final long endTime = System.currentTimeMillis() + 3000L;
        return new TreeTimer(tree, 500, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (endTime - System.currentTimeMillis() < 0) {
                    treeLockedFlashTimer.stop();
                    treeLockedFlashTimer = null;
                }
                else if (tree == null) {
                    treeLockedFlashTimer.stop();
                    treeLockedFlashTimer = null;
                    return;
                }
                else {
                    tree.toggleSelectedByOthers();
                }
            }
        });
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
    @SuppressWarnings("serial")
    private class FruitTimer extends Timer {
    	/**
    	 * The tree whose fruits are being harvested
    	 */
        private final Tree tree;
        
        /**
         * Constructs a new fruit timer for the specified tree with the specified delay and listener
         * @param tree tree whose fruits are being harvested
         * @param delay time interval between timer handler calls
         * @param client client object
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
            
            tree.resetFruitHeight();
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
            	long timeLeft = endTime - System.currentTimeMillis();
                if (timeLeft < 0) {
                    fruitTimer.stop();
                    fruitTimer = null;                    
                    resetTreeToHarvest();	//Deselect tree after fruits are harvested
                }
                else {
                    tree.updateFruitValues();                	
                }
            }
        });
    }	
	
//  ####################  Camera related functions ######################
	/**
	 * Sets the camera behind the avatar to create the third person shooter camera
	 * @param avatar avatar whom the camera should follow
	 */
	public void setCamera(Woodcutter avatar) {
		Point3D avatarCenter = avatar.getCenter_WS(); 
		//System.out.println("Woodcutter center is " + woodcutterCenter);
		Point3D avatarPosition = avatar.getPosition().add(avatarCenter);
		float avatarAngle = avatar.getHeading();

		float cameraAngle = avatarAngle;
		Point3D cameraPosition = new Point3D();
		cameraPosition.x = avatarPosition.x - AVATAR_CAMERA_DISTANCE * (float)Math.cos(cameraAngle);
		cameraPosition.z = avatarPosition.z + AVATAR_CAMERA_DISTANCE * (float)Math.sin(cameraAngle);
		cameraPosition.y = avatarPosition.y + AVATAR_CAMERA_HEIGHT;
		camera.setPoints(cameraPosition, avatarPosition);
		
		cameraAvatarVector = new Vector3D(new Point3D(avatarPosition.x, cameraPosition.y, avatarPosition.z), 
				cameraPosition);
	}

	/**
	 * Converts the number assigned to the avatar to the corresponding client id
	 * @param assignedNumber no. assigned to the avatar
	 * @return client id if the assigned no. is valid, null otherwise
	 */
    public Identifier toIdentifier(int assignedNumber) {
        for (Map.Entry<Identifier, Woodcutter> entry : avatars.entrySet()) {
            Woodcutter avatar = entry.getValue();
            if (avatar.getAssignedNumber() == assignedNumber) {
                return entry.getKey();
            }
        }
        System.err.println("No avatar with assigned number: " + assignedNumber);
        return null;
    }

    /**
     * Displays the Head-Up Display and avatar lables. 
     * @param drawable current rendering context
     */
	private void displayHud(GLAutoDrawable drawable) {
    	GL gl = drawable.getGL();
    	
	    hud.switchToOrthoProjection(drawable);
		gl.glDisable(GL.GL_LIGHTING);
		hud.displayMessage(drawable);
		
		Woodcutter avatar;
		Point labelPosition;
		Identifier id;
		//Other clients
		for (ClientData clientData : getDataModel().getOtherClients()) {
	        id = clientData.getId();
	        avatar = avatars.get(id);
			labelPosition = avatar.getLabelPosition();
			hud.displayLabel(drawable, labelPosition, client.getGameWindow3D().getChatHandle(id));
		}
		
		//This client
		id = getDataModel().getId();
		avatar = avatars.get(id);
		labelPosition = avatar.getLabelPosition();
		hud.displayLabel(drawable, new Point(labelPosition.x - 10, labelPosition.y), client.getGameWindow3D().getChatHandle(id));
		gl.glEnable(GL.GL_LIGHTING);
		hud.restorePreviousProjection(drawable);
    }
    
    //##################### Debug code ###################################
	
	/**
	 * Moves light similar to the camera. Can be used for debugging purposes.
	 * @param keyCode code of the key pressed
	 */
	private void moveLight(int keyCode) { 
	PointLight light = lights.get(0);
	switch(keyCode) {			
		case KeyEvent.VK_LEFT:
			light.rotateLeft();
			System.out.println("Light position " + light.getPosition());
			break;
		case KeyEvent.VK_RIGHT:
			light.rotateRight();
			System.out.println("Light position " + light.getPosition());
			break;
		case KeyEvent.VK_UP:
			light.moveUp();
			System.out.println("Light position " + light.getPosition());
			break;
		case KeyEvent.VK_DOWN:
			light.moveDown();
			System.out.println("Light position " + light.getPosition());
			break;
		case KeyEvent.VK_P:
			light.moveAway();
			System.out.println("Light position " + light.getPosition());
			break;
		case KeyEvent.VK_L:
			light.moveNear();
			System.out.println("Light position " + light.getPosition());
			break;
		}
	}
	
	/**
	 * Displays light as a sphere.
	 * @param drawable current rendering context
	 */
	private void displayLight(GLAutoDrawable drawable)
	{
		GL gl = drawable.getGL();
		GLUquadric quadric = glu.gluNewQuadric();
        glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
        float[] color = {0.5f, 0.5f, 0.5f, 1.0f};
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, color, 0);
        
        Point3D lightPosition = lights.get(0).getPosition();
        
        gl.glPushMatrix();
        gl.glTranslatef(lightPosition.x, lightPosition.y, lightPosition.z);
		glu.gluSphere(quadric, 2, 16, 16);	
		gl.glPopMatrix();
	}
}
