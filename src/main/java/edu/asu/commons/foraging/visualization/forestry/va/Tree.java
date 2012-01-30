package edu.asu.commons.foraging.visualization.forestry.va;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import edu.asu.commons.foraging.graphics.BoundingBox;
import edu.asu.commons.foraging.graphics.Matrix4;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Ray;
import edu.asu.commons.foraging.graphics.Triangle;
import edu.asu.commons.foraging.model.Resource;
import edu.asu.commons.foraging.util.Tuple3i;
import edu.asu.commons.foraging.visualization.GameView3d;

/**
 * The Tree class encapulates a growing tree in the forestry experiment visualization. The trees represent resources in the common pool resource.  
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version $Revision$
 *
 */
public class Tree{	
	private static final long serialVersionUID = -1790275555037051977L;
	
	/**
	 * Location of the tree in world space 
	 */
	protected Point3D location;
	
	//TODO: Add yaw if more variation in the tree visualization is needed
	
	/**
	 * Resource from the resource pool corresponding to this tree.
	 */
	private Resource resource = null;
	
	/**
	 * Matrix representing world space transformation of this tree. Used for collision detection.
	 */
	private Matrix4 translationMatrix = null;
	
	/**
	 * A list of bounding boxes of the branches forming this tree. Used for collision detection.
	 */
	private Vector<BoundingBox> branchBoundingBoxes = new Vector<BoundingBox>();
	
	/**
	 * A list of trinagles forming the foliage of this tree. Used for collision detection.
	 */
	private Vector<Triangle> leafTriangles = new Vector<Triangle>();
	
	/**
	 * Specifies if the tree is harvested or not
	 */
	protected boolean harvested = false;

	/**
	 * Holds the no. of times the tree is hit by an avatar with his axe
	 */
	protected int hitCounter = 0;
	
	/**
	 * Specifies if the tree is selected by this avatar
	 */
	private boolean selected = false;
	
	/**
	 * Specifies if the tree is selected by another avatar
	 */
	private boolean selectedByOthers = false;
	
    /**
     * View class instantiating the trees
     */ 
	protected GameView3d parentView;	
	
	/**
	 * Specifies maximum age of this tree
	 */
	public static int maxAge;

	//TODO: All these variable values can also be set using the init() function. Since we need fixed colors, we are initializing them here 
	/**
	 * Specifies the height at which fruits are created on this tree
	 */
	private float fruitHeight = 23.5f;
	
	/**
	 * Ambient material used to render the branches of this tree 
	 */
	public static float[] branchAmbient = {130.0f/255.0f, 83.0f/255.0f, 74.0f/255.0f, 1.0f};
	
	/**
	 * Diffuse material used to render the branches of this tree 
	 */
	public static float[] branchDiffuse = {130.0f/255.0f, 83.0f/255.0f, 74.0f/255.0f, 1.0f};
	
	/**
	 * Specular material used to render the branches of this tree 
	 */
	public static float[] branchSpecular = {153.0f/255.0f, 97.0f/255.0f, 87.0f/255.0f, 1.0f};
	
	/**
	 * Shininess used to render the branches of this tree 
	 */
	public static float   branchShininess = 1.0f;
	
	/**
	 * Color used to render this tree as selected by this avatar
	 */
	public static float[] selectedColor = {0.0f, 0.0f, 0.5f, 1.0f};
	
	/**
	 * Color used to render this tree as selected by another avatar 
	 */
	public static float[] selectedByOthersColor = {0.5f, 0.5f, 0.0f, 1.0f};
	
	/**
	 * Ambient material used to render the leaves of this tree 
	 */
	public static float[] leafAmbient = {0.0f, 0.3f, 0.0f, 1.0f};
	
	/**
	 * Diffuse material used to render the leaves of this tree 
	 */
	public static float[] leafDiffuse = {0.0f, 0.5f, 0.0f, 1.0f};
	
	/**
	 * Specular material used to render the leaves of this tree 
	 */
	public static float[] leafSpecular = {0.0f, 0.7f, 0.0f, 1.0f};
	
	/**
	 * Shininess used to render the leaves of this tree 
	 */
	public static float   leafShininess = 4.0f;
			
	/**
	 * Ambient material used to render the fruits of this tree 
	 */
	public static float[] fruitAmbient = {168.0f/255.0f, 54.0f/255.0f, 0.0f, 1.0f};
	
	/**
	 * Diffuse material used to render the fruits of this tree 
	 */
	public static float[] fruitDiffuse = {232.0f/255.0f, 75.0f/255.0f, 0.0f, 1.0f};
	
	/**
	 * Specular material used to render the fruits of this tree 
	 */
	public static float[] fruitSpecular = {1.0f, 83.0f/255.0f, 0.0f, 1.0f};
	
	/**
	 * Shininess used to render the fruits of this tree 
	 */
	public static float   fruitShininess = 2.0f;
	
    /**
     * Creates a tree visualization of the specified resource
     * @param resource resource from the common pool resource represented as a tree
     * @param location location of the tree in the forest
     * @param parentView game view instantiating this tree
     */
	public Tree(Resource resource, Point3D location, GameView3d parentView) {
		this.resource = resource;
		this.location = location;
		this.parentView = parentView;
		
		this.harvested = false;
		
		calculateTranslationMatrix();
		createData4Collision();
		updateData4Collision();
	}
	
	/**
	 * Initialize the maximum age of the tree
	 * @param maxAge maximum age of the tree
	 */
	public static void init(int maxAge) {
		Tree.maxAge = maxAge;
	}
	
	/**
	 * Sets the age of this tree to the specified age
	 * @param age new age of the tree
	 */
	public void setAge(int age) {		 
		resource.setAge(age);
		
		if (age == Tree.maxAge) { //Tree grows fruits. No change in the branch parameters/levels
			//Do nothing
		}
		else if (age == Tree.maxAge-1) { //The tree does not grow in the last two years
			//Do nothing
		}
		else { 
			updateData4Collision();
		}
	}
	
	/**
	 * Render branches of this tree
	 * @param drawable current rendering context
	 */
	public void displayBranches(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
				
		gl.glPushMatrix();
		gl.glTranslatef(location.x, location.y, location.z);
				
		if (!harvested) {
			if (selected) {
				gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, selectedColor, 0);
			}
			else if (selectedByOthers)
				gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, selectedByOthersColor, 0);
			
			TreeView.displayBranches(gl, resource.getAge());
//			TreeBillboardView.displayTreeGeometry(gl);
			
			if (selected || selectedByOthers)
				gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, branchDiffuse, 0);
		}		
		gl.glPopMatrix();
	}
	
	/**
	 * Returns the location of this tree
	 * @return 3d coordinate where the tree is located
	 */
	public Point3D getLocation() {
		return location;
	}
	
	/**
	 * Returns current age of this tree
	 * @return age of the tree
	 */
	public int getAge() {
		return resource.getAge();
	}
		
	/**
	 * Returns the resource represented as this tree
	 * @return resource
	 */
	public Resource getResource() {
		return resource;
	}
	
	//############ Collision related functions #####################
	/**
	 * Calculates the translation matrix to apply to the tree geometry
	 */
	private void calculateTranslationMatrix() {
		translationMatrix = new Matrix4(location, true);
	}
	
	/**
	 * Populates data for avatar-tree collision detection
	 */
	private void createData4Collision() {
		//trunk bb
		branchBoundingBoxes.add(new BoundingBox(false));
		
		//3 child branches
		branchBoundingBoxes.add( new BoundingBox(false) );
		branchBoundingBoxes.add( new BoundingBox(false) );
		branchBoundingBoxes.add( new BoundingBox(false) );
		
		//54 leaf triangles		
		for (int index = 0; index < 54; index++) {
			leafTriangles.add(new Triangle());
		}
	}
	
	/**
	 * Updates the collision detection data according to the tree age 
	 */
	private void updateData4Collision() {
		//Get the vertex buffer appropriate to the age
		FloatBuffer vertexBuffer = TreeView.getBranchVertexBuffer(resource.getAge());

		//Update trunk bounding box
		int index = 0;
		BoundingBox branchBoundingBox = branchBoundingBoxes.get(index++);
		branchBoundingBox.setBRF(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
		branchBoundingBox.setBRB(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
		branchBoundingBox.setBLB(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
		branchBoundingBox.setBLF(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
		
		Point3D ttv1 = new Point3D(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
		Point3D ttv2 = new Point3D(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
		Point3D ttv3 = new Point3D(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
		Point3D ttv4 = new Point3D(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
		branchBoundingBox.setTRF(ttv1.x, ttv1.y, ttv1. z);
		branchBoundingBox.setTRB(ttv2.x, ttv2.y, ttv2. z);
		branchBoundingBox.setTLB(ttv3.x, ttv3.y, ttv3. z);
		branchBoundingBox.setTLF(ttv4.x, ttv4.y, ttv4. z);
		branchBoundingBox.transform(translationMatrix);		
		
		for (; index < 4; index++) {
			branchBoundingBox = branchBoundingBoxes.get(index);
			branchBoundingBox.setBRF(ttv1.x, ttv1.y, ttv1.z);
			branchBoundingBox.setBRB(ttv2.x, ttv2.y, ttv2.z);
			branchBoundingBox.setBLB(ttv3.x, ttv3.y, ttv3.z);
			branchBoundingBox.setBLF(ttv4.x, ttv4.y, ttv4.z);
			
			branchBoundingBox.setTRF(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			branchBoundingBox.setTRB(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			branchBoundingBox.setTLB(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			branchBoundingBox.setTLF(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			branchBoundingBox.transform(translationMatrix);			
		}
		vertexBuffer.flip();
		
		//Update leaf triangles
		Tuple3i face = new Tuple3i();
		Triangle triangle;
		vertexBuffer = TreeView.getLeafVertexBuffer(resource.getAge());
		ByteBuffer indexBuffer = TreeView.getLeafIndexBuffer();
		for (index = 0; index < indexBuffer.limit(); index+=3) {
			face.set(indexBuffer.get()*3, indexBuffer.get()*3, indexBuffer.get()*3);
			triangle = leafTriangles.get(index/3);
			triangle.set(vertexBuffer.get(face.a), vertexBuffer.get(face.a+1), vertexBuffer.get(face.a+1), 
						vertexBuffer.get(face.b), vertexBuffer.get(face.b+1), vertexBuffer.get(face.b+1), 
						vertexBuffer.get(face.c), vertexBuffer.get(face.c+1), vertexBuffer.get(face.c+1));
			triangle.transform(translationMatrix);
		}
		indexBuffer.flip();
	}
	
	/**
	 * Checks if the ray intersects the trunk of this tree within the specified distance
	 * @param ray ray with which the intersection should be checked
	 * @param collisionDetectionDistance distance used to check the intersection
	 * @return true if there is an intersection, false otherwise
	 */
	public boolean isIntersectingWithTrunk(Ray ray, float collisionDetectionDistance) {
		return branchBoundingBoxes.get(0).isIntersecting(ray, collisionDetectionDistance);
	}
	
	/**
	 * Checks if the ray intersects the braches of this tree within the specified distance
	 * @param ray ray with which the intersection should be checked
	 * @param collisionDetectionDistance distance used to check the intersection
	 * @return true if there is an intersection, false otherwise
	 */
	public boolean isIntersectingWithBranches(Ray ray, float collisionDetectionDistance) {
		for (int index = 1; index < 4; index++) {
			if (branchBoundingBoxes.get(index).isIntersecting(ray, collisionDetectionDistance))
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if the ray intersects the leaves of this tree within the specified distance
	 * @param ray ray with which the intersection should be checked
	 * @param collisionDetectionDistance distance used to check the intersection
	 * @return true if there is an intersection, false otherwise
	 */
	public boolean isIntersectingWithLeaves(Ray ray, float collisionDetectionDistance) {
		float param;
		for (int index = 0; index < leafTriangles.size(); index++) {
			param = leafTriangles.get(index).getIntersectionParam(ray);
			if (param >= 0 && param <= collisionDetectionDistance) {
				return true;
			}
		}
		return false;
	}
	
	//############ Selection related functions #################
	/**
	 * Sets this tree as selected or deselected by this avatar
	 * @param flag flag specifying if the tree should be selected or deselected
	 */
	public void setSelected(boolean flag) {
        selected = flag;
    }
	
	/**
	 * Checks if this tree is selected by this avatar
	 * @return true if selected, false otherwise
	 */
	public boolean isSelected() {
		return selected;
	}
	
	/**
	 * Sets the tree as selected by another avatar
	 * @param locked flag specifying if the tree should be locked or unlocked
	 */
	public void setSelectedByOthers(boolean locked) {
        this.selectedByOthers = locked;
    }

	/**
	 * Toggles the selection of this tree by another avatar
	 */
    public void toggleSelectedByOthers() {
        selectedByOthers = !selectedByOthers;
    }

    /**
     * Checkes if this tree is selected by other avatars
     * @return true if selected by other avatars, false otherwise
     */
    public boolean isSelectedByOthers() {
        return selectedByOthers;
    }

	//############## Harvest related methods ########################################
    /**
     * Increments the no. of times this tree is hit by the avatar's axe and if it same as the tree age then marks this tree as harvested 
     * @return true if the tree is harvested, false otherwise
     */
	public boolean hit() {
		if (!harvested) {
			hitCounter++;		
			//If tree is hit by the axe sufficient no. of times, cut it 
			if (hitCounter >= resource.getAge()) {
				harvested = true;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the no. of times this tree is hit by the avatar's axe
	 * @return no. of times the tree is hit
	 */
	public int getHitCounter() {
		return hitCounter;
	}

	/**
	 * Checks if the tree is harvested
	 * @return true if the tree if harvested, false otherwise
	 */
	public boolean isHarvested() {
		return harvested;
	}

	/**
	 * Renders the leaves of this tree using the indexed vertex arrays
	 * @param drawable current rendering context
	 */
	public void displayLeaves(GLAutoDrawable drawable) {
		if (!harvested) {
			GL gl = drawable.getGL();
			gl.glPushMatrix();
			gl.glTranslatef(location.x, location.y, location.z);
			TreeView.displayLeaves(gl, resource.getAge());
			gl.glPopMatrix();
		}			
	}

	/**
	 * Displays fruits of this tree using the indexed vertex arrays
	 * @param drawable current rednering context
	 */
	public void displayFruits(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
	
		if (!harvested && resource.getAge() == Tree.maxAge) {
			gl.glPushMatrix();
			gl.glTranslatef(location.x, location.y+fruitHeight, location.z);
			TreeView.displayFruits(gl);
			gl.glPopMatrix();
		}
	}
	
	/**
	 * Updates the fruit elevation
	 */
	public void updateFruitValues() {
		if (fruitHeight > 1)
			fruitHeight -= 1;		
	}

	/**
	 * Resets the fruit height after harvest so that the next time fruits appear attached to the leaves
	 */
	public void resetFruitHeight() {
		fruitHeight = 23.5f;
	}


	/**
	 * Returns the game view that instantiates this tree
	 * @return pointer to the game view
	 */
	public GameView3d getParentView() {
		return parentView;
	}
	
	public String toString() {
		return String.format("Tree: [%.2f, %.2f] Resource: %s", location.x, location.z, resource.toString());
	}
}
