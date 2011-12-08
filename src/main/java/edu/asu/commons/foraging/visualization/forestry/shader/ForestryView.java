package edu.asu.commons.foraging.visualization.forestry.shader;

import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.sun.opengl.util.BufferUtil;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.ClientPoseUpdate;
import edu.asu.commons.foraging.event.HarvestFruitRequest;
import edu.asu.commons.foraging.event.HarvestResourceRequest;
import edu.asu.commons.foraging.event.LockResourceRequest;
import edu.asu.commons.foraging.event.UnlockResourceRequest;
import edu.asu.commons.foraging.fileplugin.FileLoader;
import edu.asu.commons.foraging.graphics.FractalTerrain;
import edu.asu.commons.foraging.graphics.Grid;
import edu.asu.commons.foraging.graphics.HUD;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.RGBA;
import edu.asu.commons.foraging.graphics.Ray;
import edu.asu.commons.foraging.graphics.SkyBox;
import edu.asu.commons.foraging.graphics.Triangle;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.foraging.util.Tuple2f;
import edu.asu.commons.foraging.util.Tuple2i;
import edu.asu.commons.foraging.visualization.GameView3d;
import edu.asu.commons.net.Identifier;



public class ForestryView extends GameView3d {
	
	private static final long serialVersionUID = -7254423448030862245L;
	
	//Terrain related member variables
	protected FractalTerrain terrain = null;
	protected SkyBox skyBox = null;
	
	//Tree related member variables
	protected Grid treeGrid = null;	
	private Tuple2i treeGridDimension; //(rows, cols)
	private float treeCellWidth;
	private float treeCellDepth;
	private Tuple2f terrain2treeGridRatio;
	private Point3D topLeftCellCenter;
	private Tree selectedTree;
			
	//Avatar related member variables
	protected Map<Identifier, Woodcutter> avatars = new HashMap<Identifier, Woodcutter>();
	protected static float AVATAR_CAMERA_DISTANCE = 20;	
	protected static float AVATAR_CAMERA_HEIGHT = 5;
	protected Vector3D cameraAvatarVector = new Vector3D(camera.getLookAtPoint(), camera.getPosition());
	public static double FOV = Math.cos((Math.PI / 180.0f) * 100.0f);
	
	//Heads-up display
	private HUD hud = new HUD();
	public static final String OUT_OF_TERRAIN_MSG = "You are not allowed to go out of the forest boundary";
	public static final String TREE_COLLISION_MSG = "You are going to collide with the tree";
	public static final String OUT_OF_CHAT_RANGE = "The selected avatar is out of the chatting range";
	public static final String TREE_HIT_MSG = "You hit the tree ";
	public static final String TREE_HARVESTED_MSG = "You harvested the tree";
	public static final String FRUIT_HARVESTED_MSG = "You are harvesting the fruits";
	public static final String TREE_SELECTED_MSG = "The tree is locked. You can harvest the fruits or the tree";
	public static final String TREE_SELECTED_BY_OTHERS_MSG = "The tree is locked by another avatar";
	
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
	
	//General member variables
	int BUFSIZE = 10;
	ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BufferUtil.SIZEOF_INT*BUFSIZE);
	private static GLU glu = new GLU();
	
	private final static String AVATAR_FILE = "data/forestry/woodcutter.cfg";
	
	public ForestryView() {
		byteBuffer.order(ByteOrder.nativeOrder());
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);	
		
		//Lights
		lights.get(0).setPosition(new Point3D(0, 180, -100), false);
		
		//Camera
//		camera.setPoints(new Point3D(0, 50, 100), new Point3D(0, 0, 0));
		
		//Textures
		Tree.init(client.getDataModel().getRoundConfiguration().getMaximumResourceAge(), drawable);
		SkyBox.init(drawable, "data/forestry/cubemaps/desert_hazy.png");
		
		GL gl = drawable.getGL();
		setupShaders(gl);
		gl.glBindAttribLocation(conicalFrustumProgramObject, 0, "FrustumValues_attrib");
		gl.glBindAttribLocation(conicalFrustumProgramObject, 1, "FrustumBaseCenter_attrib");
		gl.glBindAttribLocation(conicalFrustumProgramObject, 2, "Ws2OsXformRow1_attrib");
		gl.glBindAttribLocation(conicalFrustumProgramObject, 3, "Ws2OsXformRow2_attrib");
		gl.glBindAttribLocation(conicalFrustumProgramObject, 4, "Ws2OsXformRow3_attrib");
		gl.glBindAttribLocation(conicalFrustumProgramObject, 5, "Ws2OsXformRow4_attrib");
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		super.display(drawable);
		
		GL gl = drawable.getGL();
		
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		
		//displayLight(drawable);
		gl.glPushMatrix();	
			if (rasterizationMode == GL.GL_SELECT) 			
				gl.glLoadName(100);
			
			renderTerrain(drawable);
			
			renderSkyBox(drawable);
			
			gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
	    	gl.glPolygonOffset(-1, -1);
			renderAvatars(drawable);
			renderTrees(drawable);
			gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
					
			if (rasterizationMode == GL.GL_SELECT) 			
				gl.glLoadName(100);
			
			hud.display(drawable);
						
		gl.glPopMatrix();
		gl.glDisable(GL.GL_BLEND);
		
		if (mouseClickedFlag) {
			mouseClickedFlag = false;
			pickAvatars(drawable);			
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		cameraAvatarVector.set(camera.getLookAtPoint(), camera.getPosition());
		Woodcutter avatar = (Woodcutter)avatars.get(getDataModel().getId());
		Point3D avatarPosition = avatar.getPosition();
		
		int keyCode = e.getKeyCode();		
		switch(keyCode) {
		case KeyEvent.VK_W:
			if ( isOutofTerrain(avatar.getForwardPosition()) ) {
				hud.setMessage(ForestryView.OUT_OF_TERRAIN_MSG);
				hud.setVisible(true);				
            }
            else {
                Tree tree = isCollidingWithTrunk(avatarPosition, avatar.getFrontRay(true), avatar.getFrontLeftRay(true), avatar.getFrontRightRay(true));
                if (tree == null) 
                	tree = isCollidingWithBranches(avatarPosition, avatar.getFrontRay(false), avatar.getFrontLeftRay(false), avatar.getFrontRightRay(false));
                if (tree != null) {
                	hud.setMessage(ForestryView.TREE_COLLISION_MSG);
    				hud.setVisible(true);
                    break;
                }
                hud.setVisible(false);
                avatar.forward();
                processAvatarMove(avatar);
            }			
			break;
		case KeyEvent.VK_S:
			if ( isOutofTerrain(avatar.getBackwardPosition()) ) {
				hud.setMessage(ForestryView.OUT_OF_TERRAIN_MSG);
				hud.setVisible(true);
            }
            else {
                Tree tree = isCollidingWithTrunk(avatarPosition, avatar.getBackRay(true), avatar.getBackLeftRay(true), avatar.getBackRightRay(true));
                if (tree == null)
                	tree = isCollidingWithBranches(avatarPosition, avatar.getBackRay(false), avatar.getBackLeftRay(false), avatar.getBackRightRay(false));
                if (tree != null) {
                	hud.setMessage(ForestryView.TREE_COLLISION_MSG);
    				hud.setVisible(true);
    				break;
                }
                hud.setVisible(false);
                avatar.reverse();
                processAvatarMove(avatar);
            }			
			break;
		case KeyEvent.VK_A:
		{
            Tree tree = isCollidingWithTrunk(avatarPosition, avatar.getLeftRay(true), avatar.getFrontLeftRay(true), avatar.getBackLeftRay(true));
            if (tree == null)
            	tree = isCollidingWithBranches(avatarPosition, avatar.getLeftRay(false), avatar.getFrontLeftRay(false), avatar.getBackLeftRay(false));
            if (tree != null) {
            	hud.setMessage(ForestryView.TREE_COLLISION_MSG);
				hud.setVisible(true);
				break;
            }
            hud.setVisible(false);
            avatar.moveLeft();
            processAvatarMove(avatar);
			break;
		}
		case KeyEvent.VK_D:	
		{
            Tree tree = isCollidingWithTrunk(avatarPosition, avatar.getRightRay(true), avatar.getFrontRightRay(true), avatar.getBackRightRay(true));
            if (tree == null)
            	tree = isCollidingWithBranches(avatarPosition, avatar.getRightRay(false), avatar.getFrontRightRay(false), avatar.getBackRightRay(false));
            if (tree != null) {
            	hud.setMessage(ForestryView.TREE_COLLISION_MSG);
				hud.setVisible(true);
				break;
            }
            hud.setVisible(false);
            avatar.moveRight();
            processAvatarMove(avatar);				
			break;
		}
		case KeyEvent.VK_Q:
            requestTreeSelection(avatar);
            break;
        case KeyEvent.VK_E:
			avatar.axe();
			harvestTree(avatar);
			sendAvatarPose(avatar);
			break;
        case KeyEvent.VK_X:
        	harvestFruits(avatar);        	
		}			

		//moveLight(keyCode);
		//useHelperKeys(keyCode);
		update();
	}
	
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
			case KeyEvent.VK_U:
				client.transmit(new ClientPoseUpdate(client.getId(), avatar.getPosition(), 
						avatar.getHeading(), avatar.getAnimationState(), false));				
				break;
			
			}//end switch			
		}
	}
    
    public void initialize() {
        RoundConfiguration configuration = getDataModel().getRoundConfiguration();
        treeGridDimension = new Tuple2i(configuration.getResourceDepth(), configuration.getResourceWidth());
        
        //Create terrain
        float worldWidth = configuration.getWorldWidth();
        float worldDepth = configuration.getWorldDepth();
        float zExtend = worldWidth / 2.0f;
        float xExtend = worldDepth / 2.0f;
        float peakHeight = configuration.getWorldWidth()/15.0f;
        float roughnessConstant = 0.5f;
        int maxIterations = 7;
		String textureFile = "data/forestry/grass2_hazy.png";
		RGBA ambient = new RGBA(0.6f, 0.6f, 0.6f, 1);
		RGBA diffuse = new RGBA(0.6f, 0.6f, 0.6f, 1);
		RGBA specular = new RGBA(1, 1, 1, 1);
		float shininess = 12.8f;
        createTerrain(new Point3D(-xExtend, 0, -zExtend), new Point3D(
                -xExtend, 0, zExtend), new Point3D(xExtend, 0, -zExtend),
                new Point3D(xExtend, 0, zExtend), peakHeight, roughnessConstant, maxIterations, 
                textureFile, ambient, diffuse, specular, shininess);
        //Create sky box
        createSkyBox(xExtend+xExtend/2.0f, terrain.getMinElevation(), zExtend+zExtend/2.0f); //x extend, z extend
        
        //Create trees
        createTrees();
        
        //Create avatars
        Woodcutter avatar;
        synchronized (getDataModel()) {
//        	for (ClientData clientData : getDataModel().getClientDataMap().values()) {
//        		int avatarNo = clientData.getAssignedNumber();
////        		avatar = loadAvatar(AVATARS[avatarNo - 1], clientData.getPosition(), avatarNo );
////        		String avatarFile = clientData.isMale()? AVATAR_FILE_PATH + "maleWoodcutter.cfg": AVATAR_FILE_PATH + "femaleWoodcutter.cfg";
//        		String avatarFile = AVATAR_FILE;
//        		avatar = loadAvatar(avatarFile, clientData.getHairColor(), clientData.getSkinColor(), 
//        				clientData.getShirtColor(), clientData.getTrouserColor(), clientData.getShoesColor(), 
//        				clientData.getPoint3D(), avatarNo);
//        		avatars.put(clientData.getId(), avatar);
//        	}
        }
        avatar = avatars.get(client.getId());
        setCamera(avatar);
        sendAvatarPose(avatar);        
    }
    
    private void setupShaders(GL gl) {
    	String[] fileContents = new String[1];
    	int[] fileLengths = new int[1];

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
    	
    	//////////////// Conical frustum shader /////////////////////////////////////
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
    }
        
	//#################  Terrain functions  #########################
	public void createTerrain(Point3D topLeft, Point3D bottomLeft, Point3D topRight, Point3D bottomRight, 
			float peakHeight, float roughnessConstant, int maxIterations, 
			String textureFile,
			RGBA ambient, RGBA diffuse, RGBA specular, float shininess) {
		
		terrain = new FractalTerrain(topLeft, bottomLeft, topRight, bottomRight);
		terrain.generate(peakHeight, roughnessConstant, maxIterations);
		terrain.setMaterials(ambient, diffuse, specular, shininess);		
		if (!textureFile.equals("")) terrain.setTexture(textureFile);
		
		//Configure the objects		
		terrain.configure();
		
		//updateExtents(terrain);
		repaint();
	}
	
	private void renderTerrain(GLAutoDrawable drawable) {	
		if (terrain != null) {
			//display sky
			drawable.getGL().glClearColor(0.75f, 0.88f, 1.0f, 0.0f ); //sky blue
//			drawable.getGL().glClearColor(1.0f, 1.0f, 1.0f, 1.0f );
			
			terrain.display(drawable);
		}
	}
	
	//################  Tree functions  ############################	
	private void renderTrees(GLAutoDrawable drawable) {
		if (treeGrid == null) return;
				
		GL gl = drawable.getGL();
		int rowIndex, colIndex;
		Tree tree = null;
		
		gl.glEnable(GL.GL_CULL_FACE);
    	gl.glCullFace(GL.GL_BACK);
				
		gl.glUseProgramObjectARB(conicalFrustumProgramObject);

		//Send uniform variables
		//Camera position
		Point3D position = camera.getPosition();
		int uniformLoc = gl.glGetUniformLocationARB(conicalFrustumProgramObject, "CameraPosition_ws");
		gl.glUniform3fARB(uniformLoc, position.x, position.y, position.z);
		
		//light position
		position = lights.get(0).getPosition();
		uniformLoc = gl.glGetUniformLocationARB(conicalFrustumProgramObject, "LightPosition_ws");
		gl.glUniform3fARB(uniformLoc, position.x, position.y, position.z);

		//Enable texture
		gl.glEnable(GL.GL_TEXTURE_2D);
		
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, Tree.ambient, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, Tree.diffuse, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, Tree.specular, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, Tree.shininess);
		
		////////// BRANCHES ////////////////////////
		for (rowIndex = 0; rowIndex < treeGridDimension.a; rowIndex++) {				
			for (colIndex = 0; colIndex < treeGridDimension.b; colIndex++) {
				tree = (Tree) treeGrid.getNode(rowIndex, colIndex);
				if (tree == null) continue;
				tree.displayBranches(drawable);	
			}
		}
		
		////////// FRUITS /////////////////////////
		gl.glUseProgramObjectARB(sphereProgramObject);
		
		position = camera.getPosition();
		uniformLoc = gl.glGetUniformLocationARB(conicalFrustumProgramObject, "CameraPosition_ws");
		gl.glUniform3fARB(uniformLoc, position.x, position.y, position.z);
		
		//light position
		position = lights.get(0).getPosition();
		uniformLoc = gl.glGetUniformLocationARB(conicalFrustumProgramObject, "LightPosition_ws");
		gl.glUniform3fARB(uniformLoc, position.x, position.y, position.z);

		for (rowIndex = 0; rowIndex < treeGridDimension.a; rowIndex++) {				
			for (colIndex = 0; colIndex < treeGridDimension.b; colIndex++) {
				tree = (Tree) treeGrid.getNode(rowIndex, colIndex);
				if (tree == null) continue;
				tree.displayFruits(drawable);	
			}
		}
		gl.glDisable(GL.GL_CULL_FACE);
		gl.glUseProgramObjectARB(0);
		
		////////////// LEAVES ///////////////////
		gl.glUseProgramObjectARB(foliageProgramObject);
		
		Tree.leafTexture.load(drawable.getGL());
		
		for (rowIndex = 0; rowIndex < treeGridDimension.a; rowIndex++) {				
			for (colIndex = 0; colIndex < treeGridDimension.b; colIndex++) {
				tree = (Tree) treeGrid.getNode(rowIndex, colIndex);
				if (tree == null) continue;
					tree.displayLeaves(drawable);	
			}
		}
					
		gl.glDisable(GL.GL_TEXTURE_2D);		
		gl.glUseProgramObjectARB(0);
	
		//renderTreeBoundingBox(drawable);
		
		//######### Debug code #############
//		//Display bounding boxes
//		for (int rowIndex = 0; rowIndex < FOOD_GRID_DIMENSION.b; rowIndex++) {				
//			for (int colIndex = 0; colIndex < FOOD_GRID_DIMENSION.a; colIndex++) {
//				Tree tree = (Tree) treeGrid.getNode(rowIndex, colIndex);
//				tree.getBoundingBox().display(drawable);
//			}
//		}
	}
	
	public void renderTreeBoundingBox(GLAutoDrawable drawable) {
		if (treeGrid == null) return;
		
		GL gl = drawable.getGL();
		int rowIndex, colIndex;
		Tree tree = null;
		
		for (rowIndex = 0; rowIndex < treeGridDimension.a; rowIndex++) {				
			for (colIndex = 0; colIndex < treeGridDimension.b; colIndex++) {
				tree = (Tree) treeGrid.getNode(rowIndex, colIndex);
				if (tree == null) continue;
				tree.trunk.displayBoundingBox(drawable);
			}
		}
	}
	
	public void createTrees() {	
		//Create tree grid made up of tree objects
		if (terrain != null) {
			treeGrid = new Grid();
            treeGrid.addRows((int) treeGridDimension.a, (int) treeGridDimension.b);
			terrain2treeGridRatio = new Tuple2f(terrain.getRows()/treeGridDimension.a, terrain.getColumns()/treeGridDimension.b); 
			treeCellWidth = (terrain.getMaxExtent().x - terrain.getMinExtent().x) / treeGridDimension.a;
			treeCellDepth = (terrain.getMaxExtent().z - terrain.getMinExtent().z) / treeGridDimension.b;
			topLeftCellCenter = terrain.getPoint(0, 0);
			topLeftCellCenter = new Point3D(topLeftCellCenter.x+treeCellWidth/2.0f, 0, topLeftCellCenter.z+treeCellDepth/2.0f);
			int nTrees = 0;
			for (Resource resource : getDataModel().getResourceDistribution().values()) {
				addTree(resource);	
				nTrees++;
			}
		}
		//Added this line as each tree was getting added twice as it is present in 
		//the resourceDistribution as well as addedResources list
		// FIXME: figure out how this is being used.  
//		getDataModel().clearDiffLists(); 
		
		repaint();
	}
	
	@Override
	public void updateResources() {
//        for (Resource resource: getDataModel().getRemovedResources()) {
//            Tree tree = (Tree) treeGrid.getNode(resource.getPosition());
//            if (! tree.harvested) {
//                treeGrid.remove(resource.getPosition());
//            }
//        }
//        for (Resource resource: getDataModel().getAddedResources()) {
//        	addTree(resource);
//        	//System.out.println("A new tree added");
//        }
//        for (Resource resource : getDataModel().getResourceDistribution().values()) {
//            Tree tree = (Tree) treeGrid.getNode(resource.getPosition());
//            if (!tree.isHarvested() && tree.getAge() != resource.getAge())  {
//            	tree.setAge(resource.getAge());            
//            }
//        }
//        getDataModel().clearDiffLists();
    }
	
	private void addTree(Resource resource) {
        Point point = resource.getPosition();        
        Point3D xzCellCenter = new Point3D(topLeftCellCenter).add(new Point3D(point.x*treeCellWidth, -10000, point.y*treeCellDepth));
		
		//Get elevation at this cell center point
		Tuple2i terrainCellIndex = new Tuple2i((int)(point.y * terrain2treeGridRatio.a), (int)(point.x * terrain2treeGridRatio.b));
		Point3D cellCenter = getCellCenter(xzCellCenter, terrainCellIndex, terrain2treeGridRatio);
        
		float angle = (float)Math.random()* 360;
		Tree tree = new Tree(resource, cellCenter, angle, this);  
		treeGrid.setNode(point.y, point.x, tree);		
    }
		
	//#################### Camera - Tree Functions #########################
	private boolean isTreeVisible(Point3D treePosition) {
		Vector3D cameraTreeVector = new Vector3D(treePosition, camera.getPosition());
		
		//Find out angle between cameraParticipantVector and cameraTreeVector
		if (cameraTreeVector.cosXZAngle(cameraAvatarVector) < FOV)
			return false;
		
		return true;
	}
	
	//################## Terrain-Tree Functions  ##########################		
	private Point3D getCellCenter(Point3D point, Tuple2i terrain2foodGridRatio, Tuple2f terrainCellIndex) {
		Ray ray = new Ray(point, new Vector3D(0, 1, 0));
		Point3D foodCellCenter = null;
		
		for (int rowIndex = terrain2foodGridRatio.a; rowIndex < (terrain2foodGridRatio.a+Math.ceil(terrainCellIndex.a)); rowIndex++) {
			for (int colIndex = terrain2foodGridRatio.b; colIndex < (terrain2foodGridRatio.b+Math.ceil(terrainCellIndex.b)); colIndex++) {
				Point3D cellTopLeft = terrain.getPoint(rowIndex, colIndex);
				Triangle t = null;

				//Upper triangle
				t = new Triangle(cellTopLeft, terrain.getPoint(rowIndex+1, colIndex), terrain.getPoint(rowIndex, colIndex+1));
				foodCellCenter = t.getIntersection(ray);
				if (foodCellCenter != null) return foodCellCenter;

				//Lower traingle
				t = new Triangle(terrain.getPoint(rowIndex+1, colIndex), terrain.getPoint(rowIndex+1, colIndex+1), terrain.getPoint(rowIndex, colIndex+1));

				foodCellCenter = t.getIntersection(ray);
				if (foodCellCenter != null) return foodCellCenter;
			}
		}
		return null;
	}
	
	//############### Avatar functions ##################
	public Woodcutter loadAvatar(String filePath, Color hairColor, Color skinColor,  Color shirtColor, Color trouserColor, Color shoesColor, Point3D position, int assignedNo) {	//, String dataPath) {	
		System.out.println("Loading avatar #"+ assignedNo);
		Woodcutter avatar = new Woodcutter(position, 0, 0.5f, this);
		avatar.init(filePath, hairColor, skinColor, shirtColor, trouserColor, shoesColor);
		
		updateAvatarElevation(avatar);
		avatar.setMaterials(new RGBA(1.0f, 0.0f, 1.0f, 0.0f), new RGBA(1.0f, 0.0f, 1.0f, 0.0f), new RGBA(1.0f, 1.0f, 1.0f, 0.0f), 128.0f);
		avatar.setAssignedNumber(assignedNo);
		
		return avatar;
	}
	
	private void renderAvatars(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		
		for (Woodcutter avatar: avatars.values()) {
			if (rasterizationMode == GL.GL_SELECT) {
				//Write a unique name for each woodcutter if we are in the selection mode
				int assignedNo = avatar.getAssignedNumber();
//				System.out.println("Select mode: Assigned no " + assignedNo);
				gl.glLoadName(assignedNo);
			}
			avatar.display(drawable);
        }		
	}
	
	private void processAvatarMove(Woodcutter avatar) {
        avatar.walk();
		updateAvatarElevation(avatar);
		setCamera(avatar);
		sendAvatarPose(avatar);
		resetTreeToHarvest(avatar); 
    }
	
	private void sendAvatarPose(Woodcutter avatar) {
		client.transmit(new ClientPoseUpdate(client.getId(), avatar.getPosition(), 
				avatar.getHeading(), avatar.getAnimationState(), true));		
	}
	
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
		processHits(hits, selectBuffer);		
	}
	
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
			
			//for (nameCounter = 0; nameCounter < noOfNames; nameCounter++) {
				//Don't do anything if
				//1. the terrain or trees are clicked
				//2. the avatar is clicked by himself
				name = selectBuffer.get();
				if ( name == 100 || name == ((Woodcutter)avatars.get(getDataModel().getId())).getAssignedNumber() )
					continue;
			//}
				
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
	
	@Override
	public boolean shouldChat(Identifier clientId1, Identifier clientId2) {
		Point3D avatar1Position = avatars.get(clientId1).getPosition();
		Point3D avatar2Position = avatars.get(clientId2).getPosition();
		float distance = new Vector3D(new Point3D(avatar1Position.x, 0, avatar1Position.z), 
										new Point3D(avatar2Position.x, 0, avatar2Position.z)).length();
		System.out.println("Distance between two avatars is " + distance);
		if (distance <= client.getDataModel().getRoundConfiguration().getChattingRadius()) {
			hud.setVisible(false);
			return true;
		}
		else {
			hud.setMessage(ForestryView.OUT_OF_CHAT_RANGE);
			hud.setVisible(true);
		}
		return false;
	}
    
    private void initiateChat(Identifier id) {
        client.getGameWindow3D().getChatPanel().setTargetHandle(id);
    }
	
	//###############  Avatar-Terrain functions  ########################
	public void updateAvatarElevation(Woodcutter avatar) {
		if (terrain != null) {
			float elevation = terrain.getElevation(avatar.getPosition());
			if (elevation != -10000) {
				avatar.setElevation(elevation);
			}
		}
	}
	
	public boolean isOutofTerrain(Point3D potentialPosition) {
		Point3D minExtent = terrain.getMinExtent();
		Point3D maxExtent = terrain.getMaxExtent();
		
		if (potentialPosition.x < minExtent.x || potentialPosition.x > maxExtent.x || 
			potentialPosition.z < minExtent.z || potentialPosition.z > maxExtent.z)
			return true;
		
		return false;
	}
	
	//################# Avatar-Tree functions ###################
	public Tuple2i getAvatarCellIndex(Point3D avatarPosition) {
	    //Point3D topLeft = ((Tree) treeGrid.getNode(0, 0)).getPosition();
        return new Tuple2i(
                (int)( (avatarPosition.z - topLeftCellCenter.z) / treeCellDepth ),
                Math.round( (avatarPosition.x - topLeftCellCenter.x) / treeCellWidth ) );
	}
	
	public Tree isCollidingWithTrunk(Point3D avatarPosition, Ray ray1, Ray ray2, Ray ray3)
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
                if (tree != null && (
                	tree.isIntersectingTrunk(ray1, 3.5f) 	
                	|| tree.isIntersectingTrunk(ray2, 1.5f) 	
                	|| tree.isIntersectingTrunk(ray3, 1.5f)
				)) {
                	return tree;
                }
            }
        }
	
		return null;
	}

	public Tree isCollidingWithBranches(Point3D avatarPosition, Ray ray1, Ray ray2, Ray ray3)
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
                if (tree != null && (
                	tree.isIntersectingBranches(ray1, 1.5f) 	
                	|| tree.isIntersectingBranches(ray2, 1.5f) 	
                	|| tree.isIntersectingBranches(ray3, 1.5f)
				)) {
                	return tree;
                }
            }
        }
	
		return null;
	}
	
	public Tree getSelectableTree(Woodcutter avatar)
	{			
		Tuple2i avatarCellIndex = getAvatarCellIndex(avatar.getPosition());
		
		// Get trees in that cell and 4 neighboring cells
		//FIXME: Only check the tree in the direction of the avatrar's heading
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
                if (tree != null && (
                		tree.isIntersectingTrunk(avatar.getFrontRay(true), 5.0f) || 
                		tree.isIntersectingTrunk(avatar.getFrontLeftRay(true), 5.0f) ||
                		tree.isIntersectingTrunk(avatar.getFrontRightRay(true), 5.0f)) ) {
                	return tree;
                }
            }
        }
	
		return null;
	}
	
	public void harvestTree(Woodcutter avatar) {    	
//		Ray axeRay = avatar.getAxeRay();		
		if (selectedTree != null && !selectedTree.isHarvested() && avatar.isNewHit()) {
			boolean harvested = selectedTree.hit();
			if (harvested) {
				hud.setMessage(ForestryView.TREE_HARVESTED_MSG);
				hud.setVisible(true);
				client.transmit(new HarvestResourceRequest(client.getId(), selectedTree.getResource()));
//				System.out.println("Sent the HarvestResourceRequest");
			}
			else {			
				hud.setMessage(ForestryView.TREE_HIT_MSG + HUD.getNoToString(selectedTree.getHitCounter()));
				hud.setVisible(true);
			}
		}
//		else {
//			hud.setVisible(false);
//		}
		return;
	}
	
	public void harvestFruits(Woodcutter avatar) {
		if (fruitTimer == null && selectedTree != null && !selectedTree.isHarvested() && selectedTree.hasFruits()) {
	        long duration = 12000L;
	        int interval = 350;
	        int steps = (int)duration / interval - 1;

	        //Find the terrain height below each fruit
	        //and use to decide ht translation step
	        for (int fruitIndex = 0; fruitIndex < selectedTree.noOfFruits(); fruitIndex++) {
	        	//Get the location of a fruit
	        	Point3D fruitLocation = selectedTree.getFruitLocation(fruitIndex);
	        	//Get elevation of terrain below this fruit
	        	float terrainElevation = terrain.getElevation(fruitLocation);
	        	//calculate translation in the fruit elevation accordingly 
	        	selectedTree.calculateFruitElevationStep(fruitIndex, terrainElevation, steps);
	        }
	        fruitTimer = createFruitFallTimer(avatar, selectedTree, duration, interval);
	        fruitTimer.start();
	        hud.setMessage(ForestryView.FRUIT_HARVESTED_MSG);
			hud.setVisible(true);
		}
		return;
	}
	
	private void requestTreeSelection(Woodcutter avatar) {
		//Select a tree if no other tree is selected
        if (selectedTree == null) {
	        //Get tree in front of the avatar
	        Tree tree = getSelectableTree(avatar);
	        if (tree != null) {        	        	
	        	client.transmit(new LockResourceRequest(client.getId(), tree.getResource()));
//	        	System.out.println("Sent a LockResourceRequest to lock the tree " + tree);
	        }
        }
    }
	
	public void highlightResource(Resource resource) {
        selectedTree = (Tree) treeGrid.getNode(resource.getPosition());
        selectedTree.setSelected(true);
        hud.setMessage(ForestryView.TREE_SELECTED_MSG);
        hud.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                update();       
            }
        });
    }
	
	private void deselectTree() {
        if (selectedTree != null) {
        	client.transmit(new UnlockResourceRequest(client.getId(), selectedTree.getResource()));
        	selectedTree.setSelected(false);
        }
    }
	
	protected void resetTreeToHarvest(Woodcutter avatar) {
        if (timer != null) {
            timer.stop();
        }
        
        //Deselect the tree if fruits are finished falling down
        if (fruitTimer == null && selectedTree != null) {
        	deselectTree();
        	selectedTree = null;
        }
    }
	
	//#################### Timers ####################################
	private TreeTimer timer;
    
    public void flashResource(Resource resource) {
        Tree tree = (Tree) treeGrid.getNode(resource.getPosition());
        hud.setMessage(ForestryView.TREE_SELECTED_BY_OTHERS_MSG);
        hud.setVisible(true);
        if (timer != null) {
            timer.stop();
        }
        timer = createLockFlashTimer(tree);
        timer.start();
    }
    
    private static class TreeTimer extends Timer {
        private final Tree tree;
        TreeTimer(Tree tree, int delay, ActionListener listener) {
            super(delay, listener);
            this.tree = tree;
        }
        public void stop() {
            super.stop();
            tree.setSelectedByOthers(false);
        }
    }
    
    private TreeTimer createLockFlashTimer(final Tree tree) {
        final long endTime = System.currentTimeMillis() + 3000L;
        return new TreeTimer(tree, 500, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (endTime - System.currentTimeMillis() < 0) {
                    timer.stop();
                    timer = null;
                }
                else {
                    tree.toggleSelectedByOthers();
                }
            }
        });
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
        }
    }
    
    private FruitTimer createFruitFallTimer(final Woodcutter avatar, final Tree tree, long duration, int interval) {
        final long endTime = System.currentTimeMillis() + duration;
        return new FruitTimer(tree, interval, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	long timeLeft = endTime - System.currentTimeMillis();
                if (timeLeft < 0) {
                    fruitTimer.stop();
                    fruitTimer = null;                    
                    resetTreeToHarvest(avatar);	//Deselect tree after fruits are harvested
                }
                else {
                    tree.updateFruitValues(timeLeft);                	
                }
            }
        });
    }	
	
	//####################  Camera related functions ######################
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
	
	public void centerChildDialog(JFrame childFrame) {
		Dimension dimension = getSize();
		Dimension childDimension = childFrame.getSize();
		int x = (dimension.width - childDimension.width) / 2;
		int y = (dimension.height - childDimension.height) / 2;
		childFrame.setLocation(x, y);		
	}
    
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

	//####################### sky box related functions ######################
    private void createSkyBox(float x, float y, float z) {
    	skyBox = new SkyBox();
    	float h = x/2.0f;
    	skyBox.setBLB(-x, y, -z);
    	skyBox.setBLF(-x, y, z);
    	skyBox.setBRF(x, y, z);
    	skyBox.setBRB(x, y, -z);
    	skyBox.setTLB(-x, h, -z);
    	skyBox.setTLF(-x, h, z);
    	skyBox.setTRF(x, h, z);
    	skyBox.setTRB(x, h, -z);
    }
    
    private void renderSkyBox(GLAutoDrawable drawable) {
    	if (skyBox != null) {
    		skyBox.display(drawable);
    	}    		
    }
    
    
    //##################### Debug code ###################################
	
//	private void moveLight(int keyCode) { 
//	PointLight light = lights.get(0);
//	switch(keyCode) {			
//		case KeyEvent.VK_LEFT:
//			light.rotateLeft();
//			System.out.println("Light position " + light.getPosition());
//			break;
//		case KeyEvent.VK_RIGHT:
//			light.rotateRight();
//			System.out.println("Light position " + light.getPosition());
//			break;
//		case KeyEvent.VK_UP:
//			light.moveUp();
//			System.out.println("Light position " + light.getPosition());
//			break;
//		case KeyEvent.VK_DOWN:
//			light.moveDown();
//			System.out.println("Light position " + light.getPosition());
//			break;
//		case KeyEvent.VK_P:
//			light.moveAway();
//			System.out.println("Light position " + light.getPosition());
//			break;
//		case KeyEvent.VK_L:
//			light.moveNear();
//			System.out.println("Light position " + light.getPosition());
//			break;
//	}
	
//	private void useHelperKeys(int keyCode) {
	//	switch(keyCode) {
	//	case KeyEvent.VK_R:
	//		new ForestryFileSelection(this);				
	//		break;
	//	case KeyEvent.VK_I:
	//		new TerrainGenerationDialog(this);				
	//		break;
	//	case KeyEvent.VK_E:
	//	{
	//		//new TreeGenerationDialog(this);
	//		Tree.maxAge = 6;
	//		String dataFilePath = System.getProperty("user.dir") + File.separator + "data" + File.separator + "forestry";
	//		//Also load coin texture
	//		TextureLoader texLoader = new TextureLoader();
	//		String coinTextureFile = dataFilePath + File.separator + "coins.jpg";
	//		Tree.coinTexture = texLoader.getTexture(coinTextureFile, true);
	//							
	//		createTrees();				
	//		
	//		break;
	//	}
	//	case KeyEvent.VK_G:	
	//		if (treeGrid != null) {					
	//			//Increment tree age 
	//			for (int rowIndex = 0; rowIndex < resourceGridDimension.a; rowIndex++) {				
	//				for (int colIndex = 0; colIndex < resourceGridDimension.b; colIndex++) {
	//					Tree tree = (Tree) treeGrid.getNode(rowIndex, colIndex);
	//					if (tree != null && !tree.isHarvested())
	//						tree.updateAge(1);
	//				}
	//			}
	//		}
	//		break;
	//	case KeyEvent.VK_X:
	//		terrain = null;
	//		treeGrid = null;
	//		woodcutters.clear();
	//		break;
	//	case KeyEvent.VK_C:
	//		woodcutters.clear();
	//		break;
	//	case KeyEvent.VK_P:
	//		loadWoodcutter(System.getProperty("user.dir") + File.separator + "data" + File.separator + "forestry" + File.separator + "paladin.cfg");				
	//		break;	
	//	case KeyEvent.VK_S:
	//		loadWoodcutter(System.getProperty("user.dir") + File.separator + "data" + File.separator + "forestry" + File.separator + "skeleton.cfg");				
	//		break;	
	//	case KeyEvent.VK_B:
	//		loadWoodcutter(System.getProperty("user.dir") + File.separator + "data" + File.separator + "forestry" + File.separator + "woodcutter.cfg", new Point3D(0, 0, 0) );
	//		break;	
	//}
	
	public void displayLight(GLAutoDrawable drawable)
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

	public int getProgramObject() {
		return conicalFrustumProgramObject;
	}
}
