package edu.asu.commons.foraging.visualization.forestry.vbo;

import java.nio.FloatBuffer;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import edu.asu.commons.foraging.graphics.Matrix4;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Ray;
import edu.asu.commons.foraging.graphics.Texture;
import edu.asu.commons.foraging.graphics.TextureLoader;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.foraging.util.Tuple2f;
import edu.asu.commons.foraging.util.Tuple3f;
import edu.asu.commons.foraging.visualization.GameView3d;

public class Tree{	
	private static final long serialVersionUID = -1790275555037051977L;
	protected Point3D position;	//This is in world space
	protected float angle;
	private Resource resource = null;
	protected TreeBranch trunk = null;
	private Matrix4 transformation_os2ws = null;
	
	//Harvest related data members
	protected boolean harvested = false;
	protected boolean freshlyHarvested = false;
	protected int hitCounter = 0;
	
	//Selection related data members
	private boolean selected = false;	
	private boolean selectedByOthers = false;
    
	//Misc data members 
	protected GameView3d parentView;	
	private TreeView view = new TreeView(this);
	private boolean createGeometry = true;
	private boolean updateGeometry = true;
	
	//Fruit related data members
	private boolean freshlyFruited = false;
	private boolean updateFruitGeometry = false;
	private Vector<Fruit> fruits = new Vector<Fruit>();	
	private static float coinShininess = 128.0f;
	
	//Textures
	public static int maxAge;
	public static Texture branchTexture = null;
	public static Texture leafTexture = null;
	public static Texture coinHeapTexture = null;
	private static Texture fruitTexture = null;
	private static Texture coinTexture = null;
	
	//Materials
	/*public static RGBA[] branchColors = {	new RGBA(0.00f, 0.00f, 0.00f, 1.0f),	
		new RGBA(0.50f, 1.00f, 0.00f, 1.0f),
		new RGBA(0.58f, 0.52f, 0.19f, 1.0f),									
		new RGBA(0.53f, 0.39f, 0.25f, 1.0f),
		new RGBA(0.50f, 0.34f, 0.24f, 1.0f),
		new RGBA(0.44f, 0.30f, 0.22f, 1.0f),
		new RGBA(0.31f, 0.21f, 0.15f, 1.0f),
		new RGBA(0.31f, 0.21f, 0.15f, 1.0f)};*/
//	public static transient float[] branchSpecular = {0.5f, 0.5f, 0.5f, 1.0f};
	public static float[] ambient = {0.8f, 0.8f, 0.8f, 1.0f};
	public static float[] diffuse = {1.0f, 1.0f, 1.0f, 1.0f};
	public static float[] specular = {1.0f, 1.0f, 1.0f, 1.0f};
	public static float shininess = 4.0f;
	public static float[] selectedColor = {0.0f, 0.0f, 0.5f, 1.0f};
	public static float[] selectedByOthersColor = {0.5f, 0.0f, 0.0f, 1.0f};
		
	//Trunk parameters according to age of the tree
	public static float[] trunkBaseRadii = 	{0.0f, 0.125f, 0.25f, 0.352f, 0.50f, 0.75f, 1.00f, 1.25f, 1.5f, 1.5f, 1.5f};
	public static float[] trunkTopRadii = 	{0.0f, 0.075f, 0.20f, 0.30f, 0.425f, 0.6f, 0.75f, 1.00f, 1.25f, 1.25f, 1.25f};
	public static float[] trunkLengths = 	{0.0f, 4.00f, 6.00f, 8.0f, 10.0f, 12.0f, 14.0f, 15.0f, 16.0f, 16.0f, 16.0f};
		
    //Leaf related data members
    private Vector<LeafCluster> leafClusters = new Vector<LeafCluster>();
		
	public Tree(Resource resource, Point3D position, float angle, GameView3d parentView) {
		this.resource = resource;
		this.parentView = parentView;
		this.position = position;
		this.angle = angle;	
		this.harvested = false;
		
		transformation_os2ws = new Matrix4(position, true); //.multiply(rotation_os2ws);
						
		//Create tree trunk
		int age = resource.getAge();
		//System.out.println("Tree age is " + age);
		trunk = new TreeBranch(0, trunkBaseRadii[age], trunkTopRadii[age], trunkLengths[age], new Point3D(0, 0, 0), new Tuple3f(0, angle, 0), null, this);
	}
	
	public static void init(int maxAge, GLAutoDrawable drawable) {
		Tree.maxAge = maxAge;
				
		TextureLoader texLoader = new TextureLoader();
		branchTexture = texLoader.getTexture("data/forestry/bark1.jpg", true);
		branchTexture.create(drawable);
        
		leafTexture = texLoader.getTexture("data/forestry/mapleLeaves.gif", true);
		leafTexture.create(drawable);
		
		fruitTexture = texLoader.getTexture("data/forestry/fruitMap.gif", true);
		fruitTexture.create(drawable);
		
		coinTexture = texLoader.getTexture("data/forestry/coinMap.gif", true);
		coinTexture.create(drawable);
		
		coinHeapTexture = texLoader.getTexture("data/forestry/coins.jpg", true);
        coinHeapTexture.create(drawable);
	}
	
	public int create(FloatBuffer branchVertices, FloatBuffer normalBuffer, FloatBuffer textureCoordBuffer) {
//	FloatBuffer branchAttributes, FloatBuffer branchBaseCenters, FloatBuffer branchTransformationsRow1, FloatBuffer branchTransformationsRow2, FloatBuffer branchTransformationsRow3, FloatBuffer branchTransformationsRow4) {
		int nVertices = trunk.updateGeometry(branchVertices, normalBuffer, textureCoordBuffer); 
//				branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);
		nVertices += trunk.create(branchVertices, normalBuffer, textureCoordBuffer); 
//		branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);
		
		createGeometry = false;
		return nVertices;		
	}
	
	public boolean needsGeometryCreation() {
		return createGeometry;
	}
	
	public void setCreateGeometry(boolean createGeometry) {
		this.createGeometry = createGeometry;
	}
	
	//##################### Growth related methods ###########################
	public void setAge(int newAge) {		 
		resource.setAge(newAge);
		
		if (newAge == Tree.maxAge) { //Tree grows fruits. No change in the branch parameters/levels
			addFruits();
		}
		else if (newAge == Tree.maxAge-1) { //The tree does not grow in the last two years
			//Do nothing
		}
		else { 
			updateGeometry = true;
		}
	}
	
	public boolean needsGeometryUpdate() {
		return updateGeometry;
	}
	
	public void setUpdateGeometry(boolean updateGeometry) {
		this.updateGeometry = updateGeometry;
	}
		
	public int grow(FloatBuffer branchVertices, FloatBuffer normalBuffer, FloatBuffer textureCoordBuffer) { 
//	FloatBuffer branchAttributes, FloatBuffer branchBaseCenters, FloatBuffer branchTransformationsRow1, FloatBuffer branchTransformationsRow2, FloatBuffer branchTransformationsRow3, FloatBuffer branchTransformationsRow4) {
		//Remove leaves that are present as we update branch parameters and/or add new branches 
		removeLeaves();
		
		int nVertices = trunk.updateParamValues(branchVertices, normalBuffer, textureCoordBuffer);
//		branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);
		nVertices += trunk.grow(branchVertices, normalBuffer, textureCoordBuffer);
//		branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);
		
		return nVertices;
	}
			
	public void displayBranches(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
				
		if (!harvested) {
//		    RGBA branchColor = getBranchColor();	                
//	        System.out.println("Age = " + resource.getAge() + " " + branchColor);
//			RGBA branchColor = new RGBA(0.5f, 0.0f, 0.0f, 1.0f);
//			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, branchColor.getfv(), 0);		
//			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, Tree.branchSpecular, 0);
//			gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, Tree.shininess);
			if (selected) {
				gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, selectedColor, 0);
			}
			else if (selectedByOthers)
				gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, selectedByOthersColor, 0);
			
			//Load tree texture
			Tree.branchTexture.load(gl);			
			view.displayBranches(drawable);
			
			if (selected || selectedByOthers)
				gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, diffuse, 0);
		}
		else {
			gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, Tree.coinShininess);
			//Load the coin texture
			Tree.coinHeapTexture.load(gl);
			view.displayHeapOfCoins(drawable);
			gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, Tree.shininess);
		}
	}
	
	public Point3D getPosition() {
		return position;
	}
	
	public float getAngle() {
		return angle;
	}
	
	public int getAge() {
		return resource.getAge();
	}
		
	public Resource getResource() {
		return resource;
	}
	
	public Matrix4 getTransformation() {
		return transformation_os2ws;
	}
		
	//############ Collision related functions #####################
	public boolean isIntersectingTrunk(Ray ray, float collisionDetectionDistance) {
		return trunk.isIntersecting(ray, collisionDetectionDistance);
	}

	public boolean isIntersectingBranches(Ray ray, float collisionDetectionDistance) {
		return trunk.isIntersectingChildren(ray, collisionDetectionDistance);
	}
	//############ Selection related functions #################
	public void setSelected(boolean flag) {
        selected = flag;
    }
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelectedByOthers(boolean locked) {
        this.selectedByOthers = locked;
    }

    public void toggleSelectedByOthers() {
        selectedByOthers = !selectedByOthers;
    }

    public boolean isSelectedByOthers() {
        return selectedByOthers;
    }
    
	//############## Harvest related methods ########################################
	public boolean hit() {
		if (!harvested) {
			hitCounter++;		
			//If tree is hit by the axe sufficient no. of times, cut it 
			if (hitCounter >= resource.getAge()) {
				harvested = true;
				freshlyHarvested = true;
				return true;
			}
		}
		return false;
	}
	
	public int getHitCounter() {
		return hitCounter;
	}

	public boolean isHarvested() {
		return harvested;
	}
	
	public boolean isFreshlyHarvested() {
		return freshlyHarvested;
	}
	
	public void setFreshlyHarvested(boolean freshlyHarvested) {
		this.freshlyHarvested = freshlyHarvested;
	}

	public int createHeapBoundingBox(FloatBuffer vertices, FloatBuffer textureCoordBuffer) { 
	//FloatBuffer vertexAttributes, FloatBuffer baseCenters, FloatBuffer transformationsRow1, FloatBuffer transformationsRow2, FloatBuffer transformationsRow3, FloatBuffer transformationsRow4) {
		//Calculate heap parameters based on the age
		float baseRadius = resource.getAge() * 0.5f + 1.0f;
		float topRadius = 0;
		float height = baseRadius * 2.0f;

		//Calculate boudning box coordinates of a heap in world space
		Point3D BLF = transformation_os2ws.multiply(new Point3D(-baseRadius, 0, baseRadius));
		Point3D BRF = transformation_os2ws.multiply(new Point3D(baseRadius, 0, baseRadius));
		Point3D BLB = transformation_os2ws.multiply(new Point3D(-baseRadius, 0, -baseRadius));
		Point3D BRB = transformation_os2ws.multiply(new Point3D(baseRadius, 0, -baseRadius));
		Point3D TRF = transformation_os2ws.multiply(new Point3D(topRadius, height, topRadius));
		Point3D TLF = transformation_os2ws.multiply(new Point3D(-topRadius, height, topRadius));
		Point3D TRB = transformation_os2ws.multiply(new Point3D(topRadius, height, -topRadius));
		Point3D TLB = transformation_os2ws.multiply(new Point3D(-topRadius, height, -topRadius));
		
		addVertex(vertices, BRF);
		addVertex(vertices, TRF);
		addVertex(vertices, BLF);
		addVertex(vertices, BLF);		
		addVertex(vertices, TRF);
		addVertex(vertices, TLF);
		
		addVertex(vertices, BLF);
		addVertex(vertices, TLF);
		addVertex(vertices, BLB);
		addVertex(vertices, BLB);
		addVertex(vertices, TLF);
		addVertex(vertices, TLB);
		
		addVertex(vertices, BLB);
		addVertex(vertices, TLB);
		addVertex(vertices, BRB);
		addVertex(vertices, BRB);
		addVertex(vertices, TLB);
		addVertex(vertices, TRB);
		
		addVertex(vertices, BRB);
		addVertex(vertices, TRB);
		addVertex(vertices, BRF);
		addVertex(vertices, BRF);
		addVertex(vertices, TRB);
		addVertex(vertices, TRF);
		
		int nBranchVertices = 24;
//		Matrix4 ws2osTransformation = getWs2osTransformation();
//		for (int counter = 0; counter < nBranchVertices; counter++) {
//			addAttributes(vertexAttributes, baseRadius, topRadius, height);
//			addBaseCenter(baseCenters, position);
//			addBranchTransformation(transformationsRow1, transformationsRow2, transformationsRow3, transformationsRow4, ws2osTransformation);
//		}
			
		setFreshlyHarvested(false);
		return nBranchVertices;
	}
	
	private void addVertex(FloatBuffer vertices, Point3D vertex) {
		vertices.put(vertex.x);
		vertices.put(vertex.y);
		vertices.put(vertex.z);
	}
	
	private void addTexCoord(FloatBuffer textureBuffer, Tuple2f texCoord) {
		textureBuffer.put(texCoord.a);
		textureBuffer.put(texCoord.b);
	}
	
	private void addAttributes(FloatBuffer attributes, float baseRadius, float topRadius, float height) {
		attributes.put(baseRadius);
		attributes.put(topRadius);
		attributes.put(height);		
		attributes.put(height);
	}
	
	private void addBaseCenter(FloatBuffer baseCenters, Point3D baseCenter) {		
		baseCenters.put(baseCenter.x);
		baseCenters.put(baseCenter.y);
		baseCenters.put(baseCenter.z);
	}
	
	private void addBranchTransformation(FloatBuffer branchTransformationsRow1, FloatBuffer branchTransformationsRow2, 
			FloatBuffer branchTransformationsRow3, FloatBuffer branchTransformationsRow4, Matrix4 ws2osTransformation) {		
		
		for (int col = 0; col < 4; col++)
			branchTransformationsRow1.put(ws2osTransformation.mm[col][0]);
		for (int col = 0; col < 4; col++)
			branchTransformationsRow2.put(ws2osTransformation.mm[col][1]);
		for (int col = 0; col < 4; col++)
			branchTransformationsRow3.put(ws2osTransformation.mm[col][2]);
		for (int col = 0; col < 4; col++)
			branchTransformationsRow4.put(ws2osTransformation.mm[col][3]);
	}
	
	//########## Leaves related methods ######################
	public void addLeafCluster(TreeBranch parentBranch) {
		Matrix4 parentTopTransformation_os2ws = parentBranch.getOs2wsTransformation().
						multiply(new Matrix4(new Point3D(0, parentBranch.getLength()*0.9f, 0), true));
		Matrix4 rotation_x = new Matrix4(35, new Vector3D(1, 0, 0));
		
		Matrix4 leafClusterTransformation_os2ws = parentTopTransformation_os2ws.multiply(new Matrix4(0, new Vector3D(0, 1, 0)).multiply(rotation_x)); 
		leafClusters.add( new LeafCluster(leafClusterTransformation_os2ws, this) );
		
		leafClusterTransformation_os2ws = parentTopTransformation_os2ws.multiply(new Matrix4(120, new Vector3D(0, 1, 0)).multiply(rotation_x));
		leafClusters.add( new LeafCluster(leafClusterTransformation_os2ws, this) );
		
		leafClusterTransformation_os2ws = parentTopTransformation_os2ws.multiply(new Matrix4(240, new Vector3D(0, 1, 0)).multiply(rotation_x));
		leafClusters.add( new LeafCluster(leafClusterTransformation_os2ws, this) );
	}
	
	public void displayLeaves(GLAutoDrawable drawable) {
		if (!harvested) {			
			view.displayLeaves(drawable);
		}			
	}
	
	public void removeLeaves() {
		leafClusters.clear();
	}
	
	public int createLeavesVBO(FloatBuffer leafVertexBuffer, FloatBuffer leafTextureBuffer) {
		int nLeafVertices = leafClusters.size();
//		System.out.println("Total # leaf clusters = " + nLeafVertices);
		for (int clusterIndex = 0; clusterIndex < nLeafVertices; clusterIndex++) {
			LeafCluster leafCluster = leafClusters.get(clusterIndex);
			//face 1
			addVertex(leafVertexBuffer, leafCluster.getLeft());
			addVertex(leafVertexBuffer, leafCluster.getBottom());
			addVertex(leafVertexBuffer, leafCluster.getRight());
			
			addTexCoord(leafTextureBuffer, LeafCluster.leftTexCoord);
			addTexCoord(leafTextureBuffer, LeafCluster.bottomTexCoord);
			addTexCoord(leafTextureBuffer, LeafCluster.rightTexCoord);
			
			//face 2
			addVertex(leafVertexBuffer, leafCluster.getLeft());
			addVertex(leafVertexBuffer, leafCluster.getRight());
			addVertex(leafVertexBuffer, leafCluster.getTop());
			
			addTexCoord(leafTextureBuffer, LeafCluster.leftTexCoord);
			addTexCoord(leafTextureBuffer, LeafCluster.rightTexCoord);
			addTexCoord(leafTextureBuffer, LeafCluster.topTexCoord);
		}
		updateGeometry = false;
		
		return nLeafVertices * 6;
	}

	//############## Fruit related functions #####################
	private void addFruits() {
		trunk.addFruit();
		freshlyFruited = true;
	}
		
	//fruitLocation is in world space
	public void addFruit(Point3D fruitLocation) {    	
    	fruits.add(new Fruit(fruitLocation, this));
    }

	public void displayFruits(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		
		if (!harvested && fruits.size() > 0) {
			if (fruits.get(0).getHeight() < Fruit.getHtOfClrChange()) //fruits are being harvested
			{
				gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, Tree.coinShininess);
				coinTexture.load(gl);
				gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, Tree.shininess);
			}
			else
			{
				fruitTexture.load(gl);
			}
			view.displayFruits(drawable);
    	}
	}
	
	public int createFruitVBO(FloatBuffer vertexBuffer, FloatBuffer normalBuffer, FloatBuffer textureCoordBuffer) {
//	FloatBuffer attributeBuffer, FloatBuffer baseCenterBuffer, FloatBuffer transformationRow1Buffer, FloatBuffer transformationRow2Buffer, FloatBuffer transformationRow3Buffer, FloatBuffer transformationRow4Buffer) {
		
		int nVertices = 0;
		for (int fruitIndex = 0; fruitIndex < fruits.size(); fruitIndex++) {
			Fruit fruit = fruits.get(fruitIndex);
			nVertices += fruit.createVBO(vertexBuffer, normalBuffer, textureCoordBuffer); 
//			attributeBuffer, baseCenterBuffer, transformationRow1Buffer, transformationRow2Buffer, transformationRow3Buffer, transformationRow4Buffer);
		}
		freshlyFruited = false;
		
		return nVertices;
	}
	
	public int updateFruitVBO(FloatBuffer vertexBuffer, FloatBuffer normalBuffer, FloatBuffer textureCoordBuffer) { 
	//FloatBuffer attributesBuffer, FloatBuffer baseCenterBuffer, FloatBuffer transformationRow4Buffer) {
		int nVertices = 0;
		for (int fruitIndex = 0; fruitIndex < fruits.size(); fruitIndex++) {
			Fruit fruit = fruits.get(fruitIndex);
			nVertices += fruit.updateVBO(vertexBuffer, normalBuffer, textureCoordBuffer); 
//			attributesBuffer, baseCenterBuffer, transformationRow4Buffer);
		}
		updateFruitGeometry = false;
		
		return nVertices;
	}
		

	public boolean isFreshlyFruited() {
		return freshlyFruited;
	}
	
	public void setFreshlyFruited(boolean freshlyFruited) {
		this.freshlyFruited = freshlyFruited;
	}
	
	public boolean needsFruitGeometryUpdate() {
		return updateFruitGeometry;
	}
	
	public void setUpdateFruitGeometry(boolean updateFruitGeometry) {
		this.updateFruitGeometry = updateFruitGeometry;
	}
	
	public boolean hasFruits() {
		return noOfFruits() > 0;	
	}
	
	public int noOfFruits() {
		return fruits.size();
	}
	
	public Point3D getFruitLocation(int index) {
		return fruits.get(index).getLocation();
	}
	
	public void updateFruitValues(long timeLeft) {
		//Set fruits to lesser elevation
		for (int index = 0; index < fruits.size(); index++) {
			fruits.get(index).updateElevation(timeLeft);
		}
		updateFruitGeometry = true;
	}
	
	public void calculateFruitElevationStep(int index, float terrainElevation, int steps) {
		fruits.get(index).calculateElevationStep(terrainElevation, steps);
	}

	public void removeFruits() {
		fruits.clear();
	}
	
	/*public static void setFruitColor(RGBA ambient, RGBA diffuse, RGBA specular, float shininess) {
		ambient[0] = ambient.r;
		ambient[1] = ambient.g;
		ambient[2] = ambient.b;
		ambient[3] = ambient.a;
		
		diffuse[0] = diffuse.r;
		diffuse[1] = diffuse.g;
		diffuse[2] = diffuse.b;
		diffuse[3] = diffuse.a;
		
		specular[0] = specular.r;
		specular[1] = specular.g;
		specular[2] = specular.b;
		specular[3] = specular.a;

		coinShininess = shininess;
	}*/	

	//###################### View related methods #######################
	public TreeView getView() {
		return view;
	}
	
	//################## Miscalleneous methods ######################
	public GameView3d getParentView() {
		return parentView;
	}
	
//	public RGBA getBranchColor() {
//		return isSelected() ? Tree.selectedColor : isSelectedByOthers() ? Tree.selectedByOthersColor : Tree.branchColors[resource.getAge()];
//	}
	
	public Matrix4 getWs2osTransformation() {
		Matrix4 transformation_ws2os = new Matrix4();
		transformation_os2ws.getInverse(transformation_ws2os);
		return transformation_ws2os;
	}
	
//	public int getProgramObject() {
//		if (parentView.getClass() == ForestryView.class) {
//			return ((ForestryView)parentView).getProgramObject();
//		}
//		else if (parentView.getClass() == TestView.class) {
//			return ((TestView)parentView).getProgramObject();
//		}
//		return -1;
//	}
}
