package edu.asu.commons.foraging.visualization.forestry.va;

import java.nio.FloatBuffer;

import edu.asu.commons.foraging.graphics.BoundingBox;
import edu.asu.commons.foraging.graphics.Matrix4;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Vector3D;

/**
 * The LeafClusterModel class is used to create leaf clusters in the tree foliage. The geometry is approximated as 2 triangles connected 
 * to each other on one side. This is then buffered for rendering. This is a model type of class in the MVC architecture. 
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version $Revision$
 *
 */
public class LeafClusterModel {
	
	/**
	 * Bottom vertex of the leaf cluster
	 */
	private Point3D bottom;
	
	/**
	 * Top vertex of the leaf cluster
	 */
	private Point3D top;
	
	/**
	 * Left coordinate of the leaf cluster
	 */
	private Point3D left;
	
	/**
	 * Right coordinate of the leaf cluster
	 */
	private Point3D right;
	
	/**
	 * Matrix calculated by considering the angle made by this leaf cluster with X-axis 
	 */
	private static Matrix4 rollMatrix = null;
	
	/**
	 * Matrix calculated by considering the angle made by this leaf cluster with Y axis
	 */
	private Matrix4 yawMatrix = null;
	
	/**
	 * Matrix to transform from object space to tree space
	 */
	private Matrix4 transformation_os2ts = null;
	
	/**
	 * Parent branch to which this leaf cluster is attached
	 */
	private TreeBranchModel parentBranch = null;
	
	/**
	 * Tree containing this leaf cluster
	 */
	private TreeModel treeModel;
	
	/**
	 * Creates a new leaf cluster.
	 * @param yaw angle made by this leaf cluster with Y axis
	 * @param parentBranch branch to which this leaf cluster is attached
	 * @param treeModel tree containing this leaf cluster
	 */
	public LeafClusterModel(float yaw, TreeBranchModel parentBranch, TreeModel treeModel) {
		this.yawMatrix = new Matrix4(yaw, new Vector3D(0, 1, 0));
		this.parentBranch = parentBranch;
		this.treeModel = treeModel;
		
		float size = 0;
		int age = treeModel.getAge();
		size = age;
		
		calculateTransformation();
		updateGeometry(size, size);
	}

	/**
	 * Updates size according to the age of the tree. Also recalculates transformation matrix and updates the geometry.
	 */
	public void updateParamValues() {
		float size = 0;
		int age = treeModel.getAge();
		if (age < 6) {
			size = age;
		}
		else {
			size = age * 1.5f;
		}
		
		calculateTransformation();
		updateGeometry(size, size);		
	}
	
	/**
	 * Updates geometry of the leaf cluster according to the new height and width
	 * @param width width of the leaf cluster represented using a rectangle
	 * @param height height of the leaf cluster represented using a rectangle
	 */
	private void updateGeometry(float width, float height) {
		float halfWidth = width / 2.0f;
		float halfHeight = height / 2.0f;
		
		bottom = transformation_os2ts.multiply(new Point3D(0, 0, 0));		
		left = transformation_os2ts.multiply(new Point3D(-halfWidth, halfHeight, 0));
		right = transformation_os2ts.multiply(new Point3D(halfWidth, halfHeight, 0));
		top = transformation_os2ts.multiply(new Point3D(0, height, 0));
	}
	
	/**
	 * Calculates object space to tree space transformation
	 */
	private void calculateTransformation() {
		Matrix4 parentTopTransformation_os2ws = parentBranch.getOs2tsTransformation().multiply(new Matrix4(new Point3D(0, parentBranch.getLength()*0.9f, 0), true));
		transformation_os2ts = parentTopTransformation_os2ws.multiply(yawMatrix.multiply(rollMatrix));
	}
	
	/**
	 * Sets roll matrix to the specified 
	 * @param rollMatrix
	 */
	public static void setRollMatrix(Matrix4 rollMatrix) {
		LeafClusterModel.rollMatrix = rollMatrix;
	}
	
	/**
	 * Populates buffer with the geometry information of this leaf cluster
	 * @param vertexBuffer buffer storing the vertices of this leaf cluster 
	 * @param bottomVertex flag specifying if the bottom vertex should be added to the buffer or not
	 */
	public void fillGeometryInfo(FloatBuffer vertexBuffer, boolean bottomVertex) {
		if (bottomVertex)
			BoundingBox.addVertex2VA(bottom, vertexBuffer);
		
		BoundingBox.addVertex2VA(left, vertexBuffer);
		BoundingBox.addVertex2VA(top, vertexBuffer);
	}

	/**
	 * Returns the left vertex of this leaf cluster
	 * @return coordinates of the left vertex
	 */
	public Point3D getLeft() {
		return left;
	}

	/**
	 * Returns the bottom vertex of this leaf cluster
	 * @return coordinates of the bottom vertex
	 */
	public Point3D getBottom() {
		return bottom;
	}
	
}
