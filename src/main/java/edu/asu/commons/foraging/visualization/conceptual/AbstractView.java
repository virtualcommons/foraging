package edu.asu.commons.foraging.visualization.conceptual;

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
import edu.asu.commons.foraging.graphics.Grid;
import edu.asu.commons.foraging.graphics.HUD;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.PointLight;
import edu.asu.commons.foraging.graphics.RGBA;
import edu.asu.commons.foraging.graphics.TextureLoader;
import edu.asu.commons.foraging.graphics.Triangulation;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.foraging.util.Tuple2i;
import edu.asu.commons.foraging.util.Tuple3i;
import edu.asu.commons.foraging.visualization.GameView3d;
import edu.asu.commons.net.Identifier;


/**
 * 
 * $Id:AbstractView.java 129 2008-01-04 21:47:44Z deepali $
 * 
 * @author Deepali Bhagvat
 * @version $Revision:129 $
 */

public class AbstractView extends GameView3d {

    private static final long serialVersionUID = 8680837618723784369L;

    protected Triangulation basePlane = null;

    protected Grid foodGrid = null;

    private Tuple2i resourceGridDimension; //(rows, cols)
    
    private Point3D topLeftCellCenter;

    protected Map<Identifier, Agent> agents = new HashMap<Identifier, Agent>();
    
    protected static int AGENT_CAMERA_DISTANCE = 20;
    
    protected static float AGENT_CAMERA_HEIGHT = 3;

    protected Vector3D cameraAgentVector = null;

    protected static float AGENT_FOOD_DISTANCE = 1.0f;

    protected Food selectedFood = null;
    
    /**
	 * Flag specifying if the client is waiting to receive a response on the select request from the server  
	 */
	private boolean waitingForSelectRequest = false; 
	    
    // Variables related to harvesting
    protected Point3D foodHitXtremePosition1 = null;

    protected Point3D foodHitXtremePosition2 = null;

    protected Point3D foodHitIncrement = null;

    private float foodCellWidth;

    private float foodCellDepth;
    
  //Heads-up display
	private HUD hud = new HUD();
	private static final String NAV_INFO_MSG = "You can navigate by pressing down 'W', 'A', 'S', 'D' or the arrow keys.";
	private static final String OUT_OF_BOUNDARY_MSG = "You cannot go outside the plane.";
	private static final String PILLAR_COLLISION_MSG = "You can select the pillar by pressing 'Q'.";
	private static final String CHAT_START_MSG = "Started chat with the selected agent.";
	private static final String OUT_OF_CHAT_RANGE = "The selected agent is out of the chatting range.";
	private static final String PILLAR_HIT_MSG = "You hit the pillar ";
	private static final String PILLAR_HARVESTED_MSG = "You harvested the pillar.";
	private static final String PILLAR_TOP_HARVESTING_MSG = "You are harvesting the upper part of the pillar.";
	private static final String PILLAR_SELECTED_MSG = " Harvest the pillar by pressing 'E'.";
	private static final String PILLAR_SELECTED_BY_OTHERS_MSG = "The pillar is locked by another agent.";
	private static final String PILLAR_SELECTION_WAIT = "Checking if you can select the pillar.";
	private static final String SELECT_PILLAR_MSG = "Select the pillar by moving closer and pressing 'Q'.";

	//Chatting
	private int BUFSIZE = 10;
	private ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BufferUtil.SIZEOF_INT*BUFSIZE);
	private static GLU glu = new GLU();
	
    // Default Constructor
    public AbstractView() {
        // camera
        //camera.setPosition(new Point3D(0, 20, -75));
        cameraAgentVector = new Vector3D(camera.getLookAtPoint(), camera.getPosition());
        byteBuffer.order(ByteOrder.nativeOrder());
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        super.init(drawable);

        // light
        PointLight pointLight0 = lights.elementAt(0);
        pointLight0.setPosition(new Point3D(0, 100, 100), false);
        pointLight0.setAmbient(0.5f, 0.5f, 0.5f, 1.0f);
        pointLight0.setDiffuse(0.5f, 0.5f, 0.5f, 1.0f);
        pointLight0.setSpeculart(1.0f, 1.0f, 1.0f, 1.0f);
        pointLight0.init(drawable.getGL());
        
        hud.setMessage(NAV_INFO_MSG);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        super.display(drawable);

        GL gl = drawable.getGL();
        
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glCullFace(GL.GL_BACK);         
        displayAgents(drawable); // Agent
        if (rasterizationMode == GL.GL_SELECT) 			
			gl.glLoadName(100);
        
        //gl.glEnable(GL.GL_POLYGON_OFFSET_FILL); // Remove antialiasing
        //gl.glPolygonOffset(1, -1);
        
        displayBasePlane(drawable); // Base plane        
        displayFood(drawable); // Food
        
        //gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
        gl.glDisable(GL.GL_CULL_FACE);
        
        displayHud(drawable);
        
        if (mouseClickedFlag) {
			mouseClickedFlag = false;
			pickAgents(drawable);			
		}
    }

    @Override
    public void keyPressed(KeyEvent e) {
//    	Do not permit any actions when fruits are being harvested
		if (upperPartTimer != null)
			return;
		
        //super.keyPressed(e);
        cameraAgentVector = new Vector3D(camera.getLookAtPoint(), camera.getPosition());
        Agent agent = agents.get(getDataModel().getId());
        Point3D agentPosition = agent.getPosition();
        Point3D agentPotentialPosition;
        
        int keyCode = e.getKeyCode();        
        // Respond to agent actions           
        switch (keyCode) {
        case KeyEvent.VK_UP:
        case KeyEvent.VK_W:
        	agentPotentialPosition = agent.getForwardPosition();
            if ( isOutofBasePlane(agentPotentialPosition) ) {
            	hud.setMessage(AbstractView.OUT_OF_BOUNDARY_MSG);
            }
            else {            	
                Food food = isCollidingWithFood(agentPotentialPosition);
                if (food != null) {
                	if (!food.isHarvested()) hud.setMessage(AbstractView.PILLAR_COLLISION_MSG);
                    break;
                }
                // Move agent forward
                agent.forward();
                processAgentMove(agent);
                hud.setMessage(NAV_INFO_MSG);
            }
            break;
        case KeyEvent.VK_DOWN:
        case KeyEvent.VK_S: 
        	agentPotentialPosition = agent.getBackwardPosition();
        	if ( isOutofBasePlane( agentPotentialPosition) ) {
            	hud.setMessage(AbstractView.OUT_OF_BOUNDARY_MSG);
            }
            else {
	            Food food = isCollidingWithFood(agentPotentialPosition);
	            if (food != null) {
	            	if (!food.isHarvested()) hud.setMessage(AbstractView.PILLAR_COLLISION_MSG);
	                break;
	            }
	            // Move agent backward
	            agent.reverse();
	            processAgentMove(agent);
	            hud.setMessage(NAV_INFO_MSG);            
            }
            break;

        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_A:            
        {
        	/*Food food = isCollidingWithFood(agentPosition, agent.getLeftRay(), agent.getFrontLeftRay(), agent.getBackRightRay());
            if (food != null) {
                hud.setMessage(AbstractView.PILLAR_COLLISION_MSG);
                break;
            }*/
            // Move agent left
            agent.moveLeft();
            processAgentMove(agent);
            hud.setMessage(NAV_INFO_MSG);            
            break;
        }
        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_D:
        {
            /*Food food = isCollidingWithFood(agentPosition, agent.getRightRay(), agent.getFrontRightRay(), agent.getBackRightRay());
            if (food != null) {
                hud.setMessage(AbstractView.PILLAR_COLLISION_MSG);
                break;
            }*/
            // Move agent right
            agent.moveRight();
            processAgentMove(agent);
            hud.setMessage(NAV_INFO_MSG);            
            break;
        }
        case KeyEvent.VK_Q:
            //selectFoodToHarvest(agent);
//        	System.out.println("Q key pressed.");
        	requestFoodSelection(agent);
            break;
        case KeyEvent.VK_E:
            // Harvest food
//        	System.out.println("E key pressed.");
            harvestFood(agent);
            break;
        case KeyEvent.VK_X:
//        	System.out.println("X key pressed.");
        	harvestUpperFoodPart(agent);
        	break;
        }        
        
        /*//For debugging
        switch (keyCode) {
        case KeyEvent.VK_I:
            // Create the base plane
            new BasePlaneGenDialog(this);
            break;

        case KeyEvent.VK_E:
            // Create pillars as food
            new FoodGenerationDialog(this);
            break;

//        case KeyEvent.VK_B:
            // Create agent
//            createAgent();
//            break;

        case KeyEvent.VK_G:
            // Grow food
            growFood();
            break;

        case KeyEvent.VK_X:
            // Clear everything
            basePlane = null;
            foodGrid = null;
            break;

        }*/

        update();
    }
    
    private void processAgentMove(Agent agent) {
        client.transmit(new ClientPoseUpdate(client.getId(), agent.getPosition()));
        setCamera(agent);
        resetFoodToHarvest();
        waitingForSelectRequest = false;
    }

    public void centerChildDialog(JFrame childFrame) {
        Dimension dimension = getSize();
        Dimension childDimension = childFrame.getSize();
        int x = (dimension.width - childDimension.width) / 2;
        int y = (dimension.height - childDimension.height) / 2;
        childFrame.setLocation(x, y);
    }

    public void initialize() {
        RoundConfiguration configuration = getDataModel().getRoundConfiguration();
        resourceGridDimension = new Tuple2i(configuration.getResourceDepth(),
                configuration.getResourceWidth());
        //System.out.println("Resource width = " + resourceGridDimension.a + " depth = " + resourceGridDimension.b);
        float zExtend = configuration.getWorldWidth() / 2.0f;
        float xExtend = configuration.getWorldDepth() / 2.0f;
        //System.out.println("xExtend = " + xExtend);
        //System.out.println("zExtend = " + zExtend);        
        createBasePlane(new Point3D(-xExtend, 0, -zExtend), new Point3D(
                -xExtend, 0, zExtend), new Point3D(xExtend, 0, -zExtend),
                new Point3D(xExtend, 0, zExtend), new RGBA(1.0f, 0.75f, 0.75f,
                        1.0f), new RGBA(1.0f, 0.75f, 0.75f, 1.0f), new RGBA(
                        1.0f, 1.0f, 1.0f, 1.0f), 12.8f);
        initializeFoodAppearance(configuration);
        generateResources();
        createAgents();        
    }

    private void initializeFoodAppearance(RoundConfiguration configuration) {
        Food.radius = 5.0f; //8.0f
        Food.initialHeight = 0.0f;
        Food.heightIncrement = 4.0f; //6.0f
        Food.maxAge = configuration.getMaximumResourceAge();
        // FIXME: make this configurable in the future?
        Food.oldAge = Food.maxAge - 1;
        Food.color = new RGBA(0.5f, 0.0f, 1.0f, 1.0f);
        Food.oldAgeColor = new RGBA(1.0f, 1.0f, 0.0f, 1.0f);
        Food.selectedColor = new RGBA(0.0f, 1.0f, 0.0f, 1.0f);
        Food.selectedByOthersColor = new RGBA(1.0f, 0.0f, 0.0f, 1.0f);
        Food.specular = new RGBA(1.0f, 1.0f, 1.0f, 1.0f);
        Food.shininess = 32.0f;
        TextureLoader texLoader = new TextureLoader();
//        Food.coinTexture = texLoader.getTexture("data/abstract/coins.jpg", true);
//        if (Food.coinTexture == null) {
//            System.err.println("UH OH!");
//        }
    }
    


    private void addResource(Resource resource) {
        Point point = resource.getPosition();
        Point3D cellCenter = topLeftCellCenter.add(new Point3D(point.x
                * foodCellWidth, 0, point.y * foodCellDepth));
        Food food = new Food(cellCenter, resource, this);
        foodGrid.setNode(point.y, point.x, food);
    }
    
    private void generateResources() {
        foodGrid = new Grid();
        foodGrid.addRows(resourceGridDimension.a, resourceGridDimension.b);
        foodCellWidth = (basePlane.getMaxExtent().x - basePlane.getMinExtent().x) / resourceGridDimension.b;
        foodCellDepth = (basePlane.getMaxExtent().z - basePlane.getMinExtent().z) / resourceGridDimension.a;
        topLeftCellCenter = basePlane.getVertex(0).add(new Point3D(foodCellWidth / 2.0f, 0, foodCellDepth / 2.0f));
        for (Resource resource : getDataModel().getResourceDistribution().values()) {
            addResource(resource);
        }
    }

    // ######### Base Plane related functions ###################
    public void createBasePlane(Point3D topLeft, Point3D bottomLeft,
            Point3D topRight, Point3D bottomRight, RGBA ambient, RGBA diffuse,
            RGBA specular, float shininess) {

        basePlane = new Triangulation();

        // Add vertices
        basePlane.addVertex(topLeft);
        basePlane.addVertex(bottomLeft);
        basePlane.addVertex(topRight);
        basePlane.addVertex(bottomRight);

        // Map vertices to triangles
        basePlane.addFace(new Tuple3i(1, 2, 3));
        basePlane.addFace(new Tuple3i(3, 2, 4));

        // Set material colors
        basePlane.setAmbient(ambient);
        basePlane.setDiffuse(diffuse);
        basePlane.setSpecular(specular);
        basePlane.setShininess(shininess);

        basePlane.configure();
    }

    protected void displayBasePlane(GLAutoDrawable drawable) {
        if (basePlane != null) {
        	basePlane.applyMaterial(drawable.getGL());
            basePlane.display(drawable);
        }
    }

    public void saveBasePlane(String saveFilePath) {
        // TODO Auto-generated method stub

    }

    // ############## Food related functions ###########
    public void createFood() {
//        // Create food grid and add food to every cell of the grid
//        foodGrid = new Grid();
//        foodGrid.addRows(resourceGridDimension.b, resourceGridDimension.a);
//        float foodCellWidth = (basePlane.getMaxExtent().x - basePlane.getMinExtent().x) / resourceGridDimension.a;
//        float foodCellDepth = (basePlane.getMaxExtent().z - basePlane.getMinExtent().z) / resourceGridDimension.b;
//
//        Point3D topLeftCellCenter = basePlane.getVertex(0).add(
//                new Point3D(foodCellWidth / 2.0f, 0, foodCellDepth / 2.0f));
//        for (int foodRowIndex = 0; foodRowIndex < resourceGridDimension.b; foodRowIndex++) {
//            for (int foodColIndex = 0; foodColIndex < resourceGridDimension.a; foodColIndex++) {
//                Point3D cellCenter = topLeftCellCenter.add(new Point3D(
//                        foodColIndex * foodCellWidth, 0, foodRowIndex
//                        * foodCellDepth));
//                int age = (int) Math.ceil(Math.random() * 3);
//                Food food = new Food(cellCenter, new Resource(foodColIndex, foodRowIndex, age), this);
//                foodGrid.insertNode(foodRowIndex, foodColIndex, food);
//            }
//        }
//
//        repaint();
    }

    protected void displayFood(GLAutoDrawable drawable) {
        if (foodGrid != null) {
//            if (basePlane == null) {
//                ((Food) foodGrid.getNode(0, 0)).display(drawable);
//            } else {
                for (int foodRowIndex = 0; foodRowIndex < resourceGridDimension.b; foodRowIndex++) {
                    for (int foodColIndex = 0; foodColIndex < resourceGridDimension.a; foodColIndex++) {
                        Food food = (Food) foodGrid.getNode(foodRowIndex, foodColIndex);
                        if (food != null) food.display(drawable);
                    }
                }
            //}
        }
    }

//    protected void growFood() {
//        if (foodGrid != null) {
//            if (basePlane == null) {
//                ((Food) foodGrid.getNode(0, 0)).updateAge(1);
//            } else {
//                for (int foodRowIndex = 0; foodRowIndex < resourceGridDimension.b; foodRowIndex++) {
//                    for (int foodColIndex = 0; foodColIndex < resourceGridDimension.a; foodColIndex++) {
//                        ((Food) foodGrid.getNode(foodRowIndex, foodColIndex)).updateAge(1);
//                    }
//                }
//            }
//        }
//    }

    public void saveFood(String saveFile) {
        // TODO Auto-generated method stub
    }
    
    //################ Agent-Base plane related methods #############
    boolean isOutofBasePlane(Point3D potentialPosition) {
    	Point3D minExtent = basePlane.getMinExtent();
		Point3D maxExtent = basePlane.getMaxExtent();
		
		if (potentialPosition.x < minExtent.x || potentialPosition.x > maxExtent.x || 
			potentialPosition.z < minExtent.z || potentialPosition.z > maxExtent.z)
			return true;
		
		return false;
    }

    // ################ Agent related functions #################
//    protected void createAgent() {
//
//
//        String textureFile = "data/abstract/brown_waves.jpg";
//        Agent agent = new Agent(new Point3D(0, 0, 0), 0, new RGBA(1, 0, 0, 1),
//                textureFile);
//        agents.add(agent);
//    }
    
    private void createAgents() {
        Agent.radius = 3.0f;
        Agent.speed = 2.0f;
        Agent.specular = new RGBA(0.15f, 0.15f, 0.15f, 1.0f);

        Agent agent;
        synchronized (getDataModel()) {
            for (ClientData clientData : getDataModel().getClientDataMap().values()) {
                //String textureFile = TEXTURES[clientData.getAssignedNumber() - 1];
            	Color color = clientData.getSkinColor();
            	agent = new Agent(clientData.getPoint3D(), 0, new RGBA(color), Agent.textureFile, clientData.getAssignedNumber(), this);
                // this client is always the first agent in the list.
                agents.put(clientData.getId(), agent);
            }
        }
        
        //Set camera
        agent = agents.get(client.getId());
        setCamera(agent);
    }

    protected void displayAgents(GLAutoDrawable drawable) {
    	GL gl = drawable.getGL();
        for (Agent agent: agents.values()) {
            if (rasterizationMode == GL.GL_SELECT) {
				//Write a unique name for each woodcutter if we are in the selection mode
				int assignedNo = agent.getAssignedNumber();
				gl.glLoadName(assignedNo);
				//System.out.println("Agent assigned # = " + assignedNo);
			}
            agent.display(drawable);            
        }
    }

    //Returns (rowIndex, colIndex)
    protected Tuple2i getAgentCellIndex(Point3D agentPosition) {
        //Point3D topLeft = ((Food) foodGrid.getNode(0, 0)).getPosition();
        return new Tuple2i(
                (int)( (agentPosition.z - topLeftCellCenter.z) / foodCellDepth ),
                Math.round( (agentPosition.x - topLeftCellCenter.x) / foodCellWidth ) );
    }

    // ################ Agent-Food related functions #################
    protected Food isCollidingWithFood(Point3D agentPosition) {
    	Tuple2i avatarCellIndex = getAgentCellIndex(agentPosition);
		
		// Get food in that cell and 4 neighboring cells
		//FIXME: Only check the tree in the direction of the avatar's heading
        int minRowIndex = avatarCellIndex.a - 1;
        if (minRowIndex < 0)
            minRowIndex = 0;
        int minColIndex = avatarCellIndex.b - 1;
        if (minColIndex < 0)
            minColIndex = 0;
        int maxRowIndex = avatarCellIndex.a + 2;
        if (foodGrid.getRows() < maxRowIndex)
            maxRowIndex = foodGrid.getRows();
        int maxColIndex =avatarCellIndex.b + 2;
        if (foodGrid.getColumns() < maxColIndex)
            maxColIndex = foodGrid.getColumns();

        for (int rowIndex = minRowIndex; rowIndex < maxRowIndex; rowIndex++) {
            for (int colIndex = minColIndex; colIndex < maxColIndex; colIndex++) {
                Food food = (Food) foodGrid.getNode(rowIndex, colIndex);
                if (food != null && !food.isHarvested() && food.isIntersecting(agentPosition, Agent.radius)) {
                	return food;
                }
            }
        }
	
		return null;
    }
    
    /*protected void selectFoodToHarvest(Agent agent) {
        Point3D agentPosition = agent.getPosition();
        //Point3D agentPosition = new Point3D(agentCenter.x, basePlane.getMinExtent().y, agentCenter.z);
        Tuple2i agentCellIndex = getAgentCellIndex(agentPosition);
        //System.out.println("Agent cell index = (" + agentCellIndex.a + ", " + agentCellIndex.b + ")");
        //System.out.println("position = " + agentPosition);
        if (selectableFood == null) {
            selectableFood = new Vector<Food>();
            //Get food in the 2 neighboring cells            
            int colIndex = agentCellIndex.b;
            colIndex = (colIndex < 0)? 0: ( (colIndex < foodGrid.getColumns())? colIndex : foodGrid.getColumns()-1 );
            for (int potRowIndex = agentCellIndex.a+1; potRowIndex > agentCellIndex.a - 1; potRowIndex--) {
            	int rowIndex = (potRowIndex < 0)? 0 : ( (potRowIndex < foodGrid.getRows())? potRowIndex : foodGrid.getRows()-1 );
            	//System.out.println("Neighbor cell index = (" + rowIndex + " ," + colIndex + ")");
                Food food = (Food) foodGrid.getNode(rowIndex, colIndex);
                if (food != null) {
                    selectableFood.add(food);
                }
            }
            
            selectedFoodIndex = 0;
        }
        else {
            //Deselect the previously selected food
            deselect(selectableFood.get(selectedFoodIndex));
//          selectableFood.get(selectedFoodIndex).setSelected(false);

            //select next food item from the surrounding food
            selectedFoodIndex++;
            if (selectedFoodIndex == selectableFood.size()) {
                selectedFoodIndex = 0;
            }
        }
        client.transmit(new LockResourceRequest(client.getId(), 
                selectableFood.get(selectedFoodIndex).getResource()));
//        selectableFood.get(selectedFoodIndex).setSelected(true);
    }*/
    
    private synchronized void requestFoodSelection(Agent agent) {
    	//If we have already sent the request to select this tree, wait till the server responds; do not send another request
  		if (waitingForSelectRequest == true) {
  			hud.setMessage(PILLAR_SELECTION_WAIT);
  			return;
  		}
  		
        if (selectedFood == null) {
	        Food food = getSelectableFood(agent);
	        if (food != null) {        	       
	        	System.out.println("Sending LockResourceRequest " + food.getResource());
	        	client.transmit(new LockResourceRequest(client.getId(), food.getResource()));
	        	System.out.println("LockResourceRequest successfully sent.");
	        	waitingForSelectRequest = true;
	        }
	        else
	        {
	        	System.out.println("Food is null. Cannot be selected.");
	        }
        }
        else
        {
        	System.out.println("selectedFood is not null. Is any other pillar selected?");
        }
    }
    
    private Food getSelectableFood(Agent agent) {
    	Point3D agentPosition = agent.getPosition();
    	Tuple2i agentCellIndex = getAgentCellIndex(agentPosition);
		
		// Get trees in that cell and 4 neighboring cells
		//FIXME: Only check the tree in the direction of the avatrar's heading
        int minRowIndex = agentCellIndex.a - 1;
        if (minRowIndex < 0)
            minRowIndex = 0;
        int minColIndex = agentCellIndex.b - 1;
        if (minColIndex < 0)
            minColIndex = 0;
        int maxRowIndex = agentCellIndex.a + 2;
        if (foodGrid.getRows() < maxRowIndex)
            maxRowIndex = foodGrid.getRows();
        int maxColIndex =agentCellIndex.b + 2;
        if (foodGrid.getColumns() < maxColIndex)
            maxColIndex = foodGrid.getColumns();

        for (int rowIndex = minRowIndex; rowIndex < maxRowIndex; rowIndex++) {
            for (int colIndex = minColIndex; colIndex < maxColIndex; colIndex++) {
                Food food = (Food) foodGrid.getNode(rowIndex, colIndex);
                if (food != null && !food.isHarvested() && food.isCloseBy(agentPosition, Agent.radius, 5) ) {
                	return food;
                }
            }
        }
	
		return null;
    }    
    
    public void highlightResource(Resource resource) {
    	//Highlight the resource only if we are waiting for the server to respond to our select request
		if (waitingForSelectRequest) {
	    	waitingForSelectRequest = false;
	    	if (selectedFood == null || ! selectedFood.getResource().getPosition().equals(resource.getPosition())) {
		        selectedFood = (Food) foodGrid.getNode(resource.getPosition());
		        System.out.println("Received response on our LockResourceRequest for pillar " + selectedFood);
		        if (selectedFood == null) return;
		        selectedFood.setSelected(true);
		        System.out.println(selectedFood + " is selected. Should be displayed in green.");
				        
		        //Update hud
		        int age = selectedFood.getResource().getAge();
		        String message = "Age: " + age + AbstractView.PILLAR_SELECTED_MSG;
		        if (age == 10)
		        	message += " or upper part by pressing 'X'";
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
    
    protected void resetFoodToHarvest() {
        if (timer != null) {
            timer.stop();
        }      
        resetFoodSelection();
        foodHitIncrement = null;
    }

      private void resetFoodSelection() {  		
          if (selectedFood == null) return;
          System.out.println("Deselecting a previously selected pillar");
          System.out.println("Sending UnlockResourceRequest to server for pillar " + selectedFood.getResource());      	
          client.transmit(new UnlockResourceRequest(client.getId(), selectedFood.getResource()));
          selectedFood.setSelected(false);
          System.out.println("Unselecting the pillar.");
          selectedFood = null;
      }      

      private FoodTimer timer;
    
    public void flashResource(Resource resource) {
    	//Reset the previously selected tree
    	selectedFood = null;
    	
    	//Flash the tree only if we are waiting for the server to respond to our select request
    	if (waitingForSelectRequest) {
    		waitingForSelectRequest = false;
	        Food food = (Food) foodGrid.getNode(resource.getPosition());
	        System.out.println("Flashing the pillar " + food + " The server should not lock this resource");
	        if (food == null) return;
	        hud.setMessage(AbstractView.PILLAR_SELECTED_BY_OTHERS_MSG);
	        if (timer != null) {
	            timer.stop();
	        }
	        timer = createLockFlashTimer(food);
	        timer.start();
    	}
    	else
    	{
    		System.out.println("We are not waiting for select request. Why are we here in flashResource?");
    	}
    }
    
    private static class FoodTimer extends Timer {
        private final Food food;
        FoodTimer(Food food, int delay, ActionListener listener) {
            super(delay, listener);
            this.food = food;
        }
        public void stop() {
            super.stop();
            food.setLocked(false);
        }
    }
    
    private FoodTimer createLockFlashTimer(final Food food) {
        final long endTime = System.currentTimeMillis() + 3000L;
        return new FoodTimer(food, 500, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (endTime - System.currentTimeMillis() < 0) {
                    timer.stop();
                    timer = null;
                }
                else {
                    food.toggleLocked();
                }
            }
        });
    }

 
    protected void harvestFood(Agent agent) {
    	if (selectedFood == null) {
    		hud.setMessage(AbstractView.SELECT_PILLAR_MSG);
    		return;
    	}    	
    	
    	if (selectedFood.isHarvested())
    		return;
    	    	
        Point3D agentPosition = agent.getPosition();
        if (foodHitIncrement == null) {
            foodHitXtremePosition1 = agentPosition;
            foodHitXtremePosition2 = selectedFood.getPosition();
            Vector3D foodHitDirection = new Vector3D(foodHitXtremePosition2, agentPosition);
            foodHitDirection.normalize();
            foodHitIncrement = new Point3D(foodHitDirection);
        } 
        else if (agentPosition.equalsIntPart(foodHitXtremePosition1)) {
            foodHitIncrement = foodHitIncrement.multiply(-1);
        }
        //else {
            if (selectedFood.isIntersecting(agentPosition, Agent.radius)) {
                boolean harvested = selectedFood.hit();
                if (harvested) {
                    client.transmit(new HarvestResourceRequest(client.getId(), selectedFood.getResource()));
                    hud.setMessage(AbstractView.PILLAR_HARVESTED_MSG);
                }
                else {
                	hud.setMessage(AbstractView.PILLAR_HIT_MSG + HUD.getNoToString(selectedFood.getHitCounter()));
                }
		System.out.println("Selectd food is hit " + selectedFood.getHitCounter() +" times. Is it visible and green?");
                
                foodHitIncrement = foodHitIncrement.multiply(-1);	                
            }
        //}
        agent.setPosition(agent.getPosition().add(foodHitIncrement));
        agentPosition = agent.getPosition();
        //System.out.println("agentPosition " + agentPosition);
        update();    	
    }
    
    private void harvestUpperFoodPart(Agent agent) {
    	if (upperPartTimer == null && selectedFood != null && !selectedFood.isHarvested() && selectedFood.getAge() == getDataModel().getRoundConfiguration().getMaximumResourceAge()){// && selectedTree.hasFruits()) {
	        long duration = 8000L;
	        int interval = 350;
	        upperPartTimer = createUpperPartTimer(selectedFood, duration, interval);
	        upperPartTimer.start();
	        hud.setMessage(AbstractView.PILLAR_TOP_HARVESTING_MSG);
		}
		return;
		
    	/*if (selectedFood == null) {
    		hud.setMessage(AbstractView.SELECT_PILLAR_MSG);
    		return;
    	}    	
    	
    	if (selectedFood.isHarvested())
    		return;
    	
    	if (selectedFood.getResource().getAge() == getDataModel().getRoundConfiguration().getMaximumResourceAge()) {
	    	//Send the fruit harvest event to the server which will decrease the resource age.
	        client.transmit(new HarvestFruitRequest(client.getId(), selectedFood.getResource()));
	    	hud.setMessage(AbstractView.PILLAR_TOP_HARVESTED_MSG);
	    	resetFoodSelection();                  
	        update(); 
    	}*/
    }
    
    /**
     * Timer used to add a delay while harvesting the upper part of the pillars
     */
    private UpperPartTimer upperPartTimer;
         
    /**
     * The UpperPartTimer class is used to add a delay while harvesting the upper part of the pillars. 
     * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
     *
     */
    @SuppressWarnings("serial")
    private class UpperPartTimer extends Timer {
    	/**
    	 * The pillar whose upper part is being harvested
    	 */
        private final Food food;
        
        UpperPartTimer(Food food, int delay, ActionListener listener) {
            super(delay, listener);
            this.food = food;
        }
        
        /**
         * Stops the timer and sends the fruits harvested request to the server.
         */
        public void stop() {
            super.stop();    
            
            //Send the fruit harvest event to the server which will decrease the resource age.
            client.transmit(new HarvestFruitRequest(client.getId(), food.getResource()));
        }
    }
    
    private UpperPartTimer createUpperPartTimer(final Food food, long duration, int interval) {
        final long endTime = System.currentTimeMillis() + duration;
        return new UpperPartTimer(food, interval, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	long timeLeft = endTime - System.currentTimeMillis();
                if (timeLeft < 0) {
                    upperPartTimer.stop();
                    upperPartTimer = null;
                    hud.setMessage(NAV_INFO_MSG);
                    resetFoodToHarvest();
                }
                else {
                    //Do nothing                	
                }
            }
        });
    }


    //############### Agent-Camera related functions #################
    public void setCamera(Agent agent) {
        Point3D agentCenter = agent.getCenter();
        Point3D agentPosition = agent.getPosition().add(agentCenter);
        Point3D lookAtPoint = new Point3D(agentPosition.x, agentPosition.y+AGENT_CAMERA_HEIGHT, agentPosition.z);
        float agentHeading = agent.getHeading();

        float cameraAngle = agentHeading;
        Point3D cameraPosition = new Point3D();
        cameraPosition.x = agentPosition.x - AGENT_CAMERA_DISTANCE * (float) Math.cos(cameraAngle);
        cameraPosition.z = agentPosition.z + AGENT_CAMERA_DISTANCE * (float) Math.sin(cameraAngle);
        cameraPosition.y = agentPosition.y + Agent.radius;
        camera.setPoints(cameraPosition, lookAtPoint);

        cameraAgentVector = new Vector3D(lookAtPoint, cameraPosition);
    }

    public void updateResources() {
    	Map<Point, Resource> resourceDistribution = getDataModel().getResourceDistribution();
	    for (int row = 0; row < foodGrid.getRows(); row++) {
	        for (int col = 0; col < foodGrid.getColumns(); col++) {
	            Point point = new Point(row, col);
	            Food food = (Food) foodGrid.getNode(point);
	            Resource resource = resourceDistribution.get(point);
	            if (resource != null) {
	                if (food == null) {
	                    addResource(resource);
	                }
	                else if (!food.isHarvested() && food.getAge() != resource.getAge())  {
	                    food.setAge(resource.getAge());
	                }
	            }
	            else if (food != null) {
	                foodGrid.remove(point);
	            }
	        }
	    }
        /* 
        for (Resource resource: getDataModel().getRemovedResources()) {
            Food food = (Food) foodGrid.getNode(resource.getPosition());
            if (! food.harvested) {
                foodGrid.remove(resource.getPosition());
            }
        }
        for (Resource resource: getDataModel().getAddedResources()) {
            //System.err.println("adding new resource: " + resource);
            addResource(resource);
        }
        for (Resource resource : getDataModel().getResourceDistribution()) {
            Food food = (Food) foodGrid.getNode(resource.getPosition());
            food.setAge(resource.getAge());
        }
        getDataModel().clearDiffLists();
        */
    }
    
    public void updateAgentPositions() {
        for (ClientData clientData : getDataModel().getClientDataMap().values()) {
            Identifier id = clientData.getId();
            if (id.equals(client.getId())) {
                continue;
            }
            Agent agent = agents.get(id);
            agent.setPosition(clientData.getPoint3D());
            agent.setHeading(clientData.getHeading());
        }
    }

    //#################### HUD related methods #######################
    private void displayHud(GLAutoDrawable drawable) {
    	GL gl = drawable.getGL();
    	
	    hud.switchToOrthoProjection(drawable);
		gl.glDisable(GL.GL_LIGHTING);
		hud.displayMessage(drawable);
		
		Agent agent;
		Point labelPosition;
		//Other clients
		Identifier selfId = client.getId();
		for (ClientData clientData : getDataModel().getClientDataMap().values()) {
	        Identifier id = clientData.getId();
	        if (id.equals(selfId)) {
	            // skip ourselves
	            continue;
	        }
	        agent = agents.get(id);
			labelPosition = agent.getLabelPosition();
			hud.displayLabel(drawable, labelPosition, client.getGameWindow3D().getChatHandle(id));
		}
		
		//This client
		agent = agents.get(selfId);
		labelPosition = agent.getLabelPosition();
		hud.displayLabel(drawable, new Point(labelPosition.x - 10, labelPosition.y), client.getGameWindow3D().getChatHandle(selfId));
		gl.glEnable(GL.GL_LIGHTING);
		hud.restorePreviousProjection(drawable);
    }
    
    //################### Agent picking related functions #####################
	private void pickAgents(GLAutoDrawable drawable) {	
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
		System.out.println("Hits " + hits);
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
				if ( name == 100 || name == ((Agent)agents.get(getDataModel().getId())).getAssignedNumber() )
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
			System.out.println("Selected avatar # is " + selectedAvatar);
			//Check if the selected avatar is within the specified distance to start the chat
			Identifier selectedAvatarId = toIdentifier(selectedAvatar); 
			if (shouldChat(getDataModel().getId(), selectedAvatarId)) {			
				//We should never get -1 here
	            initiateChat(selectedAvatarId);
			}	        
		}
		else {
			hud.setMessage(AbstractView.NAV_INFO_MSG);
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
	
	public Identifier toIdentifier(int assignedNumber) {
        for (Map.Entry<Identifier, Agent> entry : agents.entrySet()) {
            Agent agent = entry.getValue();
            if (agent.getAssignedNumber() == assignedNumber) {
                return entry.getKey();
            }
        }
        System.err.println("No agent with assigned number: " + assignedNumber);
        return null;
    }
	
	@Override
	public boolean shouldChat(Identifier clientId1, Identifier clientId2) {
		Point3D agent1Position = agents.get(clientId1).getPosition();
		Point3D agent2Position = agents.get(clientId2).getPosition();
		float distance = new Vector3D(new Point3D(agent1Position.x, 0, agent1Position.z), 
										new Point3D(agent2Position.x, 0, agent2Position.z)).length();
		System.out.println("Distance between two avatars is " + distance);
		if (distance <= client.getDataModel().getRoundConfiguration().getChattingRadius()) {
//			hud.setVisible(false);
			hud.setMessage(AbstractView.CHAT_START_MSG);
			return true;
		}
		else {
			hud.setMessage(AbstractView.OUT_OF_CHAT_RANGE);
//			hud.setVisible(true);
		}
		return false;
	}
	
	private void initiateChat(Identifier id) {
        client.getGameWindow3D().getChatPanel().setTargetHandle(id);
    }
}
