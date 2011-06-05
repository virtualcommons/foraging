package edu.asu.commons.foraging.visualization.forestry.va;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.Vector;

import edu.asu.commons.foraging.graphics.BoundingBox;
import edu.asu.commons.foraging.graphics.Matrix4;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.util.Tuple3f;

/**
 * The TreeBranchModel class is used to create branch geometry. The geometry is a tapering bounding box. This is then buffered for 
 * rendering. This is a model type of class in the MVC architecture. 
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version $Revision: 4 $
 *
 */
public class TreeBranchModel implements Serializable {	
	private static final long serialVersionUID = 999939891255787138L;
	
	/**
	 * Level of this tree branch in the tree. Trunk is a level 0 branch, its children are level 1 branches and so on.
	 */
	private int level;
	
	/**
	 * Base radius of this tree branch
	 */
	protected float baseRadius;
	
	/**
	 * Top radius of this tree branch
	 */
	protected float topRadius;
	
	/**
	 * Length of this tree branch
	 */
	protected float length;
	
	/**
	 *  Angles made by this branch with the X, Y and Z axis
	 */
	protected Tuple3f angles;
	
	/**
	 * Center of the branch at its base
	 */
	protected Point3D baseCenter;
	
	/**
	 * Matrix representing transformation from object space to tree space
	 */
	private Matrix4 transformation_os2ts = null;
	
	/**
	 * Matrix representing rotation in the tree space
	 */
	private Matrix4 rotation_os2ts = null;
	
	/**
	 * Bounding box of this branch
	 */
	private BoundingBox boundingBox_ws = new BoundingBox(false);
				
	/**
	 * Pointer to the tree model class that instantiates this branch
	 */
	protected TreeModel treeModel;
	
	/**
	 * Pointer to the parent branch
	 */
	protected TreeBranchModel parentBranch = null;
	
	/**
	 * Vector of the arrays holding child branches of this branch
	 */
	protected Vector<TreeBranchModel> children = new Vector<TreeBranchModel>();
		
	/**
	 * Creates a new tree branch
	 * @param level level of the branch. Trunk is a branch of level 0, its children are branches of level 1 and so on.
	 * @param baseRadius radius of the branch at the bottom
	 * @param topRadius radius of the branch at the top
	 * @param length length of the branch
	 * @param baseCenter center point of the branch at the bottom
	 * @param angles angles made by this branch with X, Y and Z axis
	 * @param parentBranch pointer to the parent branch
	 * @param treeModel pointer to the container tree
	 */
	public TreeBranchModel(int level, float baseRadius, float topRadius, float length, Point3D baseCenter, Tuple3f angles, TreeBranchModel parentBranch, TreeModel treeModel) {
		this.level = level;
		this.baseRadius = baseRadius;
		this.topRadius = topRadius;
		this.length = length;
		this.baseCenter = baseCenter;
		this.angles = angles;						
		this.treeModel = treeModel;
		this.parentBranch = parentBranch;
		
		rotation_os2ts = new Matrix4(angles.a, new Vector3D(1, 0, 0)).multiply( new Matrix4(angles.b, new Vector3D(0, 1, 0)).multiply(new Matrix4(angles.c, new Vector3D(0, 0, 1))));
		calculateTransformation();
		updateBoundingBox();
	}
	
	/**
	 * Creates 3 child branches at the top of this branch separated by 120 degree angles.
	 */
	public void createChildren() {
		//Add child branches			
		float scaleFactor = 0.7f; //1.0f-0.05f*treeAge;
		float newLength = length * scaleFactor;
		float newBaseRadius = baseRadius * scaleFactor;
		float newTopRadius = topRadius * scaleFactor;
		float pitch = 35;
		
		TreeBranchModel childBranch;
		//branch1
		childBranch = new TreeBranchModel(level+1, newBaseRadius, newTopRadius, newLength, 
				new Point3D(0, length, 0), new Tuple3f(0, 0, pitch), this, treeModel);
		children.add(childBranch);
		treeModel.addLeafClusters( childBranch );
		
		//branch2			
		childBranch = new TreeBranchModel(level+1, newBaseRadius, newTopRadius, newLength,
				new Point3D(0, length, 0), new Tuple3f(0, 120, pitch), this, treeModel);
		children.add(childBranch);
		treeModel.addLeafClusters( childBranch );
		
		//branch 3			
		childBranch = new TreeBranchModel(level+1, newBaseRadius, newTopRadius, newLength,
				new Point3D(0, length, 0), new Tuple3f(0, 240, pitch), this, treeModel);
		children.add(childBranch);
		treeModel.addLeafClusters( childBranch );
	}
	
	/**
	 * Populates buffer with the branch vertices. Since bottom vertices of a branch are same as the top vertices of its parent branch and 
	 * since we use indexed vertex arrays we buffer only the top vertices for all but the trunk. We buffer the top and the bottom vertices 
	 * for the trunk. 
	 * @param vertexBuffer buffer holding the vertices
	 */
	public void fillGeometryInfo(FloatBuffer vertexBuffer) {
		//Fill bounding box information in the buffer
		if (parentBranch == null) {
			boundingBox_ws.populateVAWithBottomVertices(vertexBuffer);
		}
		
		boundingBox_ws.populateVAWithTopVertices(vertexBuffer);
	}
	
	/**
	 * Updates the parameter values of the branch according to the tree age. The parameter values include base radius, top radius, length and 
	 * base center. It also calculates the modified transforms. 
	 */
	public void updateParamValues() {
		int age = treeModel.getAge();
		
		//trunk
		if (parentBranch == null) {
			baseRadius = TreeModel.trunkBaseRadii[age];
			topRadius = TreeModel.trunkTopRadii[age];
			length = TreeModel.trunkLengths[age];			
		}
		//other branches
		else {
			float scaleFactor = 0.7f;
			
			//parameter values
			baseRadius = parentBranch.topRadius;
			topRadius = parentBranch.topRadius * scaleFactor;
			length = parentBranch.length * scaleFactor;	
			baseCenter.y = parentBranch.length * 0.9f;
		
			//transformation
			calculateTransformation();
		}
		
		//bounding box
		updateBoundingBox(); 
	}
	
	
	/**
	 * Updates bounding box according to the new parameter values
	 */
	public void updateBoundingBox() {
		//Update bounding box coordinates
		boundingBox_ws.setBLF(-baseRadius, 0, baseRadius);
		boundingBox_ws.setBRF(baseRadius, 0, baseRadius);
		boundingBox_ws.setBLB(-baseRadius, 0, -baseRadius);
		boundingBox_ws.setBRB(baseRadius, 0, -baseRadius);
		boundingBox_ws.setTRF(topRadius, length, topRadius);
		boundingBox_ws.setTLF(-topRadius, length, topRadius);
		boundingBox_ws.setTRB(topRadius, length, -topRadius);
		boundingBox_ws.setTLB(-topRadius, length, -topRadius);
		
		//Transform bounding box coordinates to world space 
		boundingBox_ws.transform(transformation_os2ts);
	}
	
	/**
	 * Returns object space to tree space transformation matrix 
	 * @return transformation matrix
	 */
	public Matrix4 getOs2tsTransformation() {
		return transformation_os2ts;
	}
	
	/**
	 * Returns base radius of this branch
	 * @return base radius
	 */
	public float getBaseRadius() {
		return baseRadius;
	}
	
	/**
	 * Returns top radius of this branch
	 * @return top radius
	 */
	public float getTopRadius() {
		return topRadius;
	}
	
	/**
	 * Returns length of this branch
	 * @return length
	 */
	public float getLength() {
		return length;
	}
	
	/**
	 * Returns level of this branch
	 * @return level
	 */
	public int getLevel() {
		return level;
	}
	
	/**
	 * Calculates object space to tree space transformation of this branch
	 */
	private void calculateTransformation() {
		transformation_os2ts = new Matrix4(baseCenter, true).multiply(rotation_os2ts);
		//Branches other than the trunk
		if (parentBranch != null) {			
			transformation_os2ts = parentBranch.getOs2tsTransformation().multiply(transformation_os2ts); 
		}
		
	}
}
